package com.plants.ui

import android.graphics.PathEffect
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.LineBreak
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.plants.data.Plant
import kotlinx.coroutines.delay
import kotlin.jvm.java


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    onStartClick: () -> Unit = {},
    viewModel: PlantsViewModel = viewModel(factory = AppViewModelProvider.Factory),
) {
    val homeUiState by viewModel.homeUiState.collectAsState()
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
    val coroutineScope = rememberCoroutineScope()
    val listState = rememberLazyListState()

    Surface(
        modifier = modifier.fillMaxSize().nestedScroll(scrollBehavior.nestedScrollConnection),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp, vertical = 24.dp)
        ) {
            Button(onClick = onStartClick) {
                Text(text = "分析をはじめる")
            }
            Spacer(modifier = Modifier.height(12.dp))
            PlantList(
                incompletePlantList = homeUiState.plantList,
                onItemTap = { plant ->
                    viewModel.updateId(plant.id)
                    onStartClick()
                },
                contentPadding = PaddingValues(bottom = 24.dp),
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

@Composable
private fun PlantList(
    incompletePlantList: List<Plant>,
    onItemTap: (Plant) -> Unit,
    // selectedStatus: (Plant, CodeStatus) -> Unit,
    contentPadding: PaddingValues,
    modifier: Modifier = Modifier
) {
    Box {
        var showDialog by remember { mutableStateOf(false) }
        var selectedPlant by remember { mutableStateOf<Plant?>(null) }

        val context = LocalContext.current
        val vibrator = context.getSystemService(Vibrator::class.java)

        LazyColumn(
            modifier = modifier,
            contentPadding = contentPadding
        ) {
            // 未完了タスクを表示
            items(
                items = incompletePlantList,
                key = { plant -> "incomplete_${plant.id}" }
            ) { item ->
                PlantItem(
                    item = item,
                    onItemTap = onItemTap,
                )
            }
        }
    }
}

@Composable
fun PlantItem(
    item: Plant,
    onItemTap: (Plant) -> Unit,
    modifier: Modifier = Modifier
) {
    // 一定間隔で更新???
    LaunchedEffect(key1 = Unit) {
        while (true) {
            delay(100)

        }
    }

    OutlinedCard(
        colors = CardDefaults.cardColors(
            containerColor = colorScheme.surface,
        ),
        border = BorderStroke(1.dp, Color.White),
        modifier = Modifier
            .padding(bottom = 6.dp)
            .fillMaxWidth()
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            ) {
                onItemTap(item)
            }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(6.dp)
        ) {
            // ここでRowを使って左右に分ける
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {

            }
            Text( // title
                text = item.name,
                color = Color.White,
                fontSize = 32.sp,
                modifier = Modifier.padding(start = 8.dp),
                style = TextStyle.Default.copy(
                    lineBreak = LineBreak.Heading
                )
            )
            // TODO Sub Todo List
        }
    }
}

@Preview
@Composable
fun PreviewHomeScreen() {
    HomeScreen()
}
