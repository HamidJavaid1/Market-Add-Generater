package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.MarketingMaterial
import com.example.data.MarketingRepository
import com.example.data.Product
import com.example.api.GeminiRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class MainViewModel(
    application: Application,
    private val repository: MarketingRepository,
    private val geminiRepository: GeminiRepository = GeminiRepository()
) : AndroidViewModel(application) {

    // Saved products
    val products: StateFlow<List<Product>> = repository.allProducts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Selected product state
    private val _selectedProduct = MutableStateFlow<Product?>(null)
    val selectedProduct: StateFlow<Product?> = _selectedProduct.asStateFlow()

    // Materials generated for the currently selected product
    val materials: StateFlow<List<MarketingMaterial>> = _selectedProduct
        .flatMapLatest { product ->
            if (product != null) {
                repository.getMaterialsForProduct(product.id)
            } else {
                flowOf(emptyList())
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Currently selected medium for viewing/generation details
    private val _selectedMedium = MutableStateFlow("Social Media")
    val selectedMedium: StateFlow<String> = _selectedMedium.asStateFlow()

    // Loading states
    private val _isGeneratingBlueprint = MutableStateFlow(false)
    val isGeneratingBlueprint: StateFlow<Boolean> = _isGeneratingBlueprint.asStateFlow()

    // Maps medium to its loading state
    private val _generatingMediums = MutableStateFlow<Set<String>>(emptySet())
    val generatingMediums: StateFlow<Set<String>> = _generatingMediums.asStateFlow()

    // Loading status texts (marketing status updates)
    private val _statusMessage = MutableStateFlow("")
    val statusMessage: StateFlow<String> = _statusMessage.asStateFlow()

    // Error message state
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    init {
        // Automatically select the first product if available
        viewModelScope.launch {
            products.firstOrNull { it.isNotEmpty() }?.firstOrNull()?.let {
                _selectedProduct.value = it
            }
        }
    }

    fun selectProduct(product: Product) {
        _selectedProduct.value = product
    }

    fun selectMedium(medium: String) {
        _selectedMedium.value = medium
    }

    fun clearError() {
        _errorMessage.value = null
    }

    /**
     * Creates a new product and generates its visual blueprint.
     */
    fun createProduct(name: String, description: String, onSuccess: () -> Unit) {
        if (name.isBlank() || description.isBlank()) {
            _errorMessage.value = "Product name and description cannot be empty."
            return
        }

        viewModelScope.launch {
            _isGeneratingBlueprint.value = true
            _statusMessage.value = "Analyzing product brief and formulating consistent design specifications..."
            _errorMessage.value = null

            try {
                // Stage 1: Generate Visual Blueprint
                val blueprint = geminiRepository.generateVisualBlueprint(name, description)
                
                val product = Product(
                    name = name,
                    description = description,
                    visualBlueprint = blueprint
                )

                val id = repository.insertProduct(product)
                val savedProduct = product.copy(id = id.toInt())
                
                _selectedProduct.value = savedProduct
                onSuccess()
            } catch (e: Exception) {
                val msg = e.message ?: ""
                _errorMessage.value = if (msg.contains("429") || (e is retrofit2.HttpException && e.code() == 429)) {
                    "API Quota or Rate Limit Exceeded (HTTP 429). Please wait a moment and try again, or check your API key's project billing and quota limits in Google AI Studio."
                } else {
                    msg.ifEmpty { "Failed to analyze product. Please verify your internet connection and API key." }
                }
            } finally {
                _isGeneratingBlueprint.value = false
                _statusMessage.value = ""
            }
        }
    }

    /**
     * Generates a marketing campaign asset (ad copy + nano-banana image) for the selected product and medium.
     */
    fun generateCampaignAsset(medium: String) {
        val product = _selectedProduct.value ?: run {
            _errorMessage.value = "Please select or create a product first."
            return
        }

        viewModelScope.launch {
            _generatingMediums.value = _generatingMediums.value + medium
            _statusMessage.value = "Drafting headlines and tailoring image layout specs for $medium..."
            _errorMessage.value = null

            try {
                // Stage 2: Generate Marketing Slogan/Headline, body copy and detailed image prompt
                val draft = geminiRepository.generateMarketingCopy(product.name, product.visualBlueprint, medium)
                
                _statusMessage.value = "Generating advertisement mockup image for $medium via nano-banana (this can take up to 30 seconds)..."

                // Stage 3: Generate Image using nano-banana (gemini-2.5-flash-image)
                val base64Image = geminiRepository.generateImage(draft.imagePrompt, medium)

                // Save or replace marketing material
                val existingMaterial = repository.getMaterialForProductAndMedium(product.id, medium)
                val material = MarketingMaterial(
                    id = existingMaterial?.id ?: 0,
                    productId = product.id,
                    medium = medium,
                    headline = draft.headline,
                    bodyText = draft.bodyText,
                    base64Image = base64Image,
                    promptUsed = draft.imagePrompt
                )

                repository.insertMaterial(material)
            } catch (e: Exception) {
                val msg = e.message ?: ""
                _errorMessage.value = if (msg.contains("429") || (e is retrofit2.HttpException && e.code() == 429)) {
                    "API Quota or Rate Limit Exceeded (HTTP 429). Please wait a moment and try again, or check your API key's project billing and quota limits in Google AI Studio."
                } else {
                    msg.ifEmpty { "Failed to generate assets for $medium." }
                }
            } finally {
                _generatingMediums.value = _generatingMediums.value - medium
                _statusMessage.value = ""
            }
        }
    }

    fun deleteProduct(product: Product) {
        viewModelScope.launch {
            repository.deleteProduct(product)
            if (_selectedProduct.value?.id == product.id) {
                _selectedProduct.value = products.value.firstOrNull { it.id != product.id }
            }
        }
    }

    fun deleteMaterial(materialId: Int) {
        viewModelScope.launch {
            repository.deleteMaterialById(materialId)
        }
    }
}

class MainViewModelFactory(
    private val application: Application,
    private val repository: MarketingRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MainViewModel(application, repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
