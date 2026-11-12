//******************************************
// Program Name: ExerciseSearchBottomSheet.java
// Date Created: 11/9/2026
// Version: 1.0
// Purpose: The pop-up opened by the "+" on the Workout tab. Works like
// the nutrition FoodSearchBottomSheet: a search bar and a list of results
// with an Add button. Also has All / Arms / Legs / Back / Chest / Shoulders / Core filter chips.
// The host Activity must implement Listener to receive the chosen exercise.
//******************************************
package com.example.healthsync;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.ViewCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.List;

public class ExerciseSearchBottomSheet extends BottomSheetDialogFragment {

    // Implemented by WorkoutActivity
    public interface Listener {
        void onExercisePicked(ExerciseCatalog.Entry entry);
    }

    private String currentFilter = ExerciseCatalog.FILTER_ALL;

    @NonNull
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.bottom_sheet_exercise_search, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ViewCompat.setAccessibilityHeading(view.findViewById(R.id.tvSheetTitle), true);

        TextInputLayout searchLayout = view.findViewById(R.id.searchLayout);
        final TextInputEditText etSearch = view.findViewById(R.id.etSearch);
        final ChipGroup chipGroup = view.findViewById(R.id.chipGroupFilter);
        final TextView tvMessage = view.findViewById(R.id.tvMessage);
        final RecyclerView list = view.findViewById(R.id.resultsList);

        final ExerciseSearchAdapter adapter = new ExerciseSearchAdapter(entry -> {
            if (requireActivity() instanceof Listener) {
                ((Listener) requireActivity()).onExercisePicked(entry);
            }
            dismiss();
        });
        list.setLayoutManager(new LinearLayoutManager(requireContext()));
        list.setAdapter(adapter);

        // Re-runs the search with the current text and filter
        final Runnable refresh = () -> {
            CharSequence text = etSearch.getText();
            List<ExerciseCatalog.Entry> results =
                    ExerciseCatalog.search(text != null ? text.toString() : "", currentFilter);
            adapter.submitList(results);
            boolean empty = results.isEmpty();
            list.setVisibility(empty ? View.GONE : View.VISIBLE);
            tvMessage.setText(empty ? getString(R.string.workout_no_results) : null);
            tvMessage.setVisibility(empty ? View.VISIBLE : View.GONE);
        };

        // The catalogue is local, so results can update as the user types
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int st, int c, int a) { }
            @Override public void onTextChanged(CharSequence s, int st, int b, int c) { }
            @Override public void afterTextChanged(Editable s) { refresh.run(); }
        });
        etSearch.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                hideKeyboard(etSearch);
                refresh.run();
                return true;
            }
            return false;
        });
        searchLayout.setStartIconOnClickListener(v -> {
            hideKeyboard(etSearch);
            refresh.run();
        });

        // Filter chips: All / Arms / Legs / Back / Chest / Sholders / Core
        chipGroup.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.isEmpty()) return;
            int id = checkedIds.get(0);
            if (id == R.id.chipArms) {
                currentFilter = ExerciseCatalog.FILTER_ARMS;
            } else if (id == R.id.chipLegs) {
                currentFilter = ExerciseCatalog.FILTER_LEGS;
            } else if(id == R.id.chipBack) {
                currentFilter = ExerciseCatalog.FILTER_BACK;
            } else if(id == R.id.chipChest) {
                currentFilter = ExerciseCatalog.FILTER_CHEST;
            } else if(id == R.id.chipShoulders) {
                currentFilter = ExerciseCatalog.FILTER_SHOULDERS;
            } else if(id == R.id.chipCore) {
                currentFilter = ExerciseCatalog.FILTER_CORE;
            } else {
                currentFilter = ExerciseCatalog.FILTER_ALL;
            }
            refresh.run();
        });

        // Show the full list straight away
        refresh.run();

        // Open with the keyboard up, ready to type
        if (getDialog() != null && getDialog().getWindow() != null) {
            getDialog().getWindow().setSoftInputMode(
                    WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE
                            | WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        }
        etSearch.requestFocus();
    }

    // Open fully expanded and tall, so results have room
    @Override
    public void onStart() {
        super.onStart();
        View v = getView();
        if (v != null && v.getLayoutParams() != null) {
            v.getLayoutParams().height = (int) (getResources().getDisplayMetrics().heightPixels * 0.9);
        }
        if (getDialog() instanceof BottomSheetDialog) {
            BottomSheetBehavior<?> behavior = ((BottomSheetDialog) getDialog()).getBehavior();
            behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
            behavior.setSkipCollapsed(true);
        }
    }

    private void hideKeyboard(View target) {
        InputMethodManager imm = requireContext().getSystemService(InputMethodManager.class);
        imm.hideSoftInputFromWindow(target.getWindowToken(), 0);
    }
}
