package com.ideiy.derstakip.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "timetable_slots")
data class TimetableSlot(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val dayOfWeek: Int,   // 1..5 (Pzt..Cum)
    val lessonIndex: Int, // 1..6
    val subject: String
)
