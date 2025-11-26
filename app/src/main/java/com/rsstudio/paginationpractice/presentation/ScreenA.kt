package com.rsstudio.paginationpractice.presentation

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.rsstudio.paginationpractice.AppDrawable
import com.rsstudio.paginationpractice.AppString
import com.rsstudio.paginationpractice.common.ErrorMessage
import com.rsstudio.paginationpractice.common.HorizontalProgressLoader
import com.rsstudio.paginationpractice.common.ProgressLoader

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScreenA(
    onAction: (ScreenAActions) -> Unit,
    viewModel: ScreenAViewModel = hiltViewModel()
) {
    // FIX: Always show Scaffold with search box, handle empty state inside content
    // BEFORE: EMPTY state took whole screen, hiding search box
    // ISSUE: User couldn't search again when "No data available" was shown
    // NOW: Search box always visible, empty state shows inside content area
    Scaffold(
        topBar = {
            TopAppBar(title = {
                Text(
                    modifier = Modifier.clickable {

                    },
                    text = "Movie Search",
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontSize = 20.sp,
                    )
                )
            }
            )
        })
    { innerPadding ->
        ScreenAContent(
            modifier = Modifier.padding(innerPadding),
            uiState = viewModel.uiState,
            onEvent = viewModel::onEvent
        )
    }

    LaunchedEffect(key1 = viewModel.uiSideEffect) {
        handleSideEffect(
            screenASideEffect = viewModel.uiSideEffect,
            onActions = {
                onAction(it)
            }
        )
        viewModel.resetSideEffect()
    }
}

@Composable
fun ScreenAContent(
    modifier: Modifier = Modifier,
    uiState: ScreenAUiState,
    onEvent: (ScreenAEvent) -> Unit
) {
    val listState = rememberLazyListState()
    LaunchedEffect(listState.canScrollForward, uiState.isLoadingMore) {
        if (!listState.canScrollForward && !uiState.isLoadingMore && uiState.data.isNotEmpty()) {
            onEvent(ScreenAEvent.LoadMore)
        }
    }

    Column(modifier = modifier.fillMaxSize()) {
        SearchBox(
            searchQuery = uiState.searchQuery,
            onSearchQueryChange = { onEvent(ScreenAEvent.OnSearchQueryChange(it)) },
            onSearch = { onEvent(ScreenAEvent.OnSearch) },
            isSearching = uiState.isSearching
        )

        if (uiState.isSearching) {
            ProgressLoader()
        } else if (uiState.data.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = when (uiState.screenState) {
                        ScreenState.EMPTY -> "No movies found. Try a different search!"
                        else -> "Search for your favorite movies above 🎬"
                    },
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(32.dp)
                )
            }
        } else {
            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize()
            ) {
                items(uiState.data) {
                    MovieItem(item = it)
                }

                if (uiState.isLoadingMore) {
                    item {
                        HorizontalProgressLoader()
                    }
                }
                if (uiState.paginationError != null) {
                    item {
                        Text(
                            text = uiState.paginationError,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SearchBox(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onSearch: () -> Unit,
    isSearching: Boolean,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchQueryChange,
            modifier = Modifier
                .weight(1f)
                .padding(end = 8.dp),
            placeholder = { Text("Search movies...") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                imeAction = ImeAction.Search
            ),
            keyboardActions = KeyboardActions(
                onSearch = { onSearch() }
            ),
            enabled = !isSearching
        )

        Button(
            onClick = onSearch,
            enabled = searchQuery.isNotBlank() && !isSearching,
            modifier = Modifier.height(56.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "Search"
            )
        }
    }
}

@Composable
fun MovieItem(
    modifier: Modifier = Modifier,
    item: MovieItem
) {
    Column(
        modifier = modifier.padding(horizontal = 24.dp)
    ) {
        AsyncImage(
            model = item.imageUrl,
            contentDescription = "movie_image",
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp),
            error = painterResource(AppDrawable.ic_launcher_foreground),
            placeholder = painterResource(AppDrawable.ic_launcher_background),
            contentScale = ContentScale.FillBounds
        )
        Spacer(Modifier.size(24.dp))
        Text(
            text = item.title,
            style = MaterialTheme.typography.bodyLarge.copy(
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        )
        Spacer(Modifier.size(12.dp))
        Text(
            text = item.description
        )
        Spacer(Modifier.size(24.dp))
    }
}

// NOTE: This extension function was previously used directly in the composable body
// which caused infinite recomposition loops. It's kept here for reference but no longer used.
// 
// OLD PROBLEMATIC USAGE:
// if (listState.isScrolledToTheEnd()) {
//     onEvent(ScreenAEvent.LoadMore)  // <- This was called every recomposition!
// }
//
// We now use LaunchedEffect with canScrollForward to properly handle pagination triggers
fun LazyListState.isScrolledToTheEnd(): Boolean {
    val layoutInfo = this.layoutInfo
    val visibleItems = layoutInfo.visibleItemsInfo
    return visibleItems.isNotEmpty() && visibleItems.last().index == layoutInfo.totalItemsCount - 1
}


fun handleSideEffect(
    screenASideEffect: ScreenASideEffect,
    onActions: (ScreenAActions) -> Unit
) {
    when (screenASideEffect) {
        ScreenASideEffect.None -> {

        }

        ScreenASideEffect.OnBack -> {

        }
    }
}

sealed interface ScreenAActions {
    data object OnBack : ScreenAActions
}

@Preview(showBackground = true)
@Composable
fun ScreenAPreview() {
    val mockData = ScreenAUiState(
        data = listOf(
            MovieItem(
                id = "1",
                title = "Inception",
                description = "A skilled thief enters people’s dreams to steal secrets.",
                imageUrl = "https://example.com/images/inception.jpg"
            ),
            MovieItem(
                id = "2",
                title = "Interstellar",
                description = "A team of explorers travels through a wormhole to save humanity.",
                imageUrl = "https://example.com/images/interstellar.jpg"
            ),
            MovieItem(
                id = "3",
                title = "The Dark Knight",
                description = "Batman faces the Joker in a battle for Gotham’s soul.",
                imageUrl = "https://example.com/images/dark_knight.jpg"
            ),
            MovieItem(
                id = "4",
                title = "Avatar",
                description = "A marine on an alien world torn between duty and love.",
                imageUrl = "https://example.com/images/avatar.jpg"
            ),
            MovieItem(
                id = "5",
                title = "Titanic",
                description = "A love story set on the ill-fated Titanic.",
                imageUrl = "https://example.com/images/titanic.jpg"
            ),
            MovieItem(
                id = "6",
                title = "The Matrix",
                description = "A hacker discovers the shocking truth about his reality.",
                imageUrl = "https://example.com/images/matrix.jpg"
            ),
            MovieItem(
                id = "7",
                title = "Gladiator",
                description = "A Roman general seeks revenge as a gladiator.",
                imageUrl = "https://example.com/images/gladiator.jpg"
            ),
            MovieItem(
                id = "8",
                title = "Joker",
                description = "A failed comedian descends into madness and chaos.",
                imageUrl = "https://example.com/images/joker.jpg"
            ),
            MovieItem(
                id = "9",
                title = "Frozen",
                description = "Two sisters navigate magic, fear, and family bonds.",
                imageUrl = "https://example.com/images/frozen.jpg"
            ),
            MovieItem(
                id = "10",
                title = "Avengers: Endgame",
                description = "Earth’s mightiest heroes unite for one last battle.",
                imageUrl = "https://example.com/images/endgame.jpg"
            )
        )
    )
    ScreenAContent(
        uiState = mockData,
        onEvent = {

        }
    )
}