package com.plants.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.carousel.HorizontalMultiBrowseCarousel
import androidx.compose.material3.carousel.rememberCarouselState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

private data class ShapeOption(
    val label: String,
    val shape: Shape,
    val colors: List<Color>
)

private data class CodeBlockUi(
    val label: String,
    val shape: Shape,
    val colors: List<Color>
)

private val shapeOptions = listOf(
    ShapeOption("Rectangle", RectangleShape, listOf(Color(0xFF9ED67D), Color(0xFF6BBF59))),
    ShapeOption("Rounded", RoundedCornerShape(20.dp), listOf(Color(0xFF7FC9FF), Color(0xFF4AA5E8))),
    ShapeOption("Cut", CutCornerShape(16.dp), listOf(Color(0xFFFFD27F), Color(0xFFFFB347))),
    ShapeOption("Circle", CircleShape, listOf(Color(0xFFE5A8FF), Color(0xFFCB7FFF)))
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CodeScreen() {
    val placedBlocks = remember {
        mutableStateListOf(
            CodeBlockUi(
                label = "Run",
                shape = RectangleShape,
                colors = listOf(Color(0xFF9ED67D), Color(0xFF6BBF59))
            )
        )
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 24.dp, bottom = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            CodeBlockList(
                blocks = placedBlocks,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            )

            BottomShapeCarousel(
                options = shapeOptions,
                onShapeSelected = { selected ->
                    placedBlocks.add(
                        CodeBlockUi(
                            label = selected.label,
                            shape = selected.shape,
                            colors = selected.colors
                        )
                    )
                }
            )
        }
    }
}

@Composable
private fun CodeBlockList(
    blocks: List<CodeBlockUi>,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(vertical = 8.dp)
    ) {
        items(blocks) { block ->
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = block.shape,
                tonalElevation = 2.dp
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Brush.horizontalGradient(block.colors))
                        .padding(horizontal = 16.dp, vertical = 18.dp)
                ) {
                    Text(
                        text = block.label,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BottomShapeCarousel(
    options: List<ShapeOption>,
    onShapeSelected: (ShapeOption) -> Unit
) {
    val carouselState = rememberCarouselState { options.size }

    HorizontalMultiBrowseCarousel(
        state = carouselState,
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp),
        preferredItemWidth = 180.dp,
        itemSpacing = 12.dp,
        contentPadding = PaddingValues(horizontal = 20.dp)
    ) { index ->
        val option = options[index]
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .clickable { onShapeSelected(option) },
            tonalElevation = 2.dp,
            shape = option.shape
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Brush.verticalGradient(option.colors)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = option.label,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
    }
}

@Preview
@Composable
fun CodeScreenPreview() {
    MaterialTheme {
        CodeScreen()
    }
}