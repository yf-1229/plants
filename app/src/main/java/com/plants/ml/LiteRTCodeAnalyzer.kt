package com.plants.ml

import android.content.Context
import android.util.Log
import com.google.ai.edge.litert.Interpreter
import com.plants.data.Block
import com.plants.data.BlockType
import java.io.IOException
import java.nio.ByteBuffer
import java.nio.ByteOrder

/**
 * Analyses a block sequence for 行動性 (activity) and 冗長性 (redundancy) using
 * the LiteRT (formerly TensorFlow Lite) on-device runtime.
 *
 * When a compatible model file is bundled at `assets/code_analyzer.tflite` the
 * [Interpreter] is used for inference.  If the asset is absent the analyser
 * falls back transparently to the rule-based [BehaviorAnalyzer].
 *
 * ## Expected model I/O
 * | Tensor  | Shape                          | Description                        |
 * |---------|--------------------------------|------------------------------------|
 * | input   | `[1, MAX_SEQ_LEN × NUM_TYPES]` | One-hot encoded block sequence     |
 * | output  | `[1, 2]`                       | `[activityScore, redundancyScore]` |
 *
 * `MAX_SEQ_LEN` = 32, `NUM_TYPES` = 10 (all [BlockType] entries).
 */
class LiteRTCodeAnalyzer(context: Context) : AutoCloseable {

    private val appContext: Context = context.applicationContext

    private companion object {
        private const val TAG = "LiteRTCodeAnalyzer"
        const val MODEL_ASSET = "code_analyzer.tflite"
        const val MAX_SEQ_LEN = 32
        val BLOCK_TYPES: List<BlockType> = BlockType.entries
        val NUM_BLOCK_TYPES: Int = BLOCK_TYPES.size // 10

        // Weights for the derived generatedParameter (must sum to 1.0).
        const val ACTIVITY_WEIGHT = 0.5f
        const val DIVERSITY_WEIGHT = 0.3f
        const val INVERSE_REDUNDANCY_WEIGHT = 0.2f
    }

    private val typeIndex: Map<BlockType, Int> =
        BLOCK_TYPES.withIndex().associate { (i, t) -> t to i }

    /** Non-null when the LiteRT model was loaded successfully from assets. */
    private val interpreter: Interpreter? = tryLoadInterpreter()

    /** True when inference is backed by the LiteRT runtime. */
    val isUsingLiteRT: Boolean get() = interpreter != null

    private fun tryLoadInterpreter(): Interpreter? =
        try {
            val bytes = appContext.assets.open(MODEL_ASSET).use { it.readBytes() }
            val modelBuffer = ByteBuffer.allocateDirect(bytes.size).apply {
                order(ByteOrder.nativeOrder())
                put(bytes)
                rewind()
            }
            Interpreter(modelBuffer).also {
                Log.d(TAG, "LiteRT model loaded from assets/$MODEL_ASSET")
            }
        } catch (e: IOException) {
            Log.d(TAG, "Model asset '$MODEL_ASSET' not found — using rule-based fallback: ${e.message}")
            null // model not yet bundled — rule-based fallback will be used
        }

    // -------------------------------------------------------------------------
    // Public API
    // -------------------------------------------------------------------------

    /**
     * Analyses [sequence] and returns [CodeAnalysisParams] containing 行動性,
     * 冗長性, diversity ratio, and a derived integer [CodeAnalysisParams.generatedParameter].
     */
    fun analyze(sequence: List<Block>): CodeAnalysisParams =
        if (interpreter != null) analyzeWithLiteRT(sequence)
        else analyzeWithRules(sequence)

    // -------------------------------------------------------------------------
    // LiteRT path
    // -------------------------------------------------------------------------

    private fun analyzeWithLiteRT(sequence: List<Block>): CodeAnalysisParams {
        val inputBuffer = buildInputBuffer(sequence)
        val output = Array(1) { FloatArray(2) }
        interpreter!!.run(inputBuffer, output)
        val activityScore = output[0][0].coerceIn(0f, 1f)
        val redundancyScore = output[0][1].coerceIn(0f, 1f)
        return buildParams(sequence, activityScore, redundancyScore, usedLiteRT = true)
    }

    /**
     * Encodes [sequence] as a flat one-hot float buffer:
     * positions 0..(MAX_SEQ_LEN–1) × NUM_BLOCK_TYPES floats, row-major.
     */
    private fun buildInputBuffer(sequence: List<Block>): ByteBuffer {
        val buffer = ByteBuffer
            .allocateDirect(4 * MAX_SEQ_LEN * NUM_BLOCK_TYPES)
            .apply { order(ByteOrder.nativeOrder()) }
        val truncated = sequence.take(MAX_SEQ_LEN)
        for (i in 0 until MAX_SEQ_LEN) {
            val blockTypeIdx = truncated.getOrNull(i)?.type?.let { typeIndex[it] }
            for (j in 0 until NUM_BLOCK_TYPES) {
                buffer.putFloat(if (j == blockTypeIdx) 1f else 0f)
            }
        }
        buffer.rewind()
        return buffer
    }

    // -------------------------------------------------------------------------
    // Rule-based fallback
    // -------------------------------------------------------------------------

    private fun analyzeWithRules(sequence: List<Block>): CodeAnalysisParams {
        val metrics = BehaviorAnalyzer.analyze(sequence)
        val avgActivity = if (metrics.isEmpty()) 0f
        else metrics.map { it.activityScore }.average().toFloat()
        val avgRedundancy = if (metrics.isEmpty()) 0f
        else metrics.map { it.redundancyScore }.average().toFloat()
        return buildParams(
            sequence, avgActivity, avgRedundancy,
            usedLiteRT = false, perBlockMetrics = metrics,
        )
    }

    // -------------------------------------------------------------------------
    // Shared helpers
    // -------------------------------------------------------------------------

    private fun buildParams(
        sequence: List<Block>,
        activityScore: Float,
        redundancyScore: Float,
        usedLiteRT: Boolean,
        perBlockMetrics: List<BehaviorMetrics> = emptyList(),
    ): CodeAnalysisParams {
        val freq = sequence.groupingBy { it.type }.eachCount()
        val dominantType = freq.maxByOrNull { it.value }?.key
        val diversityRatio = if (sequence.isEmpty()) 0f
        else (freq.size.toFloat() / sequence.size).coerceIn(0f, 1f)

        // Derived integer parameter (0–100):
        // rewards high activity and diversity, penalises redundancy.
        val generatedParameter =
            ((activityScore * ACTIVITY_WEIGHT + diversityRatio * DIVERSITY_WEIGHT +
                    (1f - redundancyScore) * INVERSE_REDUNDANCY_WEIGHT) * 100)
                .toInt().coerceIn(0, 100)

        return CodeAnalysisParams(
            activityScore = activityScore,
            redundancyScore = redundancyScore,
            diversityRatio = diversityRatio,
            dominantBlockType = dominantType,
            blocksAnalyzed = sequence.size,
            generatedParameter = generatedParameter,
            perBlockMetrics = perBlockMetrics,
            analyzedByLiteRT = usedLiteRT,
        )
    }

    override fun close() {
        interpreter?.close()
    }
}
