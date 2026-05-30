package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "app_preferences")
data class PreferenceEntity(
    @PrimaryKey val key: String,
    val value: String
)

@Entity(tableName = "daily_screentime_stats")
data class DailyStatsEntity(
    @PrimaryKey val dateStr: String, // format "YYYY-MM-DD"
    val screenTimeMinutes: Long,
    val productivityScore: Int
)

@Dao
interface TimeLeftDao {
    @Query("SELECT * FROM app_preferences")
    fun getAllPreferencesFlow(): Flow<List<PreferenceEntity>>

    @Query("SELECT * FROM app_preferences WHERE `key` = :key")
    suspend fun getPreferenceValueDirect(key: String): PreferenceEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun savePreference(preference: PreferenceEntity)

    @Query("DELETE FROM app_preferences WHERE `key` = :key")
    suspend fun deletePreference(key: String)

    @Query("SELECT * FROM daily_screentime_stats ORDER BY dateStr DESC")
    fun getDailyStatsFlow(): Flow<List<DailyStatsEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveDailyStats(stats: DailyStatsEntity)

    @Query("SELECT * FROM daily_screentime_stats WHERE dateStr = :dateStr")
    suspend fun getDailyStatsForDate(dateStr: String): DailyStatsEntity?
}
