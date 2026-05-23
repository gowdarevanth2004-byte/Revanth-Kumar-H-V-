package com.example.data

import android.content.Context
import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "saved_models")
data class SavedModelEntity(
    @PrimaryKey val modelId: Int
)

@Entity(tableName = "model_progress")
data class ModelProgressEntity(
    @PrimaryKey val modelId: Int,
    val isViewed: Boolean = false,
    val viewedCount: Int = 0,
    val lastViewedTimestamp: Long = 0
)

@Entity(tableName = "quiz_scores")
data class QuizScoreEntity(
    @PrimaryKey val modelId: Int,
    val score: Int,
    val totalQuestions: Int,
    val completedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "user_streak")
data class UserStreakEntity(
    @PrimaryKey val id: Int = 1,
    val streakCount: Int = 0,
    val lastActiveTimestamp: Long = 0,
    val totalAiQuestions: Int = 0
)

@Dao
interface BioDao {
    // Saved Models (Favorites)
    @Query("SELECT modelId FROM saved_models")
    fun getSavedModelIds(): Flow<List<Int>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveModel(saved: SavedModelEntity)

    @Query("DELETE FROM saved_models WHERE modelId = :modelId")
    suspend fun removeSavedModel(modelId: Int)

    // Model Progress (Completed read steps)
    @Query("SELECT * FROM model_progress")
    fun getAllProgress(): Flow<List<ModelProgressEntity>>

    @Query("SELECT * FROM model_progress WHERE modelId = :modelId")
    suspend fun getProgressForModel(modelId: Int): ModelProgressEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveProgress(progress: ModelProgressEntity)

    // Quiz Scores
    @Query("SELECT * FROM quiz_scores")
    fun getAllScores(): Flow<List<QuizScoreEntity>>

    @Query("SELECT * FROM quiz_scores WHERE modelId = :modelId")
    suspend fun getScoreForModel(modelId: Int): QuizScoreEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveQuizScore(score: QuizScoreEntity)

    // User Streak
    @Query("SELECT * FROM user_streak WHERE id = 1")
    fun getUserStreak(): Flow<UserStreakEntity?>

    @Query("SELECT * FROM user_streak WHERE id = 1")
    suspend fun getUserStreakDirect(): UserStreakEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveUserStreak(streak: UserStreakEntity)
}

@Database(
    entities = [
        SavedModelEntity::class,
        ModelProgressEntity::class,
        QuizScoreEntity::class,
        UserStreakEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun bioDao(): BioDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "biolab3d_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
