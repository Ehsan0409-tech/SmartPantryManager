package com.example.smartpantry;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

/** Shows suggested recipes in the RecyclerView. */
public class RecipeAdapter extends RecyclerView.Adapter<RecipeAdapter.ViewHolder> {

    public interface OnRecipeClick {
        void onClick(Recipe recipe);
    }

    private List<Recipe> recipes;
    private final OnRecipeClick listener;

    public RecipeAdapter(List<Recipe> recipes, OnRecipeClick listener) {
        this.recipes = recipes;
        this.listener = listener;
    }

    public void setRecipes(List<Recipe> newRecipes) {
        this.recipes = newRecipes;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_recipe, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Recipe r = recipes.get(position);
        holder.tvName.setText(r.getName());
        holder.tvInfo.setText(r.getIngredients().size() + " ingredients - you have them all");
        holder.itemView.setOnClickListener(v -> listener.onClick(r));
    }

    @Override
    public int getItemCount() {
        return recipes.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvInfo;

        ViewHolder(View v) {
            super(v);
            tvName = v.findViewById(R.id.tvRecipeName);
            tvInfo = v.findViewById(R.id.tvRecipeInfo);
        }
    }
}
