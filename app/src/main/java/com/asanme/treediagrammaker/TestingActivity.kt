package com.asanme.treediagrammaker

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.appcompat.app.AlertDialog

class TestingActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_testing)

        val btn = findViewById<Button>(R.id.damn)
        btn.setOnClickListener{
            val view = View.inflate(this@TestingActivity, R.layout.custom_dialog, null)
            val build = AlertDialog.Builder(this@TestingActivity)
            build.setView(view)
            val dialog = build.create()
            dialog.show()
            dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
            dialog.setCancelable(false)

            view.findViewById<Button>(R.id.cancelEditBtn).setOnClickListener{
                dialog.dismiss()
            }

        }
    }
}