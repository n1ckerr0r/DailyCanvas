package com.n1ckerr0r.dailycanvas.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.n1ckerr0r.dailycanvas.ui.model.ArtworkCard
import com.n1ckerr0r.dailycanvas.ui.model.SettingsPayload
import com.n1ckerr0r.dailycanvas.ui.theme.AccentRed
import com.n1ckerr0r.dailycanvas.ui.theme.DeepNavy
import com.n1ckerr0r.dailycanvas.ui.theme.WarmPaper

private sealed class AppDestination(val route: String, val label: String) {
    data object Auth : AppDestination("auth", "Вход")
    data object Home : AppDestination("home", "Сегодня")
    data object Gallery : AppDestination("gallery", "Галерея")
    data object Favorites : AppDestination("favorites", "Избранное")
    data object Settings : AppDestination("settings", "Настройки")
    data object Detail : AppDestination("detail/{artworkId}", "Картина")
}

@Composable
fun DailyCanvasApp(
    viewModel: DailyCanvasViewModel = viewModel(),
) {
    val navController = rememberNavController()
    val state by viewModel.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(state.error) {
        state.error?.let { snackbarHostState.showSnackbar(it) }
    }

    NavHost(
        navController = navController,
        startDestination = AppDestination.Auth.route,
        modifier = Modifier
            .fillMaxSize()
            .background(WarmPaper),
    ) {
        composable(AppDestination.Auth.route) {
            AuthScreen(
                onContinue = { navController.navigate(AppDestination.Home.route) },
            )
        }
        composable(AppDestination.Home.route) {
            MainScaffold(
                current = AppDestination.Home,
                snackbarHostState = snackbarHostState,
                onNavigate = { navController.navigate(it.route) },
            ) { padding ->
                HomeScreen(
                    modifier = Modifier.padding(padding),
                    state = state,
                    onOpenArtwork = {
                        viewModel.openArtwork(it)
                        navController.navigate("detail/$it")
                    },
                    onFavoriteToggle = viewModel::toggleFavorite,
                )
            }
        }
        composable(AppDestination.Gallery.route) {
            MainScaffold(
                current = AppDestination.Gallery,
                snackbarHostState = snackbarHostState,
                onNavigate = { navController.navigate(it.route) },
            ) { padding ->
                GalleryScreen(
                    modifier = Modifier.padding(padding),
                    state = state,
                    onOpenArtwork = {
                        viewModel.openArtwork(it)
                        navController.navigate("detail/$it")
                    },
                    onFavoriteToggle = viewModel::toggleFavorite,
                )
            }
        }
        composable(AppDestination.Favorites.route) {
            MainScaffold(
                current = AppDestination.Favorites,
                snackbarHostState = snackbarHostState,
                onNavigate = { navController.navigate(it.route) },
            ) { padding ->
                FavoritesScreen(
                    modifier = Modifier.padding(padding),
                    state = state,
                    onOpenArtwork = {
                        viewModel.openArtwork(it)
                        navController.navigate("detail/$it")
                    },
                )
            }
        }
        composable(AppDestination.Settings.route) {
            MainScaffold(
                current = AppDestination.Settings,
                snackbarHostState = snackbarHostState,
                onNavigate = { navController.navigate(it.route) },
            ) { padding ->
                SettingsScreen(
                    modifier = Modifier.padding(padding),
                    settings = state.payload?.settings,
                    onNotificationsChanged = viewModel::updateNotifications,
                )
            }
        }
        composable(
            route = AppDestination.Detail.route,
            arguments = listOf(navArgument("artworkId") { type = NavType.StringType }),
        ) {
            DetailScreen(
                artwork = state.selectedArtwork,
                onBack = { navController.popBackStack() },
                onFavoriteToggle = viewModel::toggleFavorite,
            )
        }
    }
}

@Composable
private fun MainScaffold(
    current: AppDestination,
    snackbarHostState: SnackbarHostState,
    onNavigate: (AppDestination) -> Unit,
    content: @Composable (PaddingValues) -> Unit,
) {
    val items = listOf(AppDestination.Home, AppDestination.Gallery, AppDestination.Favorites, AppDestination.Settings)
    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = WarmPaper,
        bottomBar = {
            NavigationBar(containerColor = Color.White) {
                items.forEach { destination ->
                    NavigationBarItem(
                        selected = current.route == destination.route,
                        onClick = { onNavigate(destination) },
                        label = { Text(destination.label) },
                        icon = { Text(destination.label.take(1)) },
                    )
                }
            }
        },
        content = content,
    )
}

@Composable
private fun AuthScreen(
    onContinue: () -> Unit,
) {
    var registerMode by rememberSaveable { mutableStateOf(false) }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(WarmPaper, Color(0xFFF3E7D7)),
                ),
            )
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = if (registerMode) "Регистрация" else "Вход",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
        )
        Spacer(Modifier.height(12.dp))
        Text(
            text = "Нативный Android-клиент на Kotlin и Jetpack Compose, подключенный к DailyCanvas backend.",
            style = MaterialTheme.typography.bodyLarge,
        )
        Spacer(Modifier.height(24.dp))
        Button(onClick = onContinue, modifier = Modifier.fillMaxWidth()) {
            Text(if (registerMode) "Создать аккаунт" else "Продолжить")
        }
        Spacer(Modifier.height(12.dp))
        Button(
            onClick = { registerMode = !registerMode },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(if (registerMode) "У меня уже есть аккаунт" else "Нет аккаунта")
        }
    }
}

@Composable
private fun HomeScreen(
    modifier: Modifier = Modifier,
    state: DailyCanvasUiState,
    onOpenArtwork: (String) -> Unit,
    onFavoriteToggle: (ArtworkCard) -> Unit,
) {
    val payload = state.payload
    when {
        state.isLoading -> Loader(modifier)
        payload == null -> EmptyState(modifier, "Данные пока недоступны")
        else -> LazyColumn(
            modifier = modifier.fillMaxSize(),
            contentPadding = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item {
                Text(payload.greeting, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                Text("Картина дня", style = MaterialTheme.typography.bodyMedium, color = Color(0xFF665D56))
            }
            item {
                ArtworkHeroCard(
                    artwork = payload.artworkOfDay,
                    onOpen = { onOpenArtwork(payload.artworkOfDay.id) },
                    onFavoriteToggle = { onFavoriteToggle(payload.artworkOfDay) },
                )
            }
            item {
                SectionTitle("Избранные работы")
            }
            item {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(payload.favorites) { artwork ->
                        ArtworkMiniCard(
                            artwork = artwork,
                            onOpen = { onOpenArtwork(artwork.id) },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun GalleryScreen(
    modifier: Modifier = Modifier,
    state: DailyCanvasUiState,
    onOpenArtwork: (String) -> Unit,
    onFavoriteToggle: (ArtworkCard) -> Unit,
) {
    val items = state.payload?.gallery.orEmpty()
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item { SectionTitle("Галерея") }
        items(items) { artwork ->
            ArtworkRow(
                artwork = artwork,
                onOpen = { onOpenArtwork(artwork.id) },
                onFavoriteToggle = { onFavoriteToggle(artwork) },
            )
        }
    }
}

@Composable
private fun FavoritesScreen(
    modifier: Modifier = Modifier,
    state: DailyCanvasUiState,
    onOpenArtwork: (String) -> Unit,
) {
    val items = state.payload?.favorites.orEmpty()
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item { SectionTitle("Избранное") }
        items(items) { artwork ->
            ArtworkRow(
                artwork = artwork,
                onOpen = { onOpenArtwork(artwork.id) },
                onFavoriteToggle = null,
            )
        }
    }
}

@Composable
private fun SettingsScreen(
    modifier: Modifier = Modifier,
    settings: SettingsPayload?,
    onNotificationsChanged: (Boolean, String) -> Unit,
) {
    val current = settings ?: return EmptyState(modifier, "Настройки пока не загружены")
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        SectionTitle("Настройки")
        SettingCard(title = "Коллекции", value = current.selectedCollections.joinToString())
        SettingToggleCard(
            title = "Напоминания",
            checked = current.notificationsEnabled,
            onCheckedChange = { onNotificationsChanged(it, current.notificationTime) },
        )
        SettingCard(title = "Время напоминания", value = current.notificationTime)
        SettingCard(title = "Версия", value = "Android / Compose")
    }
}

@Composable
private fun DetailScreen(
    artwork: ArtworkCard?,
    onBack: () -> Unit,
    onFavoriteToggle: (ArtworkCard) -> Unit,
) {
    if (artwork == null) {
        EmptyState(Modifier.fillMaxSize(), "Картина не выбрана")
        return
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            Button(onClick = onBack) { Text("Назад") }
        }
        item {
            ArtworkHeroCard(
                artwork = artwork,
                onOpen = {},
                onFavoriteToggle = { onFavoriteToggle(artwork) },
            )
        }
        item {
            SectionTitle("Описание")
            Text(artwork.description.ifBlank { "Описание будет подтягиваться из backend." })
        }
        if (artwork.facts.isNotEmpty()) {
            item { SectionTitle("Факты") }
            items(artwork.facts) { fact ->
                Text("• $fact", style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}

@Composable
private fun ArtworkHeroCard(
    artwork: ArtworkCard,
    onOpen: () -> Unit,
    onFavoriteToggle: () -> Unit,
) {
    Card(
        onClick = onOpen,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
    ) {
        Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp)
                    .background(
                        Brush.linearGradient(
                            colors = listOf(DeepNavy, Color(0xFFD3924D), Color(0xFF35544D)),
                        ),
                        shape = RoundedCornerShape(16.dp),
                    ),
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column {
                    Text(artwork.title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                    Text("${artwork.artist} • ${artwork.year}", color = Color(0xFF665D56))
                }
                Button(onClick = onFavoriteToggle) {
                    Text(if (artwork.isFavorite) "♥" else "♡")
                }
            }
            if (artwork.tags.isNotEmpty()) {
                Text(artwork.tags.joinToString(" · "), color = AccentRed)
            }
        }
    }
}

@Composable
private fun ArtworkMiniCard(
    artwork: ArtworkCard,
    onOpen: () -> Unit,
) {
    Card(
        onClick = onOpen,
        modifier = Modifier.fillParentMaxWidth(0.8f),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .background(
                        Brush.horizontalGradient(listOf(Color(0xFF293E61), Color(0xFFE2B26A))),
                        RoundedCornerShape(14.dp),
                    ),
            )
            Text(artwork.title, fontWeight = FontWeight.SemiBold)
            Text("${artwork.artist} • ${artwork.year}", color = Color(0xFF665D56))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ArtworkRow(
    artwork: ArtworkCard,
    onOpen: () -> Unit,
    onFavoriteToggle: (() -> Unit)?,
) {
    Card(
        onClick = onOpen,
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .height(84.dp)
                    .weight(0.28f)
                    .background(
                        Brush.linearGradient(listOf(Color(0xFF152945), Color(0xFF9B6A41))),
                        RoundedCornerShape(12.dp),
                    ),
            )
            Column(modifier = Modifier.weight(0.56f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(artwork.title, fontWeight = FontWeight.SemiBold)
                Text("${artwork.artist} • ${artwork.year}", color = Color(0xFF665D56))
                if (artwork.tags.isNotEmpty()) {
                    Text(artwork.tags.take(2).joinToString(" · "), color = AccentRed)
                }
            }
            if (onFavoriteToggle != null) {
                Button(onClick = onFavoriteToggle, modifier = Modifier.weight(0.16f)) {
                    Text(if (artwork.isFavorite) "♥" else "♡")
                }
            }
        }
    }
}

@Composable
private fun SettingCard(title: String, value: String) {
    Card(shape = RoundedCornerShape(18.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
        Column(modifier = Modifier.fillMaxWidth().padding(18.dp)) {
            Text(title, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(4.dp))
            Text(value, color = Color(0xFF665D56))
        }
    }
}

@Composable
private fun SettingToggleCard(
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Card(shape = RoundedCornerShape(18.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(title, fontWeight = FontWeight.SemiBold)
            Switch(checked = checked, onCheckedChange = onCheckedChange)
        }
    }
}

@Composable
private fun SectionTitle(title: String) {
    Text(title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
}

@Composable
private fun Loader(modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
    }
}

@Composable
private fun EmptyState(modifier: Modifier = Modifier, text: String) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(text)
    }
}
