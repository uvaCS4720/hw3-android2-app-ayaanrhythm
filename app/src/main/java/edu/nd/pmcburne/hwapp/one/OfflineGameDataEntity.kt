package edu.nd.pmcburne.hwapp.one

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate

@Entity(tableName = "cached_games")
data class OfflineGameDataEntity(
    @PrimaryKey
    @ColumnInfo(name = "cache_id")
    val cacheId: String,

    @ColumnInfo(name = "game_id")
    val gameId: String,

    @ColumnInfo(name = "gender")
    val gender: String,

    @ColumnInfo(name = "game_date")
    val gameDate: String,

    @ColumnInfo(name = "sort_order")
    val sortOrder: Int,

    @ColumnInfo(name = "api_updated_at")
    val apiUpdatedAt: String,

    @ColumnInfo(name = "away_team_name")
    val awayTeamName: String,

    @ColumnInfo(name = "home_team_name")
    val homeTeamName: String,

    @ColumnInfo(name = "away_score")
    val awayScore: String?,

    @ColumnInfo(name = "home_score")
    val homeScore: String?,

    @ColumnInfo(name = "status")
    val status: String,

    @ColumnInfo(name = "state_label")
    val stateLabel: String,

    @ColumnInfo(name = "start_time")
    val startTime: String,

    @ColumnInfo(name = "current_period")
    val currentPeriod: String,

    @ColumnInfo(name = "time_remaining")
    val timeRemaining: String,

    @ColumnInfo(name = "winner_name")
    val winnerName: String?
)

fun BasketballMatchDetails.toCachedEntity(
    gender: RhythmicSportsGender,
    date: LocalDate,
    updatedAt: String,
    sortOrder: Int
): OfflineGameDataEntity {
    return OfflineGameDataEntity(
        cacheId = "${gender.pathValue}_${date}_$matchID",
        gameId = matchID,
        gender = gender.pathValue,
        gameDate = date.toString(),
        sortOrder = sortOrder,
        apiUpdatedAt = updatedAt,
        awayTeamName = visitorName,
        homeTeamName = homeName,
        awayScore = awayPoints,
        homeScore = homePoints,
        status = state.name,
        stateLabel = statusText,
        startTime = beginTime,
        currentPeriod = presentPeriod,
        timeRemaining = leftOverTime,
        winnerName = gameVictory
    )
}

fun OfflineGameDataEntity.gameSumDetail(): BasketballMatchDetails {
    val parsedStatus = try {
        BasketballGameState.valueOf(status)
    } catch (_: Exception) {
        BasketballGameState.UPCOMING
    }

    return BasketballMatchDetails(
        matchID = gameId,
        visitorName = awayTeamName,
        homeName = homeTeamName,
        awayPoints = awayScore,
        homePoints = homeScore,
        state = parsedStatus,
        statusText = stateLabel,
        beginTime = startTime,
        presentPeriod = currentPeriod,
        leftOverTime = timeRemaining,
        gameVictory = winnerName
    )
}