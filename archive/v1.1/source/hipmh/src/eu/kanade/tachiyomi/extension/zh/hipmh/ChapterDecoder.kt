package eu.kanade.tachiyomi.extension.zh.hipmh

import kotlinx.serialization.json.Json
import java.util.Base64

internal object ChapterDecoder {

    private const val PREFIX = "qM9"
    private const val SUFFIX = "Z7"
    private const val MARKER = "Vx"
    private const val SEPARATOR = "pL0"
    private const val BLOCK_SIZE = 7
    private const val CUSTOM_ALPHABET = "_-9876543210abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ"
    private const val BASE64_URL_ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789-_"

    private val json = Json { ignoreUnknownKeys = true }

    fun decode(value: String): List<String> {
        require(value.startsWith(PREFIX) && value.endsWith(SUFFIX)) { "Unsupported image payload" }
        val body = value.substring(PREFIX.length, value.length - SUFFIX.length)
        val contentLength = body.length - MARKER.length - SEPARATOR.length
        require(contentLength > 0) { "Invalid image payload length" }

        val lastPartLength = contentLength / 3
        val firstPartLength = (contentLength - lastPartLength) / 2
        val middlePartLength = contentLength - lastPartLength - firstPartLength

        val firstPartEnd = firstPartLength
        val markerEnd = firstPartEnd + MARKER.length
        val middlePartEnd = markerEnd + middlePartLength
        val separatorEnd = middlePartEnd + SEPARATOR.length

        require(body.substring(firstPartEnd, markerEnd) == MARKER) { "Invalid image payload marker" }
        require(body.substring(middlePartEnd, separatorEnd) == SEPARATOR) { "Invalid image payload separator" }

        val reordered = buildString(contentLength) {
            append(body.substring(separatorEnd))
            append(body.substring(0, firstPartEnd))
            append(body.substring(markerEnd, middlePartEnd))
        }
        val unshuffled = buildString(reordered.length) {
            reordered.chunked(BLOCK_SIZE).forEachIndexed { index, chunk ->
                append(if (index % 2 == 0) chunk else chunk.reversed())
            }
        }
        val encoded = buildString(unshuffled.length) {
            unshuffled.forEach { character ->
                val index = CUSTOM_ALPHABET.indexOf(character)
                require(index >= 0) { "Invalid image payload character" }
                append(BASE64_URL_ALPHABET[index])
            }
        }
        val padded = encoded + "=".repeat((4 - encoded.length % 4) % 4)
        val decoded = Base64.getUrlDecoder().decode(padded).toString(Charsets.UTF_8)
        return json.decodeFromString(decoded)
    }
}
