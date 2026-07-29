package com.n1ckerr0r.dailycanvas.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.n1ckerr0r.dailycanvas.data.DailyCanvasRepository
import com.n1ckerr0r.dailycanvas.ui.model.ArtworkCard
import com.n1ckerr0r.dailycanvas.ui.model.HomePayload
import com.n1ckerr0r.dailycanvas.ui.model.SettingsPayload
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class DailyCanvasViewModel(
    private val repository: DailyCanvasRepository = DailyCanvasRepository(),
) : ViewModel() {
    private val _state = MutableStateFlow(DailyCanvasUiState())
    val state: StateFlow<DailyCanvasUiState> = _state.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            runCatching { repository.loadHome() }
                .onSuccess { payload ->
                    _state.value = DailyCanvasUiState(
                        isLoading = false,
                        payload = payload,
                        selectedArtwork = payload.artworkOfDay,
                    )
                }
                .onFailure { error ->
                    _state.value = _state.value.copy(
                        isLoading = false,
                        error = error.message ?: "Не удалось загрузить данные",
                    )
                }
        }
    }

    fun openArtwork(artworkId: String) {
        viewModelScope.launch {
            val payload = _state.value.payload ?: return@launch
            val cached = (payload.gallery + payload.favorites + payload.artworkOfDay)
                .firstOrNull { it.id == artworkId }
            _state.value = _state.value.copy(selectedArtwork = cached ?: payload.artworkOfDay)
            runCatching { repository.loadArtwork(artworkId) }
                .onSuccess { loaded -> _state.value = _state.value.copy(selectedArtwork = loaded) }
        }
    }

    fun toggleFavorite(artwork: ArtworkCard) {
        viewModelScope.launch {
            runCatching { repository.toggleFavorite(artwork.id, artwork.isFavorite) }
                .onSuccess { refresh() }
        }
    }

    fun updateNotifications(enabled: Boolean, time: String) {
        viewModelScope.launch {
            runCatching { repository.updateNotifications(enabled, time) }
                .onSuccess { settings ->
                    val payload = _state.value.payload ?: return@onSuccess
                    _state.value = _state.value.copy(
                        payload = payload.copy(settings = settings),
                    )
                }
        }
    }
}

data class DailyCanvasUiState(
    val isLoading: Boolean = true,
    val payload: HomePayload? = null,
    val selectedArtwork: ArtworkCard? = null,
    val error: String? = null,
)
