package com.example.minesweeper

import android.content.Context

//custom button for the grid
class GameCell(
    var value: Int = 0,
    var isRevealed: Boolean = false,
    var isMarked: Boolean = false,
    context: Context
) : androidx.appcompat.widget.AppCompatButton(context)