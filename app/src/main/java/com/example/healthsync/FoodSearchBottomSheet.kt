package com.example.healthsync

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout

// The pop-up opened by the green "+" button: a search bar and the list of results.
// Tapping "Add" on a result adds it to today's meals and closes the sheet.
class FoodSearchBottomSheet : BottomSheetDialogFragment() {

    private lateinit var searchVm: FoodSearchViewModel
    private lateinit var nutritionVm: NutritionViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View =
        inflater.inflate(R.layout.bottom_sheet_food_search, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        searchVm = ViewModelProvider(this)[FoodSearchViewModel::class.java]
        // Same instance the Activity uses, so adding here updates the main screen
        nutritionVm = ViewModelProvider(requireActivity())[NutritionViewModel::class.java]

        val searchLayout = view.findViewById<TextInputLayout>(R.id.searchLayout)
        val etSearch = view.findViewById<TextInputEditText>(R.id.etSearch)
        val progress = view.findViewById<ProgressBar>(R.id.progress)
        val tvMessage = view.findViewById<TextView>(R.id.tvMessage)
        val list = view.findViewById<RecyclerView>(R.id.resultsList)

        val adapter = FoodSearchAdapter { food ->
            nutritionVm.addMeal(food)
            Toast.makeText(requireContext(), "Added ${food.name}", Toast.LENGTH_SHORT).show()
            dismiss()
        }
        list.layoutManager = LinearLayoutManager(requireContext())
        list.adapter = adapter

        // Search only when the user submits (keyboard "search" key or the magnifier icon).
        // Open Food Facts limits searches to ~10/min, so no search-as-you-type.
        val doSearch = {
            hideKeyboard(etSearch)
            searchVm.search(etSearch.text?.toString().orEmpty())
        }
        etSearch.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) { doSearch(); true } else false
        }
        searchLayout.setStartIconOnClickListener { doSearch() }

        searchVm.state.observe(viewLifecycleOwner) { state ->
            progress.visibility = if (state is SearchUiState.Loading) View.VISIBLE else View.GONE
            list.visibility = if (state is SearchUiState.Results) View.VISIBLE else View.GONE

            val message = when (state) {
                is SearchUiState.Idle -> "Search for a food to see its calories and macros."
                is SearchUiState.Empty -> "No foods found. Try different words."
                is SearchUiState.Error -> state.message
                else -> null
            }
            tvMessage.text = message
            tvMessage.visibility = if (message == null) View.GONE else View.VISIBLE

            if (state is SearchUiState.Results) adapter.submitList(state.foods)
        }

        // Open with the keyboard up, ready to type
        dialog?.window?.setSoftInputMode(
            WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE or
                WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE
        )
        etSearch.requestFocus()
    }

    // Open fully expanded and tall, so results have room
    override fun onStart() {
        super.onStart()
        view?.let { v ->
            v.layoutParams = v.layoutParams?.apply {
                height = (resources.displayMetrics.heightPixels * 0.9).toInt()
            }
        }
        (dialog as? BottomSheetDialog)?.behavior?.apply {
            state = BottomSheetBehavior.STATE_EXPANDED
            skipCollapsed = true
        }
    }

    private fun hideKeyboard(target: View) {
        val imm = requireContext().getSystemService(InputMethodManager::class.java)
        imm.hideSoftInputFromWindow(target.windowToken, 0)
    }
}
