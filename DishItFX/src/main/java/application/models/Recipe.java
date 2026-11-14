package application.models;

import java.util.List;
import com.google.gson.annotations.SerializedName;

public class Recipe {
	private long id;
	
	@SerializedName("recipe_name")
	private String recipeName;
	
	private String userID;
	private List<String> categories;
	
	@SerializedName("prep_time")
	private int prepTime;
	
	@SerializedName("cook_time")
	private int cookTime;
	
	private String ingredients;
	private String instructions;
	private String notes;
	
	public Recipe() {}
	
	public Recipe(String recipeName, List<String> categories, int prepTime, int cookTime, String ingredients, String instructions, String notes) {
		this.recipeName = recipeName;
		this.categories = categories;
		this.prepTime = prepTime;
		this.cookTime = cookTime;
		this.ingredients = ingredients;
		this.instructions = instructions;
		this.notes = notes;
	}
	
	public long getID() {
		return id;
	}
	public String getRecipeName() {
		return recipeName;
	}
	public String getUserID() {
		return userID;
	}
	public List<String> getCategories(){
		return categories;
	}
	public int getPrepTime() {
		return prepTime;
	}
	public int getCookTime() {
		return cookTime;
	}
	public String getIngredients() {
		return ingredients;
	}
	public String getInstructions() {
		return instructions;
	}
	public String getNotes() {
		return notes;
	}

}
