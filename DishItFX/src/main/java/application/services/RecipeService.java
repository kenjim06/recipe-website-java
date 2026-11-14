package application.services;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

import application.models.CommunityRecipe;
import application.models.Recipe;
import application.services.SupabaseClient.SupabaseException;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

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
        return UserService.isLoggedIn() ? get() : List.copyOf(USER_RECIPES);
    }

    public void saveRecipe(Recipe recipe) {
        Recipe newRecipe = Objects.requireNonNull(recipe, "recipe");
        boolean duplicateId = USER_RECIPES.stream()
                .anyMatch(existing -> existing.id().equals(newRecipe.id()));
        if (duplicateId) {
            throw new IllegalArgumentException("A recipe with this id already exists.");
        }

        if (UserService.isLoggedIn()) {
            USER_RECIPES.add(insertRecipe(newRecipe, List.of(newRecipe.category())));
        } else {
            USER_RECIPES.add(newRecipe);
        }
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

    public static void create(
            String recipeName,
            List<String> categories,
            int prepTime,
            int cookTime,
            String ingredients,
            String instructions,
            String notes) {
        ensureAuthenticated();
        Recipe recipe = new Recipe(
                UUID.randomUUID().toString(),
                recipeName,
                firstCategory(categories),
                prepTime,
                cookTime,
                splitLines(ingredients),
                splitLines(instructions),
                Objects.requireNonNullElse(notes, ""));
        USER_RECIPES.add(insertRecipe(recipe, categories));
    }

    public static List<Recipe> get() {
        ensureAuthenticated();
        String response = SupabaseClient.fromEnvironment().request(
                "GET",
                "/rest/v1/recipes?select=*",
                null,
                UserService.getAccessToken(),
                null);
        JsonElement parsed = JsonParser.parseString(response);
        if (!parsed.isJsonArray()) {
            throw new SupabaseException("Supabase returned an invalid recipe collection.");
        }

        List<Recipe> recipes = new ArrayList<>();
        for (JsonElement element : parsed.getAsJsonArray()) {
            recipes.add(fromSupabase(element.getAsJsonObject()));
        }
        return List.copyOf(recipes);
    }

    public static void update(
            long recipeId,
            String recipeName,
            List<String> categories,
            int prepTime,
            int cookTime,
            String ingredients,
            String instructions,
            String notes) {
        ensureAuthenticated();
        Recipe recipe = new Recipe(
                Long.toString(recipeId),
                recipeName,
                firstCategory(categories),
                prepTime,
                cookTime,
                splitLines(ingredients),
                splitLines(instructions),
                Objects.requireNonNullElse(notes, ""));
        SupabaseClient.fromEnvironment().request(
                "PATCH",
                "/rest/v1/recipes?id=eq." + recipeId,
                toSupabasePayload(recipe, categories).toString(),
                UserService.getAccessToken(),
                "return=minimal");
    }

    public static void delete(long recipeId) {
        ensureAuthenticated();
        SupabaseClient.fromEnvironment().request(
                "DELETE",
                "/rest/v1/recipes?id=eq." + recipeId,
                null,
                UserService.getAccessToken(),
                "return=minimal");
    }

    private static Recipe insertRecipe(Recipe recipe, List<String> categories) {
        ensureAuthenticated();
        String response = SupabaseClient.fromEnvironment().request(
                "POST",
                "/rest/v1/recipes",
                toSupabasePayload(recipe, categories).toString(),
                UserService.getAccessToken(),
                "return=representation");
        JsonElement parsed = JsonParser.parseString(response);
        if (!parsed.isJsonArray() || parsed.getAsJsonArray().isEmpty()) {
            throw new SupabaseException("Supabase did not return the saved recipe.");
        }
        return fromSupabase(parsed.getAsJsonArray().get(0).getAsJsonObject());
    }

    private static JsonObject toSupabasePayload(Recipe recipe, List<String> categories) {
        JsonObject payload = new JsonObject();
        payload.addProperty("recipe_name", recipe.name());
        payload.addProperty("prep_time", recipe.prepTimeMinutes());
        payload.addProperty("cook_time", recipe.cookTimeMinutes());
        payload.addProperty("ingredients", String.join(System.lineSeparator(), recipe.ingredients()));
        payload.addProperty("instructions", String.join(System.lineSeparator(), recipe.instructions()));
        payload.addProperty("notes", recipe.notes());
        String ownerId = UserService.getUserId();
        if (ownerId != null) {
            payload.addProperty("userID", ownerId);
        }

        JsonArray categoryValues = new JsonArray();
        for (String category : Objects.requireNonNull(categories, "categories")) {
            categoryValues.add(Objects.requireNonNull(category, "category"));
        }
        payload.add("categories", categoryValues);
        return payload;
    }

    private static Recipe fromSupabase(JsonObject value) {
        String id = requiredString(value, "id");
        String name = requiredString(value, "recipe_name");
        JsonElement categoriesValue = value.get("categories");
        List<String> categories = readStringList(categoriesValue);
        if (categories.isEmpty()) {
            throw new SupabaseException("Supabase recipe is missing its category.");
        }
        return new Recipe(
                id,
                name,
                categories.get(0),
                requiredInt(value, "prep_time"),
                requiredInt(value, "cook_time"),
                readStringList(value.get("ingredients")),
                readStringList(value.get("instructions")),
                optionalString(value, "notes"));
    }

    private static List<String> readStringList(JsonElement value) {
        if (value == null || value.isJsonNull()) {
            return List.of();
        }
        if (value.isJsonArray()) {
            List<String> values = new ArrayList<>();
            for (JsonElement item : value.getAsJsonArray()) {
                values.add(item.getAsString().strip());
            }
            return values.stream().filter(item -> !item.isEmpty()).toList();
        }
        return splitLines(value.getAsString());
    }

    private static String requiredString(JsonObject value, String property) {
        if (!value.has(property) || value.get(property).isJsonNull()) {
            throw new SupabaseException("Supabase recipe is missing required field: " + property + ".");
        }
        return value.get(property).getAsString();
    }

    private static int requiredInt(JsonObject value, String property) {
        if (!value.has(property) || value.get(property).isJsonNull()) {
            throw new SupabaseException("Supabase recipe is missing required field: " + property + ".");
        }
        return value.get(property).getAsInt();
    }

    private static String optionalString(JsonObject value, String property) {
        return value.has(property) && !value.get(property).isJsonNull()
                ? value.get(property).getAsString()
                : "";
    }

    private static List<String> splitLines(String value) {
        if (value == null || value.isBlank()) {
            return List.of();
        }
        return Arrays.stream(value.split("\\R"))
                .map(String::strip)
                .filter(line -> !line.isEmpty())
                .toList();
    }

    private static String firstCategory(List<String> categories) {
        if (categories == null || categories.isEmpty()) {
            throw new IllegalArgumentException("At least one recipe category is required.");
        }
        return Objects.requireNonNull(categories.get(0), "category");
    }

    private static void ensureAuthenticated() {
        if (!UserService.isLoggedIn()) {
            throw new SupabaseException("Log in before accessing Supabase recipes.");
        }
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
