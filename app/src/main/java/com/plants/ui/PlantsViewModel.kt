package com.plants.ui

import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage





class PlantsViewModel {

}

class QRCodeAnalyzer(private val onCodeScanned: (String) -> Unit) : ImageAnalysis.Analyzer {
    private val scanner = BarcodeScanning.getClient()
    private var lastValue: String? = null
    private var lastScannedAt: Long = 0L
    private val debounceMs = 1500L

    @androidx.annotation.OptIn(ExperimentalGetImage::class)
    @OptIn(ExperimentalGetImage::class)
    override fun analyze(imageProxy: ImageProxy) {
        val mediaImage = imageProxy.image ?: return imageProxy.close()
        val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)

        scanner.process(image)
            .addOnSuccessListener { barcodes ->
                val value = barcodes.firstOrNull()?.rawValue ?: return@addOnSuccessListener
                val now = System.currentTimeMillis()
                val isDuplicate = value == lastValue && (now - lastScannedAt) < debounceMs
                if (!isDuplicate) {
                    lastValue = value
                    lastScannedAt = now
                    onCodeScanned(value)
                }
            }
            .addOnCompleteListener { imageProxy.close() }
    }
}