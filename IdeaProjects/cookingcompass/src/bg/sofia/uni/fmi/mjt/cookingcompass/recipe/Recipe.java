package bg.sofia.uni.fmi.mjt.cookingcompass.recipe;

import java.util.Collection;

public record Recipe(String label,
                     Collection<DietLabel> dietLabels,
                     Collection<HealthLabel> healthLabels,
                     Collection<String> ingredientLines,
                     Collection<Cuisine> cuisineType,
                     Collection<MealType> mealType,
                     Collection<DishType> dishType,
                     Float totalWeight) {

}
