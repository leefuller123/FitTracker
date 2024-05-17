package com.example.fittracker

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class WorkoutPlansActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var workoutPlansAdapter: WorkoutPlansAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_workout_plans)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        if (auth.currentUser == null) {
            finish() // Close the activity if the user is not authenticated
        }

        val workoutPlansRecyclerView = findViewById<RecyclerView>(R.id.workoutPlansRecyclerView)
        workoutPlansRecyclerView.layoutManager = LinearLayoutManager(this)

        workoutPlansAdapter = WorkoutPlansAdapter(
            editClickListener = { workoutPlanId ->
                val intent = Intent(this, WorkoutActivity::class.java)
                intent.putExtra("WORKOUT_PLAN_ID", workoutPlanId)
                startActivityForResult(intent, EDIT_WORKOUT_PLAN_REQUEST)
            },
            viewClickListener = { workoutPlanId ->
                val intent = Intent(this, ViewWorkoutPlanActivity::class.java)
                intent.putExtra("WORKOUT_PLAN_ID", workoutPlanId)
                startActivity(intent)
            },
            deleteClickListener = { workoutPlan ->
                deleteWorkoutPlan(workoutPlan)
            }
        )

        workoutPlansRecyclerView.adapter = workoutPlansAdapter

        fetchWorkoutPlans()

        val addWorkoutPlanButton = findViewById<FloatingActionButton>(R.id.addWorkoutPlanButton)
        addWorkoutPlanButton.setOnClickListener {
            val intent = Intent(this, WorkoutActivity::class.java)
            startActivityForResult(intent, ADD_WORKOUT_PLAN_REQUEST)
        }
    }

    private fun fetchWorkoutPlans() {
        db.collection("workoutPlans")
            .get()
            .addOnSuccessListener { result ->
                val workoutPlans = result.map { document ->
                    WorkoutPlan(document.id, document.getString("planName") ?: "", document["exercises"] as List<Map<String, Any>>)
                }
                Log.d("WorkoutPlansActivity", "Fetched workout plans: $workoutPlans")
                workoutPlansAdapter.submitList(workoutPlans)
            }
            .addOnFailureListener { exception ->
                Log.e("WorkoutPlansActivity", "Error fetching workout plans", exception)
            }
    }

    private fun deleteWorkoutPlan(workoutPlan: WorkoutPlan) {
        db.collection("workoutPlans").document(workoutPlan.id)
            .delete()
            .addOnSuccessListener {
                val updatedList = workoutPlansAdapter.currentList.toMutableList()
                updatedList.remove(workoutPlan)
                workoutPlansAdapter.submitList(updatedList)
                Log.d("WorkoutPlansActivity", "Deleted workout plan: ${workoutPlan.id}")
            }
            .addOnFailureListener { e ->
                Log.e("WorkoutPlansActivity", "Error deleting workout plan", e)
            }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == ADD_WORKOUT_PLAN_REQUEST || requestCode == EDIT_WORKOUT_PLAN_REQUEST) {
            fetchWorkoutPlans() // Refresh the list when returning from the WorkoutActivity
        }
    }

    companion object {
        const val ADD_WORKOUT_PLAN_REQUEST = 1
        const val EDIT_WORKOUT_PLAN_REQUEST = 2
    }
}

data class WorkoutPlan(val id: String, val name: String, val exercises: List<Map<String, Any>>)
