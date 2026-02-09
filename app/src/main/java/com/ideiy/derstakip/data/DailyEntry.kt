package com.ideiy.derstakip.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "daily_entries")
data class DailyEntry(
    @PrimaryKey val date: String, // yyyy-MM-dd
    val turkce: Int = 0,
    val matematik: Int = 0,
    val sosyal: Int = 0,
    val fen: Int = 0,
    val din: Int = 0,
    val ingilizce: Int = 0,
    val readingPages: Int = 0
) {
    val totalQuestions: Int
        get() = turkce + matematik + sosyal + fen + din + ingilizce
}
