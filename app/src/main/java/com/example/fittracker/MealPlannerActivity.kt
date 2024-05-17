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

class MealPlannerActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var recipesAdapter: RecipesAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_meal_planning)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        if (auth.currentUser == null) {
            finish() // Close the activity if the user is not authenticated
        }

        val recipesRecyclerView = findViewById<RecyclerView>(R.id.recipesRecyclerView)
        recipesRecyclerView.layoutManager = LinearLayoutManager(this)

        recipesAdapter = RecipesAdapter(
            editClickListener = { recipeId ->
                val intent = Intent(this, CreateRecipeActivity::class.java)
                intent.putExtra("RECIPE_ID", recipeId)
                startActivityForResult(intent, EDIT_RECIPE_REQUEST)
            },
            viewClickListener = { recipeId ->
                val intent = Intent(this, ViewRecipeActivity::class.java)
                intent.putExtra("RECIPE_ID", recipeId)
                startActivity(intent)
            },
            deleteClickListener = { recipe ->
                deleteRecipe(recipe)
            }
        )

        recipesRecyclerView.adapter = recipesAdapter

        fetchRecipes()

        val addRecipeButton = findViewById<FloatingActionButton>(R.id.addRecipeButton)
        addRecipeButton.setOnClickListener {
            val intent = Intent(this, CreateRecipeActivity::class.java)
            startActivityForResult(intent, ADD_RECIPE_REQUEST)
        }
    }

    private fun fetchRecipes() {
        db.collection("recipes")
            .get()
            .addOnSuccessListener { result ->
                val recipes = result.map { document ->
                    Recipe(document.id, document.getString("name") ?: "", document.getString("instructions") ?: "", document.get("ingredients") as List<String>, document.getString("imageUrl") ?: "")
                }
                Log.d("MealPlannerActivity", "Fetched recipes: $recipes")
                recipesAdapter.submitList(recipes)
            }
            .addOnFailureListener { exception ->
                Log.e("MealPlannerActivity", "Error fetching recipes", exception)
            }
    }

    private fun deleteRecipe(recipe: Recipe) {
        db.collection("recipes").document(recipe.id)
            .delete()
            .addOnSuccessListener {
                val updatedList = recipesAdapter.currentList.toMutableList()
                updatedList.remove(recipe)
                recipesAdapter.submitList(updatedList)
                Log.d("MealPlannerActivity", "Deleted recipe: ${recipe.id}")
            }
            .addOnFailureListener { e ->
                Log.e("MealPlannerActivity", "Error deleting recipe", e)
            }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == ADD_RECIPE_REQUEST || requestCode == EDIT_RECIPE_REQUEST) {
            fetchRecipes() // Refresh the list when returning from CreateRecipeActivity
        }
    }

    companion object {
        const val ADD_RECIPE_REQUEST = 1
        const val EDIT_RECIPE_REQUEST = 2
    }
}

data class Recipe(val id: String, val name: String, val instructions: String, val ingredients: List<String>, val imageUrl: String)
