import bg.sofia.uni.fmi.mjt.cookingcompass.client.RecipeClient;
import bg.sofia.uni.fmi.mjt.cookingcompass.recipe.HealthLabel;
import bg.sofia.uni.fmi.mjt.cookingcompass.recipe.MealType;
import bg.sofia.uni.fmi.mjt.cookingcompass.recipe.Recipe;
import bg.sofia.uni.fmi.mjt.cookingcompass.recipe.RecipeDeserializer;
import com.google.gson.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Main {
    public static void main(String[] args) {
        //pagination - inside "next"
        //mealtype,health
        String json;
        {
            try {
                json = Files.readString(Path.of("/home/g8row/IdeaProjects/cookingcompass/src/bg/sofia/uni/fmi/mjt/cookingcompass/recipe.json"));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        //GsonBuilder gsonB = new GsonBuilder().registerTypeAdapter(Recipe.class, new RecipeDeserializer());
        //Gson gson = gsonB.create();
        //JsonObject response = JsonParser.parseString(json).getAsJsonObject();
        //Recipe rocket = gson.fromJson(json, Recipe.class);
        RecipeClient client = new RecipeClient("54024895", "eb77aeb59bd0f5f2e314af78449b282d");
        //RecipeClient client = new RecipeClient("54895", "eb7759bd0f5f2e314af78449b282d");

        Collection<Recipe> recipes = client
                .setKeywords("chicken", "beef", "cum")
                .setMealTypes(MealType.SNACK)
                .setHealthLabels(HealthLabel.NO_OIL_ADDED)
                .getRecipes();
        recipes = client
                    .setKeywords("chicken")
                    .setMealTypes(MealType.SNACK)
                    .setHealthLabels(HealthLabel.NO_OIL_ADDED)
                    .getRecipes();

        recipes = client
                .setKeywords("chicken", "beef")
                .setMealTypes(MealType.LUNCH_DINNER)
                .setHealthLabels(HealthLabel.PESCATARIAN, HealthLabel.DAIRY_FREE)
                .getRecipes();

        //Collection<Recipe> recipes = response.get("hits").getAsJsonArray().asList().stream().map(x -> gson.fromJson(x.getAsJsonObject().get("recipe"),Recipe.class)).toList();

        System.out.println("meow");

    }
}