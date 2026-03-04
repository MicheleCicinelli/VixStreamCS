package it.vixstreamcs

import com.lagradost.cloudstream3.*
import com.lagradost.cloudstream3.LoadResponse.Companion.addTMDbId
import com.lagradost.cloudstream3.LoadResponse.Companion.addScore
import com.lagradost.cloudstream3.utils.AppUtils.parseJson
import com.lagradost.cloudstream3.utils.AppUtils.toJson
import com.lagradost.cloudstream3.utils.ExtractorLink
import com.lagradost.cloudstream3.metaproviders.TmdbProvider
import com.fasterxml.jackson.annotation.JsonProperty

class VixStreamCS : TmdbProvider() {
    override var name = "VixStreamCS"
    override var mainUrl = "https://vixsrc.to"
    override val supportedTypes = setOf(TvType.Movie, TvType.TvSeries)
    override var lang = "it"
    override val hasMainPage = true

    private val tmdbAPI = "https://api.themoviedb.org/3"
    private val apiKey = "28bd82fffe57c8854946f191d7276bf7" // Recuperata dai sorgenti forniti

    override val mainPage = mainPageOf(
        "$tmdbAPI/trending/all/day?language=it-IT&api_key=$apiKey" to "In Tendenza",
        "$tmdbAPI/movie/popular?language=it-IT&api_key=$apiKey" to "Film Popolari",
        "$tmdbAPI/tv/popular?language=it-IT&api_key=$apiKey" to "Serie TV Popolari",
        "$tmdbAPI/movie/top_rated?language=it-IT&api_key=$apiKey" to "Film Più Votati",
        "$tmdbAPI/tv/top_rated?language=it-IT&api_key=$apiKey" to "Serie TV Più Votate"
    )

    override suspend fun getMainPage(page: Int, request: MainPageRequest): HomePageResponse {
        val response = app.get("${request.data}&page=$page").parsedSafe<TmdbResults>()
        val home = response?.results?.mapNotNull { it.toSearchResponse() } ?: throw ErrorLoadingException("Nessun risultato da TMDB")
        return newHomePageResponse(request.name, home)
    }

    override suspend fun search(query: String): List<SearchResponse> {
        val url = "$tmdbAPI/search/multi?api_key=$apiKey&language=it-IT&query=$query&include_adult=true"
        val response = app.get(url).parsedSafe<TmdbResults>()
        return response?.results?.mapNotNull { it.toSearchResponse() } ?: emptyList()
    }

    override suspend fun load(url: String): LoadResponse {
        val data = parseJson<TmdbData>(url)
        val append = "external_ids,videos,credits,recommendations"
        val resUrl = if (data.type == "movie") {
            "$tmdbAPI/movie/${data.id}?api_key=$apiKey&language=it-IT&append_to_response=$append"
        } else {
            "$tmdbAPI/tv/${data.id}?api_key=$apiKey&language=it-IT&append_to_response=$append"
        }

        val res = app.get(resUrl).parsedSafe<TmdbDetails>() ?: throw ErrorLoadingException("Errore nel caricamento dei dettagli")

        return if (data.type == "movie") {
            newMovieLoadResponse(res.title ?: res.name ?: "", url, TvType.Movie, data.toJson()) {
                this.posterUrl = getImageUrl(res.posterPath)
                this.backgroundPosterUrl = getImageUrl(res.backdropPath, true)
                this.year = res.releaseDate?.split("-")?.firstOrNull()?.toIntOrNull()
                this.plot = res.overview
                this.addTMDbId(res.id.toString())
                this.addScore(res.voteAverage?.toString())
            }
        } else {
            val episodes = mutableListOf<Episode>()
            res.seasons?.forEach { season ->
                val sUrl = "$tmdbAPI/tv/${res.id}/season/${season.seasonNumber}?api_key=$apiKey&language=it-IT"
                val sRes = app.get(sUrl).parsedSafe<TmdbSeason>()
                sRes?.episodes?.forEach { ep ->
                    episodes.add(newEpisode(TmdbData(res.id, "tv", season.seasonNumber, ep.episodeNumber).toJson()) {
                        this.name = ep.name
                        this.season = season.seasonNumber
                        this.episode = ep.episodeNumber
                        this.posterUrl = getImageUrl(ep.stillPath)
                        this.description = ep.overview
                    })
                }
            }

            newTvSeriesLoadResponse(res.name ?: res.title ?: "", url, TvType.TvSeries, episodes) {
                this.posterUrl = getImageUrl(res.posterPath)
                this.backgroundPosterUrl = getImageUrl(res.backdropPath, true)
                this.year = res.firstAirDate?.split("-")?.firstOrNull()?.toIntOrNull()
                this.plot = res.overview
                this.addTMDbId(res.id.toString())
                this.addScore(res.voteAverage?.toString())
            }
        }
    }

    override suspend fun loadLinks(
        data: String,
        isCasting: Boolean,
        subtitleCallback: (SubtitleFile) -> Unit,
        callback: (ExtractorLink) -> Unit
    ): Boolean {
        val tmdbData = parseJson<TmdbData>(data)
        
        val vixUrl = if (tmdbData.type == "movie") {
            "https://vixsrc.to/movie/${tmdbData.id}"
        } else {
            "https://vixsrc.to/tv/${tmdbData.id}/${tmdbData.season ?: 1}/${tmdbData.episode ?: 1}"
        }

        VixSrcExtractor().getUrl(
            url = vixUrl,
            referer = "https://vixsrc.to/",
            subtitleCallback = subtitleCallback,
            callback = callback
        )

        return true
    }

    private fun getImageUrl(path: String?, original: Boolean = false): String? {
        if (path == null) return null
        val size = if (original) "original" else "w500"
        return "https://image.tmdb.org/t/p/$size$path"
    }

    private fun TmdbResult.toSearchResponse(): SearchResponse? {
        val mediaType = this.mediaType ?: if (this.title != null) "movie" else "tv"
        if (mediaType == "person") return null
        val type = if (mediaType == "movie") TvType.Movie else TvType.TvSeries
        return if (type == TvType.Movie) {
            newMovieSearchResponse(this.title ?: this.name ?: return null, TmdbData(this.id, "movie").toJson(), type) {
                this.posterUrl = getImageUrl(this@toSearchResponse.posterPath)
            }
        } else {
            newTvSeriesSearchResponse(this.name ?: this.title ?: return null, TmdbData(this.id, "tv").toJson(), type) {
                this.posterUrl = getImageUrl(this@toSearchResponse.posterPath)
            }
        }
    }

    data class TmdbData(
        val id: Int,
        val type: String,
        val season: Int? = null,
        val episode: Int? = null
    )

    data class TmdbResults(
        @JsonProperty("results") val results: List<TmdbResult>? = null
    )

    data class TmdbResult(
        @JsonProperty("id") val id: Int,
        @JsonProperty("media_type") val mediaType: String? = null,
        @JsonProperty("title") val title: String? = null,
        @JsonProperty("name") val name: String? = null,
        @JsonProperty("poster_path") val posterPath: String? = null
    )

    data class TmdbDetails(
        @JsonProperty("id") val id: Int,
        @JsonProperty("title") val title: String? = null,
        @JsonProperty("name") val name: String? = null,
        @JsonProperty("overview") val overview: String? = null,
        @JsonProperty("poster_path") val posterPath: String? = null,
        @JsonProperty("backdrop_path") val backdropPath: String? = null,
        @JsonProperty("release_date") val releaseDate: String? = null,
        @JsonProperty("first_air_date") val firstAirDate: String? = null,
        @JsonProperty("vote_average") val voteAverage: Double? = null,
        @JsonProperty("seasons") val seasons: List<TmdbSeasonInfo>? = null
    )

    data class TmdbSeasonInfo(
        @JsonProperty("season_number") val seasonNumber: Int
    )

    data class TmdbSeason(
        @JsonProperty("episodes") val episodes: List<TmdbEpisode>? = null
    )

    data class TmdbEpisode(
        @JsonProperty("episode_number") val episodeNumber: Int,
        @JsonProperty("name") val name: String? = null,
        @JsonProperty("overview") val overview: String? = null,
        @JsonProperty("still_path") val stillPath: String? = null
    )
}
