package com.example.data

import kotlinx.coroutines.flow.Flow
import java.util.Calendar

class BioRepository(private val bioDao: BioDao) {
    val savedModelIds: Flow<List<Int>> = bioDao.getSavedModelIds()
    val allProgress: Flow<List<ModelProgressEntity>> = bioDao.getAllProgress()
    val allScores: Flow<List<QuizScoreEntity>> = bioDao.getAllScores()
    val userStreak: Flow<UserStreakEntity?> = bioDao.getUserStreak()

    suspend fun toggleFavorite(modelId: Int, isFav: Boolean) {
        if (isFav) {
            bioDao.saveModel(SavedModelEntity(modelId))
        } else {
            bioDao.removeSavedModel(modelId)
        }
    }

    suspend fun recordModelViewed(modelId: Int) {
        val existing = bioDao.getProgressForModel(modelId)
        val count = (existing?.viewedCount ?: 0) + 1
        bioDao.saveProgress(
            ModelProgressEntity(
                modelId = modelId,
                isViewed = true,
                viewedCount = count,
                lastViewedTimestamp = System.currentTimeMillis()
            )
        )
        updateStreak()
    }

    suspend fun saveQuizScore(modelId: Int, score: Int, total: Int) {
        bioDao.saveQuizScore(
            QuizScoreEntity(
                modelId = modelId,
                score = score,
                totalQuestions = total,
                completedAt = System.currentTimeMillis()
            )
        )
        updateStreak()
    }

    suspend fun incrementAiQuestionCount() {
        val current = bioDao.getUserStreakDirect() ?: UserStreakEntity()
        bioDao.saveUserStreak(current.copy(totalAiQuestions = current.totalAiQuestions + 1))
    }

    private suspend fun updateStreak() {
        val current = bioDao.getUserStreakDirect() ?: UserStreakEntity()
        val now = System.currentTimeMillis()
        val lastActive = current.lastActiveTimestamp

        if (lastActive == 0L) {
            bioDao.saveUserStreak(
                current.copy(
                    streakCount = 1,
                    lastActiveTimestamp = now
                )
            )
            return
        }

        val calNow = Calendar.getInstance().apply { timeInMillis = now }
        val calLast = Calendar.getInstance().apply { timeInMillis = lastActive }

        val diffYears = calNow.get(Calendar.YEAR) - calLast.get(Calendar.YEAR)
        val diffDays = if (diffYears == 0) {
            calNow.get(Calendar.DAY_OF_YEAR) - calLast.get(Calendar.DAY_OF_YEAR)
        } else {
            // Simple approximation for year cross
            365 * diffYears + (calNow.get(Calendar.DAY_OF_YEAR) - calLast.get(Calendar.DAY_OF_YEAR))
        }

        when {
            diffDays == 0 -> {
                // Same day, update last active but keep current streak
                bioDao.saveUserStreak(current.copy(lastActiveTimestamp = now))
            }
            diffDays == 1 -> {
                // Consecutive day, increment streak
                bioDao.saveUserStreak(
                    current.copy(
                        streakCount = current.streakCount + 1,
                        lastActiveTimestamp = now
                    )
                )
            }
            else -> {
                // Broke streak, reset to 1
                bioDao.saveUserStreak(
                    current.copy(
                        streakCount = 1,
                        lastActiveTimestamp = now
                    )
                )
            }
        }
    }
}
