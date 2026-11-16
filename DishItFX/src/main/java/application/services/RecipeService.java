package application.services;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import application.models.Recipe;

public class RecipeService {
    
    // Convert Supabase JSON into a Java Recipe.
    private static Recipe fromSupabase(JsonObject value) {

        long id = requiredLong(value, "id");
        String name = requiredString(value, "recipe_name");
        String category = requiredString(value, "category");
        int prepTime = requiredInt(value, "prep_time");
        int cookTime = requiredInt(value, "cook_time");
        List<String> ingredients = readStringList(value.get("ingredients"));
        List<String> instructions = readStringList(value.get("instructions"));
        String notes = optionalString(value, "notes");
        String imagePath = optionalString(value, "image_path");
        String authorName = optionalString(value, "author_name");
        int estimatedCostDollars = optionalInt(value, "estimated_cost_dollars");
        String allergens = optionalString(value, "allergens");
        int popularity = optionalInt(value, "popularity");

        return new Recipe(
                id,
                name,
                category,
                prepTime,
                cookTime,
                ingredients,
                instructions,
                notes,
                imagePath,
                authorName,
                estimatedCostDollars,
                allergens,
                popularity
        );
    }

    public List<Recipe> getHomePageRecipes() {
        List<Recipe> recipes = fetchRecipes(
            "/rest/v1/recipes?select=*"
        );
        return recipes;
    }
    
    public List<Recipe> getSavedRecipes() {
        ensureAuthenticated();

        String response = SupabaseClient.fromEnvironment().request(
                "GET",
                "/rest/v1/favorites?select=recipe:recipes(*)&user_id=eq."
                        + UserService.getUserId(),
                null,
                UserService.getAccessToken(),
                null
        );

        JsonElement parsed = JsonParser.parseString(response);

        if (!parsed.isJsonArray()) {
            throw new SupabaseException("Supabase returned an invalid favorites collection.");
        }

        List<Recipe> recipes = new ArrayList<>();

        for (JsonElement element : parsed.getAsJsonArray()) {
            JsonObject favorite = element.getAsJsonObject();

            // The related recipe is returned inside the "recipe" property.
            JsonElement recipeValue = favorite.get("recipe");

            if (recipeValue != null && recipeValue.isJsonObject()) {
                recipes.add(fromSupabase(recipeValue.getAsJsonObject()));
            }
        }

        return List.copyOf(recipes);
    }

    public List<Recipe> getMyRecipes() {
        return get();
    }

    public void saveRecipe(Recipe recipe) {
        Objects.requireNonNull(recipe, "recipe");

        ensureAuthenticated();

        insertRecipe(recipe, List.of(recipe.category()));
    }

    // READ: Get the currently logged-in user's recipes.
    public static List<Recipe> get() {
        ensureAuthenticated();

        String userId = UserService.getUserId();

        return fetchRecipes(
                "/rest/v1/recipes?select=*&user_id=eq." + userId
        );
    }

    // CREATE: Insert a new recipe.
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
                0L,
                recipeName,
                firstCategory(categories),
                prepTime,
                cookTime,
                splitLines(ingredients),
                splitLines(instructions),
                Objects.requireNonNullElse(notes, ""),
                "",
                "",
                0,
                "",
                0
        );

        insertRecipe(recipe, categories);
    }

    // Shared INSERT request.
    private static Recipe insertRecipe(
            Recipe recipe,
            List<String> categories) {

        ensureAuthenticated();

        String response = SupabaseClient.fromEnvironment().request(
                "POST",
                "/rest/v1/recipes",
                toSupabasePayload(recipe, categories).toString(),
                UserService.getAccessToken(),
                "return=representation"
        );

        JsonElement parsed = JsonParser.parseString(response);

        if (!parsed.isJsonArray() || parsed.getAsJsonArray().isEmpty()) {
            throw new SupabaseException(
                    "Supabase did not return the saved recipe."
            );
        }

        return fromSupabase(
                parsed.getAsJsonArray().get(0).getAsJsonObject()
        );
    }

    private static List<Recipe> fetchRecipes(String endpoint) {
        String response = SupabaseClient.fromEnvironment().request(
                "GET",
                endpoint,
                null,
                UserService.getAccessToken(),
                null
        );

        JsonElement parsed = JsonParser.parseString(response);
        if (!parsed.isJsonArray()) {
            throw new SupabaseException("Supabase did not return a JSON array.");
        }

        List<Recipe> recipes = new ArrayList<>();
        for (JsonElement element : parsed.getAsJsonArray()) {
            recipes.add(fromSupabase(element.getAsJsonObject()));
        }
        return List.copyOf(recipes);
    }

    // UPDATE: Modify an existing recipe.
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
                recipeId,
                recipeName,
                firstCategory(categories),
                prepTime,
                cookTime,
                splitLines(ingredients),
                splitLines(instructions),
                Objects.requireNonNullElse(notes, ""),
                "",
                "",
                0,
                "",
                0
        );

        SupabaseClient.fromEnvironment().request(
                "PATCH",
                "/rest/v1/recipes?id=eq." + recipeId
                        + "&user_id=eq." + UserService.getUserId(),
                toSupabasePayload(recipe, categories).toString(),
                UserService.getAccessToken(),
                "return=minimal"
        );
    }
    // DELETE: Remove a recipe.
    public static void delete(long recipeId) {
        ensureAuthenticated();

        SupabaseClient.fromEnvironment().request(
                "DELETE",
                "/rest/v1/recipes?id=eq." + recipeId
                        + "&user_id=eq." + UserService.getUserId(),
                null,
                UserService.getAccessToken(),
                "return=minimal"
        );
    }   

    // Convert a Java Recipe into JSON for Supabase.
    private static JsonObject toSupabasePayload(
            Recipe recipe,
            List<String> categories) {

        JsonObject payload = new JsonObject();

        payload.addProperty("recipe_name", recipe.name());
        payload.addProperty("category", firstCategory(categories));
        payload.addProperty("prep_time", recipe.prepTimeMinutes());
        payload.addProperty("cook_time", recipe.cookTimeMinutes());
        payload.addProperty("notes", recipe.notes());
        payload.addProperty("ingredients", String.join(System.lineSeparator(), recipe.ingredients()));
        payload.addProperty("instructions", String.join(System.lineSeparator(), recipe.instructions()));
        if (!recipe.imageUrl().isBlank()) {
            payload.addProperty("image_path", recipe.imageUrl());
        }
        if (!recipe.userName().isBlank()) {
            payload.addProperty("author_name", recipe.userName());
        }
        if (recipe.estimatedCostDollars() > 0) {
            payload.addProperty("estimated_cost_dollars", recipe.estimatedCostDollars());
        }
        if (!recipe.allergens().isBlank()) {
            payload.addProperty("allergens", recipe.allergens());
        }
        if (recipe.popularity() > 0) {
            payload.addProperty("popularity", recipe.popularity());
        }

        // Associate the recipe with the currently logged-in user.
        payload.addProperty("user_id", UserService.getUserId());

        return payload;
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

    private static long requiredLong(JsonObject value, String property){
        if(!value.has(property) || value.get(property).isJsonNull()) {
            throw new SupabaseException("Supabase recipe is missing required field: " + property + ".");
        }
        return value.get(property).getAsLong();
    }

    private static String optionalString(JsonObject value, String property) {
        return value.has(property) && !value.get(property).isJsonNull()
                ? value.get(property).getAsString()
                : "";
    }

    private static String firstOptionalString(JsonObject value, String... properties) {
        for (String property : properties) {
            String result = optionalString(value, property);
            if (!result.isBlank()) {
                return result;
            }
        }
        return "";
    }

    private static int optionalInt(JsonObject value, String property) {
        return value.has(property) && !value.get(property).isJsonNull()
                ? value.get(property).getAsInt()
                : 0;
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
        if (!UserService.isLoggedIn()
                || UserService.getUserId() == null
                || UserService.getUserId().isBlank()) {
            throw new SupabaseException("Log in before accessing Supabase recipes.");
        }
    }

}
