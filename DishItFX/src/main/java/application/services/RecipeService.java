package application.services;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import application.models.CommunityRecipe;
import application.models.Recipe;

public class RecipeService {
    private static final List<Recipe> USER_RECIPES = new ArrayList<>();

    private static final List<Recipe> HOME_PAGE_RECIPES = List.of(
            recipe("1", "Avocado Toast", "Breakfast", 8, 5),
            recipe("2", "Blueberry Pancakes", "Breakfast", 10, 15),
            recipe("3", "Garden Omelet", "Brunch", 10, 8),
            recipe("4", "Caprese Sandwich", "Lunch", 10, 0),
            recipe("5", "Lemon Herb Chicken", "Dinner", 15, 30),
            recipe("6", "Tomato Basil Soup", "Lunch", 10, 25),
            recipe("7", "Mushroom Risotto", "Dinner", 15, 35),
            recipe("8", "Berry Yogurt Bowl", "Breakfast", 5, 0),
            recipe("9", "Roasted Vegetable Tacos", "Dinner", 15, 20),
            recipe("10", "Chocolate Chip Cookies", "Dessert", 15, 12),
            recipe("11", "Spinach Quiche", "Brunch", 20, 35),
            recipe("12", "Grilled Salmon", "Dinner", 10, 18),
            recipe("13", "Apple Cinnamon Oatmeal", "Breakfast", 5, 10),
            recipe("14", "Chickpea Salad", "Lunch", 12, 0),
            recipe("15", "Banana Bread", "Snack", 15, 45),
            recipe("16", "Margherita Flatbread", "Lunch", 10, 12),
            recipe("17", "Vanilla Berry Parfait", "Dessert", 10, 0),
            recipe("18", "Sweet Potato Fries", "Snack", 10, 25),
            recipe("19", "Pesto Pasta", "Dinner", 10, 15),
            recipe("20", "Lemon Shortbread", "Dessert", 15, 20));

    public List<Recipe> getHomePageRecipes() {
        return HOME_PAGE_RECIPES;
    }

    public List<Recipe> getSavedRecipes() {
        return List.of(
                HOME_PAGE_RECIPES.get(0),
                HOME_PAGE_RECIPES.get(3),
                HOME_PAGE_RECIPES.get(5),
                HOME_PAGE_RECIPES.get(7),
                HOME_PAGE_RECIPES.get(13));
    }

    public List<Recipe> getMyRecipes() {
        return List.copyOf(USER_RECIPES);
    }

    public void saveRecipe(Recipe recipe) {
        Recipe newRecipe = Objects.requireNonNull(recipe, "recipe");
        boolean duplicateId = USER_RECIPES.stream()
                .anyMatch(existing -> existing.id().equals(newRecipe.id()));
        if (duplicateId) {
            throw new IllegalArgumentException("A recipe with this id already exists.");
        }
        USER_RECIPES.add(newRecipe);
    }

    public List<CommunityRecipe> getCommunityRecipes() {
        return List.of(
                communityRecipe("101", "Peanut Noodle Bowl", "Lunch", 8, 6,
                        "Mia C.", 4, Set.of("peanuts", "soy", "gluten"), 248),
                communityRecipe("102", "Almond Berry Overnight Oats", "Breakfast", 10, 0,
                        "Jordan K.", 3, Set.of("tree nuts", "dairy"), 192),
                communityRecipe("103", "Roasted Tomato Pasta", "Dinner", 12, 12,
                        "Sam R.", 8, Set.of("gluten"), 164),
                communityRecipe("104", "Crispy Chickpea Salad", "Lunch", 14, 0,
                        "Avery T.", 5, Set.of(), 131),
                communityRecipe("105", "Spinach Egg Muffins", "Breakfast", 10, 12,
                        "Taylor P.", 6, Set.of("eggs", "dairy"), 108),
                communityRecipe("106", "Cashew Coconut Curry", "Dinner", 15, 20,
                        "Noah W.", 9, Set.of("tree nuts"), 84),
                communityRecipe("107", "Black Bean Breakfast Tacos", "Breakfast", 12, 5,
                        "Riley M.", 4, Set.of(), 71),
                communityRecipe("108", "Lemon Herb Lentils", "Dinner", 10, 20,
                        "Casey L.", 7, Set.of(), 56));
    }

    private static Recipe recipe(String id, String name, String category, int prepTime, int cookTime) {
        return new Recipe(id, name, category, prepTime, cookTime, List.of(), List.of(), "");
    }

    private static CommunityRecipe communityRecipe(
            String id,
            String name,
            String category,
            int prepTime,
            int cookTime,
            String author,
            int cost,
            Set<String> allergens,
            int popularity) {
        return new CommunityRecipe(
                recipe(id, name, category, prepTime, cookTime),
                author,
                cost,
                allergens,
                popularity);
    }
}
