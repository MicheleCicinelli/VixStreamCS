package it.vixstreamcs

import com.lagradost.cloudstream3.SubtitleFile
import com.lagradost.cloudstream3.app
import com.lagradost.cloudstream3.utils.ExtractorApi
import com.lagradost.cloudstream3.utils.ExtractorLink
import com.lagradost.cloudstream3.utils.ExtractorLinkType
import com.lagradost.cloudstream3.utils.newExtractorLink
import okhttp3.HttpUrl.Companion.toHttpUrl
import org.json.JSONObject

class VixSrcExtractor : ExtractorApi() {
    override val mainUrl = "vixsrc.to"
    override val name = "VixCloud"
    override val requiresReferer = false

    override suspend fun getUrl(
        url: String,
        referer: String?,
        subtitleCallback: (SubtitleFile) -> Unit,
        callback: (ExtractorLink) -> Unit
    ) {
        val finalUrl = if ("/movie/" in url || "/tv/" in url) {
            val apiUrl = url.replace("https://vixsrc.to/movie/", "https://vixsrc.to/api/movie/")
                .replace("https://vixsrc.to/tv/", "https://vixsrc.to/api/tv/")
            
            try {
                val apiResp = app.get(apiUrl, headers = mapOf("Referer" to url)).text
                val src = JSONObject(apiResp).getString("src")
                if (src.startsWith("/")) "https://vixsrc.to$src" else src
            } catch (e: Exception) {
                url
            }
        } else {
            url
        }

        val playlistUrl = getPlaylistLink(finalUrl, referer ?: "https://vixsrc.to/")

        callback.invoke(
            newExtractorLink(
                source = "VixSrc",
                name = "VixSrc",
                url = playlistUrl,
                type = ExtractorLinkType.M3U8
            ) {
                this.referer = referer ?: "https://vixsrc.to/"
            }
        )
    }

    private suspend fun getPlaylistLink(url: String, referer: String): String {
        val script = getScript(url, referer)
        val masterPlaylist = script.getJSONObject("masterPlaylist")
        val masterPlaylistParams = masterPlaylist.getJSONObject("params")
        val token = masterPlaylistParams.getString("token")
        val expires = masterPlaylistParams.getString("expires")
        val playlistUrl = masterPlaylist.getString("url")

        val params = "token=${token}&expires=${expires}"
        var masterPlaylistUrl = if ("?b" in playlistUrl) {
            "${playlistUrl.replace("?b:1", "?b=1")}&$params"
        } else {
            "${playlistUrl}?$params"
        }

        if (script.optBoolean("canPlayFHD", false)) {
            masterPlaylistUrl += "&h=1"
        }

        return masterPlaylistUrl
    }

    private suspend fun getScript(url: String, referer: String): JSONObject {
        val headers = mapOf(
            "Accept" to "*/*",
            "Referer" to referer,
            "User-Agent" to "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/119.0.0.0 Safari/537.36",
        )

        val resp = app.get(url, headers = headers).document
        val scripts = resp.select("script")
        val scriptMatch = scripts.find { it.data().contains("masterPlaylist") } 
            ?: throw Exception("Could not find masterPlaylist script")
        val scriptData = scriptMatch.data().replace("\n", "\t")

        val scriptJson = getSanitisedScript(scriptData)
        return JSONObject(scriptJson)
    }

    private fun getSanitisedScript(script: String): String {
        val parts = Regex("""window\.(\w+)\s*=""")
            .split(script)
            .drop(1)

        val keys = Regex("""window\.(\w+)\s*=""")
            .findAll(script)
            .map { it.groupValues[1] }
            .toList()

        val jsonObjects = keys.zip(parts).map { (key, value) ->
            val cleaned = value
                .replace(";", "")
                .replace(Regex("""(\{|\[|,)\s*(\w+)\s*:"""), "$1 \"$2\":")
                .replace(Regex(""",(\s*[}\]])"""), "$1")
                .trim()

            "\"$key\": $cleaned"
        }
        return "{\n${jsonObjects.joinToString(",\n")}\n}".replace("'", "\"")
    }
}
