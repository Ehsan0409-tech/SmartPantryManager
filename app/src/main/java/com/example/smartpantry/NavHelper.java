package com.example.smartpantry;

import android.app.Activity;
import android.content.Intent;

import com.google.android.material.bottomnavigation.BottomNavigationView;

/** Sets up the bottom navigation bar used on the three main screens. */
public class NavHelper {

    public static void setup(Activity activity, int selectedId) {
        BottomNavigationView nav = activity.findViewById(R.id.bottom_nav);
        nav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == selectedId) return true;

            Class<?> target;
            if (id == R.id.nav_pantry) target = MainActivity.class;
            else if (id == R.id.nav_recipes) target = SuggestedRecipesActivity.class;
            else target = SettingsActivity.class;

            Intent intent = new Intent(activity, target);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            activity.startActivity(intent);
            if (!(activity instanceof MainActivity)) activity.finish();
            return true;
        });
        nav.setSelectedItemId(selectedId);
    }
}
