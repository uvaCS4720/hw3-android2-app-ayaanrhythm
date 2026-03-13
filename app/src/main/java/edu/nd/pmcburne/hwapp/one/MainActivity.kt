package edu.nd.pmcburne.hwapp.one

import android.app.DatePickerDialog
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.BorderStroke
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import edu.nd.pmcburne.hwapp.one.ui.theme.RhythmicBlackB
import edu.nd.pmcburne.hwapp.one.ui.theme.RhythmicBlack
import edu.nd.pmcburne.hwapp.one.ui.theme.RhythmicGreyB
import edu.nd.pmcburne.hwapp.one.ui.theme.RhythmicRed3
import edu.nd.pmcburne.hwapp.one.ui.theme.RhythmicGreyC
import edu.nd.pmcburne.hwapp.one.ui.theme.RhythmicGrey
import edu.nd.pmcburne.hwapp.one.ui.theme.RhythmicRed
import edu.nd.pmcburne.hwapp.one.ui.theme.RhythmicGreen
import edu.nd.pmcburne.hwapp.one.ui.theme.RhythmicBlackC
import edu.nd.pmcburne.hwapp.one.ui.theme.RhythmicDrkGrey
import edu.nd.pmcburne.hwapp.one.ui.theme.RhythmicLiteGray
import edu.nd.pmcburne.hwapp.one.ui.theme.RhythmicWhite
import edu.nd.pmcburne.hwapp.one.ui.theme.RhythmicWhiteB
import edu.nd.pmcburne.hwapp.one.ui.theme.RhythmicStartingTheme
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            RhythmicStartingTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    InterfaceForBasketballScores()
                }
            }
        }
    }
}

private data class InterfaceState(
    val screenLoad: Boolean = false,
    val scoreUpdatedOn: String = "",
    val dateChosen: LocalDate = LocalDate.now(),
    val genderChosen: RhythmicSportsGender = RhythmicSportsGender.MEN,
    val basketballGames: List<BasketballMatchDetails> = emptyList(),
    val appError: String? = null,
    val appInfoMsg: String? = null
)

@Composable
private fun InterfaceForBasketballScores() {
    val gameContext = LocalContext.current
    val gameRepository = remember {
        RhythmicAppGameRepository(gameContext.applicationContext)
    }

    val gameDateFormat = remember {
        DateTimeFormatter.ofPattern("EEE, MMM d, yyyy", Locale.US)
    }

    var gameRefreshAction by remember { mutableIntStateOf(0) }
    var interfaceState by remember {
        mutableStateOf(
            InterfaceState(
                screenLoad = true,
                dateChosen = LocalDate.now(),
                genderChosen = RhythmicSportsGender.MEN
            )
        )
    }

    LaunchedEffect(
        interfaceState.dateChosen,
        interfaceState.genderChosen,
        gameRefreshAction
    ) {
        interfaceState = interfaceState.copy(
            screenLoad = true,
            appError = null,
            appInfoMsg = null
        )

        interfaceState = try {
            val result = gameRepository.fetchScores(
                gameGender = interfaceState.genderChosen,
                gameDate = interfaceState.dateChosen
            )

            interfaceState.copy(
                screenLoad = false,
                scoreUpdatedOn = result.lastUpdated,
                basketballGames = result.gameList,
                appError = null,
                appInfoMsg = result.statusMessage
            )
        } catch (e: Exception) {
            interfaceState.copy(
                screenLoad = false,
                scoreUpdatedOn = "",
                basketballGames = emptyList(),
                appError = e.message ?: "There is an unknown network or database error",
                appInfoMsg = null
            )
        }
    }

    val gameDatePickerAction = remember(interfaceState.dateChosen) {
        DatePickerDialog(
            gameContext,
            { _, year, month, dayOfMonth ->
                interfaceState = interfaceState.copy(
                    dateChosen = LocalDate.of(year, month + 1, dayOfMonth)
                )
            },
            interfaceState.dateChosen.year,
            interfaceState.dateChosen.monthValue - 1,
            interfaceState.dateChosen.dayOfMonth
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(RhythmicBlackB)
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .navigationBarsPadding(),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                RhythmicAppHeader()
            }

            item {
                RhythmicAppControl(
                    chosenDateMessage = interfaceState.dateChosen.format(gameDateFormat),
                    chosenGender = interfaceState.genderChosen,
                    chosenOnDate = { gameDatePickerAction.show() },
                    chosenOnMen = {
                        interfaceState = interfaceState.copy(genderChosen = RhythmicSportsGender.MEN)
                    },
                    chosenOnWomen = {
                        interfaceState = interfaceState.copy(genderChosen = RhythmicSportsGender.WOMEN)
                    },
                    chosenRefresh = { gameRefreshAction++ }
                )
            }

            item {
                BasketballGameScoreboardDetailsABC(
                    chosenGender = interfaceState.genderChosen,
                    totalGames = interfaceState.basketballGames.size,
                    appUpdatesAt = interfaceState.scoreUpdatedOn
                )
            }

            interfaceState.appInfoMsg?.let { message ->
                item {
                    GameDetailsCardABC(message = message)
                }
            }

            interfaceState.appError?.let { message ->
                item {
                    GameDetailsErrorCardABC(message = message)
                }
            }

            if (!interfaceState.screenLoad && interfaceState.appError == null && interfaceState.basketballGames.isEmpty()) {
                item {
                    GameDetailsEmptyCard()
                }
            }

            items(
                items = interfaceState.basketballGames,
                key = { it.matchID }
            ) { game ->
                BasketballGamesDetailsCardCC(game = game)
            }

            item {
                Spacer(modifier = Modifier.height(6.dp))
            }
        }

        if (interfaceState.screenLoad) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0x66000000)),
                contentAlignment = Alignment.Center
            ) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = RhythmicBlackC),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 24.dp, vertical = 20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        CircularProgressIndicator(
                            color = RhythmicRed,
                            trackColor = RhythmicGrey
                        )
                        Text(
                            text = "Loading scores...",
                            color = RhythmicWhite,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun RhythmicAppHeader() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(RhythmicBlack)
            .statusBarsPadding()
            .padding(horizontal = 18.dp, vertical = 18.dp)
    ) {
        Text(
            text = "RHYTHMIC SPORTS",
            style = MaterialTheme.typography.labelLarge,
            color = RhythmicRed,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = "College Basketball Scores",
            style = MaterialTheme.typography.headlineSmall,
            color = RhythmicWhite
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = "Live scores with offline saved-game support",
            style = MaterialTheme.typography.bodyMedium,
            color = RhythmicWhiteB
        )
    }
}

@Composable
private fun RhythmicAppControl(
    chosenDateMessage: String,
    chosenGender: RhythmicSportsGender,
    chosenOnDate: () -> Unit,
    chosenOnMen: () -> Unit,
    chosenOnWomen: () -> Unit,
    chosenRefresh: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = RhythmicBlackC),
        border = BorderStroke(1.dp, RhythmicGrey)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                text = "Controls",
                style = MaterialTheme.typography.titleMedium,
                color = RhythmicWhite
            )

            SelectGenderOption(
                chosenGender = chosenGender,
                chosenGenderMen = chosenOnMen,
                chosenGenderWomen = chosenOnWomen
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = chosenOnDate,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, RhythmicGrey),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = RhythmicWhite,
                        containerColor = RhythmicDrkGrey
                    )
                ) {
                    Text("Date: $chosenDateMessage")
                }

                Button(
                    onClick = chosenRefresh,
                    modifier = Modifier.weight(0.75f),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = RhythmicRed,
                        contentColor = RhythmicWhite
                    )
                ) {
                    Text("Refresh")
                }
            }
        }
    }
}

@Composable
private fun SelectGenderOption(
    chosenGender: RhythmicSportsGender,
    chosenGenderMen: () -> Unit,
    chosenGenderWomen: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(RhythmicDrkGrey)
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        ButtonGenderSection(
            chosenText = "Men",
            chosen = chosenGender == RhythmicSportsGender.MEN,
            chosenOnClick = chosenGenderMen,
            modifier = Modifier.weight(1f)
        )

        ButtonGenderSection(
            chosenText = "Women",
            chosen = chosenGender == RhythmicSportsGender.WOMEN,
            chosenOnClick = chosenGenderWomen,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun ButtonGenderSection(
    chosenText: String,
    chosen: Boolean,
    chosenOnClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val chosenColorContainer = if (chosen) RhythmicRed else Color.Transparent
    val chosenBorderColor = if (chosen) RhythmicRed else Color.Transparent

    Button(
        onClick = chosenOnClick,
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = chosenColorContainer,
            contentColor = RhythmicWhite
        ),
        border = BorderStroke(1.dp, chosenBorderColor),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
    ) {
        Text(text = chosenText)
    }
}

@Composable
private fun BasketballGameScoreboardDetailsABC(
    chosenGender: RhythmicSportsGender,
    totalGames: Int,
    appUpdatesAt: String
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = RhythmicBlackC),
        border = BorderStroke(1.dp, RhythmicGrey)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = if (chosenGender == RhythmicSportsGender.MEN) {
                    "MEN'S SCOREBOARD"
                } else {
                    "WOMEN'S SCOREBOARD"
                },
                style = MaterialTheme.typography.labelLarge,
                color = RhythmicRed
            )

            Text(
                text = "$totalGames Games",
                style = MaterialTheme.typography.titleMedium,
                color = RhythmicWhite
            )

            if (appUpdatesAt.isNotBlank()) {
                Text(
                    text = "Updated: $appUpdatesAt",
                    style = MaterialTheme.typography.bodyMedium,
                    color = RhythmicWhiteB
                )
            }
        }
    }
}

@Composable
private fun GameDetailsCardABC(message: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = RhythmicBlackC),
        border = BorderStroke(1.dp, RhythmicGrey)
    ) {
        Text(
            text = message,
            modifier = Modifier.padding(16.dp),
            style = MaterialTheme.typography.bodyMedium,
            color = RhythmicWhiteB
        )
    }
}

@Composable
private fun GameDetailsErrorCardABC(message: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = RhythmicBlackC),
        border = BorderStroke(1.dp, RhythmicRed)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Could not load scores",
                style = MaterialTheme.typography.titleMedium,
                color = RhythmicWhite
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = RhythmicWhiteB
            )
        }
    }
}

@Composable
private fun GameDetailsEmptyCard() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = RhythmicBlackC),
        border = BorderStroke(1.dp, RhythmicGrey)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "No games found",
                style = MaterialTheme.typography.titleMedium,
                color = RhythmicWhite
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Please select another date or switch between Men's and Women's games",
                style = MaterialTheme.typography.bodyMedium,
                color = RhythmicWhiteB
            )
        }
    }
}

@Composable
private fun BasketballGamesDetailsCardCC(game: BasketballMatchDetails) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = RhythmicBlackC),
        border = BorderStroke(1.dp, RhythmicGrey)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                BasketballGameStateAA(status = game.state)
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = "${game.homeName} vs ${game.visitorName}",
                    style = MaterialTheme.typography.labelMedium,
                    color = RhythmicWhiteB
                )
            }

            GameScoreRow(
                gameLabel = "H",
                gameTeamName = game.homeName,
                gameScore = game.homePoints,
                winnerOfGame = game.gameVictory == game.homeName
            )

            GameScoreRow(
                gameLabel = "A",
                gameTeamName = game.visitorName,
                gameScore = game.awayPoints,
                winnerOfGame = game.gameVictory == game.visitorName
            )

            HorizontalDivider(color = RhythmicGrey, thickness = 1.dp)

            GameDetailsSections(game = game)
        }
    }
}

@Composable
private fun BasketballGameStateAA(status: BasketballGameState) {
    val appBackgroundColour = when (status) {
        BasketballGameState.LIVE -> RhythmicRed3
        BasketballGameState.FINAL -> RhythmicGreyB
        BasketballGameState.UPCOMING -> RhythmicGreyC
    }

    val appMessageCC = when (status) {
        BasketballGameState.LIVE -> "LIVE"
        BasketballGameState.FINAL -> "FINAL"
        BasketballGameState.UPCOMING -> "UPCOMING"
    }

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(999.dp))
            .background(appBackgroundColour)
            .padding(horizontal = 10.dp, vertical = 5.dp)
    ) {
        Text(
            text = appMessageCC,
            color = RhythmicWhite,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun GameScoreRow(
    gameLabel: String,
    gameTeamName: String,
    gameScore: String?,
    winnerOfGame: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(28.dp)
                .clip(CircleShape)
                .background(if (gameLabel == "H") RhythmicRed else RhythmicDrkGrey)
                .border(1.dp, RhythmicGrey, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = gameLabel,
                color = RhythmicWhite,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.size(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = gameTeamName,
                style = MaterialTheme.typography.titleMedium,
                color = RhythmicWhite
            )
            Text(
                text = if (gameLabel == "H") "Home Team" else "Away Team",
                style = MaterialTheme.typography.labelSmall,
                color = if (winnerOfGame) RhythmicGreen else RhythmicLiteGray
            )
        }

        Text(
            text = gameScore ?: "-",
            style = MaterialTheme.typography.titleLarge,
            color = RhythmicWhite,
            fontWeight = if (winnerOfGame) FontWeight.Bold else FontWeight.SemiBold
        )
    }
}

@Composable
private fun GameDetailsSections(game: BasketballMatchDetails) {
    GameDetailLabels(label = "Status", value = gameTextState(game.state))

    when (game.state) {
        BasketballGameState.UPCOMING -> {
            GameDetailLabels(label = "Start Time", value = game.beginTime)
            GameDetailLabels(
                label = "Matchup",
                value = "${game.homeName} (H) vs ${game.visitorName} (A)"
            )
        }

        BasketballGameState.LIVE -> {
            GameDetailLabels(
                label = "Score",
                value = "${game.homeName} ${game.homePoints ?: "-"} - ${game.awayPoints ?: "-"} ${game.visitorName}"
            )
            GameDetailLabels(label = "Period", value = game.presentPeriod)
            GameDetailLabels(label = "Time Remaining", value = game.leftOverTime)
        }

        BasketballGameState.FINAL -> {
            GameDetailLabels(
                label = "Final Score",
                value = "${game.homeName} ${game.homePoints ?: "-"} - ${game.awayPoints ?: "-"} ${game.visitorName}"
            )
            GameDetailLabels(label = "Winner", value = game.gameVictory ?: "Unknown")
        }
    }
}

@Composable
private fun GameDetailLabels(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(
            text = "$label:",
            modifier = Modifier.weight(0.34f),
            style = MaterialTheme.typography.bodyMedium,
            color = RhythmicWhiteB,
            fontWeight = FontWeight.SemiBold
        )

        Text(
            text = value,
            modifier = Modifier.weight(0.66f),
            style = MaterialTheme.typography.bodyMedium,
            color = RhythmicWhite
        )
    }
}

private fun gameTextState(status: BasketballGameState): String {
    return when (status) {
        BasketballGameState.UPCOMING -> "Upcoming"
        BasketballGameState.LIVE -> "Live"
        BasketballGameState.FINAL -> "Final"
    }
}