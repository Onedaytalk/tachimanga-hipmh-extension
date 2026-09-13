package eu.kanade.tachiyomi.extension.zh.hipmh

import eu.kanade.tachiyomi.network.GET
import eu.kanade.tachiyomi.source.model.FilterList
import eu.kanade.tachiyomi.source.model.MangasPage
import eu.kanade.tachiyomi.source.model.Page
import eu.kanade.tachiyomi.source.model.SChapter
import eu.kanade.tachiyomi.source.model.SManga
import eu.kanade.tachiyomi.source.model.SMangaUpdate
import keiyoushi.annotation.Source
import keiyoushi.network.get
import keiyoushi.network.rateLimit
import keiyoushi.source.KeiSource
import keiyoushi.utils.asJsoup
import keiyoushi.utils.parseAs
import kotlinx.serialization.json.JsonElement
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import org.jsoup.nodes.Document

@Source
abstract class Hipmh : KeiSource() {

    private val apiUrl = "https://hipapi1.s3file.top"
    private val coverBaseUrl = "https://cover.s3imgs.top"
    private val readerBaseUrl = "https://reader.hipmh.top"

    override fun OkHttpClient.Builder.configureClient() = rateLimit(3)

    override suspend fun getPopularManga(page: Int): MangasPage = getMangaPage(page, "popular")

    override suspend fun getLatestUpdates(page: Int): MangasPage = getMangaPage(page, "updated")

    private suspend fun getMangaPage(page: Int, sort: String): MangasPage {
        val url = "$apiUrl/v1/mangas".toHttpUrl().newBuilder()
            .addQueryParameter("sort", sort)
            .addQueryParameter("page", page.toString())
            .addQueryParameter("per_page", PAGE_SIZE.toString())
            .build()
        val response = client.get(url).parseAs<ApiResponse<MangaPageData>>()
        response.requireSuccess()
        return MangasPage(
            response.data.items.map { it.toSManga(::resolveCoverUrl) },
            page < response.data.totalPages,
        )
    }

    override fun getFilterList(data: JsonElement?) = FilterList()

    override suspend fun getSearchMangaList(page: Int, query: String, filters: FilterList): MangasPage {
        if (query.isBlank()) return getMangaPage(page, "updated")

        val url = "$apiUrl/v1/search".toHttpUrl().newBuilder()
            .addQueryParameter("q", query.trim())
            .addQueryParameter("page", page.toString())
            .addQueryParameter("page_size", PAGE_SIZE.toString())
            .build()
        val response = client.get(url).parseAs<ApiResponse<SearchPageData>>()
        response.requireSuccess()
        return MangasPage(
            response.data.items.map { it.toSManga(::resolveCoverUrl) },
            page < response.data.totalPages,
        )
    }

    override fun getMangaUrl(manga: SManga): String = when {
        manga.url.startsWith("http") -> manga.url
        else -> baseUrl + manga.url
    }

    override fun getChapterUrl(chapter: SChapter): String = when {
        chapter.url.startsWith("http") -> chapter.url
        else -> "$readerBaseUrl/chapter/${chapter.url}"
    }

    override suspend fun getMangaByUrl(url: HttpUrl): SManga? {
        if (url.host == "reader.hipmh.top" && url.pathSegments.firstOrNull() == "chapter") {
            val mangaUrl = client.get(url).asJsoup()
                .selectFirst("#chapcontent")
                ?.attr("data-manga-path")
                ?.toHttpUrlOrNull()
                ?: return null
            return getMangaByUrl(mangaUrl)
        }

        val isMainHost = url.host == "hipmh.com" || url.host.endsWith(".hipmh.com")
        val worksIndex = url.pathSegments.indexOf("works")
        if (!isMainHost || worksIndex < 0) return null
        val slug = url.pathSegments.getOrNull(worksIndex + 1)?.takeIf(String::isNotBlank) ?: return null
        return mangaDetailsParse(client.get(url).asJsoup()).apply {
            this.url = "/works/$slug"
        }
    }

    override suspend fun fetchMangaUpdate(
        manga: SManga,
        chapters: List<SChapter>,
        fetchDetails: Boolean,
        fetchChapters: Boolean,
    ): SMangaUpdate {
        val updatedManga = if (fetchDetails) {
            mangaDetailsParse(client.get(getMangaUrl(manga)).asJsoup()).apply { url = manga.url }
        } else {
            manga
        }
        val updatedChapters = if (fetchChapters) fetchChapterList(manga) else chapters
        return SMangaUpdate(updatedManga, updatedChapters)
    }

    private fun mangaDetailsParse(document: Document): SManga = SManga.create().apply {
        title = document.selectFirst("aside h1")?.text()?.trim()
            ?.takeIf(String::isNotEmpty)
            ?: error("Missing manga title")
        thumbnail_url = document.selectFirst("aside img[alt=cover image]")
            ?.attr("src")
            ?.let(::resolveCoverUrl)
        author = document.select("aside a[href^=/author/]")
            .map { it.text().trim() }
            .filter(String::isNotEmpty)
            .distinct()
            .joinToString()
        genre = document.select("aside a[href^=/genre/]")
            .map { it.text().trim() }
            .filter(String::isNotEmpty)
            .distinct()
            .joinToString()
        description = document.selectFirst("meta[name=description]")?.attr("content")?.trim()
        val statusPath = document.select("aside a[href]")
            .firstOrNull { it.attr("href") == "/ongoing" || it.attr("href") == "/completed" }
            ?.attr("href")
        status = when (statusPath) {
            "/ongoing" -> SManga.ONGOING
            "/completed" -> SManga.COMPLETED
            else -> SManga.UNKNOWN
        }
        initialized = true
    }

    private suspend fun fetchChapterList(manga: SManga): List<SChapter> {
        val mid = manga.url.substringAfter("/works/").substringBefore('-')
            .takeIf(String::isNotBlank)
            ?: error("Missing manga id")
        val chapters = mutableListOf<ChapterItem>()
        var page = 1
        var totalPages: Int

        do {
            val url = "$apiUrl/v1/manga/chapters".toHttpUrl().newBuilder()
                .addQueryParameter("mid", mid)
                .addQueryParameter("page", page.toString())
                .addQueryParameter("per_page", CHAPTER_PAGE_SIZE.toString())
                .addQueryParameter("order", "desc")
                .build()
            val response = client.get(url).parseAs<ApiResponse<ChapterPageData>>()
            response.requireSuccess()
            chapters += response.data.items
            totalPages = response.data.totalPages
            page++
        } while (page <= totalPages)

        return chapters.map(ChapterItem::toSChapter)
    }

    override suspend fun getPageList(chapter: SChapter): List<Page> {
        val readerUrl = getChapterUrl(chapter)
        val config = client.get(readerUrl).asJsoup().selectFirst("#chapcontent")
            ?: error("Missing chapter configuration")
        val apiHid = config.attr("data-api-hid").takeIf(String::isNotBlank)
            ?: error("Missing chapter API id")
        val chapterApiBase = config.attr("data-api-base-url")
            .takeIf(String::isNotBlank)
            ?: apiUrl
        val url = chapterApiBase.toChapterApiUrl().toHttpUrl().newBuilder()
            .addQueryParameter("hid", apiHid)
            .build()
        val response = client.get(url).parseAs<ApiResponse<ChapterData>>()
        response.requireSuccess()

        val imageHost = when (response.data.line) {
            9 -> config.firstNonBlankAttribute(
                "data-chapter-img-base-line1s",
                "data-chapter-img-base-line2s",
                "data-chapter-img-base",
            )
            else -> config.firstNonBlankAttribute(
                "data-chapter-img-base-line1",
                "data-chapter-img-base-line2",
                "data-chapter-img-base",
            )
        } ?: error("Missing chapter image host")

        val imagePaths = ChapterDecoder.decode(response.data.images).toMutableList()
        removeDecoyImage(imagePaths, response.data.orderId, response.data.sid)
        return imagePaths.mapIndexed { index, path ->
            val imageUrl = if (path.startsWith("http")) {
                path
            } else if (path.startsWith("//")) {
                "https:$path"
            } else {
                imageHost.trimEnd('/') + "/" + path.trimStart('/')
            }
            Page(index, imageUrl = imageUrl)
        }
    }

    override fun imageRequest(page: Page): Request {
        val imageHeaders = headers.newBuilder()
            .set("Referer", "$readerBaseUrl/")
            .build()
        return GET(page.imageUrl!!, imageHeaders)
    }

    private fun resolveCoverUrl(path: String): String = when {
        path.startsWith("http") -> path
        path.startsWith("//") -> "https:$path"
        else -> coverBaseUrl.trimEnd('/') + "/" + path.trimStart('/')
    }

    private fun String.toChapterApiUrl(): String {
        val normalized = trimEnd('/')
        return when {
            normalized.endsWith("/v2/chapter") -> normalized
            normalized.endsWith("/v2") -> "$normalized/chapter"
            normalized.endsWith("/v1") -> normalized.removeSuffix("/v1") + "/v2/chapter"
            else -> "$normalized/v2/chapter"
        }
    }

    private fun org.jsoup.nodes.Element.firstNonBlankAttribute(vararg names: String): String? = names.firstNotNullOfOrNull { name -> attr(name).takeIf(String::isNotBlank) }

    private fun removeDecoyImage(images: MutableList<String>, orderId: Int?, sid: Long?) {
        if (images.isEmpty() || orderId == null || sid == null) return
        val size = images.size.toLong()
        val offset = (((sid * DECOY_SID_FACTOR) xor (size * DECOY_SIZE_FACTOR)) % size).toInt()
        val decoyIndex = orderId xor offset
        if (decoyIndex in images.indices) images.removeAt(decoyIndex)
    }

    companion object {
        private const val PAGE_SIZE = 20
        private const val CHAPTER_PAGE_SIZE = 50
        private const val DECOY_SID_FACTOR = 2_654_435_761L
        private const val DECOY_SIZE_FACTOR = 2_246_822_507L
    }
}
