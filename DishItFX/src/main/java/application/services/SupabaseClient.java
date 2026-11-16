package application.services;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

public class SupabaseClient {

    private final String supabaseUrl;
    private final String supabaseKey;

    private final HttpClient httpClient;

    public SupabaseClient(String supabaseUrl, String supabaseKey) {
        if (supabaseUrl == null || supabaseUrl.isBlank()
                || supabaseKey == null || supabaseKey.isBlank()) {
            throw new IllegalArgumentException("Supabase URL and publishable key are required.");
        }
        this.supabaseUrl = supabaseUrl.strip().replaceAll("/+$", "");
        this.supabaseKey = supabaseKey.strip();

        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
    }

    // Create a client using environment variables.
    public static SupabaseClient fromEnvironment() {

        String url = SupabaseConfig.SUPABASE_URL;
        String key = SupabaseConfig.SUPABASE_KEY;

        if (url == null || url.isBlank()
                || key == null || key.isBlank()) {
            throw new SupabaseException(
                    "Supabase is not configured. Set SUPABASE_URL and SUPABASE_PUBLISHABLE_KEY."
            );
        }

        return new SupabaseClient(url, key);
    }

    // General HTTP request method.
    public String request(
            String method,
            String endpoint,
            String body,
            String accessToken,
            String prefer) {

        try {

            HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                    .uri(URI.create(supabaseUrl + endpoint))
                    .timeout(Duration.ofSeconds(30))
                    .header("apikey", supabaseKey)
                    .header("Authorization", "Bearer " + 
                            (accessToken != null ? accessToken : supabaseKey))
                    .header("Content-Type", "application/json")
                    .header("Accept", "application/json");

            // Optional Supabase Prefer header.
            if (prefer != null && !prefer.isBlank()) {
                requestBuilder.header("Prefer", prefer);
            }

            // Set HTTP method and request body.
            if (body != null) {

                requestBuilder.method(
                        method,
                        HttpRequest.BodyPublishers.ofString(body)
                );

            } else {

                requestBuilder.method(
                        method,
                        HttpRequest.BodyPublishers.noBody()
                );
            }

            HttpRequest request = requestBuilder.build();

            // Send request.
            HttpResponse<String> response = httpClient.send(
                    request,
                    HttpResponse.BodyHandlers.ofString()
            );

            // Check response.
            if (response.statusCode() < 200 ||
                    response.statusCode() >= 300) {

                throw new SupabaseException(
                        "Supabase request failed. Status: "
                        + response.statusCode()
                        + "\nResponse: "
                        + response.body()
                );
            }

            return response.body();

        } catch (IOException e) {

            throw new SupabaseException(
                    "Could not connect to Supabase: " + e.getMessage()
            );

        } catch (InterruptedException e) {

            Thread.currentThread().interrupt();

            throw new SupabaseException(
                    "Supabase request was interrupted."
            );
        }
    }
}
