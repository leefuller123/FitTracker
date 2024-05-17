package com.example.fittracker

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class HydrationActivity : AppCompatActivity() {

    private var totalWaterIntake = 0.0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_hydration)

        val waterInput = findViewById<EditText>(R.id.waterInput)
        val addWaterButton = findViewById<Button>(R.id.addWaterButton)
        val totalWaterText = findViewById<TextView>(R.id.totalWaterText)

        addWaterButton.setOnClickListener {
            val waterStr = waterInput.text.toString()

            if (waterStr.isNotEmpty()) {
                val water = waterStr.toDouble()
                totalWaterIntake += water
                totalWaterText.text = "Total water consumed: %.1f ml".format(totalWaterIntake)
                waterInput.text.clear() // Clear the input field after adding
            } else {
                totalWaterText.text = "Please enter the amount of water"
            }
        }
    }
}
