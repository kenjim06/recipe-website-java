package application.services;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;


//This is currently the grounds for seeing if an authenticated user can connect to supabase using a test user credentials

public class UserService {
	public static String accessToken;
	public static void setAccessToken(String token) {
		accessToken = token;
	}
	public static String getAccessToken() {
		return accessToken;
	}
	public static boolean isLoggedIn() {
		return accessToken != null && !accessToken.isBlank();
	}
	public static void loggedOut() {
		accessToken = null;
	}
	public static String userLogin(String email, String password) {
		
		String url = "https://vmrpsuzutybigfkgyirl.supabase.co";
		String key = "sb_publishable_4JM7gvJhpq2UMimGhZHO8w_pOQWJXbk";
		
		JsonObject json = new JsonObject();
		json.addProperty("email", email);
		json.addProperty("password", password);
		
		//Checks if the strings are null and will send back missing if so, error handler
		if(url == null || key == null || email == null || password == null) {
			System.out.println("MISSING!");
			return null;
		}
		
		try {
			//Creating the client, the json we need to send, the request with out json, and then the response supabase gives whether we actually sign in and pass or not
			HttpClient client = HttpClient.newHttpClient();
			
			HttpRequest request = HttpRequest.newBuilder()
					.uri(URI.create(url + "/auth/v1/token?grant_type=password"))
					.header("apikey", key)
					.header("Content-Type", "application/json")
					.POST(HttpRequest.BodyPublishers.ofString(json.toString()))
					.build();
			
			HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
			System.out.println("Status code: " + response.statusCode());
			
			//If it works, it'll parse the json and find the access token and respond back with success
			if(response.statusCode() == 200) {
				JsonObject jsonResponse = JsonParser.parseString(response.body()).getAsJsonObject();
				String token = jsonResponse.get("access_token").getAsString();
				accessToken = token;
				return token;
			} else {
				System.out.println("Login failed: " + response.body());
				return null;
			}
					
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
}
