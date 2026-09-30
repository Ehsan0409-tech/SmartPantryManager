package com.example.smartpantry;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;

import java.util.ArrayList;
import java.util.List;

/** Shows only the recipes the user can make with what is already in the pantry. */
public class SuggestedRecipesActivity extends AppCompatActivity {

    private DatabaseHelper db;
    private RecipeAdapter adapter;
    private TextView tvEmpty;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_suggested);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("Suggested Recipes");

        db = new DatabaseHelper(this);
        tvEmpty = findViewById(R.id.tvEmpty);

        RecyclerView rv = findViewById(R.id.rvRecipes);
        rv.setLayoutManager(new LinearLayoutManager(this));
        adapter = new RecipeAdapter(new ArrayList<>(), recipe -> {
            Intent i = new Intent(this, RecipeDetailActivity.class);
            i.putExtra(RecipeDetailActivity.EXTRA_RECIPE_ID, recipe.getId());
            startActivity(i);
        });
        rv.setAdapter(adapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        NavHelper.setup(this, R.id.nav_recipes);

        // Run the strict-matching rule against the current pantry
        List<PantryItem> pantry = db.getAllItems();
        List<Recipe> suggested = MatchUtil.getSuggested(db.getAllRecipes(), pantry);
        adapter.setRecipes(suggested);
        tvEmpty.setVisibility(suggested.isEmpty() ? View.VISIBLE : View.GONE);
    }
}
