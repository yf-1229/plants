package com.plants.ml

import com.plants.data.BlockType

/**
 * Bigram (n=2) next-block predictor trained on example Scratch-like programs.
 *
 * Training sequences represent common patterns: motion loops, conditional checks,
 * variable manipulation, event-driven broadcasts, and sound effects.
 */
class BlockPredictor {

    // bigram counts: given [context] -> (next -> count)
    private val bigramCounts: MutableMap<BlockType, MutableMap<BlockType, Int>> = mutableMapOf()
    // unigram fallback counts
    private val unigramCounts: MutableMap<BlockType, Int> = mutableMapOf()

    init {
        val trainingSequences: List<List<BlockType>> = listOf(
            // Simple move-and-turn loop
            listOf(BlockType.REPEAT, BlockType.MOVE, BlockType.TURN, BlockType.MOVE, BlockType.TURN, BlockType.STOP),
            // Say something, then move
            listOf(BlockType.SAY, BlockType.WAIT, BlockType.MOVE, BlockType.MOVE, BlockType.TURN, BlockType.MOVE),
            // Conditional with sound
            listOf(BlockType.IF, BlockType.SOUND, BlockType.WAIT, BlockType.SAY, BlockType.STOP),
            // Variable-driven loop
            listOf(BlockType.SET_VAR, BlockType.REPEAT, BlockType.MOVE, BlockType.CHANGE_VAR, BlockType.TURN, BlockType.MOVE, BlockType.STOP),
            // Sound-and-move pattern
            listOf(BlockType.SOUND, BlockType.MOVE, BlockType.TURN, BlockType.SOUND, BlockType.MOVE, BlockType.STOP),
            // Wait-based timing sequence
            listOf(BlockType.MOVE, BlockType.WAIT, BlockType.MOVE, BlockType.WAIT, BlockType.TURN, BlockType.WAIT, BlockType.STOP),
            // Nested repeat with variable
            listOf(BlockType.SET_VAR, BlockType.REPEAT, BlockType.IF, BlockType.CHANGE_VAR, BlockType.MOVE, BlockType.TURN),
            // Say-wait dialog pattern
            listOf(BlockType.SAY, BlockType.WAIT, BlockType.SAY, BlockType.WAIT, BlockType.SAY, BlockType.STOP),
            // Full motion program
            listOf(BlockType.MOVE, BlockType.TURN, BlockType.MOVE, BlockType.TURN, BlockType.MOVE, BlockType.TURN, BlockType.STOP),
            // Conditional branching
            listOf(BlockType.IF, BlockType.MOVE, BlockType.TURN, BlockType.IF, BlockType.SAY, BlockType.WAIT, BlockType.STOP),
            // Variable change loop
            listOf(BlockType.SET_VAR, BlockType.MOVE, BlockType.CHANGE_VAR, BlockType.MOVE, BlockType.CHANGE_VAR, BlockType.STOP),
            // Sound sequence
            listOf(BlockType.SOUND, BlockType.WAIT, BlockType.SOUND, BlockType.WAIT, BlockType.STOP),
            // Repeat-move-sound
            listOf(BlockType.REPEAT, BlockType.MOVE, BlockType.SOUND, BlockType.TURN, BlockType.MOVE, BlockType.STOP),
            // Complex conditional
            listOf(BlockType.IF, BlockType.SET_VAR, BlockType.REPEAT, BlockType.MOVE, BlockType.CHANGE_VAR, BlockType.STOP),
            // Mixed program
            listOf(BlockType.SAY, BlockType.MOVE, BlockType.TURN, BlockType.SOUND, BlockType.WAIT, BlockType.MOVE, BlockType.STOP)
        )

        for (seq in trainingSequences) {
            for (block in seq) {
                unigramCounts[block] = (unigramCounts[block] ?: 0) + 1
            }
            for (i in 0 until seq.size - 1) {
                val context = seq[i]
                val next = seq[i + 1]
                bigramCounts
                    .getOrPut(context) { mutableMapOf() }
                    .let { it[next] = (it[next] ?: 0) + 1 }
            }
        }
    }

    /**
     * Predict the next block type given the last block in the current sequence.
     * Falls back to unigram distribution when no bigram context is available.
     *
     * @param sequence Current sequence of block types (may be empty)
     * @return Pairs of (BlockType, probability) sorted by probability descending
     */
    fun predict(sequence: List<BlockType>): List<Pair<BlockType, Float>> {
        val context = sequence.lastOrNull()

        val rawCounts: Map<BlockType, Int> = if (context != null && bigramCounts.containsKey(context)) {
            bigramCounts[context]!!
        } else {
            unigramCounts
        }

        val total = rawCounts.values.sum().toFloat()
        if (total == 0f) return emptyList()

        return rawCounts
            .map { (type, count) -> type to count / total }
            .sortedByDescending { it.second }
    }
}

// ---------------------------------------------------------------------------
// LiteRT-LM scaffold — uncomment when a .task model is available
// ---------------------------------------------------------------------------
//
// import android.content.Context
// import com.google.ai.edge.litert.lm.LlmInference
// import com.google.ai.edge.litert.lm.LlmInferenceOptions
//
// /**
//  * Wraps the LiteRT-LM runtime for on-device next-block inference.
//  * Replace the NGram predictor with this once a fine-tuned .task model exists.
//  */
// class LiteRTModelManager(context: Context) {
//
//     private val options = LlmInferenceOptions.builder()
//         .setModelPath("/data/local/tmp/generate_block.task")
//         .setMaxTokens(256)
//         .setTemperature(0.7f)
//         .setTopK(40)
//         .build()
//
//     private val llm: LlmInference = LlmInference.createFromOptions(context, options)
//
//     /**
//      * Convert the current block sequence to a prompt and decode the next block prediction.
//      */
//     fun predict(sequence: List<BlockType>): List<Pair<BlockType, Float>> {
//         val prompt = "Scratch program: ${sequence.joinToString(", ") { it.label }}. Next block:"
//         val response = llm.generateResponse(prompt)
//         // Parse response tokens back to BlockType probabilities
//         return parseResponse(response)
//     }
//
//     private fun parseResponse(response: String): List<Pair<BlockType, Float>> {
//         return BlockType.entries
//             .filter { response.contains(it.label, ignoreCase = true) }
//             .mapIndexed { index, type -> type to (1f / (index + 1)) }
//     }
//
//     fun close() = llm.close()
// }