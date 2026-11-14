package application.models;

import java.util.Objects;
import java.util.Set;

public record CommunityRecipe(
        Recipe recipe,
        String author,
        int estimatedCostDollars,
        Set<String> allergens,
        int popularity) {

    public CommunityRecipe {
        recipe = Objects.requireNonNull(recipe, "recipe");
        author = Objects.requireNonNull(author, "author").strip();
        allergens = Set.copyOf(Objects.requireNonNull(allergens, "allergens"));

        if (author.isEmpty()) {
            throw new IllegalArgumentException("Recipe author must not be blank.");
        }
        if (estimatedCostDollars < 0 || popularity < 0) {
            throw new IllegalArgumentException("Recipe cost and popularity must not be negative.");
        }
    }
}
