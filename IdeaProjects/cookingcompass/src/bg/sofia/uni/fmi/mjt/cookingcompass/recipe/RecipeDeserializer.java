package bg.sofia.uni.fmi.mjt.cookingcompass.recipe;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;

public class RecipeDeserializer implements JsonDeserializer<Recipe> {
    @Override
    public Recipe deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext)
            throws JsonParseException {
        JsonObject obj = jsonElement.getAsJsonObject();
        String label = obj.get("label").getAsString();
        Collection<DietLabel> dietLabels = obj.getAsJsonArray("dietLabels").asList().stream()
                .map(x -> DietLabel.valueOf(x.getAsString().toUpperCase().replace('-', '_')))
                .toList();
        Collection<HealthLabel> healthLabels = obj.getAsJsonArray("healthLabels").asList().stream()
                .map(x -> HealthLabel.valueOf(x.getAsString().toUpperCase().replaceAll("[ -]", "_")))
                .toList();
        Collection<String> ingredientLines = obj.getAsJsonArray("ingredientLines").asList().stream()
                .map(JsonElement::getAsString)
                .toList();
        Collection<Cuisine> cuisineTypes = obj.getAsJsonArray("cuisineType").asList().stream()
                .map(x -> Cuisine.valueOf(x.getAsString().toUpperCase().replace(' ', '_')))
                .toList();
        Collection<MealType> mealTypes = obj.getAsJsonArray("mealType").asList().stream()
                .map(x -> MealType.fromParameter(x.getAsString()))
                .toList();
        Collection<DishType> dishTypes = new ArrayList<>();
        if (obj.has("dishType")) {
            dishTypes = obj.getAsJsonArray("dishType").asList().stream()
                    .map(x -> DishType.valueOf(x.getAsString().toUpperCase().replace(' ', '_')))
                    .toList();
        }
        Float weight = obj.get("totalWeight").getAsFloat();
        return new Recipe(label, dietLabels, healthLabels, ingredientLines, cuisineTypes, mealTypes, dishTypes, weight);
    }
}

