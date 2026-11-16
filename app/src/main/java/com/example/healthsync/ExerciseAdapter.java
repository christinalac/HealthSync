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

// Shows the exercise list. Tapping a name (or the circled "i") shows its info; checking the
// box marks it done and saves it; long-pressing a row removes it after asking.
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
        final View infoButton; // the circled "i"

        ExerciseViewHolder(@NonNull View itemView) {
            super(itemView);
            infoButton = itemView.findViewById(R.id.btnExerciseInfo);
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

        // Tapping the name OR the circled "i" shows this exercise in the Exercise Info card
        View.OnClickListener showInfo = v -> {
            int pos = holder.getBindingAdapterPosition();
            if (pos != RecyclerView.NO_POSITION) {
                onItemClick.onExerciseClick(exerciseList.get(pos));
            }
        };
        holder.tvName.setOnClickListener(showInfo);
        holder.infoButton.setOnClickListener(showInfo);
        // Spoken label for screen readers, since the icon has no text
        holder.infoButton.setContentDescription("Show info and tips for " + exercise.getName());

        // Clear the old listener before setting state so recycled rows don't fire it
        holder.checkBox.setOnCheckedChangeListener(null);
        holder.checkBox.setChecked(exercise.isChecked());
        applyCompletedStyle(holder.tvName, exercise.isChecked());

        // Spoken name for screen readers (the checkbox has no visible text)
        holder.checkBox.setContentDescription(holder.itemView.getContext()
                .getString(R.string.workout_mark_complete, exercise.getName()));

        // Checking an exercise marks it as done: it stays in the list (crossed out and faded)
        // and is saved, so it is still checked after switching tabs. Unchecking undoes it
        holder.checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            int pos = holder.getBindingAdapterPosition();
            if (pos == RecyclerView.NO_POSITION) return;

            exerciseList.get(pos).setChecked(isChecked);
            applyCompletedStyle(holder.tvName, isChecked);
            holder.itemView.setAlpha(isChecked ? 0.6f : 1f);

            if (holder.itemView.getContext() instanceof WorkoutActivity) {
                ((WorkoutActivity) holder.itemView.getContext()).saveExercises();
            }
        });
        holder.itemView.setAlpha(exercise.isChecked() ? 0.6f : 1f);

        // Long-press a row to remove the exercise from the list (asks first)
        holder.itemView.setOnLongClickListener(v -> {
            int pos = holder.getBindingAdapterPosition();
            if (pos == RecyclerView.NO_POSITION) return false;

            new androidx.appcompat.app.AlertDialog.Builder(v.getContext(),
                    R.style.ThemeOverlay_HealthSync_SleepDialog)
                    .setTitle("Remove " + exerciseList.get(pos).getName() + "?")
                    .setPositiveButton("Remove", (dialog, which) -> removeAt(holder))
                    .setNegativeButton("Cancel", null)
                    .show();
            return true;
        });
    }

    // Removes the exercise, saves the list, and resets the info card if the list is now empty
    private void removeAt(ExerciseViewHolder holder) {
        int pos = holder.getBindingAdapterPosition();
        if (pos == RecyclerView.NO_POSITION) return;

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
