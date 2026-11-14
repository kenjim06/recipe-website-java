package application.services;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

public final class SupabaseClient {
    private static final HttpClient HTTP_CLIENT = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    private final String projectUrl;
    private final String publishableKey;

    private SupabaseClient(String projectUrl, String publishableKey) {
        this.projectUrl = projectUrl;
        this.publishableKey = publishableKey;
    }

    public static SupabaseClient fromEnvironment() {
        String projectUrl = System.getenv("SUPABASE_URL");
        String publishableKey = System.getenv("SUPABASE_PUBLISHABLE_KEY");
        if (projectUrl == null || projectUrl.isBlank()
                || publishableKey == null || publishableKey.isBlank()) {
            throw new SupabaseException(
                    "Set SUPABASE_URL and SUPABASE_PUBLISHABLE_KEY to enable Supabase.");
        }

        URI uri = URI.create(projectUrl.strip());
        if (!"https".equalsIgnoreCase(uri.getScheme()) || uri.getHost() == null) {
            throw new SupabaseException("SUPABASE_URL must be a valid HTTPS project URL.");
        }
        return new SupabaseClient(
                projectUrl.strip().replaceAll("/+$", ""),
                publishableKey.strip());
    }

    public String request(
            String method,
            String path,
            String jsonBody,
            String accessToken,
            String prefer) {
        if (path == null || !path.startsWith("/")) {
            throw new IllegalArgumentException("Supabase request paths must start with '/'.");
        }

        HttpRequest.Builder request = HttpRequest.newBuilder()
                .uri(URI.create(projectUrl + path))
                .timeout(Duration.ofSeconds(30))
                .header("apikey", publishableKey)
                .header("Accept", "application/json");
        if (accessToken != null && !accessToken.isBlank()) {
            request.header("Authorization", "Bearer " + accessToken);
        }
        if (prefer != null && !prefer.isBlank()) {
            request.header("Prefer", prefer);
        }
        if (jsonBody == null) {
            request.method(method, HttpRequest.BodyPublishers.noBody());
        } else {
            request.header("Content-Type", "application/json")
                    .method(method, HttpRequest.BodyPublishers.ofString(jsonBody));
        }

        try {
            HttpResponse<String> response = HTTP_CLIENT.send(
                    request.build(),
                    HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new SupabaseException(
                        "Supabase request failed with HTTP " + response.statusCode() + ".");
            }
            return response.body();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new SupabaseException("Supabase request was interrupted.", e);
        } catch (IOException e) {
            throw new SupabaseException("Could not reach Supabase.", e);
        }
    }

    public static final class SupabaseException extends RuntimeException {
        private static final long serialVersionUID = 1L;

        public SupabaseException(String message) {
            super(message);
        }

        public SupabaseException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
