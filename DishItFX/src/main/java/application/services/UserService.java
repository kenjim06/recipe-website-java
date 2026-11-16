package application.services;

import java.util.Objects;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import application.models.User;

public class UserService {
    private static volatile String accessToken;
    private static volatile String userId;

    public static void setAccessToken(String token) {
        accessToken = token;
    }

    public static String getAccessToken() {
        return accessToken;
    }

    public static String getUserId() {
        return userId;
    }

    public static boolean isLoggedIn() {
        return accessToken != null && !accessToken.isBlank();
    }

    public static void loggedOut() {
        accessToken = null;
        userId = null;
    }

    public static String userLogin(String email, String password) {
        if (email == null || email.isBlank() || password == null || password.isBlank()) {
            throw new IllegalArgumentException("Email and password are required.");
        }

        JsonObject payload = new JsonObject();
        payload.addProperty("email", email.strip());
        payload.addProperty("password", password);

        String response = SupabaseClient.fromEnvironment().request(
                "POST",
                "/auth/v1/token?grant_type=password",
                payload.toString(),
                null,
                null);
        JsonObject result = JsonParser.parseString(response).getAsJsonObject();
        if (!result.has("access_token") || result.get("access_token").isJsonNull()) {
            throw new SupabaseException("Supabase sign-in response did not contain an access token.");
        }
        accessToken = result.get("access_token").getAsString();
        userId = result.has("user") && result.get("user").isJsonObject()
                && result.getAsJsonObject("user").has("id")
                ? result.getAsJsonObject("user").get("id").getAsString()
                : null;
        return accessToken;
    }

    public void saveAccount(User user, String password) {
        User account = Objects.requireNonNull(user, "user");
        if (password == null || password.isBlank()) {
            throw new IllegalArgumentException("Password is required.");
        }

        JsonObject metadata = new JsonObject();
        metadata.addProperty("display_name", account.displayName());

        JsonObject payload = new JsonObject();
        payload.addProperty("email", account.email());
        payload.addProperty("password", password);
        payload.add("data", metadata);

        String response = SupabaseClient.fromEnvironment().request(
                "POST",
                "/auth/v1/signup",
                payload.toString(),
                null,
                null);
        if (response != null && !response.isBlank()) {
            JsonObject result = JsonParser.parseString(response).getAsJsonObject();
            if (result.has("access_token") && !result.get("access_token").isJsonNull()) {
                accessToken = result.get("access_token").getAsString();
            }
            userId = result.has("user") && result.get("user").isJsonObject()
                    && result.getAsJsonObject("user").has("id")
                    ? result.getAsJsonObject("user").get("id").getAsString()
                    : null;
        }
    }
}
