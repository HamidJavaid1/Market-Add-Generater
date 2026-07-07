package com.example.ui

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.data.MarketingMaterial
import com.example.data.Product
import kotlinx.coroutines.delay

@Composable
fun rememberBitmapFromBase64(base64: String?): ImageBitmap? {
    return remember(base64) {
        if (base64 == null) return@remember null
        try {
            val decodedBytes = android.util.Base64.decode(base64, android.util.Base64.DEFAULT)
            val bitmap = android.graphics.BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
            bitmap?.asImageBitmap()
        } catch (e: Exception) {
            null
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    
    // Core states observed reactively
    val products by viewModel.products.collectAsStateWithLifecycle()
    val selectedProduct by viewModel.selectedProduct.collectAsStateWithLifecycle()
    val materials by viewModel.materials.collectAsStateWithLifecycle()
    val selectedMedium by viewModel.selectedMedium.collectAsStateWithLifecycle()
    
    val isGeneratingBlueprint by viewModel.isGeneratingBlueprint.collectAsStateWithLifecycle()
    val generatingMediums by viewModel.generatingMediums.collectAsStateWithLifecycle()
    val statusMessage by viewModel.statusMessage.collectAsStateWithLifecycle()
    val errorMessage by viewModel.errorMessage.collectAsStateWithLifecycle()

    // Form inputs for creating a product
    var productNameInput by remember { mutableStateOf("") }
    var productDescInput by remember { mutableStateOf("") }
    var isNewProductDialogOpen by remember { mutableStateOf(false) }

    // Display error Toast if one occurs
    LaunchedEffect(errorMessage) {
        errorMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
            viewModel.clearError()
        }
    }

    // List of predefined mediums
    val mediums = remember { listOf("Social Media", "Billboard", "Newspaper", "Transit Ad") }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = Color(0xFF60A5FA), // Accent Blue
                            modifier = Modifier.padding(end = 8.dp)
                        )
                        Text(
                            text = "Marketing Material Generator",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color(0xFF0F172A),
                    titleContentColor = Color.White
                ),
                actions = {
                    if (selectedProduct != null) {
                        IconButton(
                            onClick = { isNewProductDialogOpen = true },
                            modifier = Modifier
                                .testTag("btn_new_product_top")
                                .minimumInteractiveComponentSize()
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Create New Product",
                                tint = Color(0xFF60A5FA)
                            )
                        }
                    }
                }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .drawBehind {
                    // Create a premium subtle studio glow in the background
                    drawRect(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                Color(0xFF1E293B), // Slate deep blue/dark gray
                                Color(0xFF0F172A)  // Very dark slate
                            ),
                            center = Offset(size.width / 2f, size.height / 3f),
                            radius = size.width * 1.5f
                        )
                    )
                }
        ) {
            if (selectedProduct == null) {
                // First launch or no products: Onboarding / Product Entry Screen
                EmptyProductState(
                    productName = productNameInput,
                    onNameChange = { productNameInput = it },
                    productDesc = productDescInput,
                    onDescChange = { productDescInput = it },
                    isGenerating = isGeneratingBlueprint,
                    statusMessage = statusMessage,
                    onGenerateBlueprint = {
                        viewModel.createProduct(productNameInput, productDescInput) {
                            productNameInput = ""
                            productDescInput = ""
                        }
                    },
                    savedProducts = products,
                    onSelectProduct = { viewModel.selectProduct(it) }
                )
            } else {
                // Main Workspace Layout
                val activeProduct = selectedProduct!!
                val currentMaterial = materials.find { it.medium.equals(selectedMedium, ignoreCase = true) }
                val isMediumLoading = generatingMediums.contains(selectedMedium)

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Active Product Header Card
                    ProductHeaderCard(
                        product = activeProduct,
                        onDelete = { viewModel.deleteProduct(activeProduct) },
                        onNewProduct = { isNewProductDialogOpen = true }
                    )

                    // Expandable Product Blueprint Spec Card
                    ProductBlueprintCard(blueprint = activeProduct.visualBlueprint)

                    // Section Heading
                    Text(
                        text = "Imagine Campaign Shots",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier.padding(top = 8.dp)
                    )

                    // Segmented Control Tabs for Mediums
                    MediumSelectorTabs(
                        mediums = mediums,
                        selectedMedium = selectedMedium,
                        onMediumSelected = { viewModel.selectMedium(it) },
                        generatingMediums = generatingMediums
                    )

                    // Visual Mockup and Ad Copy Presentation Block
                    CampaignMaterialPresenter(
                        medium = selectedMedium,
                        material = currentMaterial,
                        isLoading = isMediumLoading,
                        statusMessage = statusMessage,
                        onGenerate = { viewModel.generateCampaignAsset(selectedMedium) }
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Quick History / Saved Products Carousel
                    if (products.size > 1) {
                        SavedProductsCarousel(
                            products = products,
                            selectedProduct = activeProduct,
                            onSelect = { viewModel.selectProduct(it) }
                        )
                    }
                }
            }

            // Dialogue / Dialog for creating a new product from the workspace
            if (isNewProductDialogOpen) {
                AlertDialog(
                    onDismissRequest = { isNewProductDialogOpen = false },
                    title = {
                        Text(
                            "New Product Campaign",
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    },
                    text = {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            OutlinedTextField(
                                value = productNameInput,
                                onValueChange = { productNameInput = it },
                                label = { Text("Product Name") },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(
                                    capitalization = KeyboardCapitalization.Words,
                                    imeAction = ImeAction.Next
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("dialog_product_name_input")
                            )

                            OutlinedTextField(
                                value = productDescInput,
                                onValueChange = { productDescInput = it },
                                label = { Text("Describe the product...") },
                                minLines = 3,
                                maxLines = 5,
                                keyboardOptions = KeyboardOptions(
                                    capitalization = KeyboardCapitalization.Sentences,
                                    imeAction = ImeAction.Done
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("dialog_product_desc_input")
                            )
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                viewModel.createProduct(productNameInput, productDescInput) {
                                    productNameInput = ""
                                    productDescInput = ""
                                    isNewProductDialogOpen = false
                                }
                            },
                            enabled = productNameInput.isNotBlank() && productDescInput.isNotBlank(),
                            modifier = Modifier.testTag("dialog_confirm_btn")
                        ) {
                            Text("Formulate Spec")
                        }
                    },
                    dismissButton = {
                        TextButton(
                            onClick = { isNewProductDialogOpen = false },
                            modifier = Modifier.testTag("dialog_dismiss_btn")
                        ) {
                            Text("Cancel")
                        }
                    }
                )
            }
        }
    }
}

/**
 * Onboarding view displayed when there are no products loaded.
 */
@Composable
fun EmptyProductState(
    productName: String,
    onNameChange: (String) -> Unit,
    productDesc: String,
    onDescChange: (String) -> Unit,
    isGenerating: Boolean,
    statusMessage: String,
    onGenerateBlueprint: () -> Unit,
    savedProducts: List<Product>,
    onSelectProduct: (Product) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Spacer(modifier = Modifier.height(24.dp))
        
        // Studio branding badge
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(CircleShape)
                .background(
                    Brush.sweepGradient(
                        listOf(
                            Color(0xFF3B82F6), // Blue
                            Color(0xFF6366F1), // Indigo
                            Color(0xFFEC4899), // Pink
                            Color(0xFF3B82F6)
                        )
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.AutoAwesome,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(36.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Ad Studio Pro",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            textAlign = TextAlign.Center
        )

        Text(
            text = "Formulate consistent, professional marketing campaigns. Describe your product, and we'll generate an exhaustive blueprint followed by tailored, human-free mockups for billboard, newspaper, and social ads.",
            style = MaterialTheme.typography.bodyMedium,
            color = Color(0xFF94A3B8),
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Product Input Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B).copy(alpha = 0.8f)),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Introduce Your Product",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                OutlinedTextField(
                    value = productName,
                    onValueChange = onNameChange,
                    label = { Text("Product Name", color = Color(0xFF94A3B8)) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = Color(0xFF60A5FA),
                        unfocusedBorderColor = Color(0xFF334155),
                        focusedContainerColor = Color(0xFF0F172A),
                        unfocusedContainerColor = Color(0xFF0F172A)
                    ),
                    placeholder = { Text("e.g. Aerodynamic Carbon Flask", color = Color(0xFF475569)) },
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Words,
                        imeAction = ImeAction.Next
                    ),
                    enabled = !isGenerating,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_product_name")
                )

                OutlinedTextField(
                    value = productDesc,
                    onValueChange = onDescChange,
                    label = { Text("Describe its materials, shapes, aesthetics...", color = Color(0xFF94A3B8)) },
                    minLines = 3,
                    maxLines = 5,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = Color(0xFF60A5FA),
                        unfocusedBorderColor = Color(0xFF334155),
                        focusedContainerColor = Color(0xFF0F172A),
                        unfocusedContainerColor = Color(0xFF0F172A)
                    ),
                    placeholder = { Text("e.g. A cylinder water bottle made of matte black steel with a raw textured wooden bamboo cap, featuring a subtle geometric design.", color = Color(0xFF475569)) },
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Sentences,
                        imeAction = ImeAction.Done
                    ),
                    enabled = !isGenerating,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_product_desc")
                )

                // Generation feedback / status
                if (isGenerating) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
                    ) {
                        LinearProgressIndicator(
                            color = Color(0xFF60A5FA),
                            trackColor = Color(0xFF334155),
                            modifier = Modifier.fillMaxWidth().clip(CircleShape)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = statusMessage,
                            fontSize = 12.sp,
                            color = Color(0xFF60A5FA),
                            textAlign = TextAlign.Center,
                            fontStyle = FontStyle.Italic
                        )
                    }
                } else {
                    Button(
                        onClick = onGenerateBlueprint,
                        enabled = productName.isNotBlank() && productDesc.isNotBlank(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF3B82F6),
                            disabledContainerColor = Color(0xFF1E293B)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("btn_generate_blueprint"),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text("Formulate Visual Blueprint", fontWeight = FontWeight.Bold, color = Color.White)
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(imageVector = Icons.Default.ArrowForward, contentDescription = null, tint = Color.White)
                        }
                    }
                }
            }
        }

        // Saved Products / History on first launch
        if (savedProducts.isNotEmpty()) {
            Spacer(modifier = Modifier.height(32.dp))
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(imageVector = Icons.Default.History, contentDescription = null, tint = Color(0xFF94A3B8), modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Load Saved Studio Briefs",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF94A3B8)
                )
            }
            Spacer(modifier = Modifier.height(12.dp))

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                savedProducts.take(3).forEach { product ->
                    Card(
                        onClick = { onSelectProduct(product) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("saved_brief_${product.id}"),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A).copy(alpha = 0.5f)),
                        border = BorderStroke(1.dp, Color(0xFF1E293B))
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(product.name, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 14.sp)
                                Text(
                                    product.description,
                                    fontSize = 12.sp,
                                    color = Color.Gray,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            Icon(imageVector = Icons.Default.ChevronRight, contentDescription = null, tint = Color.Gray)
                        }
                    }
                }
            }
        }
    }
}

/**
 * Top active product info card.
 */
@Composable
fun ProductHeaderCard(
    product: Product,
    onDelete: () -> Unit,
    onNewProduct: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().testTag("product_header_card"),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, Color(0xFF334155))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Active Workspace",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF60A5FA),
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = product.name,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onNewProduct,
                        modifier = Modifier
                            .testTag("btn_new_campaign_header")
                            .minimumInteractiveComponentSize()
                    ) {
                        Icon(imageVector = Icons.Default.AddCircleOutline, contentDescription = "New Campaign", tint = Color.LightGray)
                    }
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier
                            .testTag("btn_delete_product")
                            .minimumInteractiveComponentSize()
                    ) {
                        Icon(imageVector = Icons.Default.DeleteOutline, contentDescription = "Delete Brief", tint = Color(0xFFEF4444))
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(color = Color(0xFF334155))
            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "Brief Description:",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF94A3B8)
            )
            Text(
                text = product.description,
                fontSize = 13.sp,
                color = Color.White,
                modifier = Modifier.padding(top = 2.dp)
            )
        }
    }
}

/**
 * Expandable Blueprint card showing consistent specs.
 */
@Composable
fun ProductBlueprintCard(blueprint: String) {
    var isExpanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("product_blueprint_card")
            .clickable { isExpanded = !isExpanded },
        colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
        border = BorderStroke(1.dp, Color(0xFF1E293B)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Palette,
                        contentDescription = null,
                        tint = Color(0xFFF59E0B),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "AI Visual Consistency Blueprint",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
                Icon(
                    imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = null,
                    tint = Color.Gray
                )
            }

            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(modifier = Modifier.padding(top = 12.dp)) {
                    Text(
                        text = "To maintain design consistency across mediums, Gemini has formulated a fixed product specification:",
                        fontSize = 12.sp,
                        color = Color(0xFF94A3B8),
                        lineHeight = 16.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF1E293B).copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                            .border(1.dp, Color(0xFF1E293B), RoundedCornerShape(8.dp))
                            .padding(10.dp)
                    ) {
                        Text(
                            text = blueprint,
                            fontSize = 12.sp,
                            color = Color.White,
                            fontFamily = FontFamily.Monospace,
                            lineHeight = 18.sp
                        )
                    }
                }
            }

            if (!isExpanded) {
                Text(
                    text = "Tap to inspect materials, coloring, and alignment spec...",
                    fontSize = 12.sp,
                    color = Color.Gray,
                    fontStyle = FontStyle.Italic,
                    modifier = Modifier.padding(top = 6.dp)
                )
            }
        }
    }
}

/**
 * Segmented selection tabs for mediums.
 */
@Composable
fun MediumSelectorTabs(
    mediums: List<String>,
    selectedMedium: String,
    onMediumSelected: (String) -> Unit,
    generatingMediums: Set<String>
) {
    ScrollableTabRow(
        selectedTabIndex = mediums.indexOf(selectedMedium),
        containerColor = Color.Transparent,
        contentColor = Color.White,
        edgePadding = 0.dp,
        divider = {},
        indicator = { tabPositions ->
            val index = mediums.indexOf(selectedMedium)
            if (index in tabPositions.indices) {
                Box(
                    Modifier
                        .tabIndicatorOffset(tabPositions[index])
                        .height(3.dp)
                        .clip(RoundedCornerShape(topStart = 3.dp, topEnd = 3.dp))
                        .background(Color(0xFF60A5FA))
                )
            }
        },
        modifier = Modifier.fillMaxWidth().testTag("medium_tabs")
    ) {
        mediums.forEach { medium ->
            val isSelected = medium == selectedMedium
            val isLoading = generatingMediums.contains(medium)
            
            Tab(
                selected = isSelected,
                onClick = { onMediumSelected(medium) },
                text = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                color = Color(0xFF60A5FA),
                                strokeWidth = 2.dp,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                        } else {
                            val icon = when (medium.lowercase()) {
                                "social media" -> Icons.Default.PhoneAndroid
                                "billboard" -> Icons.Default.Business
                                "newspaper" -> Icons.Default.Newspaper
                                "transit ad" -> Icons.Default.DirectionsBus
                                else -> Icons.Default.Photo
                            }
                            Icon(
                                imageVector = icon,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = if (isSelected) Color(0xFF60A5FA) else Color.Gray
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                        }
                        Text(
                            text = medium,
                            fontSize = 13.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) Color.White else Color.Gray
                        )
                    }
                },
                modifier = Modifier.testTag("tab_$medium")
            )
        }
    }
}

/**
 * Renders the mockup and marketing content blocks for the selected medium.
 */
@Composable
fun CampaignMaterialPresenter(
    medium: String,
    material: MarketingMaterial?,
    isLoading: Boolean,
    statusMessage: String,
    onGenerate: () -> Unit
) {
    val bitmap = rememberBitmapFromBase64(material?.base64Image)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("campaign_material_presenter"),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B).copy(alpha = 0.5f)),
        border = BorderStroke(1.dp, Color(0xFF1E293B)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            
            // Loading Overlay State
            if (isLoading) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 48.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    CircularProgressIndicator(
                        color = Color(0xFF60A5FA),
                        strokeWidth = 3.dp,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Rendering Studio Model...",
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontSize = 15.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = statusMessage,
                        fontSize = 12.sp,
                        color = Color(0xFF94A3B8),
                        textAlign = TextAlign.Center,
                        fontStyle = FontStyle.Italic,
                        modifier = Modifier.padding(horizontal = 24.dp)
                    )
                }
            } else if (material == null) {
                // Empty / Placeholder state for this specific medium
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 36.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF0F172A)),
                        contentAlignment = Alignment.Center
                    ) {
                        val icon = when (medium.lowercase()) {
                            "social media" -> Icons.Default.Layers
                            "billboard" -> Icons.Default.DesktopMac
                            "newspaper" -> Icons.Default.Receipt
                            "transit ad" -> Icons.Default.LocationOn
                            else -> Icons.Default.Image
                        }
                        Icon(imageVector = icon, contentDescription = null, tint = Color(0xFF94A3B8), modifier = Modifier.size(28.dp))
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "No $medium Asset",
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontSize = 16.sp
                    )
                    Text(
                        text = "Tap generate to draft copy, slogans, and render the product in the exact medium backdrop using the nano-banana image synthesizer.",
                        color = Color.Gray,
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp)
                    )
                    Spacer(modifier = Modifier.height(18.dp))
                    Button(
                        onClick = onGenerate,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF60A5FA)),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.testTag("btn_generate_asset_$medium")
                    ) {
                        Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Synthesize Shot", fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
                    }
                }
            } else {
                // Asset is Generated and available
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    
                    // Medium presentation frame
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF0F172A))
                            .border(1.dp, Color(0xFF1E293B), RoundedCornerShape(12.dp))
                    ) {
                        if (bitmap != null) {
                            Image(
                                bitmap = bitmap,
                                contentDescription = "Generated advertising shot for $medium",
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .aspectRatio(
                                        when (medium.lowercase()) {
                                            "billboard" -> 1.77f // 16:9
                                            "newspaper" -> 0.75f // 3:4
                                            "social media" -> 1.0f  // 1:1
                                            "transit ad" -> 0.66f // 2:3
                                            else -> 1.0f
                                        }
                                    )
                                    .clip(RoundedCornerShape(12.dp)),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(200.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("Failed to render image bitmap.", color = Color.Red, fontSize = 12.sp)
                            }
                        }

                        // Medium Label Badge
                        Surface(
                            color = Color.Black.copy(alpha = 0.75f),
                            shape = RoundedCornerShape(bottomEnd = 12.dp),
                            modifier = Modifier.align(Alignment.TopStart)
                        ) {
                            Text(
                                text = medium.uppercase(),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF60A5FA),
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                letterSpacing = 1.sp
                            )
                        }
                    }

                    // Headline Slogan Display (Styled specifically to emulate real-life mediums!)
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF0F172A), RoundedCornerShape(12.dp))
                            .border(1.dp, Color(0xFF1E293B), RoundedCornerShape(12.dp))
                            .padding(16.dp)
                    ) {
                        Text(
                            text = "ADVERTISEMENT CONTENT",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Gray,
                            letterSpacing = 1.sp,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )

                        // Slogan Headline with custom display style based on medium
                        when (medium.lowercase()) {
                            "billboard" -> {
                                Text(
                                    text = material.headline,
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color.White,
                                    lineHeight = 30.sp,
                                    fontFamily = FontFamily.SansSerif,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
                                )
                            }
                            "newspaper" -> {
                                Text(
                                    text = material.headline.uppercase(),
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    fontFamily = FontFamily.Serif,
                                    textDecoration = TextDecoration.Underline,
                                    modifier = Modifier.padding(vertical = 4.dp)
                                )
                            }
                            else -> {
                                Text(
                                    text = material.headline,
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF60A5FA),
                                    lineHeight = 26.sp,
                                    fontFamily = FontFamily.SansSerif,
                                    modifier = Modifier.padding(vertical = 4.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        // Ad Body copy
                        Text(
                            text = material.bodyText,
                            fontSize = 13.sp,
                            color = Color(0xFF94A3B8),
                            lineHeight = 18.sp,
                            fontStyle = if (medium.lowercase() == "newspaper") FontStyle.Italic else FontStyle.Normal
                        )
                    }

                    // Prompt used breakdown
                    var isPromptExpanded by remember { mutableStateOf(false) }
                    Card(
                        onClick = { isPromptExpanded = !isPromptExpanded },
                        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                        border = BorderStroke(1.dp, Color(0xFF1E293B))
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(imageVector = Icons.Default.Code, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Inspect Nano-Banana Prompt", fontSize = 12.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                                }
                                Icon(
                                    imageVector = if (isPromptExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                    contentDescription = null,
                                    tint = Color.Gray,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                            AnimatedVisibility(visible = isPromptExpanded) {
                                Text(
                                    text = material.promptUsed,
                                    fontSize = 11.sp,
                                    color = Color.Gray,
                                    fontFamily = FontFamily.Monospace,
                                    modifier = Modifier.padding(top = 8.dp),
                                    lineHeight = 16.sp
                                )
                            }
                        }
                    }

                    // Regenerate controls
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedButton(
                            onClick = onGenerate,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f).testTag("btn_regenerate_$medium"),
                            border = BorderStroke(1.dp, Color(0xFF334155))
                        ) {
                            Icon(imageVector = Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Re-Imagine Shot", color = Color.White)
                        }
                    }
                }
            }
        }
    }
}

/**
 * Saved products horizontal scrolling slider.
 */
@Composable
fun SavedProductsCarousel(
    products: List<Product>,
    selectedProduct: Product,
    onSelect: (Product) -> Unit
) {
    Column {
        Text(
            text = "Switch Studio Brief",
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF94A3B8),
            modifier = Modifier.padding(bottom = 8.dp)
        )
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxWidth().testTag("saved_products_carousel")
        ) {
            items(products) { product ->
                val isSelected = product.id == selectedProduct.id
                Card(
                    onClick = { onSelect(product) },
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSelected) Color(0xFF3B82F6).copy(alpha = 0.2f) else Color(0xFF0F172A)
                    ),
                    border = BorderStroke(
                        width = 1.dp,
                        color = if (isSelected) Color(0xFF60A5FA) else Color(0xFF1E293B)
                    ),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.width(160.dp).testTag("carousel_brief_${product.id}")
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = product.name,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontSize = 13.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = product.description,
                            color = Color.Gray,
                            fontSize = 11.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}
