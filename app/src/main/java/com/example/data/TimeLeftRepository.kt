package com.example.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class TimeLeftRepository(private val dao: TimeLeftDao) {

    // Exposures
    val allPreferences: Flow<List<PreferenceEntity>> = dao.getAllPreferencesFlow()
    val dailyStats: Flow<List<DailyStatsEntity>> = dao.getDailyStatsFlow()

    suspend fun getPreference(key: String): String? {
        return dao.getPreferenceValueDirect(key)?.value
    }

    suspend fun savePreference(key: String, value: String) {
        dao.savePreference(PreferenceEntity(key, value))
    }

    suspend fun deletePreference(key: String) {
        dao.deletePreference(key)
    }

    // Daily statistics
    suspend fun recordScreentimeStats(dateStr: String, screenTimeMinutes: Long, targetLimitMinutes: Long) {
        // Calculate productivity score: 100 is excellent, scaling down as screen time exceeds target.
        // Formula: score = (1.0 - (usage / (target * 1.5))) * 100
        // Safeguard between 0 and 100.
        val score = if (screenTimeMinutes == 0L) {
            100
        } else {
            val ratio = screenTimeMinutes.toDouble() / targetLimitMinutes.toDouble()
            val baseScore = ((1.5 - ratio) / 1.5) * 100
            baseScore.coerceIn(0.0, 100.0).toInt()
        }

        dao.saveDailyStats(
            DailyStatsEntity(
                dateStr = dateStr,
                screenTimeMinutes = screenTimeMinutes,
                productivityScore = score
            )
        )
    }

    suspend fun getDailyStatsForDate(dateStr: String): DailyStatsEntity? {
        return dao.getDailyStatsForDate(dateStr)
    }

    suspend fun saveDailyStats(entity: DailyStatsEntity) {
        dao.saveDailyStats(entity)
    }
}
