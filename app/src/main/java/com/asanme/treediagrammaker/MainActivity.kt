package com.asanme.treediagrammaker

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View.SYSTEM_UI_FLAG_FULLSCREEN

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //val canvas = DrawingBoard(this)
        val canvas = DrawFigures(this)
        canvas.systemUiVisibility = SYSTEM_UI_FLAG_FULLSCREEN
        setContentView(canvas)
    }
}