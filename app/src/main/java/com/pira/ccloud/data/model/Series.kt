package com.pira.ccloud.data.model

import kotlinx.serialization.Serializable

@Serializable
data class Series(
    val id: Int,
    val type: String,
    val title: String,
    val description: String,
    val year: Int,
    val imdb: Double,
    val rating: Double,
    val duration: String?,
    val image: String,
    val cover: String,
    val genres: List<Genre>,
    val country: List<Country>
) {
    // Convert Series to Movie for compatibility with existing UI components
    fun toMovie(): Movie {
        return Movie(
            id = id,
            type = type,
            title = title,
            description = description,
            year = year,
            imdb = imdb,
            rating = rating,
            duration = duration,
            image = image,
            cover = cover,
            genres = genres,
            sources = emptyList(), // Series don't have sources in this model
            country = country
        )
    }
}

@Serializable
data class Season(
    val id: Int,
    val title: String,
    val episodes: List<Episode>
)

@Serializable
data class Episode(
    val id: Int,
    val title: String,
    val description: String,
    val duration: String?,
    val image: String,
    val sources: List<Source>
)

@Serializable
data class WatchedEpisode(
    val seriesId: Int,
    val seasonId: Int,
    val episodeId: Int,
    val watchedAt: Long = System.currentTimeMillis()
)

/**
 * Stores how far the user got into a specific video (movie or episode) so that
 * playback can offer to resume from that point next time it's opened.
 *
 * [key] uniquely identifies the piece of content:
 *  - For series episodes: "series_{seriesId}_season_{seasonId}_episode_{episodeId}"
 *  - For movies (no series/season/episode ids available): the raw video URL
 */
@Serializable
data class PlaybackProgress(
    val key: String,
    val positionMs: Long,
    val durationMs: Long,
    val updatedAt: Long = System.currentTimeMillis()
)