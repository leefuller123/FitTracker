package com.example.fittracker

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class RecipesAdapter(
    private val editClickListener: (String) -> Unit,
    private val viewClickListener: (String) -> Unit,
    private val deleteClickListener: (Recipe) -> Unit
) : ListAdapter<Recipe, RecipesAdapter.RecipeViewHolder>(RecipeDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecipeViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_recipe, parent, false)
        return RecipeViewHolder(view)
    }

    override fun onBindViewHolder(holder: RecipeViewHolder, position: Int) {
        val recipe = getItem(position)
        holder.bind(recipe, editClickListener, viewClickListener, deleteClickListener)
    }

    class RecipeViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val recipeNameTextView: TextView = itemView.findViewById(R.id.recipeNameTextView)
        private val recipeImageView: ImageView = itemView.findViewById(R.id.recipeImageView)
        private val editRecipeButton: Button = itemView.findViewById(R.id.editRecipeButton)
        private val viewRecipeButton: Button = itemView.findViewById(R.id.viewRecipeButton)
        private val deleteRecipeButton: Button = itemView.findViewById(R.id.deleteRecipeButton)

        fun bind(
            recipe: Recipe,
            editClickListener: (String) -> Unit,
            viewClickListener: (String) -> Unit,
            deleteClickListener: (Recipe) -> Unit
        ) {
            recipeNameTextView.text = recipe.name
            if (recipe.imageUrl.isNotEmpty()) {
                Glide.with(itemView.context).load(recipe.imageUrl).into(recipeImageView)
            } else {
                recipeImageView.setImageResource(R.drawable.placeholder_image)
            }

            editRecipeButton.setOnClickListener {
                editClickListener(recipe.id)
            }
            viewRecipeButton.setOnClickListener {
                viewClickListener(recipe.id)
            }
            deleteRecipeButton.setOnClickListener {
                deleteClickListener(recipe)
            }
        }
    }
}

class RecipeDiffCallback : DiffUtil.ItemCallback<Recipe>() {
    override fun areItemsTheSame(oldItem: Recipe, newItem: Recipe): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: Recipe, newItem: Recipe): Boolean {
        return oldItem == newItem
    }
}
