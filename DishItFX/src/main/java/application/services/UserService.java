package application.services;

import java.util.Objects;

import application.models.User;

public class UserService {
    public void saveAccount(User user, String password) {
        Objects.requireNonNull(user, "user");
        if (password == null || password.isBlank()) {
            throw new IllegalArgumentException("Password is required.");
        }

        throw new PersistenceUnavailableException(
                "The account API is not connected yet. No account data was saved.");
    }

    public static final class PersistenceUnavailableException extends RuntimeException {
        private static final long serialVersionUID = 1L;

        public PersistenceUnavailableException(String message) {
            super(message);
        }
    }
}
