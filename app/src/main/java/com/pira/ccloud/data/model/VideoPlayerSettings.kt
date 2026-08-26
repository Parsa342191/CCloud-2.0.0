package com.pira.ccloud.data.model

import kotlinx.serialization.Serializable

@Serializable
data class VideoPlayerSettings(
    val seekTimeSeconds: Int = 10
) {
    companion object {
        val DEFAULT = VideoPlayerSettings()
    }
}