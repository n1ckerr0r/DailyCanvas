package com.n1ckerr0r.dailycanvas.data

import com.n1ckerr0r.dailycanvas.data.remote.DailyCanvasApi
import com.n1ckerr0r.dailycanvas.data.remote.FavoriteMutationRequestDto
import com.n1ckerr0r.dailycanvas.data.remote.NetworkModule
import com.n1ckerr0r.dailycanvas.data.remote.NotificationSettingsPatchDto
import com.n1ckerr0r.dailycanvas.data.remote.SettingsPatchDto
import com.n1ckerr0r.dailycanvas.ui.model.ArtworkCard
import com.n1ckerr0r.dailycanvas.ui.model.HomePayload
import com.n1ckerr0r.dailycanvas.ui.model.SettingsPayload
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class DailyCanvasRepository(
    private val api: DailyCanvasApi = NetworkModule.api,
) {
    suspend fun loadHome(): HomePayload = withContext(Dispatchers.IO) {
        val main = api.getMain()
        val gallery = api.getGallery().items.map { it.toCard() }
        val favorites = api.getFavorites().items.map { it.toCard() }
        val settings = api.getSettings().toPayload()

        HomePayload(
            greeting = "Доброе утро",
            artworkOfDay = main.todayArtwork.toCard(),
            gallery = gallery,
            favorites = favorites,
            settings = settings,
            notificationMessage = main.todayStatus.message,
        )
    }

    suspend fun loadArtwork(artworkId: String): ArtworkCard = withContext(Dispatchers.IO) {
        api.getArtwork(artworkId).toCard()
    }

    suspend fun toggleFavorite(artworkId: String, isFavorite: Boolean) = withContext(Dispatchers.IO) {
        if (isFavorite) {
            api.removeFavorite(artworkId)
        } else {
            api.addFavorite(FavoriteMutationRequestDto(artworkId))
        }
    }

    suspend fun updateNotifications(enabled: Boolean, time: String): SettingsPayload = withContext(Dispatchers.IO) {
        api.updateSettings(
            SettingsPatchDto(
                notifications = NotificationSettingsPatchDto(
                    enabled = enabled,
                    time = time,
                ),
            ),
        ).toPayload()
    }
}
