package com.example.data

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query
import java.util.concurrent.TimeUnit
import com.example.BuildConfig

@JsonClass(generateAdapter = true)
data class GeminiPart(
    @Json(name = "text") val text: String
)

@JsonClass(generateAdapter = true)
data class GeminiContent(
    @Json(name = "parts") val parts: List<GeminiPart>
)

@JsonClass(generateAdapter = true)
data class GeminiGenerationConfig(
    @Json(name = "temperature") val temperature: Float? = null,
    @Json(name = "maxOutputTokens") val maxOutputTokens: Int? = null
)

@JsonClass(generateAdapter = true)
data class GeminiRequest(
    @Json(name = "contents") val contents: List<GeminiContent>,
    @Json(name = "systemInstruction") val systemInstruction: GeminiContent? = null,
    @Json(name = "generationConfig") val generationConfig: GeminiGenerationConfig? = null
)

@JsonClass(generateAdapter = true)
data class GeminiCandidate(
    @Json(name = "content") val content: GeminiContent? = null
)

@JsonClass(generateAdapter = true)
data class GeminiResponse(
    @Json(name = "candidates") val candidates: List<GeminiCandidate>? = null
)

interface GeminiApiService {
    @POST("v1beta/models/gemini-3.5-flash:generateContent")
    suspend fun generateContent(
        @Query("key") apiKey: String,
        @Body request: GeminiRequest
    ): GeminiResponse
}

object GeminiRetrofitClient {
    private const val BASE_URL = "https://generativelanguage.googleapis.com/"

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    val service: GeminiApiService by lazy {
        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create())
            .build()
        retrofit.create(GeminiApiService::class.java)
    }

    suspend fun askAssistant(prompt: String, conversationHistory: List<Pair<String, Boolean>> = emptyList()): String {
        val key = BuildConfig.GEMINI_API_KEY
        if (key.isEmpty() || key == "MY_GEMINI_API_KEY") {
            return "Note: Gemini AI is in offline demo mode. To activate, please add a valid GEMINI_API_KEY in the AI Studio Secrets panel.\n\nOffline Help: Biological systems are composed of cell levels, organ functions, and genetic instructions. How can I assist you with offline curriculum content today?"
        }

        val systemPrompt = "You are a friendly, enthusiastic AI Biology Professor in BioLab 3D. Explain complex biological materials like cell organelles, double helices, cardiac pulses, nervous system connections, and digestion in simple, narrative terms. Answer accurately and with academic discipline, using bullet points for structural clarity. Help the school student learn safely! If a question is off-topic from biology, request they stay on-topic."

        val contentsList = mutableListOf<GeminiContent>()
        
        // Load history (Pair of <Text, IsUser>)
        conversationHistory.takeLast(10).forEach { (text, isUser) ->
            contentsList.add(
                GeminiContent(
                    parts = listOf(GeminiPart(text = text))
                )
            )
        }

        // Add current prompt
        contentsList.add(
            GeminiContent(
                parts = listOf(GeminiPart(text = prompt))
            )
        )

        val request = GeminiRequest(
            contents = contentsList,
            systemInstruction = GeminiContent(parts = listOf(GeminiPart(text = systemPrompt))),
            generationConfig = GeminiGenerationConfig(temperature = 0.7f, maxOutputTokens = 1000)
        )

        return try {
            val result = service.generateContent(key, request)
            result.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text 
                ?: "I apologize, I didn't receive a clear cellular signal. Could you repeat that?"
        } catch (e: Exception) {
            "Error contacting neural gateway: ${e.localizedMessage}. Please double-check your internet hookups."
        }
    }
}
