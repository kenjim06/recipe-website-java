package application.models;

import java.util.Locale;
import java.util.Objects;

public record User(String displayName, String email) {
    public User {
        displayName = Objects.requireNonNull(displayName, "displayName").strip();
        email = Objects.requireNonNull(email, "email").strip().toLowerCase(Locale.ROOT);
        if (displayName.isEmpty() || email.isEmpty()) {
            throw new IllegalArgumentException("Display name and email are required.");
        }
    }
}
