package com.ideiy.derstakip.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface DailyDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entry: DailyEntry)

    @Query("SELECT * FROM daily_entries WHERE date = :date LIMIT 1")
    suspend fun getByDate(date: String): DailyEntry?

    @Query("SELECT * FROM daily_entries ORDER BY date DESC")
    fun observeAll(): Flow<List<DailyEntry>>

    @Query("SELECT SUM(turkce+matematik+sosyal+fen+din+ingilizce) FROM daily_entries")
    fun observeTotalQuestions(): Flow<Int?>

    @Query("SELECT SUM(readingPages) FROM daily_entries")
    fun observeTotalReadingPages(): Flow<Int?>

    @Query(
        """
        SELECT * FROM daily_entries
        WHERE date >= :startDate AND date <= :endDate
        ORDER BY date ASC
        """
    )
    suspend fun getBetween(startDate: String, endDate: String): List<DailyEntry>
}
