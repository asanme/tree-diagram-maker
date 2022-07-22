package com.asanme.treediagrammaker

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.appcompat.app.AlertDialog
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout

class TestingActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_testing)

        val btn = findViewById<Button>(R.id.damn)
        val btn2 = findViewById<Button>(R.id.damn2)
        val btn3 = findViewById<Button>(R.id.damn3)

        btn.setOnClickListener{
            val view = View.inflate(this@TestingActivity, R.layout.custom_dialog_edit, null)
            val build = AlertDialog.Builder(this@TestingActivity)
            build.setView(view)

            val dialog = build.create()
            val editTextError =  view.findViewById<TextInputLayout>(R.id.editNodeTextInput)
            val editText =  view.findViewById<TextInputEditText>(R.id.editNodeEditText)

            dialog.show()
            dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
            dialog.setCancelable(false)

            view.findViewById<Button>(R.id.cancelEditBtn).setOnClickListener{
                dialog.dismiss()
            }

            view.findViewById<Button>(R.id.editNodeBtn).setOnClickListener {
                if(editText.text.toString() == ""){
                    editTextError.error = "El nom no pot estar en blanc"
                } else {
                    dialog.dismiss()
                }
            }
        }

        btn2.setOnClickListener{
            val view = View.inflate(this@TestingActivity, R.layout.custom_dialog_delete, null)
            val build = AlertDialog.Builder(this@TestingActivity)
            build.setView(view)

            val dialog = build.create()

            dialog.show()
            dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
            dialog.setCancelable(false)

            view.findViewById<Button>(R.id.cancelDeleteBtn).setOnClickListener{
                dialog.dismiss()
            }
        }

        btn3.setOnClickListener{
            val view = View.inflate(this@TestingActivity, R.layout.custom_dialog_add, null)
            val build = AlertDialog.Builder(this@TestingActivity)
            build.setView(view)

            val dialog = build.create()
            val addNodeEditTextInput =  view.findViewById<TextInputLayout>(R.id.addNodeTextInput)
            val addNodeEditText =  view.findViewById<TextInputEditText>(R.id.addNodeEditText)

            dialog.show()
            dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
            dialog.setCancelable(false)

            view.findViewById<Button>(R.id.cancelEditBtn).setOnClickListener{
                dialog.dismiss()
            }

            view.findViewById<Button>(R.id.editNodeBtn).setOnClickListener {
                if(addNodeEditText.text.toString() == ""){
                    addNodeEditTextInput.error = "El nom no pot estar en blanc"
                } else {
                    dialog.dismiss()
                }
            }
        }
    }


}