package iset.dsi.gamesorting


import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import iset.dsi.gamesorting.ui.theme.GameSortingTheme
import java.util.*







class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
         GameSortingTheme {
             Surface(
                 modifier = Modifier.fillMaxSize(),
             ) { NumberSortingGame() }

                    }
                }
            }

        }


@Composable
fun NumberSortingGame() {
    val result = generateRandomNumbers()
    var numbers by remember { mutableStateOf(result) }
    var positions by remember { mutableStateOf(List(numbers.size) { it }) }
    var startTime by remember { mutableStateOf(0L) }
    var endTime by remember { mutableStateOf(0L) }
    var isGameFinished by remember { mutableStateOf(false) }
    var leaderboardVisible by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Display the numbers with draggable modifiers
        numbers.forEachIndexed { index, number ->
            Box(
                modifier = Modifier
                    .padding(8.dp)
                    .dragModifier(index, positions, numbers.size)

            ) {
                Text(
                    text = number.toString(),
                    style = MaterialTheme.typography.h4,
                    modifier = Modifier.background(MaterialTheme.colors.primary)
                )
            }
        }

        if (isGameFinished) {
            // Display the result and time taken
            val sortedNumbers = positions.map { numbers[it] }
            val isCorrect = checkAnswer(sortedNumbers)

            Text(
                text = "Game Finished! ${if (isCorrect) "Correct" else "Incorrect"}",
                style = MaterialTheme.typography.h5,
                modifier = Modifier.padding(16.dp)
            )

            Text(
                text = "Time taken: ${(endTime - startTime) / 1000} seconds",
                style = MaterialTheme.typography.body1,
                modifier = Modifier.padding(8.dp)
            )

            if (isCorrect) {
                // Save the score to the database
                saveScore("PlayerName", (endTime - startTime).toInt())

                // Get the top 10 scores from the database
                val topScores = getTopScores(10)
                Leaderboard(topScores)
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Button to play again
                IconButton(
                    onClick = {
                        numbers = result
                        positions = List(numbers.size) { it }
                        startTime = 0L
                        endTime = 0L
                        isGameFinished = false
                        leaderboardVisible = false
                    }
                ) {
                    Icon(imageVector = Icons.Default.Refresh, contentDescription = "Play Again")
                    Text(text = "Play Again")
                }

                // Button to show leaderboard
                IconButton(
                    onClick = {
                        leaderboardVisible = !leaderboardVisible
                    }
                ) {
                    Icon(imageVector = Icons.Default.ArrowUpward, contentDescription = "Leaderboard")
                    Text(text = "Leaderboard")
                }
            }
        }
    }
}
@Composable
fun Modifier.dragModifier(
    index: Int,
    positions: List<Int>,
    size: Int
): Modifier = pointerInput(Unit) {
    val density = LocalDensity.current.density
    val offset by remember { mutableStateOf(Offset(0f, 0f)) }

    detectTransformGestures { _, pan, _,_ ->
        offset = Offset(offset.x + pan.x / density, offset.y + pan.y / density)
    }

    awaitPointerEventScope {
        while (true) {
            val event = awaitPointerEvent()
            when (event.changes.firstOrNull()?.actual) {
                is PointerPosition ->
                    if (event.changes.any { it.id == 0 }) {
                        val newPosition = offset.toIntPosition()
                        if (newPosition in 0 until size && newPosition != positions[index]) {
                            positions = positions.toMutableList().apply {
                                val draggedIndex = positions.indexOf(newPosition)
                                set(index, newPosition)
                                set(draggedIndex, positions[index])
                            }
                        }
                    }
                else -> Unit
            }
        }
    }
}


@Composable
fun Leaderboard(scores: List<Score>) {
    if (scores.isNotEmpty()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .background(MaterialTheme.colors.surface)
                .padding(16.dp)
        ) {
            Text(
                text = "Leaderboard",
                style = MaterialTheme.typography.h5,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            scores.forEachIndexed { index, score ->
                Text(
                    text = "${index + 1}. ${score.playerName}: ${score.time} seconds",
                    style = MaterialTheme.typography.body1,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
            }
        }
    }
}
@Composable
fun checkAnswer(sortedNumbers: List<Int>): Boolean {

    val correctSequence = listOf(1, 2, 3, 4, 5)

    return sortedNumbers == correctSequence
}
@Composable
fun saveScore(playerName: String, time: Int) {
    val dbHelper = DatabaseHelper(LocalContext.current)
    val scoreId = dbHelper.saveScore(playerName, time)
}
@Composable
fun getTopScores(limit: Int): List<Score> {
    val dbHelper = DatabaseHelper(LocalContext.current)
    return dbHelper.getTopScores(limit)
}
@Composable
fun generateRandomNumbers(): List<Int> {
    val random = Random()
    return List(5) { random.nextInt(101) }
}
@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    GameSortingTheme{
        NumberSortingGame()
    }
}


