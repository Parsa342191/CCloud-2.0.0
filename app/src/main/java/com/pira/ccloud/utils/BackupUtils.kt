package com.pira.ccloud.utils

import android.content.Context
import android.net.Uri
import android.util.Log
import com.pira.ccloud.data.model.FavoriteGroup
import com.pira.ccloud.data.model.FavoriteItem
import com.pira.ccloud.data.model.WatchedEpisode
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * Container for all backup-able user data. Kept as a single versioned
 * structure so future fields can be added without breaking old backups.
 */
@Serializable
data class BackupData(
    val backupVersion: Int = 1,
    val exportedAt: Long = System.currentTimeMillis(),
    val favorites: List<FavoriteItem> = emptyList(),
    val favoriteGroups: List<FavoriteGroup> = emptyList(),
    val watchedEpisodes: List<WatchedEpisode> = emptyList()
)

sealed class BackupResult {
    data class Success(val message: String) : BackupResult()
    data class Error(val message: String) : BackupResult()
}

object BackupUtils {
    private const val TAG = "BackupUtils"

    private val json = Json {
        prettyPrint = true
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    /**
     * Builds a single JSON string containing the user's favorites (playlist),
     * favorite groups, and watch history.
     */
    fun buildBackupJson(context: Context): String {
        val backup = BackupData(
            favorites = StorageUtils.loadAllFavorites(context),
            favoriteGroups = StorageUtils.loadAllFavoriteGroups(context),
            watchedEpisodes = StorageUtils.loadAllWatchedEpisodes(context)
        )
        return json.encodeToString(backup)
    }

    /**
     * Writes the backup JSON to the given SAF Uri (chosen by the user via
     * ActivityResultContracts.CreateDocument).
     */
    fun exportToUri(context: Context, uri: Uri): BackupResult {
        return try {
            val jsonString = buildBackupJson(context)
            context.contentResolver.openOutputStream(uri)?.use { out ->
                out.write(jsonString.toByteArray(Charsets.UTF_8))
            } ?: return BackupResult.Error("Could not open output stream")
            Log.d(TAG, "Backup exported successfully")
            BackupResult.Success("Backup saved successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error exporting backup", e)
            BackupResult.Error("Export failed: ${e.message}")
        }
    }

    /**
     * Reads a backup JSON from the given SAF Uri and merges it into local
     * storage. Existing items with the same id/type are overwritten by the
     * imported ones; items that only exist locally are kept.
     */
    fun importFromUri(context: Context, uri: Uri, replaceExisting: Boolean = false): BackupResult {
        return try {
            val jsonString = context.contentResolver.openInputStream(uri)?.use { input ->
                input.readBytes().toString(Charsets.UTF_8)
            } ?: return BackupResult.Error("Could not open input stream")

            val backup = json.decodeFromString<BackupData>(jsonString)

            // Favorites
            val currentFavorites = if (replaceExisting) mutableListOf() else StorageUtils.loadAllFavorites(context).toMutableList()
            backup.favorites.forEach { imported ->
                currentFavorites.removeAll { it.id == imported.id && it.type == imported.type }
                currentFavorites.add(imported)
            }
            StorageUtils.saveAllFavorites(context, currentFavorites)

            // Favorite groups
            val currentGroups = if (replaceExisting) mutableListOf() else StorageUtils.loadAllFavoriteGroups(context).toMutableList()
            backup.favoriteGroups.forEach { imported ->
                currentGroups.removeAll { it.id == imported.id }
                currentGroups.add(imported)
            }
            if (currentGroups.none { it.isDefault }) {
                currentGroups.add(0, FavoriteGroup(id = "default", name = "Favorites", isDefault = true))
            }
            StorageUtils.saveAllFavoriteGroups(context, currentGroups)

            // Watched episodes
            val currentWatched = if (replaceExisting) mutableListOf() else StorageUtils.loadAllWatchedEpisodes(context).toMutableList()
            backup.watchedEpisodes.forEach { imported ->
                currentWatched.removeAll {
                    it.seriesId == imported.seriesId &&
                        it.seasonId == imported.seasonId &&
                        it.episodeId == imported.episodeId
                }
                currentWatched.add(imported)
            }
            StorageUtils.saveAllWatchedEpisodes(context, currentWatched)

            Log.d(TAG, "Backup imported successfully: ${backup.favorites.size} favorites, ${backup.favoriteGroups.size} groups, ${backup.watchedEpisodes.size} watched episodes")
            BackupResult.Success("Restored ${backup.favorites.size} favorites, ${backup.favoriteGroups.size} groups, ${backup.watchedEpisodes.size} watch history entries")
        } catch (e: Exception) {
            Log.e(TAG, "Error importing backup", e)
            BackupResult.Error("Import failed: ${e.message}")
        }
    }
}
