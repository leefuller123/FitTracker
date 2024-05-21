package com.example.fittracker

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView

class WorkoutPlansAdapter(
    private val editClickListener: (String) -> Unit,
    private val viewClickListener: (String) -> Unit,
    private val deleteClickListener: (WorkoutPlan) -> Unit
) : ListAdapter<WorkoutPlan, WorkoutPlansAdapter.WorkoutPlanViewHolder>(WorkoutPlanDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WorkoutPlanViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_workout_plan, parent, false)
        return WorkoutPlanViewHolder(view)
    }

    override fun onBindViewHolder(holder: WorkoutPlanViewHolder, position: Int) {
        val workoutPlan = getItem(position)
        holder.bind(workoutPlan, editClickListener, viewClickListener, deleteClickListener)
    }

    class WorkoutPlanViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val planNameTextView: TextView = itemView.findViewById(R.id.planNameTextView)
        private val editPlanButton: Button = itemView.findViewById(R.id.editPlanButton)
        private val viewPlanButton: Button = itemView.findViewById(R.id.viewPlanButton)
        private val deletePlanButton: Button = itemView.findViewById(R.id.deletePlanButton)

        fun bind(
            workoutPlan: WorkoutPlan,
            editClickListener: (String) -> Unit,
            viewClickListener: (String) -> Unit,
            deleteClickListener: (WorkoutPlan) -> Unit
        ) {
            planNameTextView.text = workoutPlan.name
            editPlanButton.setOnClickListener {
                editClickListener(workoutPlan.id)
            }
            viewPlanButton.setOnClickListener {
                viewClickListener(workoutPlan.id)
            }
            deletePlanButton.setOnClickListener {
                deleteClickListener(workoutPlan)
            }
        }
    }
}

class WorkoutPlanDiffCallback : DiffUtil.ItemCallback<WorkoutPlan>() {
    override fun areItemsTheSame(oldItem: WorkoutPlan, newItem: WorkoutPlan): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: WorkoutPlan, newItem: WorkoutPlan): Boolean {
        return oldItem == newItem
    }
}
