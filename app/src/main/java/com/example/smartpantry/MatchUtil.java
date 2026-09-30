package com.example.smartpantry;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Holds the strict-matching logic. A recipe is only suggested when EVERY
 * ingredient is in the pantry in at least the required quantity.
 */
public class MatchUtil {

    /** Makes names comparable: lower case, trimmed, plural turned into singular. */
    public static String normalizeName(String name) {
        String n = name.trim().toLowerCase(Locale.ROOT);
        if (n.endsWith("ies") && n.length() > 4) {
            return n.substring(0, n.length() - 3) + "y";   // cherries -> cherry
        }
        if (n.endsWith("oes") && n.length() > 4) {
            return n.substring(0, n.length() - 2);          // tomatoes -> tomato
        }
        if (n.endsWith("s") && !n.endsWith("ss") && n.length() > 3) {
            return n.substring(0, n.length() - 1);          // eggs -> egg
        }
        return n;
    }

    /** 1 = counted things (pcs), 0 = measured things (weight/volume). */
    private static int family(String unit) {
        String u = unit.trim().toLowerCase(Locale.ROOT);
        if (u.equals("pcs") || u.equals("pc") || u.equals("piece") || u.equals("pieces") || u.isEmpty()) {
            return 1;
        }
        return 0;
    }

    /** Converts to a base unit (g or ml, treated as roughly equal) so kg, l, tsp etc. compare fairly. */
    private static double toBase(double qty, String unit) {
        switch (unit.trim().toLowerCase(Locale.ROOT)) {
            case "kg":   return qty * 1000;
            case "l":    return qty * 1000;
            case "tsp":  return qty * 5;
            case "tbsp": return qty * 15;
            case "cup":  return qty * 240;
            default:     return qty; // g, ml, pcs
        }
    }

    /** True only if every ingredient of the recipe is available in the pantry. */
    public static boolean canMake(Recipe recipe, List<PantryItem> pantry) {
        for (RecipeIngredient need : recipe.getIngredients()) {
            String key = normalizeName(need.getName());
            int fam = family(need.getUnit());
            double required = toBase(need.getQuantity(), need.getUnit());
            double have = 0;
            for (PantryItem p : pantry) {
                if (normalizeName(p.getName()).equals(key) && family(p.getUnit()) == fam) {
                    have += toBase(p.getQuantity(), p.getUnit());
                }
            }
            if (have + 0.0001 < required) {
                return false; // one missing or too little = not suggested
            }
        }
        return true;
    }

    public static List<Recipe> getSuggested(List<Recipe> all, List<PantryItem> pantry) {
        List<Recipe> result = new ArrayList<>();
        for (Recipe r : all) {
            if (canMake(r, pantry)) result.add(r);
        }
        return result;
    }

    /** Shows 2 instead of 2.0 but keeps 0.5 as 0.5. */
    public static String formatQty(double q) {
        if (q == Math.rint(q)) return String.valueOf((long) q);
        return String.valueOf(q);
    }
}
