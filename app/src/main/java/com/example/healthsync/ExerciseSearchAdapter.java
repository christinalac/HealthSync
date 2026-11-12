//******************************************
// Program Name: ExerciseSearchAdapter.java
// Date Created: 11/9/2026
// Version: 1.0
// Purpose: One row per exercise search result: name, muscle group
// with sets/reps, and an Add button (mirrors FoodSearchAdapter).
//******************************************
package com.example.healthsync;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

public class ExerciseSearchAdapter
        extends ListAdapter<ExerciseCatalog.Entry, ExerciseSearchAdapter.VH> {

    public interface OnAdd {
        void onAdd(ExerciseCatalog.Entry entry);
    }

    private final OnAdd onAdd;

    public ExerciseSearchAdapter(OnAdd onAdd) {
        super(DIFF);
        this.onAdd = onAdd;
    }

    static class VH extends RecyclerView.ViewHolder {
        final TextView name;
        final TextView details;
        final View addButton;

        VH(View view) {
            super(view);
            name = view.findViewById(R.id.tvResultName);
            details = view.findViewById(R.id.tvResultDetails);
            addButton = view.findViewById(R.id.btnAddExercise);
        }
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new VH(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_exercise_result, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        final ExerciseCatalog.Entry entry = getItem(position);
        holder.name.setText(entry.name);
        holder.details.setText(entry.group + " · " + entry.setsReps);
        // Give each Add button a unique spoken name for screen readers
        holder.addButton.setContentDescription(
                holder.itemView.getContext().getString(R.string.workout_add) + " " + entry.name);
        holder.addButton.setOnClickListener(v -> onAdd.onAdd(entry));
    }

    private static final DiffUtil.ItemCallback<ExerciseCatalog.Entry> DIFF =
            new DiffUtil.ItemCallback<ExerciseCatalog.Entry>() {
                @Override
                public boolean areItemsTheSame(@NonNull ExerciseCatalog.Entry a,
                                               @NonNull ExerciseCatalog.Entry b) {
                    return a.name.equals(b.name);
                }

                @Override
                public boolean areContentsTheSame(@NonNull ExerciseCatalog.Entry a,
                                                  @NonNull ExerciseCatalog.Entry b) {
                    return a.name.equals(b.name) && a.group.equals(b.group)
                            && a.setsReps.equals(b.setsReps);
                }
            };
}
