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

    @NonNull
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
        final TextInputEditText etSearch = view.findViewById(R.id.etSearch);
        final ProgressBar progress = view.findViewById(R.id.progress);
        final TextView tvMessage = view.findViewById(R.id.tvMessage);
        final RecyclerView list = view.findViewById(R.id.resultsList);

        final FoodSearchAdapter adapter = new FoodSearchAdapter(food -> {
            nutritionVm.addMeal(food);
            Toast.makeText(requireContext(), "Added " + food.getName(), Toast.LENGTH_SHORT).show();
            dismiss();
        });
        list.setLayoutManager(new LinearLayoutManager(requireContext()));
        list.setAdapter(adapter);

        // Search only when the user submits (keyboard "search" key or the magnifier icon).
        // Open Food Facts limits searches to ~10/min, so no search-as-you-type.
        final Runnable doSearch = () -> {
            hideKeyboard(etSearch);
            CharSequence text = etSearch.getText();
            searchVm.search(text != null ? text.toString() : "");
        };
        etSearch.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                doSearch.run();
                return true;
            }
            return false;
        });
        searchLayout.setStartIconOnClickListener(v -> doSearch.run());

        searchVm.getState().observe(getViewLifecycleOwner(), state -> {
            progress.setVisibility(state instanceof SearchUiState.Loading ? View.VISIBLE : View.GONE);
            list.setVisibility(state instanceof SearchUiState.Results ? View.VISIBLE : View.GONE);

            String message;
            if (state instanceof SearchUiState.Idle) {
                message = "Search for a food to see its calories and macros.";
            } else if (state instanceof SearchUiState.Empty) {
                message = "No foods found. Try different words.";
            } else if (state instanceof SearchUiState.Error) {
                message = ((SearchUiState.Error) state).getMessage();
            } else {
                message = null;
            }
            tvMessage.setText(message);
            tvMessage.setVisibility(message == null ? View.GONE : View.VISIBLE);

            if (state instanceof SearchUiState.Results) {
                adapter.submitList(((SearchUiState.Results) state).getFoods());
            }
        });

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
