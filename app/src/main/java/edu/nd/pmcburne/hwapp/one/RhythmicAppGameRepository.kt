package edu.nd.pmcburne.hwapp.one

import android.content.Context
import androidx.room.withTransaction
import java.time.LocalDate
import java.util.Locale

enum class RhythmicSportsGender(val pathValue: String) {
    WOMEN("women"),
    MEN ("men")
}

enum class BasketballGameState {
    LIVE,
    FINAL,
    UPCOMING
}

enum class BasketballGameDataSource {
    OFFLINE_CACHE,
    NETWORK,
    CACHE_AFTER_NETWORK_ERROR
}

data class BasketballGameResult(
    val refreshTime: String,
    val gameResults: List<BasketballMatchDetails>
)

data class LoadedScoreboardResult(
    val lastUpdated: String,
    val gameList: List<BasketballMatchDetails>,
    val resultOrigin: BasketballGameDataSource,
    val statusMessage: String?
)

data class BasketballMatchDetails(
    val matchID: String,
    val visitorName: String,
    val homeName: String,
    val awayPoints: String?,
    val homePoints: String?,
    val state: BasketballGameState,
    val statusText: String,
    val beginTime: String,
    val presentPeriod: String,
    val leftOverTime: String,
    val gameVictory: String?
)

class RhythmicAppGameRepository(
    context: Context,
    private val gameAPI: RhythmicAppAPICalls = APIProvider.ConnectionAPI
) {
    private val gameDB = RhythmicDBScores.fetchGameDB(context)
    private val gameCashedData = gameDB.offlineGameDao()
    private val connectivityChecker = GameConnectivityMonitor(context)

    suspend fun fetchScores(
        gameGender: RhythmicSportsGender,
        gameDate: LocalDate
    ): LoadedScoreboardResult {
        val offlineResult = offlineScoresLoading(gameGender, gameDate)

        if (connectivityChecker.isConnected()) {
            return try {
                val apiResult = getScoresOfGames(gameGender, gameDate)
                storeScoreOffline(gameGender, gameDate, apiResult)
                offlineScoresLoading(gameGender, gameDate).copy(
                    resultOrigin = BasketballGameDataSource.NETWORK,
                    statusMessage = null
                )
            } catch (e: Exception) {
                if (offlineResult.gameList.isNotEmpty()) {
                    offlineResult.copy(
                        resultOrigin = BasketballGameDataSource.CACHE_AFTER_NETWORK_ERROR,
                        statusMessage = "Showing previously saved Basketball scores"
                    )
                } else {
                    throw e
                }
            }
        }

        return if (offlineResult.gameList.isNotEmpty()) {
            offlineResult.copy(
                resultOrigin = BasketballGameDataSource.OFFLINE_CACHE,
                statusMessage = "No Internet connection.Showing previously saved Basketball scores."
            )
        } else {
            offlineResult.copy(
                resultOrigin = BasketballGameDataSource.OFFLINE_CACHE,
                statusMessage = "No Internet connection. There is no saved scores for this date."
            )
        }
    }

    private suspend fun getScoresOfGames(
        gameGender: RhythmicSportsGender,
        gameDate: LocalDate
    ): BasketballGameResult {
        val requestFinish = String.format(
            Locale.US,
            "scoreboard/basketball-%s/d1/%04d/%02d/%02d",
            gameGender.pathValue,
            gameDate.year,
            gameDate.monthValue,
            gameDate.dayOfMonth
        )

        val gameRespond = gameAPI.fetchGameScores(requestFinish)
        return gameRespond.gameScoreResult(gameGender)
    }

    private suspend fun storeScoreOffline(
        gameGender: RhythmicSportsGender,
        gameDate: LocalDate,
        gameScoresR: BasketballGameResult
    ) {
        val offlineDB = gameScoresR.gameResults.mapIndexed { position, sumGame ->
            sumGame.toCachedEntity(
                gender = gameGender,
                date = gameDate,
                updatedAt = gameScoresR.refreshTime,
                sortOrder = position
            )
        }

        gameDB.withTransaction {
            gameCashedData.removeGamesBasedOnDateGender(
                gender = gameGender.pathValue,
                gameDate = gameDate.toString()
            )

            if (offlineDB.isNotEmpty()) {
                gameCashedData.inputGameData(offlineDB)
            }
        }
    }

    private suspend fun offlineScoresLoading(
        gameGender: RhythmicSportsGender,
        gameDate: LocalDate
    ): LoadedScoreboardResult {
        val offlineGameData = gameCashedData.fetchGameBasedOnDateGender(
            gender = gameGender.pathValue,
            gameDate = gameDate.toString()
        )

        return LoadedScoreboardResult(
            lastUpdated = offlineGameData.firstOrNull()?.apiUpdatedAt ?: "",
            gameList = offlineGameData.map { it.gameSumDetail() },
            resultOrigin = BasketballGameDataSource.OFFLINE_CACHE,
            statusMessage = null
        )
    }

    private fun GameScoreRespond.gameScoreResult(
        gameGender: RhythmicSportsGender
    ): BasketballGameResult {
        return BasketballGameResult(
            refreshTime = updatedAt,
            gameResults = games.map { it.game.gameSumDetail(gameGender) }
        )
    }

    private fun BasketballGameABC.gameSumDetail(gender: RhythmicSportsGender): BasketballMatchDetails {
        val receivedState = when (gameState.lowercase(Locale.US)) {
            "live" -> BasketballGameState.LIVE
            "final" -> BasketballGameState.FINAL
            else -> BasketballGameState.UPCOMING
        }

        val gameWinner = when {
            receivedState != BasketballGameState.FINAL -> null
            away.winner -> away.getTeamName()
            home.winner -> home.getTeamName()
            else -> null
        }

        val gameStatus = when (receivedState) {
            BasketballGameState.UPCOMING -> "Upcoming"
            BasketballGameState.LIVE -> "Live"
            BasketballGameState.FINAL -> "Final"
        }

        return BasketballMatchDetails(
            matchID = gameID,
            visitorName = away.getTeamName(),
            homeName = home.getTeamName(),
            awayPoints = away.score?.takeIf { it.isNotBlank() },
            homePoints = home.score?.takeIf { it.isNotBlank() },
            state = receivedState,
            statusText = gameStatus,
            beginTime = startTime,
            presentPeriod = gamePrdLabel(currentPeriod, receivedState, gender),
            leftOverTime = gameTimeLeft(contestClock, receivedState),
            gameVictory = gameWinner
        )
    }

    private fun gamePrdLabel(
        gamePeriod: String,
        gameState: BasketballGameState,
        gameGender: RhythmicSportsGender
    ): String {
        if (gameState == BasketballGameState.FINAL) return "Final"
        if (gamePeriod.isBlank()) return ""

        return when (gameGender) {
            RhythmicSportsGender.MEN -> {
                when (gamePeriod.lowercase(Locale.US)) {
                    "1st" -> "1st Half"
                    "2nd" -> "2nd Half"
                    "ot" -> "OT"
                    else -> gamePeriod
                }
            }

            RhythmicSportsGender.WOMEN -> {
                when (gamePeriod.lowercase(Locale.US)) {
                    "1st" -> "1st Quarter"
                    "2nd" -> "2nd Quarter"
                    "3rd" -> "3rd Quarter"
                    "4th" -> "4th Quarter"
                    "ot" -> "OT"
                    else -> gamePeriod
                }
            }
        }
    }

    private fun gameTimeLeft(
        gameTimer: String,
        gameState: BasketballGameState
    ): String {
        return when (gameState) {
            BasketballGameState.FINAL -> "Final"
            BasketballGameState.LIVE -> if (gameTimer.isBlank()) "N/A" else gameTimer
            BasketballGameState.UPCOMING -> ""
        }
    }

    private fun BasketballGameBCD.getTeamName(): String {
        return when {
            names.short.isNotBlank() -> names.short
            names.full.isNotBlank() -> names.full
            names.char6.isNotBlank() -> names.char6
            else -> "Unknown Team"
        }
    }
}