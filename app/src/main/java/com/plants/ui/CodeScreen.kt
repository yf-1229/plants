package com.plants.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.carousel.HorizontalMultiBrowseCarousel
import androidx.compose.material3.carousel.rememberCarouselState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.plants.data.BlockType
import com.plants.ml.CodeAnalysisParams

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CodeScreen(
    viewModel: PlantsViewModel = viewModel(factory = AppViewModelProvider.Factory),
) {
    val context = LocalContext.current
    val analysisParams by viewModel.analysisParams.collectAsState()
    val placedBlocks = viewModel.placedBlocks

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
            // Placed block list
            PlacedBlockList(
                blockTypes = placedBlocks.map { it.type },
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            )

            // Analysis result panel (visible once analysis has run)
            analysisParams?.let { params ->
                AnalysisResultPanel(
                    params = params,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp)
                )
            }

            // Action buttons
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                OutlinedButton(onClick = { viewModel.removeLastBlock() }) {
                    Text("削除")
                }
                Button(onClick = { viewModel.analyzeBlocks(context) }) {
                    Text("分析")
                }
            }

            // Block-type palette
            BlockTypePalette(
                onBlockSelected = { viewModel.addBlock(it) }
            )
        }
    }
}

@Composable
private fun PlacedBlockList(
    blockTypes: List<BlockType>,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier.padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(vertical = 8.dp),
    ) {
        items(blockTypes) { blockType ->
            val blockColor = Color(blockType.color)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(blockColor.copy(alpha = 0.15f))
                    .border(
                        width = 1.dp,
                        color = blockColor,
                        shape = RoundedCornerShape(8.dp),
                    )
                    .padding(horizontal = 16.dp, vertical = 14.dp)
            ) {
                Text(
                    text = blockType.label,
                    style = MaterialTheme.typography.titleMedium,
                    color = blockColor,
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }
    }
}

/**
 * Shows the 行動性 / 冗長性 scores and the generated parameter derived by
 * [LiteRTCodeAnalyzer].
 */
@Composable
private fun AnalysisResultPanel(
    params: CodeAnalysisParams,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = Color(0xFF1A1A2E),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "分析結果",
                    style = MaterialTheme.typography.titleSmall,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                )
                if (params.analyzedByLiteRT) {
                    Text(
                        text = "LiteRT",
                        color = Color(0xFF4ECDC4),
                        fontSize = 11.sp,
                    )
                }
            }

            ScoreRow(label = "行動性", score = params.activityScore)
            ScoreRow(label = "冗長性", score = params.redundancyScore)
            ScoreRow(label = "多様性", score = params.diversityRatio)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "パラメータ",
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 13.sp,
                )
                Text(
                    text = params.generatedParameter.toString(),
                    color = Color.White,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                )
            }
        }
    }
}

@Composable
private fun ScoreRow(label: String, score: Float) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = label,
            color = Color.White.copy(alpha = 0.7f),
            fontSize = 13.sp,
            modifier = Modifier.width(48.dp),
        )
        LinearProgressIndicator(
            progress = { score },
            modifier = Modifier
                .weight(1f)
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp)),
            color = Color(0xFF4ECDC4),
            trackColor = Color.White.copy(alpha = 0.1f),
        )
        Text(
            text = "${(score * 100).toInt()}%",
            color = Color.White,
            fontSize = 12.sp,
            modifier = Modifier.width(36.dp),
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BlockTypePalette(
    onBlockSelected: (BlockType) -> Unit,
) {
    val blockTypes = BlockType.entries
    val carouselState = rememberCarouselState { blockTypes.size }

    HorizontalMultiBrowseCarousel(
        state = carouselState,
        modifier = Modifier
            .fillMaxWidth()
            .height(160.dp)
            .background(Color.Black),
        preferredItemWidth = 140.dp,
        itemSpacing = 10.dp,
        contentPadding = PaddingValues(horizontal = 16.dp),
    ) { index ->
        val blockType = blockTypes[index]
        val blockColor = Color(blockType.color)

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(vertical = 10.dp)
                .clickable { onBlockSelected(blockType) }
                .clip(RoundedCornerShape(20.dp))
                .background(Color.Black)
                .border(
                    width = 2.dp,
                    color = blockColor.copy(alpha = 0.8f),
                    shape = RoundedCornerShape(20.dp),
                )
        ) {
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(44.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(blockColor.copy(alpha = 0.2f))
                    .border(
                        width = 2.dp,
                        color = blockColor,
                        shape = RoundedCornerShape(8.dp),
                    )
            )
            Text(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 12.dp),
                text = blockType.label,
                style = MaterialTheme.typography.labelMedium,
                color = blockColor,
                fontWeight = FontWeight.SemiBold,
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
