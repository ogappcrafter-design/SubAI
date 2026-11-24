package com.ogapp.subai.data.ai

import com.ogapp.subai.data.AiProvider
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.add
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonArray
import kotlinx.serialization.json.putJsonObject

/**
 * Bring-your-own-key vision diagnostician. Sends the user's setup photo plus a
 * structured prompt to their chosen provider and returns the model's text.
 *
 * Model ids are constants so they're trivial to bump as providers release newer
 * vision models. All three endpoints are vision-capable chat/generate APIs.
 */
object AiDiagnostician {

    // Default vision models (editable). The user's own key pays for these.
    const val GEMINI_MODEL = "gemini-2.0-flash"
    const val CLAUDE_MODEL = "claude-3-5-sonnet-latest"
    const val OPENAI_MODEL = "gpt-4o"

    private const val ANTHROPIC_VERSION = "2023-06-01"

    private val json = Json { ignoreUnknownKeys = true; isLenient = true }

    private fun client(): HttpClient = HttpClient(OkHttp) {
        engine { config { retryOnConnectionFailure(true) } }
    }

    /**
     * @param base64Jpeg base64 (no data: prefix) of a JPEG of the setup
     * @return Result with the diagnosis text, or a readable error message
     */
    suspend fun diagnose(
        provider: AiProvider,
        apiKey: String,
        base64Jpeg: String,
        prompt: String
    ): Result<String> {
        if (apiKey.isBlank()) {
            return Result.failure(IllegalStateException("No API key set. Add one in Settings."))
        }
        return try {
            client().use { http ->
                when (provider) {
                    AiProvider.GEMINI -> gemini(http, apiKey, base64Jpeg, prompt)
                    AiProvider.CLAUDE -> claude(http, apiKey, base64Jpeg, prompt)
                    AiProvider.OPENAI -> openai(http, apiKey, base64Jpeg, prompt)
                }
            }
        } catch (t: Throwable) {
            Result.failure(t)
        }
    }

    // ---- Gemini ----
    private suspend fun gemini(http: HttpClient, key: String, img: String, prompt: String): Result<String> {
        val url = "https://generativelanguage.googleapis.com/v1beta/models/$GEMINI_MODEL:generateContent?key=$key"
        val body = buildJsonObject {
            putJsonArray("contents") {
                add(buildJsonObject {
                    putJsonArray("parts") {
                        add(buildJsonObject { put("text", prompt) })
                        add(buildJsonObject {
                            putJsonObject("inline_data") {
                                put("mime_type", "image/jpeg")
                                put("data", img)
                            }
                        })
                    }
                })
            }
        }
        val resp = http.post(url) {
            contentType(ContentType.Application.Json)
            setBody(body.toString())
        }
        return parse(resp) { root ->
            root["candidates"]?.jsonArray?.firstOrNull()?.jsonObject
                ?.get("content")?.jsonObject
                ?.get("parts")?.jsonArray?.firstOrNull()?.jsonObject
                ?.get("text")?.jsonPrimitive?.content
        }
    }

    // ---- Claude ----
    private suspend fun claude(http: HttpClient, key: String, img: String, prompt: String): Result<String> {
        val body = buildJsonObject {
            put("model", CLAUDE_MODEL)
            put("max_tokens", 1024)
            putJsonArray("messages") {
                add(buildJsonObject {
                    put("role", "user")
                    putJsonArray("content") {
                        add(buildJsonObject {
                            put("type", "image")
                            putJsonObject("source") {
                                put("type", "base64")
                                put("media_type", "image/jpeg")
                                put("data", img)
                            }
                        })
                        add(buildJsonObject {
                            put("type", "text")
                            put("text", prompt)
                        })
                    }
                })
            }
        }
        val resp = http.post("https://api.anthropic.com/v1/messages") {
            header("x-api-key", key)
            header("anthropic-version", ANTHROPIC_VERSION)
            contentType(ContentType.Application.Json)
            setBody(body.toString())
        }
        return parse(resp) { root ->
            root["content"]?.jsonArray?.firstOrNull()?.jsonObject
                ?.get("text")?.jsonPrimitive?.content
        }
    }

    // ---- OpenAI ----
    private suspend fun openai(http: HttpClient, key: String, img: String, prompt: String): Result<String> {
        val body = buildJsonObject {
            put("model", OPENAI_MODEL)
            put("max_tokens", 1024)
            putJsonArray("messages") {
                add(buildJsonObject {
                    put("role", "user")
                    putJsonArray("content") {
                        add(buildJsonObject { put("type", "text"); put("text", prompt) })
                        add(buildJsonObject {
                            put("type", "image_url")
                            putJsonObject("image_url") {
                                put("url", "data:image/jpeg;base64,$img")
                            }
                        })
                    }
                })
            }
        }
        val resp = http.post("https://api.openai.com/v1/chat/completions") {
            header("Authorization", "Bearer $key")
            contentType(ContentType.Application.Json)
            setBody(body.toString())
        }
        return parse(resp) { root ->
            root["choices"]?.jsonArray?.firstOrNull()?.jsonObject
                ?.get("message")?.jsonObject
                ?.get("content")?.jsonPrimitive?.content
        }
    }

    private suspend fun parse(resp: HttpResponse, extract: (JsonObject) -> String?): Result<String> {
        val text = resp.bodyAsText()
        if (!resp.status.isSuccess()) {
            val msg = runCatching {
                val root = json.parseToJsonElement(text).jsonObject
                root["error"]?.jsonObject?.get("message")?.jsonPrimitive?.content
            }.getOrNull()
            return Result.failure(
                RuntimeException("AI request failed (${resp.status.value}): ${msg ?: shorten(text)}")
            )
        }
        val root = json.parseToJsonElement(text).jsonObject
        val out = extract(root)
        return if (out.isNullOrBlank()) {
            Result.failure(RuntimeException("The AI returned an empty answer. Try another photo or provider."))
        } else {
            Result.success(out.trim())
        }
    }

    private fun shorten(s: String): String = if (s.length > 180) s.take(180) + "…" else s
}
