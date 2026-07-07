package com.example.api

import android.util.Log
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@JsonClass(generateAdapter = true)
data class GeneratedMarketingResponse(
    val headline: String,
    val bodyText: String,
    val imagePrompt: String
)

class GeminiRepository {
    private val apiService = RetrofitClient.service
    private val moshi = Moshi.Builder().addLast(KotlinJsonAdapterFactory()).build()
    private val responseAdapter = moshi.adapter(GeneratedMarketingResponse::class.java)

    private fun getApiKey(): String {
        val key = com.example.BuildConfig.GEMINI_API_KEY
        if (key.isEmpty() || key == "MY_GEMINI_API_KEY") {
            throw IllegalStateException("Gemini API Key is missing. Please configure your key in the Secrets panel in AI Studio.")
        }
        return key
    }

    /**
     * Stage 1: Generate a detailed, consistent visual blueprint for the product.
     */
    suspend fun generateVisualBlueprint(productName: String, productDescription: String): String = withContext(Dispatchers.IO) {
        val apiKey = getApiKey()
        val systemInstruction = "You are an expert industrial designer and product photographer. Your job is to take a product name and brief description and create a highly detailed, consistent physical design blueprint. Describe its materials, precise shape, surface textures, branding/logo styling, color codes, and fine design details. This blueprint will be used to generate images of the product in various settings. DO NOT write an advertisement; write a neutral, exhaustive technical design spec."
        
        val prompt = "Product Name: $productName\nDescription: $productDescription\n\nGenerate the exhaustive physical design blueprint."
        
        val request = GenerateContentRequest(
            contents = listOf(Content(parts = listOf(Part(text = prompt)))),
            systemInstruction = Content(parts = listOf(Part(text = systemInstruction)))
        )

        try {
            val response = apiService.generateContent("gemini-3.5-flash", apiKey, request)
            response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                ?: throw IllegalStateException("Empty response from Gemini during blueprint generation.")
        } catch (e: Exception) {
            Log.e("GeminiRepository", "Error generating blueprint", e)
            throw e
        }
    }

    /**
     * Stage 2: Generate the marketing copy and tailored image prompt for a specific medium.
     */
    suspend fun generateMarketingCopy(
        productName: String,
        visualBlueprint: String,
        medium: String
    ): GeneratedMarketingResponse = withContext(Dispatchers.IO) {
        val apiKey = getApiKey()
        
        val systemInstruction = """
            You are a senior creative director and copywriter. Your task is to design a high-converting marketing campaign for a product in a specific advertising medium.
            You must output your response in structured JSON format according to the schema provided.
            
            Strict rules:
            1. DO NOT include any people, hands, faces, or human figures in the image prompt. The ad must focus purely on the product on its own in its environment.
            2. The image prompt must describe the product in extreme, consistent detail based on the visual blueprint.
            3. The image prompt must place the product in a context suitable for the specified medium (e.g. Billboard, Newspaper ad, Social Media ad).
            4. Do not include any human interaction or hands holding the product. Use stands, tables, or outdoor settings.
        """.trimIndent()

        val prompt = """
            Product Name: $productName
            Visual Blueprint:
            $visualBlueprint
            
            Advertising Medium: $medium
            
            Provide:
            1. A catchy ad headline appropriate for $medium.
            2. High-impact body copy or visual descriptions appropriate for $medium.
            3. A detailed image-generation prompt for a photo of this advertisement in the $medium environment. Place the product beautifully within the environment (e.g., a billboard on a sunset highway, a vintage newsprint texture page, a square Instagram-ready studio product photography shot) but ensure NO PEOPLE OR HUMAN PARTS (like hands) are present.
        """.trimIndent()

        // Configure the JSON Schema for structured output
        val marketingSchema = ResponseSchema(
            type = "OBJECT",
            properties = mapOf(
                "headline" to ResponseSchemaProperty(
                    type = "STRING",
                    description = "A catchy, short, medium-appropriate headline or slogan."
                ),
                "bodyText" to ResponseSchemaProperty(
                    type = "STRING",
                    description = "Ad copy, features, or call to action text tailored for this medium."
                ),
                "imagePrompt" to ResponseSchemaProperty(
                    type = "STRING",
                    description = "An ultra-detailed prompt for image generation. It must place the product (styled exactly per the visual blueprint) inside the medium setting (e.g., billboard, printed newspaper, social ad) with NO humans, hands, or faces."
                )
            ),
            required = listOf("headline", "bodyText", "imagePrompt")
        )

        val request = GenerateContentRequest(
            contents = listOf(Content(parts = listOf(Part(text = prompt)))),
            generationConfig = GenerationConfig(
                responseMimeType = "application/json",
                responseSchema = marketingSchema,
                temperature = 0.8f
            ),
            systemInstruction = Content(parts = listOf(Part(text = systemInstruction)))
        )

        try {
            val response = apiService.generateContent("gemini-3.5-flash", apiKey, request)
            val jsonText = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                ?: throw IllegalStateException("Empty response from Gemini during marketing draft.")
            
            responseAdapter.fromJson(jsonText)
                ?: throw IllegalStateException("Failed to parse marketing response JSON.")
        } catch (e: Exception) {
            Log.e("GeminiRepository", "Error generating marketing copy for $medium", e)
            throw e
        }
    }

    /**
     * Stage 3: Generate the actual advertisement image using gemini-2.5-flash-image (nano-banana).
     */
    suspend fun generateImage(prompt: String, medium: String): String = withContext(Dispatchers.IO) {
        val apiKey = getApiKey()
        
        // Adjust aspect ratio based on the medium to make it extremely realistic!
        val aspectRatio = when (medium.lowercase()) {
            "billboard" -> "16:9" // Wide for billboards
            "newspaper" -> "3:4"  // Portrait printed page
            "social media" -> "1:1" // Square for Instagram/social posts
            "transit ad" -> "2:3" // Tall vertical for bus shelters
            else -> "1:1"
        }

        // We enhance the prompt to reinforce consistency, high aesthetic quality, and the absolute absence of people.
        val finalPrompt = "$prompt. High-fidelity professional product advertising shot. Commercial photography, clean lighting, extremely detailed, octane render, 8k resolution. STRICTLY no people, no human figures, no hands, empty background environment."

        val request = GenerateContentRequest(
            contents = listOf(Content(parts = listOf(Part(text = finalPrompt)))),
            generationConfig = GenerationConfig(
                imageConfig = ImageConfig(aspectRatio = aspectRatio, imageSize = "1K"),
                responseModalities = listOf("TEXT", "IMAGE")
            )
        )

        try {
            // Call the gemini-2.5-flash-image model
            val response = apiService.generateContent("gemini-2.5-flash-image", apiKey, request)
            
            // Extract the inline image data
            val candidates = response.candidates
            val parts = candidates?.firstOrNull()?.content?.parts
            
            // Find the part that has inlineData containing the image
            val imagePart = parts?.firstOrNull { it.inlineData != null }
            val base64Data = imagePart?.inlineData?.data
            
            if (base64Data != null) {
                base64Data
            } else {
                // Sometimes if it fails or returns text instead, let's log and see if there's text
                val textResponse = parts?.firstOrNull { it.text != null }?.text
                throw IllegalStateException(textResponse ?: "No image data was generated by the Gemini model.")
            }
        } catch (e: Exception) {
            Log.e("GeminiRepository", "Error generating image with gemini-2.5-flash-image", e)
            throw e
        }
    }
}
