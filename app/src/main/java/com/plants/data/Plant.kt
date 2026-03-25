package com.plants.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "plants")
data class Plant(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    val description: String,
    val parameter: Int = 0,
)

enum class BlockType(val label: String, val color: Long) {
    MOVE("Move", 0xFF4C97FF),
    TURN("Turn", 0xFF4C97FF),
    WAIT("Wait", 0xFFFFAB19),
    REPEAT("Repeat", 0xFFFFAB19),
    IF("If", 0xFFFFAB19),
    SAY("Say", 0xFF9966FF),
    SOUND("Sound", 0xFF9966FF),
    STOP("Stop", 0xFFFF6680),
    SET_VAR("Set Var", 0xFFFF8C1A),
    CHANGE_VAR("Change Var", 0xFFFF8C1A)
}

data class Block(
    val id: String = UUID.randomUUID().toString(),
    val type: BlockType,
    val parameter: String = ""
)