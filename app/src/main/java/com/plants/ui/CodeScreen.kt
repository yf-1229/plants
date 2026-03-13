package com.plants.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

private data class CodeBlock(
    val label: String,
    val shape: Shape
)


private val codeBlocks = listOf(
    CodeBlock("Rectangle", RectangleShape),
    CodeBlock("Rounded", RoundedCornerShape(20.dp)),
    CodeBlock("Cut", CutCornerShape(16.dp)),
    CodeBlock("Circle", CircleShape)
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CodeScreen() {
    val placedBlocks = remember { // TODO: move to viewmodel
        mutableStateListOf(
            CodeBlock(
                label = "Run",
                shape = RectangleShape
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
                blocks = codeBlocks,
                onShapeSelected = { selected ->
                    placedBlocks.add(
                        CodeBlock(
                            label = selected.label,
                            shape = selected.shape
                        )
                    )
                }
            )
        }
    }
}

@Composable
private fun CodeBlockList(
    blocks: List<CodeBlock>,
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
                        .background(Color.Black)
                        .padding(horizontal = 16.dp, vertical = 18.dp)
                ) {
                    Text(
                        text = block.label,
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BottomShapeCarousel(
    blocks: List<CodeBlock>,
    onShapeSelected: (CodeBlock) -> Unit
) {
    val carouselState = rememberCarouselState { blocks.size }

    HorizontalMultiBrowseCarousel(
        state = carouselState,
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp)
            .background(Color.Black),
        preferredItemWidth = 180.dp,
        itemSpacing = 12.dp,
        contentPadding = PaddingValues(horizontal = 20.dp)
    ) { index ->
        val option = blocks[index]

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(vertical = 10.dp)
                .clickable { onShapeSelected(option) }
                .clip(RoundedCornerShape(28.dp))
                .background(Color.Black)
                .border(
                    width = 2.dp,
                    color = Color.White.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(28.dp)
                )
        ) {
            // Boxを重ねて、黒系のShapeレイヤーを見せる
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(92.dp)
                    .clip(option.shape)
                    .background(Color.Black)
                    .border(
                        width = 2.dp,
                        color = Color.White.copy(alpha = 0.95f),
                        shape = option.shape
                    )
            )

            Text(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 14.dp),
                text = option.label,
                style = MaterialTheme.typography.labelLarge,
                color = Color.White
            )
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