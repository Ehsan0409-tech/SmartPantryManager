package com.example.smartpantry;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

/** SQLite database: pantry CRUD plus seeded recipes. */
public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DB_NAME = "smart_pantry.db";
    private static final int DB_VERSION = 1;

    public DatabaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onConfigure(SQLiteDatabase db) {
        super.onConfigure(db);
        db.setForeignKeyConstraintsEnabled(true);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE pantry (id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "name TEXT NOT NULL, quantity REAL NOT NULL, unit TEXT NOT NULL, expiry TEXT)");
        db.execSQL("CREATE TABLE recipes (id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "name TEXT NOT NULL, steps TEXT NOT NULL)");
        db.execSQL("CREATE TABLE recipe_ingredients (id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "recipe_id INTEGER NOT NULL, name TEXT NOT NULL, quantity REAL NOT NULL, "
                + "unit TEXT NOT NULL, FOREIGN KEY(recipe_id) REFERENCES recipes(id) ON DELETE CASCADE)");
        seedRecipes(db); // runs only the first time the database is created
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS recipe_ingredients");
        db.execSQL("DROP TABLE IF EXISTS recipes");
        db.execSQL("DROP TABLE IF EXISTS pantry");
        onCreate(db);
    }

    // ---------------- Pantry CRUD ----------------

    private ContentValues toValues(PantryItem item) {
        ContentValues cv = new ContentValues();
        cv.put("name", item.getName());
        cv.put("quantity", item.getQuantity());
        cv.put("unit", item.getUnit());
        cv.put("expiry", item.getExpiry());
        return cv;
    }

    private PantryItem fromCursor(Cursor c) {
        return new PantryItem(
                c.getInt(c.getColumnIndexOrThrow("id")),
                c.getString(c.getColumnIndexOrThrow("name")),
                c.getDouble(c.getColumnIndexOrThrow("quantity")),
                c.getString(c.getColumnIndexOrThrow("unit")),
                c.getString(c.getColumnIndexOrThrow("expiry")));
    }

    /** Create */
    public long addItem(PantryItem item) {
        return getWritableDatabase().insert("pantry", null, toValues(item));
    }

    /** Read (all) */
    public List<PantryItem> getAllItems() {
        List<PantryItem> list = new ArrayList<>();
        try (Cursor c = getReadableDatabase().query("pantry", null, null, null, null, null,
                "name COLLATE NOCASE ASC")) {
            while (c.moveToNext()) list.add(fromCursor(c));
        }
        return list;
    }

    /** Read (one) */
    public PantryItem getItem(int id) {
        try (Cursor c = getReadableDatabase().query("pantry", null, "id=?",
                new String[]{String.valueOf(id)}, null, null, null)) {
            if (c.moveToFirst()) return fromCursor(c);
        }
        return null;
    }

    /** Update */
    public int updateItem(PantryItem item) {
        return getWritableDatabase().update("pantry", toValues(item), "id=?",
                new String[]{String.valueOf(item.getId())});
    }

    /** Delete */
    public int deleteItem(int id) {
        return getWritableDatabase().delete("pantry", "id=?", new String[]{String.valueOf(id)});
    }

    // ---------------- Recipes ----------------

    public List<Recipe> getAllRecipes() {
        List<Recipe> list = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        try (Cursor c = db.query("recipes", null, null, null, null, null, "name ASC")) {
            while (c.moveToNext()) {
                int id = c.getInt(c.getColumnIndexOrThrow("id"));
                list.add(new Recipe(id,
                        c.getString(c.getColumnIndexOrThrow("name")),
                        c.getString(c.getColumnIndexOrThrow("steps")),
                        getIngredients(db, id)));
            }
        }
        return list;
    }

    public Recipe getRecipe(int recipeId) {
        SQLiteDatabase db = getReadableDatabase();
        try (Cursor c = db.query("recipes", null, "id=?",
                new String[]{String.valueOf(recipeId)}, null, null, null)) {
            if (c.moveToFirst()) {
                return new Recipe(recipeId,
                        c.getString(c.getColumnIndexOrThrow("name")),
                        c.getString(c.getColumnIndexOrThrow("steps")),
                        getIngredients(db, recipeId));
            }
        }
        return null;
    }

    private List<RecipeIngredient> getIngredients(SQLiteDatabase db, int recipeId) {
        List<RecipeIngredient> list = new ArrayList<>();
        try (Cursor c = db.query("recipe_ingredients", null, "recipe_id=?",
                new String[]{String.valueOf(recipeId)}, null, null, null)) {
            while (c.moveToNext()) {
                list.add(new RecipeIngredient(
                        c.getString(c.getColumnIndexOrThrow("name")),
                        c.getDouble(c.getColumnIndexOrThrow("quantity")),
                        c.getString(c.getColumnIndexOrThrow("unit"))));
            }
        }
        return list;
    }

    // ---------------- Seed data (20 recipes) ----------------

    /** Each ingredient is written as "name|quantity|unit". */
    private void addRecipe(SQLiteDatabase db, String name, String steps, String... ingredients) {
        ContentValues cv = new ContentValues();
        cv.put("name", name);
        cv.put("steps", steps);
        long recipeId = db.insert("recipes", null, cv);
        for (String s : ingredients) {
            String[] p = s.split("\\|");
            ContentValues ic = new ContentValues();
            ic.put("recipe_id", recipeId);
            ic.put("name", p[0]);
            ic.put("quantity", Double.parseDouble(p[1]));
            ic.put("unit", p[2]);
            db.insert("recipe_ingredients", null, ic);
        }
    }

    private void seedRecipes(SQLiteDatabase db) {
        addRecipe(db, "Scrambled Eggs",
                "1. Crack the eggs into a bowl and whisk with salt.\n2. Melt the butter in a pan.\n3. Pour in the eggs and stir gently until just set.",
                "egg|3|pcs", "butter|10|g", "salt|1|tsp");
        addRecipe(db, "Cheese Omelette",
                "1. Whisk the eggs with salt.\n2. Melt butter in a pan and add the eggs.\n3. Sprinkle cheese on top, fold in half and cook for one more minute.",
                "egg|3|pcs", "cheese|50|g", "butter|10|g", "salt|1|tsp");
        addRecipe(db, "Pancakes",
                "1. Mix flour, sugar, egg and milk into a smooth batter.\n2. Melt a little butter in a pan.\n3. Pour in small rounds and cook until golden on both sides.",
                "flour|200|g", "egg|2|pcs", "milk|300|ml", "sugar|2|tbsp", "butter|20|g");
        addRecipe(db, "Tomato Pasta",
                "1. Boil the pasta in salted water.\n2. Fry chopped onion and garlic in oil, then add chopped tomatoes.\n3. Simmer for 10 minutes and mix with the pasta.",
                "pasta|200|g", "tomato|4|pcs", "onion|1|pcs", "garlic|2|pcs", "oil|2|tbsp", "salt|1|tsp");
        addRecipe(db, "Egg Fried Rice",
                "1. Fry chopped onion in oil.\n2. Push to the side, scramble the eggs in the pan.\n3. Add cooked rice and soy sauce and stir fry for 3 minutes.",
                "rice|200|g", "egg|2|pcs", "onion|1|pcs", "oil|2|tbsp", "soy sauce|2|tbsp");
        addRecipe(db, "Cheese Toast",
                "1. Butter the bread.\n2. Top with cheese.\n3. Grill or toast until the cheese melts.",
                "bread|4|pcs", "cheese|80|g", "butter|20|g");
        addRecipe(db, "Mashed Potatoes",
                "1. Peel and boil the potatoes until soft.\n2. Drain and mash with butter and warm milk.\n3. Season with salt.",
                "potato|500|g", "butter|30|g", "milk|100|ml", "salt|1|tsp");
        addRecipe(db, "Vegetable Soup",
                "1. Chop the potatoes, carrots and onion.\n2. Fry the onion in oil, add the rest and cover with water.\n3. Simmer for 25 minutes and season with salt.",
                "potato|2|pcs", "carrot|2|pcs", "onion|1|pcs", "oil|1|tbsp", "salt|1|tsp");
        addRecipe(db, "Rice and Beans",
                "1. Cook the rice.\n2. Fry chopped onion in oil, add the beans and salt.\n3. Simmer for 10 minutes and serve over the rice.",
                "rice|200|g", "bean|200|g", "onion|1|pcs", "oil|1|tbsp", "salt|1|tsp");
        addRecipe(db, "Banana Smoothie",
                "1. Peel the bananas.\n2. Blend with milk and sugar until smooth.\n3. Serve cold.",
                "banana|2|pcs", "milk|300|ml", "sugar|1|tbsp");
        addRecipe(db, "French Toast",
                "1. Whisk the eggs, milk and sugar.\n2. Dip the bread slices in the mixture.\n3. Fry in butter until golden on both sides.",
                "bread|4|pcs", "egg|2|pcs", "milk|100|ml", "sugar|1|tbsp", "butter|10|g");
        addRecipe(db, "Chicken Stir Fry",
                "1. Slice the chicken, onion and carrots.\n2. Fry the chicken in hot oil until cooked.\n3. Add the vegetables and soy sauce and stir fry for 5 minutes.",
                "chicken|300|g", "onion|1|pcs", "carrot|2|pcs", "oil|2|tbsp", "soy sauce|2|tbsp");
        addRecipe(db, "Tuna Mayo Sandwich",
                "1. Drain the tuna and mix with mayonnaise.\n2. Spread on the bread.\n3. Close the sandwich and cut in half.",
                "bread|4|pcs", "tuna|1|pcs", "mayonnaise|2|tbsp");
        addRecipe(db, "Garlic Butter Rice",
                "1. Cook the rice.\n2. Melt butter in a pan and fry crushed garlic.\n3. Mix in the rice and salt.",
                "rice|200|g", "butter|30|g", "garlic|3|pcs", "salt|1|tsp");
        addRecipe(db, "Potato Omelette",
                "1. Slice the potatoes and onion and fry in oil until soft.\n2. Pour in the whisked eggs with salt.\n3. Cook slowly and flip once until set.",
                "potato|2|pcs", "egg|4|pcs", "onion|1|pcs", "oil|2|tbsp", "salt|1|tsp");
        addRecipe(db, "Lentil Curry",
                "1. Fry onion and garlic in oil.\n2. Add curry powder, tomatoes and lentils with water.\n3. Simmer for 25 minutes until soft.",
                "lentil|200|g", "onion|1|pcs", "tomato|2|pcs", "garlic|2|pcs", "curry powder|2|tsp", "oil|1|tbsp");
        addRecipe(db, "Peanut Butter Banana Toast",
                "1. Toast the bread.\n2. Spread with peanut butter.\n3. Top with sliced banana.",
                "bread|2|pcs", "peanut butter|2|tbsp", "banana|1|pcs");
        addRecipe(db, "Macaroni Cheese",
                "1. Boil the macaroni.\n2. Make a sauce with butter, flour and milk, then stir in the cheese.\n3. Mix in the macaroni and serve hot.",
                "macaroni|250|g", "cheese|150|g", "milk|250|ml", "butter|30|g", "flour|2|tbsp");
        addRecipe(db, "Egg Fried Noodles",
                "1. Boil the noodles and drain.\n2. Scramble the eggs in hot oil.\n3. Add noodles and soy sauce and toss for 2 minutes.",
                "noodle|200|g", "egg|2|pcs", "oil|1|tbsp", "soy sauce|2|tbsp");
        addRecipe(db, "Oat Porridge",
                "1. Put the oats and milk in a pot.\n2. Cook on low heat for 5 minutes, stirring.\n3. Sweeten with sugar.",
                "oat|80|g", "milk|300|ml", "sugar|1|tbsp");
    }
}
