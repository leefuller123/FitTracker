package com.example.fittracker

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore
import kotlin.math.pow

class BMIActivity : AppCompatActivity() {

    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bmi)

        // Initialize Firebase Firestore
        db = FirebaseFirestore.getInstance()

        val heightInput = findViewById<EditText>(R.id.heightInput)
        val weightInput = findViewById<EditText>(R.id.weightInput)
        val calculateBmiButton = findViewById<Button>(R.id.calculateBmiButton)
        val bmiResultText = findViewById<TextView>(R.id.bmiResultText)

        calculateBmiButton.setOnClickListener {
            val heightStr = heightInput.text.toString()
            val weightStr = weightInput.text.toString()

            if (heightStr.isNotEmpty() && weightStr.isNotEmpty()) {
                val height = heightStr.toDouble()
                val weight = weightStr.toDouble()

                if (height > 0) {
                    val bmi = weight / height.pow(2)
                    bmiResultText.text = "Your BMI is %.2f".format(bmi)
                    saveBmiToFirestore(height, weight, bmi)
                } else {
                    bmiResultText.text = "Height must be greater than 0"
                }
            } else {
                bmiResultText.text = "Please enter both height and weight"
            }
        }
    }

    private fun saveBmiToFirestore(height: Double, weight: Double, bmi: Double) {
        val bmiData = hashMapOf(
            "height" to height,
            "weight" to weight,
            "bmi" to bmi
        )

        db.collection("bmiRecords")
            .add(bmiData)
            .addOnSuccessListener { documentReference ->
                // Successfully saved
                // You can add additional actions here if needed
            }
            .addOnFailureListener { e ->
                // Handle the error
            }
    }
}
