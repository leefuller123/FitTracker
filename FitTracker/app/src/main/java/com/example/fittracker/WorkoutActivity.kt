package com.example.fittracker

import android.os.Bundle
import android.text.InputType
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class WorkoutActivity : AppCompatActivity() {

    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private var currentWorkoutPlanId: String? = null
    private var currentWorkoutPlanName: String? = null
    private val exercises = mutableListOf<Exercise>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_workout)

        // Initialize Firebase
        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        // Check if user is authenticated
        if (auth.currentUser == null) {
            finish() // Close the activity if the user is not authenticated
        }

        val workoutPlanNameInput = findViewById<EditText>(R.id.workoutPlanNameInput)
        val createWorkoutPlanButton = findViewById<Button>(R.id.createWorkoutPlanButton)
        val currentWorkoutPlanText = findViewById<TextView>(R.id.currentWorkoutPlanText)

        val exerciseNameInput = findViewById<EditText>(R.id.exerciseNameInput)
        val exerciseWeightInput = findViewById<EditText>(R.id.exerciseWeightInput)
        val addExerciseButton = findViewById<Button>(R.id.addExerciseButton)
        val exercisesListText = findViewById<TextView>(R.id.exercisesListText)
        val exercisesContainer = findViewById<LinearLayout>(R.id.exercisesContainer)
        val saveWorkoutPlanButton = findViewById<Button>(R.id.saveWorkoutPlanButton)

        currentWorkoutPlanId = intent.getStringExtra("WORKOUT_PLAN_ID")
        currentWorkoutPlanId?.let { planId ->
            loadWorkoutPlan(planId)
        }

        createWorkoutPlanButton.setOnClickListener {
            val workoutPlanName = workoutPlanNameInput.text.toString()
            if (workoutPlanName.isNotEmpty()) {
                currentWorkoutPlanName = workoutPlanName
                currentWorkoutPlanText.text = "Current Workout Plan: $workoutPlanName"
                workoutPlanNameInput.text.clear()
                exercises.clear()
                exercisesContainer.removeAllViews()
                exercisesListText.text = "Exercises:"
            }
        }

        addExerciseButton.setOnClickListener {
            val exerciseName = exerciseNameInput.text.toString()
            val exerciseWeightStr = exerciseWeightInput.text.toString()

            if (exerciseName.isNotEmpty() && exerciseWeightStr.isNotEmpty()) {
                val exerciseWeight = exerciseWeightStr.toDouble()
                val exercise = Exercise(exerciseName, exerciseWeight)
                exercises.add(exercise)

                val exerciseView = createExerciseView(exercise, exercisesContainer)
                exercisesContainer.addView(exerciseView)

                exerciseNameInput.text.clear()
                exerciseWeightInput.text.clear()
            }
        }

        saveWorkoutPlanButton.setOnClickListener {
            saveWorkoutPlanToFirestore()
        }
    }

    private fun loadWorkoutPlan(planId: String) {
        db.collection("workoutPlans").document(planId).get()
            .addOnSuccessListener { document ->
                currentWorkoutPlanName = document.getString("planName")
                currentWorkoutPlanName?.let { name ->
                    findViewById<TextView>(R.id.currentWorkoutPlanText).text = "Current Workout Plan: $name"
                }

                val exercisesData = document["exercises"] as List<Map<String, Any>>
                exercises.clear()
                exercisesData.forEach { exerciseData ->
                    val exercise = Exercise(
                        name = exerciseData["name"] as String,
                        weight = (exerciseData["weight"] as Double)
                    )
                    exercises.add(exercise)

                    val exerciseView = createExerciseView(exercise, findViewById(R.id.exercisesContainer))
                    findViewById<LinearLayout>(R.id.exercisesContainer).addView(exerciseView)
                }
            }
    }

    private fun createExerciseView(exercise: Exercise, container: LinearLayout): View {
        val exerciseLayout = LinearLayout(this)
        exerciseLayout.orientation = LinearLayout.HORIZONTAL
        exerciseLayout.setPadding(0, 10, 0, 10)

        val exerciseTextView = TextView(this)
        exerciseTextView.text = "${exercise.name} - ${exercise.weight} kg"
        exerciseTextView.layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)

        val editButton = Button(this)
        editButton.text = "Edit"
        editButton.setOnClickListener {
            showEditExerciseDialog(exercise, exerciseTextView)
        }

        val deleteButton = Button(this)
        deleteButton.text = "Delete"
        deleteButton.setOnClickListener {
            container.removeView(exerciseLayout)
            exercises.remove(exercise)
        }

        exerciseLayout.addView(exerciseTextView)
        exerciseLayout.addView(editButton)
        exerciseLayout.addView(deleteButton)

        return exerciseLayout
    }

    private fun showEditExerciseDialog(exercise: Exercise, textView: TextView) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_edit_exercise, null)
        val exerciseNameInput = dialogView.findViewById<EditText>(R.id.editExerciseNameInput)
        val exerciseWeightInput = dialogView.findViewById<EditText>(R.id.editExerciseWeightInput)

        exerciseNameInput.setText(exercise.name)
        exerciseWeightInput.setText(exercise.weight.toString())
        exerciseWeightInput.inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL

        val dialog = android.app.AlertDialog.Builder(this)
            .setTitle("Edit Exercise")
            .setView(dialogView)
            .setPositiveButton("Save") { _, _ ->
                val newName = exerciseNameInput.text.toString()
                val newWeight = exerciseWeightInput.text.toString().toDouble()

                exercise.name = newName
                exercise.weight = newWeight
                textView.text = "$newName - $newWeight kg"
            }
            .setNegativeButton("Cancel", null)
            .create()

        dialog.show()
    }

    private fun saveWorkoutPlanToFirestore() {
        currentWorkoutPlanName?.let { planName ->
            val exercisesMap = exercises.map { exercise ->
                hashMapOf("name" to exercise.name, "weight" to exercise.weight)
            }

            val workoutPlan = hashMapOf(
                "planName" to planName,
                "exercises" to exercisesMap
            )

            currentWorkoutPlanId?.let { planId ->
                db.collection("workoutPlans").document(planId)
                    .set(workoutPlan)
                    .addOnSuccessListener {
                        finish()
                    }
                    .addOnFailureListener { e ->

                    }
            } ?: run {
                db.collection("workoutPlans")
                    .add(workoutPlan)
                    .addOnSuccessListener { documentReference ->
                        currentWorkoutPlanId = documentReference.id
                        finish()
                    }
                    .addOnFailureListener { e ->

                    }
            }
        }
    }
}

data class Exercise(var name: String, var weight: Double)
