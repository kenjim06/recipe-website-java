package application.models;

import java.util.List;
import java.util.Objects;

public record Recipe(
        long id,
        String name,
        String category,
        int prepTimeMinutes,
        int cookTimeMinutes,
        List<String> ingredients,
        List<String> instructions,
        String notes,
        String imageUrl,
        String userName,
        int estimatedCostDollars,
        String allergens,
        int popularity
    ) {

    public Recipe {
        id = Objects.requireNonNull(id, "id");
        name = Objects.requireNonNull(name, "name").strip();
        category = Objects.requireNonNull(category, "category").strip();
        ingredients = List.copyOf(Objects.requireNonNull(ingredients, "ingredients"));
        instructions = List.copyOf(Objects.requireNonNull(instructions, "instructions"));
        notes = Objects.requireNonNull(notes, "notes").strip();
        imageUrl = Objects.requireNonNull(imageUrl, "imageUrl").strip();
        userName = Objects.requireNonNull(userName, "userName").strip();
        estimatedCostDollars = Objects.requireNonNull(estimatedCostDollars, "estimatedCostDollars");
        allergens = Objects.requireNonNull(allergens, "allergens").strip();
        popularity = Objects.requireNonNull(popularity, "popularity");

        if (name.isEmpty() || category.isEmpty()) {
            throw new IllegalArgumentException("Recipe name and category must not be blank.");
        }
        if (prepTimeMinutes < 0 || cookTimeMinutes < 0) {
            throw new IllegalArgumentException("Recipe times must not be negative.");
        }
    }

    public int totalTimeMinutes() {
        return prepTimeMinutes + cookTimeMinutes;
    }
}
