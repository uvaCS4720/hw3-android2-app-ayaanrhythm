package edu.nd.pmcburne.hwapp.one

import com.google.gson.annotations.SerializedName

data class GameScoreRespond(
    @SerializedName("updated_at")
    val updatedAt: String = "",
    val games: List<BasketballGameWrapper> = emptyList()
)

data class BasketballGameWrapper(
    val game: BasketballGameABC = BasketballGameABC()
)

data class BasketballGameABC(
    val gameID: String = "",
    val away: BasketballGameBCD = BasketballGameBCD(),
    val home: BasketballGameBCD = BasketballGameBCD(),
    val finalMessage: String = "",
    val startTime: String = "",
    val startTimeEpoch: String = "",
    val gameState: String = "",
    val startDate: String = "",
    val currentPeriod: String = "",
    val contestClock: String = ""
)

data class BasketballGameBCD(
    val score: String? = "",
    val names: TeamNames = TeamNames(),
    val winner: Boolean = false
)

data class TeamNames(
    val short: String = "",
    val full: String = "",
    val char6: String = ""
)