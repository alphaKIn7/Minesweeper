package com.example.minesweeper


import android.content.Intent
import android.widget.Chronometer
import android.os.Bundle
import android.os.SystemClock
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
//import com.example.minesweeper.MainActivity.Companion.HIGH_SCORE
//import com.example.minesweeper.MainActivity.Companion.HIGH_SCORE_INTENT
//import com.example.minesweeper.MainActivity.Companion.LAST_GAME_SCORE_INTENT
import kotlinx.android.synthetic.main.game_board_grid.*

class GameSetup2 : AppCompatActivity() {
    private var getRowIntent = 0 //no. of rows from intent
    private var getColIntent = 0 //no. of columns from intent
    private var getMineIntent = 0 //no. of mines from intent
    private var mineCount = 0
    private var flag = 0
    private var lastGameTime = 0
    private var bestTime = 0
    private var elapsedTime: Long = 0
    private lateinit var minesCountLabel: TextView
    private lateinit var grid: Array<Array<GameCell>> // 2-D array of custom button : GameCell
    private lateinit var chronometer: Chronometer
    private var minesOptions: MutableSet<Int> =
        mutableSetOf() // list of buttons in which mines are present

    companion object {
        const val MINE = -1
        val movement = intArrayOf(-1, 0, 1)
        const val LAST_GAME_SCORE = "LAST_GAME_SCORE"
        const val STATUS_WON = "STATUS WON"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.game_board_grid)

        minesCountLabel = findViewById(R.id.mines_left)
        getRowIntent = intent.getIntExtra(MainActivity.INTENT_ROWS, 9)
        getColIntent = intent.getIntExtra(MainActivity.INTENT_COLS, 9)
        getMineIntent = intent.getIntExtra(MainActivity.INTENT_MINES, 10)
        mineCount = getMineIntent
        minesCountLabel.text = mineCount.toString()
        grid = Array(getRowIntent) { Array(getColIntent) { GameCell(context = this) } }

        //SETUP BOARD ACCORDING TO NO. OF ROWS AND COLUMNS
        setupGameBoard(getRowIntent, getColIntent)

        //update neighbours around the mines
        for (i in 0 until getRowIntent) {
            for (j in 0 until getColIntent) {
                if (grid[i][j].value == MINE) { ///MINE
                    updateNeighbours(i, j)
                }
            }
        }

        //move function decides what will happen if we click button of the grid
        for (i in 0 until getRowIntent) {
            for (j in 0 until getColIntent) {
                move(i, j)
            }
        }

        //restart button restarts the game and send time to the MainActivity only if the you won the
        // game
        val restartButton = findViewById<Button>(R.id.restart_button)
        restartButton.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java).apply {
                putExtra(LAST_GAME_SCORE, lastGameTime)
                if (status == Status.WON) {
                    putExtra(STATUS_WON, "STATUS WON")
                }
            }
            startActivity(intent)
        }
    }

    private var status = Status.ONGOING

    //setup the game board into grid
    private fun setupGameBoard(row: Int, col: Int) {
        val params1 = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 150)
        var counter = 1
        val params2 = LinearLayout.LayoutParams(150, 150)
        for (i in 0 until row) {
            val linearLayout = LinearLayout(this)
            linearLayout.orientation = LinearLayout.HORIZONTAL
            linearLayout.layoutParams = params1
            for (j in 0 until col) {
                val button = GameCell(context = this)
                button.id = counter
                button.layoutParams = params2
                button.setBackgroundResource(R.drawable.blank_tile)
                button.textSize = 30F
                grid[i][j] = button
                linearLayout.addView(grid[i][j])
                counter++
            }
            game_grid.addView(linearLayout)
        }
        randomMines()
    }

    // function to start a chronometer
    private fun startTimer() {
        chronometer = findViewById(R.id.chronometer)
        chronometer.base = SystemClock.elapsedRealtime()
        chronometer.start()
    }

    //this function gives random values between a provided range
    private fun rand(start: Int, end: Int): Int {
        require(start <= end) { "Illegal Argument" }
        return (start..end).random()
    }

    //this function puts mines in random location of the grid
    private fun randomMines() {
        while (minesOptions.size != getMineIntent) {
            minesOptions.add(rand(1, (getRowIntent * getColIntent)))
        }
        for (i in 0 until getRowIntent) {
            for (j in 0 until getColIntent) {
                if (minesOptions.contains(grid[i][j].id)) {
                    grid[i][j].value = MINE
                }
            }
        }
    }

    //this function shows images on the button after it is revealed or marked
    private fun showClickedTiles(row: Int, col: Int) {
        val drawableResource = when (grid[row][col].value) {
            0 -> R.drawable.number_0
            1 -> R.drawable.number_1
            2 -> R.drawable.number_2
            3 -> R.drawable.number_3
            4 -> R.drawable.number_4
            5 -> R.drawable.number_5
            6 -> R.drawable.number_6
            7 -> R.drawable.number_7
            else -> R.drawable.number_8
        }
        if (grid[row][col].value == MINE && grid[row][col].isRevealed) {
            grid[row][col].setBackgroundResource(R.drawable.mines3)
        } else if (grid[row][col].value != MINE && grid[row][col].isRevealed) {
            grid[row][col].setBackgroundResource(drawableResource)
        } else if (grid[row][col].isMarked) {
            grid[row][col].setBackgroundResource(R.drawable.flag)
        } else if (!grid[row][col].isMarked) {
            grid[row][col].setBackgroundResource(R.drawable.blank_tile)
        }
    }

    //decides the functionality of the whole game i.e, basically whole logic of the game. This
    // function reveals the button if it is clicked and marked the button if it is long Pressed

    private fun move(row: Int, col: Int) {
        if (grid[row][col].isRevealed) {
            grid[row][col].isClickable = false
        }
        grid[row][col].setOnClickListener {
            if (grid[row][col].isMarked && !grid[row][col].isRevealed) {
                mineCount++
                minesCountLabel.text = "$mineCount"
                grid[row][col].isRevealed = true
            }
            if (grid[row][col].value == MINE) {
                //display image of all mines
                status = Status.LOST
                grid[row][col].isRevealed = true
                showClickedTiles(row, col)
                displayBoard()
                disableButtons()
                chronometer.stop()
                elapsedTime = SystemClock.elapsedRealtime() - chronometer.base
                lastGameTime = elapsedTime.toInt()
                Toast.makeText(this, "Sorry! you lost. Try again", Toast.LENGTH_SHORT).show()
            } else {
                // show value of button
                grid[row][col].isRevealed = true
                showClickedTiles(row, col)
                reveal(row, col)
            }
            //to start a timer only on first button click
            if (flag == 0) {
                flag = 1
                startTimer()
            }
        }
        grid[row][col].setOnLongClickListener {
            if (!grid[row][col].isMarked && !grid[row][col].isRevealed) {
                grid[row][col].isMarked = true
                showClickedTiles(row, col)
                mineCount--
                minesCountLabel.text = "$mineCount"

            } else if (!grid[row][col].isRevealed) {
                grid[row][col].isMarked = false
                showClickedTiles(row, col)
                mineCount++
                minesCountLabel.text = "$mineCount"
            }
            if (isComplete() && status != Status.LOST) {
                status = Status.WON
                chronometer.stop()
                elapsedTime = SystemClock.elapsedRealtime() - chronometer.base
                disableButtons()
                lastGameTime = elapsedTime.toInt()
                Toast.makeText(this, "Congratulations! You won.", Toast.LENGTH_SHORT).show()
            }
            return@setOnLongClickListener true
        }
    }

    //this function update neighbours around a mine
    private fun updateNeighbours(row: Int, col: Int) {
        for (i in movement) {
            for (j in movement) {
                if ((row + i) >= 0 && (row + i) < getRowIntent && (col + j) >= 0 && (col + j) < getColIntent
                    && grid[row + i][col + j].value != MINE
                ) {
                    grid[row + i][col + j].value++
                }
            }
        }
    }

    //this function checks if the game is complete or not
    private fun isComplete(): Boolean {
        var markedMine = true
        grid.forEach { row ->
            row.forEach {
                if (it.value == MINE && !it.isMarked)
                    markedMine = false
            }
        }
        var areRevealed = true
        grid.forEach { row ->
            row.forEach {
                if (it.value != MINE && !it.isRevealed) {
                    areRevealed = false
                }
            }
        }
        return markedMine || areRevealed
    }

    // this function reveals the button if its value is 0 and also its neighbours whose value is 0
    // and not equal to MINE(-1)
    private fun reveal(row: Int, col: Int) {
        if (grid[row][col].value == 0) {
            for (i in movement) {
                for (j in movement) {
                    if ((i != 0 || j != 0) && (row + i) >= 0 && (row + i) < getRowIntent && (col + j) >= 0 && (col + j) < getColIntent
                        && grid[row + i][col + j].value != MINE
                    ) {
                        if (!grid[row + i][col + j].isRevealed && !grid[row + i][col + j].isMarked) {
                            grid[row + i][col + j].isRevealed = true
                            showClickedTiles(row + i, col + j)
                            reveal(row + i, col + j)
                        }
                    }
                }
            }
        }
    }

    //disables all the grid buttons when game is complete (lost or won)
    private fun disableButtons() {
        grid.forEach { row ->
            row.forEach {
                it.isEnabled = false
            }
        }
    }

    //reveals all buttons where mine is present if you won or lost
    private fun displayBoard() {
        grid.forEach { row ->
            row.forEach {
                if (status == Status.WON && it.value == MINE) {
                    it.setBackgroundResource(R.drawable.won_mine)
                } else if (status == Status.LOST && it.value == MINE) {
                    it.setBackgroundResource(R.drawable.mines3)
                }
            }
        }
    }
}
