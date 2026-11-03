package com.example.healthsync

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ExerciseAdapter(private val exerciseList: List<Exercise>) :
    RecyclerView.Adapter<ExerciseAdapter.ExerciseViewHolder>() {

    class ExerciseViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        // Replace these IDs with the actual IDs inside your item layout XML (e.g., bg_axcercie_item.xml)
        val tvName: TextView = itemView.findViewById(R.id.tvExerciseName)
        val tvCategory: TextView = itemView.findViewById(R.id.tvExerciseDetails)
        val checkbox: TextView = itemView.findViewById(R.id.cbExerciseStatus)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExerciseViewHolder {
        // Infates a simple default list layout. Swap 'android.R.layout.simple_list_item_2'
        // with your own XML layout (like R.layout.bg_axcercie_item) when ready.
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_exercise, parent, false)
        return ExerciseViewHolder(view)
    }

    override fun onBindViewHolder(holder: ExerciseViewHolder, position: Int) {
        val exercise = exerciseList[position]
        holder.tvName.text = exercise.name
        holder.checkbox
        holder.tvCategory.text = "${exercise.category} | ${exercise.setsReps}"
    }

    override fun getItemCount(): Int {
        return exerciseList.size
    }
}

