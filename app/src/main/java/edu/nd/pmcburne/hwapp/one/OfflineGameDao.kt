package edu.nd.pmcburne.hwapp.one

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface OfflineGameDao {
    @Query(
        """
        SELECT * FROM cached_games
        WHERE gender = :gender AND game_date = :gameDate
        ORDER BY sort_order ASC
        """
    )
    suspend fun fetchGameBasedOnDateGender(
        gender: String,
        gameDate: String
    ): List<OfflineGameDataEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun inputGameData(games: List<OfflineGameDataEntity>)

    @Query(
        """
        DELETE FROM cached_games
        WHERE gender = :gender AND game_date = :gameDate
        """
    )
    suspend fun removeGamesBasedOnDateGender(
        gender: String,
        gameDate: String
    )
}