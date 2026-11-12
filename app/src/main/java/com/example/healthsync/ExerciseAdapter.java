package com.example.healthsync;

import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

// Shows the exercise list. Tapping a name shows its info; checking the box removes it
// from the list and saves the updated list.
public class ExerciseAdapter extends RecyclerView.Adapter<ExerciseAdapter.ExerciseViewHolder> {

    public interface OnExerciseClickListener {
        void onExerciseClick(Exercise exercise);
    }

    private final List<Exercise> exerciseList;
    private final OnExerciseClickListener onItemClick;

    public ExerciseAdapter(List<Exercise> exerciseList, OnExerciseClickListener onItemClick) {
        this.exerciseList = exerciseList;
        this.onItemClick = onItemClick;
    }

    static class ExerciseViewHolder extends RecyclerView.ViewHolder {
        final TextView tvName;
        final TextView tvCategory;
        final CheckBox checkBox;

        ExerciseViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvExerciseName);
            tvCategory = itemView.findViewById(R.id.tvExerciseDetails);
            checkBox = itemView.findViewById(R.id.cbExerciseStatus);
        }
    }

    @NonNull
    @Override
    public ExerciseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_exercise, parent, false);
        return new ExerciseViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ExerciseViewHolder holder, int position) {
        Exercise exercise = exerciseList.get(position);

        holder.tvName.setText(exercise.getName());
        holder.tvCategory.setText(exercise.getCategory() + " | " + exercise.getSetsReps());

        holder.tvName.setOnClickListener(v -> {
            int pos = holder.getBindingAdapterPosition();
            if (pos != RecyclerView.NO_POSITION) {
                onItemClick.onExerciseClick(exerciseList.get(pos));
            }
        });

        // Clear the old listener before setting state so recycled rows don't fire it
        holder.checkBox.setOnCheckedChangeListener(null);
        holder.checkBox.setChecked(exercise.isChecked());
        applyCompletedStyle(holder.tvName, exercise.isChecked());

        // Spoken name for screen readers (the checkbox has no visible text)
        holder.checkBox.setContentDescription(holder.itemView.getContext()
                .getString(R.string.workout_mark_complete, exercise.getName()));

        // Checking an exercise removes it, saves the list, and resets the info card if empty
        holder.checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            int pos = holder.getBindingAdapterPosition();
            if (isChecked && pos != RecyclerView.NO_POSITION) {
                exerciseList.remove(pos);
                notifyItemRemoved(pos);

                if (holder.itemView.getContext() instanceof WorkoutActivity) {
                    WorkoutActivity activity = (WorkoutActivity) holder.itemView.getContext();
                    activity.saveExercises();
                    if (exerciseList.isEmpty()) {
                        activity.resetExerciseInfoCard();
                    }
                }
            }
        });
    }

    private static void applyCompletedStyle(TextView name, boolean done) {
        if (done) {
            name.setPaintFlags(name.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        } else {
            name.setPaintFlags(name.getPaintFlags() & ~Paint.STRIKE_THRU_TEXT_FLAG);
        }
    }

    @Override
    public int getItemCount() {
        return exerciseList.size();
    }
}
