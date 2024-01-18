package bg.sofia.uni.fmi.mjt.cookingcompass.client;

import bg.sofia.uni.fmi.mjt.cookingcompass.recipe.HealthLabel;
import bg.sofia.uni.fmi.mjt.cookingcompass.recipe.MealType;
import bg.sofia.uni.fmi.mjt.cookingcompass.recipe.Recipe;

import java.util.Collection;

public interface RecipeClientAPI {
    RecipeClient setKeywords(String... keyword);

    RecipeClient setMealTypes(MealType... mealTypes);

    RecipeClient setHealthLabels(HealthLabel... healthLabels);

    Collection<Recipe> getRecipes() throws RecipeApiException, NoParametersException;
}
