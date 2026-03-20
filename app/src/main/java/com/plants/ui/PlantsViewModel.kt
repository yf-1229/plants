package com.plants.ui

import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage
import com.plants.data.PlantsRepository

class PlantsViewModel(
    private val savedStateHandle: SavedStateHandle,
    private val plantsRepository: PlantsRepository
) : ViewModel() {
    private val plantIdKey = "plantId"
    fun updateNodeId(id: Int) {
        savedStateHandle[plantIdKey] = id
    }
}

