package com.example.healthsync

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

// One row per search result: photo, name, brand, nutrition per serving, and an Add button.
class FoodSearchAdapter(
    private val onAdd: (FoodItem) -> Unit
) : ListAdapter<FoodItem, FoodSearchAdapter.VH>(Diff) {

    class VH(view: View) : RecyclerView.ViewHolder(view) {
        val image: ImageView = view.findViewById(R.id.ivFood)
        val name: TextView = view.findViewById(R.id.tvFoodName)
        val brand: TextView = view.findViewById(R.id.tvFoodBrand)
        val serving: TextView = view.findViewById(R.id.tvFoodServing)
        val nutrients: TextView = view.findViewById(R.id.tvFoodNutrients)
        val addButton: View = view.findViewById(R.id.btnAdd)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        VH(LayoutInflater.from(parent.context).inflate(R.layout.item_food_result, parent, false))

    override fun onBindViewHolder(holder: VH, position: Int) {
        val food = getItem(position)
        holder.name.text = food.name
        holder.brand.text = food.brand
        holder.brand.visibility = if (food.brand.isEmpty()) View.GONE else View.VISIBLE
        holder.serving.text = "Per serving (${food.servingLabel})"
        holder.nutrients.text = nutrientLine(food.kcal, food.carbsG, food.proteinG, food.fatG)
        Glide.with(holder.image)
            .load(food.imageUrl)
            .placeholder(R.drawable.bg_food_placeholder)
            .error(R.drawable.bg_food_placeholder)
            .centerCrop()
            .into(holder.image)
        holder.addButton.setOnClickListener { onAdd(food) }
    }

    private object Diff : DiffUtil.ItemCallback<FoodItem>() {
        override fun areItemsTheSame(a: FoodItem, b: FoodItem) = a.code == b.code && a.name == b.name
        override fun areContentsTheSame(a: FoodItem, b: FoodItem) = a == b
    }
}
