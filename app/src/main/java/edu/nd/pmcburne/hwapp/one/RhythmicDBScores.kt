package edu.nd.pmcburne.hwapp.one

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [OfflineGameDataEntity::class],
    version = 1,
    exportSchema = false
)
abstract class RhythmicDBScores : RoomDatabase() {
    abstract fun offlineGameDao(): OfflineGameDao

    companion object {
        @Volatile
        private var databaseInst: RhythmicDBScores? = null

        fun fetchGameDB(applicationContext: Context): RhythmicDBScores {
            return databaseInst ?: synchronized(this) {
                val createDatabaseGame = Room.databaseBuilder(
                    applicationContext.applicationContext,
                    RhythmicDBScores::class.java,
                    "rhythm_scores_database"
                ).build()

                databaseInst = createDatabaseGame
                createDatabaseGame
            }
        }
    }
}