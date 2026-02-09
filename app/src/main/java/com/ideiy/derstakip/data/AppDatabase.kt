package com.ideiy.derstakip.data

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [DailyEntry::class, TimetableSlot::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun dailyDao(): DailyDao
    abstract fun timetableDao(): TimetableDao
}
