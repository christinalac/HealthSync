package com.example.healthsync
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ExerciseAdapter(
    private val exerciseList: MutableList<Exercise>,
    private val onItemClick: (Exercise) -> Unit // 1. Added click listener lambda parameter
) : RecyclerView.Adapter<ExerciseAdapter.ExerciseViewHolder>() {

    class ExerciseViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvName: TextView = itemView.findViewById(R.id.tvExerciseName)
        val tvCategory: TextView = itemView.findViewById(R.id.tvExerciseDetails)
        val checkBox: CheckBox = itemView.findViewById(R.id.cbExerciseStatus)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExerciseViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_exercise, parent, false)
        return ExerciseViewHolder(view)
    }

    override fun onBindViewHolder(holder: ExerciseViewHolder, position: Int) {
        val exercise = exerciseList[position]

        holder.tvName.text = exercise.name
        holder.tvCategory.text = "${exercise.category} | ${exercise.setsReps}"

        // 2. Triggers when the user taps on the exercise name text
        holder.tvName.setOnClickListener {
            val actualPosition = holder.adapterPosition
            if (actualPosition != RecyclerView.NO_POSITION) {
                onItemClick(exerciseList[actualPosition])
            }
        }

        // Reset listener to null first to avoid weird scrolling recycling bugs
        holder.checkBox.setOnCheckedChangeListener(null)
        holder.checkBox.isChecked = exercise.isChecked

        // Set up the delete-on-click action
        holder.checkBox.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                val actualPosition = holder.adapterPosition
                if (actualPosition != RecyclerView.NO_POSITION) {
                    // Remove from data source
                    exerciseList.removeAt(actualPosition)

                    // Trigger the sleek item vanishing animation
                    notifyItemRemoved(actualPosition)
                    notifyItemRangeChanged(actualPosition, exerciseList.size)
                }
            }
        }
    }

    override fun getItemCount(): Int = exerciseList.size
}

