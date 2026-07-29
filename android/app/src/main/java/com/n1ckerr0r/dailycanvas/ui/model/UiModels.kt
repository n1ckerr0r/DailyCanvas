package com.n1ckerr0r.dailycanvas.ui.model

data class ArtworkCard(
    val id: String,
    val title: String,
    val artist: String,
    val year: String,
    val imageUrl: String,
    val isFavorite: Boolean,
    val description: String,
    val facts: List<String>,
    val tags: List<String>,
)

data class SettingsPayload(
    val selectedCollections: List<String>,
    val notificationsEnabled: Boolean,
    val notificationTime: String,
)

data class HomePayload(
    val greeting: String,
    val artworkOfDay: ArtworkCard,
    val gallery: List<ArtworkCard>,
    val favorites: List<ArtworkCard>,
    val settings: SettingsPayload,
    val notificationMessage: String,
)
