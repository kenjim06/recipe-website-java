package application.models;

import java.util.List;
import java.util.Objects;

public record Recipe(
        String id,
        String name,
        String category,
        int prepTimeMinutes,
        int cookTimeMinutes,
        List<String> ingredients,
        List<String> instructions,
        String notes) {

    public Recipe {
        id = Objects.requireNonNull(id, "id").strip();
        name = Objects.requireNonNull(name, "name").strip();
        category = Objects.requireNonNull(category, "category").strip();
        ingredients = List.copyOf(Objects.requireNonNull(ingredients, "ingredients"));
        instructions = List.copyOf(Objects.requireNonNull(instructions, "instructions"));
        notes = Objects.requireNonNull(notes, "notes").strip();

        if (id.isEmpty() || name.isEmpty() || category.isEmpty()) {
            throw new IllegalArgumentException("Recipe id, name, and category must not be blank.");
        }
        if (prepTimeMinutes < 0 || cookTimeMinutes < 0) {
            throw new IllegalArgumentException("Recipe times must not be negative.");
        }
    }

    public int totalTimeMinutes() {
        return prepTimeMinutes + cookTimeMinutes;
    }
}
