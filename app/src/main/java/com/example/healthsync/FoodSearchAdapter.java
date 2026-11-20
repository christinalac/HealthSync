//******************************************
// Program Name: FoodSearchAdapter.java
// Developer: Maximiliano Salinas
// Date Created: 10/12/2026
// Version: 2.0
// Purpose: RecyclerView adapter that displays food search results with a photo, nutrition info, and an Add button.
//******************************************

package com.example.healthsync;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

// One row per search result: photo, name, brand, nutrition per serving, and an Add button.
public class FoodSearchAdapter extends RecyclerView.Adapter<FoodSearchAdapter.VH> {

    public interface OnAddListener {
        void onAdd(FoodItem food);
    }

    private final List<FoodItem> items = new ArrayList<>();
    private final OnAddListener listener;

    public FoodSearchAdapter(OnAddListener listener) {
        this.listener = listener;
    }

    public void setItems(List<FoodItem> newItems) {
        items.clear();
        items.addAll(newItems);
        notifyDataSetChanged();
    }

    static class VH extends RecyclerView.ViewHolder {
        final ImageView image;
        final TextView name, brand, serving, nutrients;
        final View addButton;

        VH(View rowView) {
            super(rowView);
            image = rowView.findViewById(R.id.ivFood);
            name = rowView.findViewById(R.id.tvFoodName);
            brand = rowView.findViewById(R.id.tvFoodBrand);
            serving = rowView.findViewById(R.id.tvFoodServing);
            nutrients = rowView.findViewById(R.id.tvFoodNutrients);
            addButton = rowView.findViewById(R.id.btnAdd);
        }
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new VH(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_food_result, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        final FoodItem food = items.get(position);
        int brandVisibility = View.VISIBLE;
        if (food.brand.isEmpty()) {
            brandVisibility = View.GONE;
        } else {
            brandVisibility = View.VISIBLE;
        }
        holder.name.setText(food.name);
        holder.brand.setText(food.brand);
        holder.brand.setVisibility(brandVisibility);
        holder.serving.setText("Per serving (" + food.servingLabel + ")");
        holder.nutrients.setText(Format.nutrientLine(food.kcal, food.carbsG, food.proteinG, food.fatG));
        Glide.with(holder.image)
                .load(food.imageUrl)
                .placeholder(R.drawable.bg_food_placeholder)
                .error(R.drawable.bg_food_placeholder)
                .centerCrop()
                .into(holder.image);
        holder.addButton.setOnClickListener(clickedView -> listener.onAdd(food));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }
}
