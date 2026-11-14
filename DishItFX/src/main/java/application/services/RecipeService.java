package application.services;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.ArrayList;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import application.models.Recipe;


public class RecipeService {
	public static void create(String recipeName, List<String> categories, int prepTime, int cookTime, String ingredients, String instructions, String notes) {
		String url = "https://vmrpsuzutybigfkgyirl.supabase.co";
		String key = "sb_publishable_4JM7gvJhpq2UMimGhZHO8w_pOQWJXbk";
		
		String token = UserService.getAccessToken();
		
		if (token == null || token.isBlank()) {
			System.out.println("Cannot create recipe, login failed!");
			return;
		}
		
		JsonObject json = new JsonObject();
		
		json.addProperty("recipe_name", recipeName);
		json.addProperty("prep_time", prepTime);
		json.addProperty("cook_time", cookTime);
		json.addProperty("ingredients", ingredients);
		json.addProperty("instructions", instructions);
		json.addProperty("notes", notes);
		
		JsonArray categoriesArray = new JsonArray();
		
		if (categories != null) {
			for(String category : categories) {
				categoriesArray.add(category);
			}
		}
		
		json.add("categories", categoriesArray);
		
		try {
			HttpClient client = HttpClient.newHttpClient();
			
			HttpRequest request = HttpRequest.newBuilder()
					.uri(URI.create(url + "/rest/v1/recipes"))
					.header("apikey", key)
					.header("Authorization", "Bearer " + token)
					.header("Content-Type", "application/json")
					.header("Prefer", "return=representation")
					.POST(HttpRequest.BodyPublishers.ofString(json.toString()))
					.build();
			
			HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
			
			System.out.println("Create Status Code: " + response.statusCode());
			System.out.println("Response: " + response.body());
			
		} catch(Exception e) {
			e.printStackTrace();
		}
		
	}
	
	public static List<Recipe> get() {
		String url = "https://vmrpsuzutybigfkgyirl.supabase.co";
		String key = "sb_publishable_4JM7gvJhpq2UMimGhZHO8w_pOQWJXbk";
		
		String token = UserService.getAccessToken();
		
		if (token == null || token.isBlank()) {
			System.out.println("Cannot retrieve recipe, login failed");
			return new ArrayList<>();
		}
		
		List<Recipe> recipes = new ArrayList<>();
		
		try {
			HttpClient client = HttpClient.newHttpClient();
			
			HttpRequest request = HttpRequest.newBuilder()
					.uri(URI.create(url + "/rest/v1/recipes?select=*"))
					.header("apikey", key)
					.header("Authorization", "Bearer " + token)
					.GET()
					.build();
			
			HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
			System.out.println("GET Status Code: " + response.statusCode());
			if(response.statusCode() == 200) {
				Gson gson = new Gson();
				JsonArray jsonRecipes = JsonParser.parseString(response.body()).getAsJsonArray();
				
				for(JsonElement element : jsonRecipes) {
					Recipe recipe = gson.fromJson(element, Recipe.class);
					recipes.add(recipe);
				}
			} else {
				System.out.println("Cannot get recipe: " + response.body());
			}
			
		} catch(Exception e) {
			e.printStackTrace();
		}
		return recipes;
	}
	
	public static void update(long recipeID ,String recipeName, List<String> categories, int prepTime, int cookTime, String ingredients, String instructions, String notes) {
		String url = "https://vmrpsuzutybigfkgyirl.supabase.co";
		String key = "sb_publishable_4JM7gvJhpq2UMimGhZHO8w_pOQWJXbk";
		
		String token = UserService.getAccessToken();
		
		if (token == null || token.isBlank()) {
			System.out.println("Cannot update recipe, login failed");
			return;
		}
		
		JsonObject json = new JsonObject();
		
		json.addProperty("recipe_name", recipeName);
		json.addProperty("prep_time", prepTime);
		json.addProperty("cook_time", cookTime);
		json.addProperty("ingredients", ingredients);
		json.addProperty("instructions", instructions);
		json.addProperty("notes", notes);
		
		JsonArray categoriesArray = new JsonArray();
		if (categories != null) {
			for(String category : categories) {
				categoriesArray.add(category);
			}
		}
		
		json.add("categories", categoriesArray);
		
		
		try {
			HttpClient client = HttpClient.newHttpClient();
			
			HttpRequest request = HttpRequest.newBuilder()
					.uri(URI.create(url + "/rest/v1/recipes?id=eq." + recipeID))
					.header("apikey", key)
					.header("Authorization", "Bearer " + token)
					.header("Content-Type", "application/json")
					.header("Prefer", "return=representation")
					.method("PATCH", HttpRequest.BodyPublishers.ofString(json.toString()))
					.build();
			
			HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
			System.out.println("UPDATE Status Code: " + response.statusCode());
			System.out.println("Response: " + response.body());
			
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void delete(long recipeID) {
		String url = "https://vmrpsuzutybigfkgyirl.supabase.co";
		String key = "sb_publishable_4JM7gvJhpq2UMimGhZHO8w_pOQWJXbk";
		
		String token = UserService.getAccessToken();
		
		if (token == null || token.isBlank()) {
			System.out.println("Cannot delete recipe, login failed!");
			return;
		}
		
		
		try {
			HttpClient client = HttpClient.newHttpClient();
			
			HttpRequest request = HttpRequest.newBuilder()
					.uri(URI.create(url + "/rest/v1/recipes?id=eq." + recipeID))
					.header("apikey", key)
					.header("Authorization", "Bearer " + token)
					.header("Prefer", "return=representation")
					.DELETE()
					.build();
			
			HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
			System.out.println("DELETE Status Code: " + response.statusCode());
			System.out.println("Response: " + response.body());
			
			if(response.statusCode() == 200) {
				JsonArray deletedRecipe = JsonParser.parseString(response.body()).getAsJsonArray();
				if(deletedRecipe.size() > 0) {
					System.out.println("Recipe deleted successfully!");
				} else {
					System.out.println("No matching recipe was found and deleted!");
				}
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
		
	}
	
	public static void main(String[] args) {
		//create("Chicken", List.of("Dinner", "Lunch"), 15, 30, "Chicken and spices", "Cook chicken on stove for 15 minutes at low heat", "remember to bring more than just salt and pepper");
		//delete(7);
		//update(7, "Chicken Alfredo", List.of("Dinner", "Pasta"), 30, 30, "Chicken, noodles and spices", "Cook chicken on stove for 15 minutes at low heat. Then cook pasta for its equal time", "remember to bring more than just salt and pepper");
		//The ID of the recipes keep increasing by 1, if created the id becomes 6, to delete it use 6, repeat when a new recipe is created such as new recipe = new id 7, then delete using 7.
		/*List<Recipe> recipes = get();
		System.out.println("Total recipes: " + recipes.size());
		for(Recipe recipe : recipes) {
			System.out.println("ID: " + recipe.getID());
			System.out.println("Name: " + recipe.getRecipeName());
			System.out.println("Category: " + recipe.getCategories());
			System.out.println("Prep Time: " + recipe.getPrepTime());
			System.out.println("Cook Time: " + recipe.getCookTime());
			System.out.println("Ingredients: " + recipe.getIngredients());
			System.out.println("Instructions: " + recipe.getInstructions());
			System.out.println("Notes: " + recipe.getNotes());
			System.out.println("---------------------------");
		}*/
	}

}


