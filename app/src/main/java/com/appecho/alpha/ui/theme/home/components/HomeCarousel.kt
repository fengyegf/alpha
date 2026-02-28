package com.appecho.alpha.ui.theme.home.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material3.carousel.HorizontalMultiBrowseCarousel
import androidx.compose.material3.carousel.rememberCarouselState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.appecho.alpha.ui.theme.ParsedResult

@Composable
fun FullScreenCarousel(images: List<String>) {
    val pagerState = rememberPagerState(pageCount = { images.size })

    HorizontalPager(
        state = pagerState,
        modifier = Modifier
            .fillMaxWidth()
    ) { page ->
        AsyncImage(
            model = images[page],
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PeekCarousel(
    results: List<ParsedResult>,
    onItemClick: (ParsedResult) -> Unit
) {
    if (results.isEmpty()) return

    val state = rememberCarouselState { results.size }
    val windowInfo = LocalWindowInfo.current
    val density = LocalDensity.current
    val screenWidth = with(density) {
        windowInfo.containerSize.width.toDp()
    }
    val heroItemWidth = screenWidth - 56.dp

    HorizontalMultiBrowseCarousel(
        state = state,
        modifier = Modifier
            .fillMaxWidth()
            .height(250.dp),
        preferredItemWidth = heroItemWidth,
        itemSpacing = 16.dp,
        contentPadding = PaddingValues(horizontal = 16.dp)
    ) { i ->
        val item = results[i]
        Card(
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .maskClip(MaterialTheme.shapes.extraLarge),
            onClick = { onItemClick(item) }
        ) {
            AsyncImage(
                model = item.coverUrl ?: item.videoUrl,
                contentDescription = item.title,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }
    }
}