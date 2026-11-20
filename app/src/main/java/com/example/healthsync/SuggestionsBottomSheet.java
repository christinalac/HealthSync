//******************************************
// Program Name: SuggestionsBottomSheet.java
// Developer: Anthony Ramirez and Christina LaCombe
// Date Created: 11/15/2026
// Version: 1.1
// Purpose: The pop-up that lists food or workout suggestions from
// SuggestionEngine. Opened from the "Suggestions" button in the Nutrition
// and Workout headers, and from "See all" on the Progress page.
// The Add button only shows on the tab that can add the item:
// food on Nutrition (added to today's meals), workouts on Workout (added to today's plan).
//******************************************
package com.example.healthsync;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.ViewCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.util.List;
import java.util.Locale;

public class SuggestionsBottomSheet extends BottomSheetDialogFragment {

    // ── Which list to show ────────────────────────────────────
    public static final String TYPE_FOOD = "food";
    public static final String TYPE_WORKOUT = "workout";
    private static final String ARG_TYPE = "type";
    private static final int MAX_SUGGESTIONS = 8;

    /** Use this to open the pop-up, e.g. SuggestionsBottomSheet.newInstance(TYPE_FOOD) */
    public static SuggestionsBottomSheet newInstance(String type) {
        SuggestionsBottomSheet sheet = new SuggestionsBottomSheet();
        Bundle args = new Bundle();
        args.putString(ARG_TYPE, type);
        sheet.setArguments(args);
        return sheet;
    }
    // end of newInstance

    @NonNull
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.bottom_sheet_suggestions, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        boolean isFood = TYPE_FOOD.equals(requireArguments().getString(ARG_TYPE));
        List<SuggestionEngine.Suggestion> items = null;
        String titleText = "";
        String basisText = "";
        boolean canAdd = false;
        OnAdd addHandler = null;

        // ── Load the user's profile and pick the suggestions ──
        UserProfile profile = new DatabaseHelper(requireContext()).getLatestUserProfile();
        int dailyKcal = SuggestionEngine.dailyCalories(profile);

        if (isFood) {
            items = SuggestionEngine.foodSuggestions(profile, dailyKcal, MAX_SUGGESTIONS);
            titleText = "Food Suggestions";
            basisText = String.format(Locale.US, "Based on: %s · %,d kcal / day", profile.getGoal(), dailyKcal);
        } else {
            items = SuggestionEngine.workoutSuggestions(requireContext(), profile, MAX_SUGGESTIONS);
            titleText = "Workout Suggestions";
            basisText = "Based on: " + profile.getGoal() + " · " + profile.getActivityLevel();
        }

        TextView title = view.findViewById(R.id.tvSuggestionsTitle);
        title.setText(titleText);
        ViewCompat.setAccessibilityHeading(title, true);

        TextView basis = view.findViewById(R.id.tvSuggestionsBasis);
        basis.setText(basisText);

        // ── Can this tab add the item? ──
        // Food can be added from the Nutrition tab, workouts from the Workout tab.
        // Anywhere else (the Progress page) the list is just for viewing
        if (isFood) {
            canAdd = requireActivity() instanceof NutritionActivity;
        } else {
            canAdd = requireActivity() instanceof ExerciseSearchBottomSheet.Listener;
        }
        if (canAdd) {
            addHandler = this::add;
        } else {
            addHandler = null;
        }

        RecyclerView list = view.findViewById(R.id.listSuggestions);
        list.setLayoutManager(new LinearLayoutManager(requireContext()));
        list.setAdapter(new SuggestionAdapter(items, addHandler));
    }
    // end of onViewCreated

    // Adds the chosen suggestion to today's meals or today's plan, then closes
    private void add(SuggestionEngine.Suggestion suggestion) {
        if (suggestion.food != null) {
            // Same NutritionViewModel the Nutrition tab uses, so the screen updates right away
            new ViewModelProvider(requireActivity()).get(NutritionViewModel.class).addMeal(suggestion.food);
            Toast.makeText(requireContext(), "Added " + suggestion.title, Toast.LENGTH_SHORT).show();
        } else if (suggestion.workout != null && requireActivity() instanceof ExerciseSearchBottomSheet.Listener) {
            // WorkoutActivity shows its own "Added ..." message (or "already added")
            ((ExerciseSearchBottomSheet.Listener) requireActivity()).onExercisePicked(suggestion.workout);
        }
        // end of food/workout if/else
        dismiss();
    }
    // end of add

    // Open fully so the whole list is visible without dragging the sheet up
    @Override
    public void onStart() {
        super.onStart();
        if (getDialog() instanceof BottomSheetDialog) {
            ((BottomSheetDialog) getDialog()).getBehavior().setState(BottomSheetBehavior.STATE_EXPANDED);
        }
    }
    // end of onStart

    // ─────────────────────────────────────────────────────────
    // LIST ADAPTER (one row per suggestion, from item_suggestion.xml)
    // ─────────────────────────────────────────────────────────

    interface OnAdd {
        void add(SuggestionEngine.Suggestion s);
    }

    static class SuggestionAdapter extends RecyclerView.Adapter<SuggestionAdapter.VH> {
        private final List<SuggestionEngine.Suggestion> items;
        // null = view only, so the Add button is hidden
        @Nullable private final OnAdd onAdd;

        SuggestionAdapter(List<SuggestionEngine.Suggestion> items, @Nullable OnAdd onAdd) {
            this.items = items;
            this.onAdd = onAdd;
        }

        static class VH extends RecyclerView.ViewHolder {
            final TextView name, detail, reason;
            final Button add;

            VH(View itemView) {
                super(itemView);
                name = itemView.findViewById(R.id.tvSuggestionName);
                detail = itemView.findViewById(R.id.tvSuggestionDetail);
                reason = itemView.findViewById(R.id.tvSuggestionReason);
                add = itemView.findViewById(R.id.btnAddSuggestion);
            }
        }
        // end of class VH

        @NonNull
        @Override
        public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new VH(LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_suggestion, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull VH holder, int position) {
            SuggestionEngine.Suggestion suggestion = items.get(position);
            holder.name.setText(suggestion.title);
            holder.detail.setText(suggestion.detail);
            holder.reason.setText(suggestion.reason);

            if (onAdd == null) {
                holder.add.setVisibility(View.GONE);
            } else {
                holder.add.setVisibility(View.VISIBLE);
                holder.add.setContentDescription("Add " + suggestion.title);
                holder.add.setOnClickListener(v -> onAdd.add(suggestion));
            }
            // end of add button if/else
        }

        @Override
        public int getItemCount() {
            return items.size();
        }
    }
    // end of class SuggestionAdapter

}
// end of class SuggestionsBottomSheet
