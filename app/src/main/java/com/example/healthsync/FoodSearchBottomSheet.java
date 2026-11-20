//******************************************
// Program Name: FoodSearchBottomSheet.java
// Developer: Maximiliano Salinas
// Date Created: 10/12/2026
// Version: 2.0
// Purpose: Pop-up opened by the + button. Provides the search bar and results list, and adds the chosen food to Today's Meals.
//******************************************

package com.example.healthsync;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

// The pop-up opened by the green "+" button: a search bar and the list of results.
// Tapping "Add" on a result adds it to today's meals and closes the sheet.
public class FoodSearchBottomSheet extends BottomSheetDialogFragment {

    private FoodSearchViewModel searchVm;
    private NutritionViewModel nutritionVm;
    private TextInputEditText etSearch;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.bottom_sheet_food_search, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        searchVm = new ViewModelProvider(this).get(FoodSearchViewModel.class);
        // Same instance the Activity uses, so adding here updates the main screen
        nutritionVm = new ViewModelProvider(requireActivity()).get(NutritionViewModel.class);

        TextInputLayout searchLayout = view.findViewById(R.id.searchLayout);
        etSearch = view.findViewById(R.id.etSearch);
        final ProgressBar progress = view.findViewById(R.id.progress);
        final TextView tvMessage = view.findViewById(R.id.tvMessage);
        final RecyclerView list = view.findViewById(R.id.resultsList);

        final FoodSearchAdapter adapter = new FoodSearchAdapter(food -> {
            nutritionVm.addMeal(food);
            Toast.makeText(requireContext(), "Added " + food.name, Toast.LENGTH_SHORT).show();
            dismiss();
        });
        list.setLayoutManager(new LinearLayoutManager(requireContext()));
        list.setAdapter(adapter);

        // Search only when the user submits (keyboard "search" key or the magnifier icon).
        // Open Food Facts limits searches to ~10/min, so no search-as-you-type.
        etSearch.setOnEditorActionListener((textView, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                doSearch();
                return true;
            }
            return false;
        });
        searchLayout.setStartIconOnClickListener(iconView -> doSearch());

        searchVm.getState().observe(getViewLifecycleOwner(), state -> {
            String message = null;
            int progressVisibility = View.GONE;
            int listVisibility = View.GONE;
            int messageVisibility = View.GONE;

            if (state.type == SearchUiState.Type.LOADING) {
                progressVisibility = View.VISIBLE;
            } else {
                progressVisibility = View.GONE;
            }
            if (state.type == SearchUiState.Type.RESULTS) {
                listVisibility = View.VISIBLE;
            } else {
                listVisibility = View.GONE;
            }
            progress.setVisibility(progressVisibility);
            list.setVisibility(listVisibility);

            switch (state.type) {
                case IDLE:
                    message = "Search for a food to see its calories and macros.";
                    break;
                case EMPTY:
                    message = "No foods found. Try different words.";
                    break;
                case ERROR:
                    message = state.message;
                    break;
                default:
                    break;
            }
            tvMessage.setText(message);
            if (message == null) {
                messageVisibility = View.GONE;
            } else {
                messageVisibility = View.VISIBLE;
            }
            tvMessage.setVisibility(messageVisibility);

            if (state.type == SearchUiState.Type.RESULTS) adapter.setItems(state.foods);
        });

        // Open with the keyboard up, ready to type
        if (getDialog() != null && getDialog().getWindow() != null) {
            getDialog().getWindow().setSoftInputMode(
                    WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE
                            | WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        }
        etSearch.requestFocus();
    }

    private void doSearch() {
        CharSequence text = etSearch.getText();
        String queryText = "";
        hideKeyboard(etSearch);
        if (text == null) {
            queryText = "";
        } else {
            queryText = text.toString();
        }
        searchVm.search(queryText);
    }

    // Open fully expanded and tall, so results have room
    @Override
    public void onStart() {
        super.onStart();
        View rootView = getView();
        if (rootView != null && rootView.getLayoutParams() != null) {
            rootView.getLayoutParams().height = (int) (getResources().getDisplayMetrics().heightPixels * 0.9);
            rootView.requestLayout();
        }
        if (getDialog() instanceof BottomSheetDialog) {
            BottomSheetBehavior<?> behavior = ((BottomSheetDialog) getDialog()).getBehavior();
            behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
            behavior.setSkipCollapsed(true);
        }
    }

    private void hideKeyboard(View target) {
        InputMethodManager inputMethodManager = requireContext().getSystemService(InputMethodManager.class);
        if (inputMethodManager != null) inputMethodManager.hideSoftInputFromWindow(target.getWindowToken(), 0);
    }
}
