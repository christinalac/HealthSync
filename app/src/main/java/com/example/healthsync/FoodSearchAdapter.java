package com.example.healthsync;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

// One row per search result: photo, name, brand, nutrition per serving, and an Add button.
public class FoodSearchAdapter extends ListAdapter<FoodItem, FoodSearchAdapter.VH> {

    public interface OnAdd {
        void onAdd(FoodItem food);
    }

    private final OnAdd onAdd;

    public FoodSearchAdapter(OnAdd onAdd) {
        super(DIFF);
        this.onAdd = onAdd;
    }

    static class VH extends RecyclerView.ViewHolder {
        final ImageView image;
        final TextView name;
        final TextView brand;
        final TextView serving;
        final TextView nutrients;
        final View addButton;

        VH(View view) {
            super(view);
            image = view.findViewById(R.id.ivFood);
            name = view.findViewById(R.id.tvFoodName);
            brand = view.findViewById(R.id.tvFoodBrand);
            serving = view.findViewById(R.id.tvFoodServing);
            nutrients = view.findViewById(R.id.tvFoodNutrients);
            addButton = view.findViewById(R.id.btnAdd);
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
        final FoodItem food = getItem(position);
        holder.name.setText(food.getName());
        holder.brand.setText(food.getBrand());
        holder.brand.setVisibility(food.getBrand().isEmpty() ? View.GONE : View.VISIBLE);
        holder.serving.setText("Per serving (" + food.getServingLabel() + ")");
        holder.nutrients.setText(NutritionFormat.nutrientLine(
                food.getKcal(), food.getCarbsG(), food.getProteinG(), food.getFatG()));
        Glide.with(holder.image)
                .load(food.getImageUrl())
                .placeholder(R.drawable.bg_food_placeholder)
                .error(R.drawable.bg_food_placeholder)
                .centerCrop()
                .into(holder.image);
        holder.addButton.setOnClickListener(v -> onAdd.onAdd(food));
    }

    private static final DiffUtil.ItemCallback<FoodItem> DIFF = new DiffUtil.ItemCallback<FoodItem>() {
        @Override
        public boolean areItemsTheSame(@NonNull FoodItem a, @NonNull FoodItem b) {
            return a.getCode().equals(b.getCode()) && a.getName().equals(b.getName());
        }

        @Override
        public boolean areContentsTheSame(@NonNull FoodItem a, @NonNull FoodItem b) {
            return a.equals(b);
        }
    };
}
