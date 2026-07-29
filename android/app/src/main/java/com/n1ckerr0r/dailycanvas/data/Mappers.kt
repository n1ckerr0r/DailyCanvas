package com.n1ckerr0r.dailycanvas.data

import com.n1ckerr0r.dailycanvas.data.remote.ArtworkDetailDto
import com.n1ckerr0r.dailycanvas.data.remote.ArtworkSummaryDto
import com.n1ckerr0r.dailycanvas.data.remote.FavoriteArtworkDto
import com.n1ckerr0r.dailycanvas.data.remote.SettingsDto
import com.n1ckerr0r.dailycanvas.ui.model.ArtworkCard
import com.n1ckerr0r.dailycanvas.ui.model.SettingsPayload

fun ArtworkDetailDto.toCard(): ArtworkCard = ArtworkCard(
    id = id,
    title = title,
    artist = artist.name,
    year = year.toString(),
    imageUrl = imageUrl,
    isFavorite = isFavorite ?: false,
    description = description,
    facts = facts,
    tags = tags.map { it.name },
)

fun ArtworkSummaryDto.toCard(): ArtworkCard = ArtworkCard(
    id = id,
    title = title,
    artist = artist.name,
    year = year.toString(),
    imageUrl = imageUrl,
    isFavorite = isFavorite ?: false,
    description = "",
    facts = emptyList(),
    tags = tags.map { it.name },
)

fun FavoriteArtworkDto.toCard(): ArtworkCard = ArtworkCard(
    id = id,
    title = title,
    artist = artist.name,
    year = year.toString(),
    imageUrl = imageUrl,
    isFavorite = isFavorite ?: true,
    description = "",
    facts = emptyList(),
    tags = tags.map { it.name },
)

fun SettingsDto.toPayload(): SettingsPayload = SettingsPayload(
    selectedCollections = selectedCollections,
    notificationsEnabled = notifications.enabled,
    notificationTime = notifications.time,
)
