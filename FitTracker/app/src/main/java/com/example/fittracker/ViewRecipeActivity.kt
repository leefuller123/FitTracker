package com.example.fittracker

import android.os.Bundle
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.firebase.firestore.FirebaseFirestore

class ViewRecipeActivity : AppCompatActivity() {

    private lateinit var db: FirebaseFirestore
    private var recipeId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_recipe)

        db = FirebaseFirestore.getInstance()
        recipeId = intent.getStringExtra("RECIPE_ID")

        recipeId?.let { id ->
            loadRecipe(id)
        }
    }

    private fun loadRecipe(id: String) {
        db.collection("recipes").document(id).get()
            .addOnSuccessListener { document ->
                val name = document.getString("name")
                val instructions = document.getString("instructions")
                val ingredients = document["ingredients"] as List<String>
                val imageUrl = document.getString("imageUrl")

                findViewById<TextView>(R.id.viewRecipeNameTextView).text = name
                findViewById<TextView>(R.id.viewCookingInstructionsTextView).text = instructions

                val ingredientsContainer = findViewById<LinearLayout>(R.id.viewIngredientsContainer)
                ingredients.forEach { ingredient ->
                    val ingredientTextView = TextView(this)
                    ingredientTextView.text = ingredient
                    ingredientsContainer.addView(ingredientTextView)
                }

                if (imageUrl != null && imageUrl.isNotEmpty()) {
                    Glide.with(this).load(imageUrl).into(findViewById(R.id.viewRecipeImageView))
                }
            }
            .addOnFailureListener { e ->
                // Handle the error
            }
    }
}
