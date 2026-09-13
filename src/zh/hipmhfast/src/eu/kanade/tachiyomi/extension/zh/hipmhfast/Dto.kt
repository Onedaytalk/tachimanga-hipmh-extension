package eu.kanade.tachiyomi.extension.zh.hipmhfast

import eu.kanade.tachiyomi.source.model.SChapter
import eu.kanade.tachiyomi.source.model.SManga
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.time.Instant

@Serializable
class ApiResponse<T>(
    val code: Int,
    val message: String = "",
    val data: T,
) {
    fun requireSuccess() {
        check(code == 200) { message.ifBlank { "API error: $code" } }
    }
}

@Serializable
class MangaPageData(
    val items: List<MangaListItem> = emptyList(),
    @SerialName("total_pages") val totalPages: Int = 1,
)

@Serializable
class MangaListItem(
    val mid: String,
    val title: String,
    @SerialName("vertical_image_url") private val verticalImageUrl: String? = null,
    @SerialName("cover_image_url") private val coverImageUrl: String? = null,
    @SerialName("author_names") private val authorNames: List<String> = emptyList(),
    private val genres: List<String> = emptyList(),
) {
    fun toSManga(resolveCoverUrl: (String) -> String): SManga = SManga.create().apply {
        url = "/works/${this@MangaListItem.mid}"
        title = this@MangaListItem.title
        thumbnail_url = (verticalImageUrl ?: coverImageUrl)?.let(resolveCoverUrl)
        author = authorNames.joinToString()
        genre = genres.joinToString()
    }
}

@Serializable
class MangaDetailsData(
    val title: String,
    @SerialName("vertical_image_url") private val verticalImageUrl: String? = null,
    @SerialName("cover_image_url") private val coverImageUrl: String? = null,
    private val description: String? = null,
    private val authors: List<NamedItem> = emptyList(),
    private val genres: List<NamedItem> = emptyList(),
    private val status: String? = null,
) {
    fun toSManga(resolveCoverUrl: (String) -> String): SManga = SManga.create().apply {
        title = this@MangaDetailsData.title
        thumbnail_url = (verticalImageUrl ?: coverImageUrl)?.let(resolveCoverUrl)
        author = authors.joinToString { it.name }
        genre = genres.joinToString { it.name }
        description = this@MangaDetailsData.description
        status = when (this@MangaDetailsData.status) {
            "ongoing" -> SManga.ONGOING
            "completed" -> SManga.COMPLETED
            else -> SManga.UNKNOWN
        }
        initialized = true
    }
}

@Serializable
class SearchPageData(
    @SerialName("data") val items: List<SearchItem> = emptyList(),
    @SerialName("total_pages") val totalPages: Int = 1,
)

@Serializable
class SearchItem(
    val id: String,
    val title: String,
    @SerialName("vertical_image_url") private val verticalImageUrl: String? = null,
    private val description: String? = null,
    private val authors: List<NamedItem> = emptyList(),
    private val genres: List<NamedItem> = emptyList(),
    private val status: String? = null,
) {
    fun toSManga(resolveCoverUrl: (String) -> String): SManga = SManga.create().apply {
        url = "/works/${this@SearchItem.id}"
        title = this@SearchItem.title
        thumbnail_url = verticalImageUrl?.let(resolveCoverUrl)
        author = authors.joinToString { it.name }
        genre = genres.joinToString { it.name }
        description = this@SearchItem.description
        status = when (this@SearchItem.status) {
            "ongoing" -> SManga.ONGOING
            "completed" -> SManga.COMPLETED
            else -> SManga.UNKNOWN
        }
    }
}

@Serializable
class NamedItem(
    val name: String,
)

@Serializable
class ChapterPageData(
    val items: List<ChapterItem> = emptyList(),
    val total: Int? = null,
    @SerialName("total_pages") val totalPages: Int = 1,
)

@Serializable
class ChapterItem(
    val hid: String,
    @SerialName("chapter_number") private val chapterNumber: Float = -1F,
    private val title: String = "",
    @SerialName("updated_at") private val updatedAt: String? = null,
) {
    fun toSChapter(): SChapter = SChapter.create().apply {
        url = hid
        name = title.ifBlank { "Chapter $chapterNumber" }
        chapter_number = chapterNumber
        date_upload = updatedAt?.let { runCatching { Instant.parse(it).toEpochMilli() }.getOrDefault(0L) } ?: 0L
    }
}

@Serializable
class ChapterData(
    val images: String,
    @SerialName("order_id") val orderId: Int? = null,
    val sid: Long? = null,
    val line: Int? = null,
)
