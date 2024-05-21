package com.example.fittracker

import android.os.Bundle
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore

class ViewWorkoutPlanActivity : AppCompatActivity() {

    private lateinit var db: FirebaseFirestore
    private var workoutPlanId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_workout_plan)

        db = FirebaseFirestore.getInstance()
        workoutPlanId = intent.getStringExtra("WORKOUT_PLAN_ID")

        workoutPlanId?.let { planId ->
            loadWorkoutPlan(planId)
        }
    }

    private fun loadWorkoutPlan(planId: String) {
        db.collection("workoutPlans").document(planId).get()
            .addOnSuccessListener { document ->
                val planName = document.getString("planName")
                val exercisesData = document["exercises"] as List<Map<String, Any>>

                findViewById<TextView>(R.id.viewPlanNameTextView).text = planName

                val exercisesContainer = findViewById<LinearLayout>(R.id.viewExercisesContainer)
                exercisesData.forEach { exerciseData ->
                    val exerciseView = TextView(this)
                    val exerciseName = exerciseData["name"] as String
                    val exerciseWeight = exerciseData["weight"] as Double
                    exerciseView.text = "$exerciseName - $exerciseWeight kg"
                    exercisesContainer.addView(exerciseView)
                }
            }
    }
}
