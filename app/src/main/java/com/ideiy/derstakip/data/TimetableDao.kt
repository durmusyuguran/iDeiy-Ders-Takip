package com.ideiy.derstakip.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface TimetableDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(slot: TimetableSlot)

    @Query("DELETE FROM timetable_slots")
    suspend fun clearAll()

    @Query("SELECT * FROM timetable_slots ORDER BY dayOfWeek ASC, lessonIndex ASC")
    fun observeAll(): Flow<List<TimetableSlot>>
}
