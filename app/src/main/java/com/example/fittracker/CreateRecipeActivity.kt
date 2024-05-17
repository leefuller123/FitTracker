package com.example.fittracker

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference

class CreateRecipeActivity : AppCompatActivity() {

    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private lateinit var storage: FirebaseStorage
    private lateinit var storageRef: StorageReference
    private var recipeImageUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_recipe)

        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()
        storage = FirebaseStorage.getInstance()
        storageRef = storage.reference

        if (auth.currentUser == null) {
            finish() // Close the activity if the user is not authenticated
        }

        val recipeNameInput = findViewById<EditText>(R.id.recipeNameInput)
        val ingredientNameInput = findViewById<EditText>(R.id.ingredientNameInput)
        val addIngredientButton = findViewById<Button>(R.id.addIngredientButton)
        val ingredientsContainer = findViewById<LinearLayout>(R.id.ingredientsContainer)
        val cookingInstructionsInput = findViewById<EditText>(R.id.cookingInstructionsInput)
        val uploadImageButton = findViewById<Button>(R.id.uploadImageButton)
        val recipeImageView = findViewById<ImageView>(R.id.recipeImageView)
        val saveRecipeButton = findViewById<Button>(R.id.saveRecipeButton)

        val ingredients = mutableListOf<String>()

        addIngredientButton.setOnClickListener {
            val ingredientName = ingredientNameInput.text.toString()
            if (ingredientName.isNotEmpty()) {
                ingredients.add(ingredientName)
                val ingredientTextView = TextView(this)
                ingredientTextView.text = ingredientName
                ingredientsContainer.addView(ingredientTextView)
                ingredientNameInput.text.clear()
            }
        }

        uploadImageButton.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent, PICK_IMAGE_REQUEST)
        }

        saveRecipeButton.setOnClickListener {
            val recipeName = recipeNameInput.text.toString()
            val cookingInstructions = cookingInstructionsInput.text.toString()

            if (recipeName.isNotEmpty() && ingredients.isNotEmpty() && cookingInstructions.isNotEmpty()) {
                val recipe = hashMapOf(
                    "name" to recipeName,
                    "ingredients" to ingredients,
                    "instructions" to cookingInstructions
                )

                if (recipeImageUri != null) {
                    val imageRef = storageRef.child("recipes/${auth.currentUser?.uid}/${recipeImageUri?.lastPathSegment}")
                    imageRef.putFile(recipeImageUri!!)
                        .addOnSuccessListener {
                            imageRef.downloadUrl.addOnSuccessListener { uri ->
                                recipe["imageUrl"] = uri.toString()
                                saveRecipeToFirestore(recipe)
                            }
                        }
                        .addOnFailureListener { e ->
                            Toast.makeText(this, "Failed to upload image: ${e.message}", Toast.LENGTH_LONG).show()
                        }
                } else {
                    recipe["imageUrl"] = ""
                    saveRecipeToFirestore(recipe)
                }
            } else {
                Toast.makeText(this, "Please fill out all fields and add at least one ingredient.", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun saveRecipeToFirestore(recipe: HashMap<String, Any>) {
        db.collection("recipes")
            .add(recipe)
            .addOnSuccessListener {
                setResult(Activity.RESULT_OK)
                finish() // Navigate back to MealPlannerActivity
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Failed to save recipe: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null && data.data != null) {
            recipeImageUri = data.data
            findViewById<ImageView>(R.id.recipeImageView).setImageURI(recipeImageUri)
        }
    }

    companion object {
        private const val PICK_IMAGE_REQUEST = 1
    }
}
