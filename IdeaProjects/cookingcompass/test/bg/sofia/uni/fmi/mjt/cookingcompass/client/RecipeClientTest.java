package bg.sofia.uni.fmi.mjt.cookingcompass.client;

import bg.sofia.uni.fmi.mjt.cookingcompass.recipe.Cuisine;
import bg.sofia.uni.fmi.mjt.cookingcompass.recipe.DietLabel;
import bg.sofia.uni.fmi.mjt.cookingcompass.recipe.DishType;
import bg.sofia.uni.fmi.mjt.cookingcompass.recipe.HealthLabel;
import bg.sofia.uni.fmi.mjt.cookingcompass.recipe.MealType;
import bg.sofia.uni.fmi.mjt.cookingcompass.recipe.Recipe;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Collection;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

class RecipeClientTest {
    private static final String VALID_APP_ID = "123";
    private static final String VALID_APP_KEY = "123";
    private static final int SUCCESS = 200;
    private RecipeClient recipeClient;
    private HttpClient mockHttpClient;

    @BeforeEach
    public void setUp() {
        mockHttpClient = mock(HttpClient.class);
        recipeClient = new RecipeClient(VALID_APP_ID, VALID_APP_KEY, mockHttpClient);
    }

    @Test
    public void testGetRecipesWithNoParameters() {
        assertThrows(NoParametersException.class, () -> recipeClient.getRecipes());
    }

    @Test
    public void testGetRecipesWithInvalidAppId() {
        // Mocking an invalid API key to simulate an API error
        RecipeClient invalidKeyClient = new RecipeClient("invalid_app_id", "invalid_app_key");
        invalidKeyClient.setKeywords("dkasd");
        assertThrows(RecipeApiException.class, invalidKeyClient::getRecipes);
    }

    @Test
    public void testSetMealTypesWithNoMealTypes() {
        // Ensure that no exception is thrown when setting meal types to null or an empty collection
        assertDoesNotThrow(() -> recipeClient.setMealTypes());
        assertDoesNotThrow(() -> recipeClient.setMealTypes((MealType[]) null));
        assertDoesNotThrow(() -> recipeClient.setMealTypes(new MealType[0]));
    }

    @Test
    public void testSetMealTypesWithMealTypes() throws IOException, InterruptedException {
        // Ensure that no exception is thrown when setting meal types to null or an empty collection
        HttpResponse<String> mockResponse = mock(HttpResponse.class);
        when(mockResponse.statusCode()).thenReturn(200);
        when(mockResponse.body()).thenReturn("{\"from\":1,\"to\":20,\"count\":10000," +
                "\"_links\":{}," +
                "\"hits\":[{\"recipe\":{\"label\":\"Chicken Vesuvio\",\"dietLabels\":[\"Low-Carb\"]," +
                "\"healthLabels\":[\"Mollusk-Free\",\"Kosher\"],\"ingredientLines\":[\"1/2 cup olive oil\"," +
                "\"1 cup frozen peas, thawed\"],\"totalWeight\":2976.850615011728,\"cuisineType\":[\"italian\"]," +
                "\"mealType\":[\"lunch/dinner\"],\"dishType\":[\"main course\"]}}]}");

        when(mockHttpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenReturn(mockResponse);
        recipeClient.setMealTypes(MealType.SNACK);
        assertDoesNotThrow(() -> recipeClient.getRecipes());
    }

    @Test
    public void testSetMealTypesWithHealthLabels() throws IOException, InterruptedException {
        // Ensure that no exception is thrown when setting meal types to null or an empty collection
        HttpResponse<String> mockResponse = mock(HttpResponse.class);
        when(mockResponse.statusCode()).thenReturn(200);
        when(mockResponse.body()).thenReturn("{\"from\":1,\"to\":20,\"count\":10000," +
                "\"_links\":{}," +
                "\"hits\":[{\"recipe\":{\"label\":\"Chicken Vesuvio\",\"dietLabels\":[\"Low-Carb\"]," +
                "\"healthLabels\":[\"Mollusk-Free\",\"Kosher\"],\"ingredientLines\":[\"1/2 cup olive oil\"," +
                "\"1 cup frozen peas, thawed\"],\"totalWeight\":2976.850615011728,\"cuisineType\":[\"italian\"]," +
                "\"mealType\":[\"lunch/dinner\"],\"dishType\":[\"main course\"]}}]}");

        when(mockHttpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenReturn(mockResponse);
        recipeClient.setHealthLabels(HealthLabel.DAIRY_FREE);
        assertDoesNotThrow(() -> recipeClient.getRecipes());
    }

    @Test
    public void testSetHealthLabelsWithNoHealthLabels() {
        // Ensure that no exception is thrown when setting health labels to null or an empty collection
        assertDoesNotThrow(() -> recipeClient.setHealthLabels());
        assertDoesNotThrow(() -> recipeClient.setHealthLabels((HealthLabel[]) null));
        assertDoesNotThrow(() -> recipeClient.setHealthLabels(new HealthLabel[0]));
    }

    @Test
    public void testGetRecipesNoParameters() {
        assertThrows(NoParametersException.class, () -> recipeClient.getRecipes());
    }

    @Test
    public void testGetRecipesWithHttpClientMock() throws RecipeApiException, NoParametersException, IOException, InterruptedException {
        // Mocking behavior of HttpClient
        HttpResponse<String> mockResponse = mock(HttpResponse.class);
        when(mockResponse.statusCode()).thenReturn(200);
        when(mockResponse.body()).thenReturn("{\"from\":1,\"to\":20,\"count\":10000," +
                "\"_links\":{}," +
                "\"hits\":[{\"recipe\":{\"label\":\"Chicken Vesuvio\",\"dietLabels\":[\"Low-Carb\"]," +
                "\"healthLabels\":[\"Mollusk-Free\",\"Kosher\"],\"ingredientLines\":[\"1/2 cup olive oil\"," +
                "\"1 cup frozen peas, thawed\"],\"totalWeight\":2976.850615011728,\"cuisineType\":[\"italian\"]," +
                "\"mealType\":[\"lunch/dinner\"],\"dishType\":[\"main course\"]}}]}");

        when(mockHttpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenReturn(mockResponse);

        Collection<Recipe> recipes = recipeClient.setKeywords("chicken vesuvio  ").getRecipes();

        // Validate that the requestRecipes method was called the expected number of times
        // In this case, we are assuming that it is only called once, modify if needed
        verify(mockHttpClient, times(1)).send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class));
        assertNotNull(recipes);
        assertEquals(1, recipes.size());
        for (Recipe recipe : recipes) {
            assertEquals(recipe.label(), "Chicken Vesuvio");
            assertIterableEquals(recipe.dietLabels(), List.of(DietLabel.LOW_CARB));
            assertIterableEquals(recipe.cuisineType(), List.of(Cuisine.ITALIAN));
            assertIterableEquals(recipe.healthLabels(), List.of(HealthLabel.MOLLUSK_FREE, HealthLabel.KOSHER));
            assertIterableEquals(recipe.ingredientLines(), List.of("1/2 cup olive oil", "1 cup frozen peas, thawed"));
            assertIterableEquals(recipe.mealType(), List.of(MealType.LUNCH_DINNER));
            assertIterableEquals(recipe.dishType(), List.of(DishType.MAIN_COURSE));
        }
        // Add more assertions based on the expected behavior
    }

    @Test
    public void testGetRecipesWithPages() throws RecipeApiException, NoParametersException, IOException, InterruptedException {
        // Mocking behavior of HttpClient
        HttpResponse<String> mockResponse = mock(HttpResponse.class);
        when(mockResponse.statusCode()).thenReturn(200);
        when(mockResponse.body()).thenReturn("{\"from\":1,\"to\":20,\"count\":10000," +
                "\"_links\":{\"next\":{\"href\":\"https://api.edamam.com/api/recipes/v2?type=public&app_id=54024895&app_key=eb77aeb59bd0f5f2e314af78449b282d&q=sdasdasd\"}}," +
                "\"hits\":[{\"recipe\":{\"label\":\"Chicken Vesuvio\",\"dietLabels\":[\"Low-Carb\"]," +
                "\"healthLabels\":[\"Mollusk-Free\",\"Kosher\"],\"ingredientLines\":[\"1/2 cup olive oil\"," +
                "\"1 cup frozen peas, thawed\"],\"totalWeight\":2976.850615011728,\"cuisineType\":[\"italian\"]," +
                "\"mealType\":[\"lunch/dinner\"],\"dishType\":[\"main course\"]}}]}");

        when(mockHttpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenReturn(mockResponse);

        Collection<Recipe> recipes = recipeClient.setKeywords("chicken vesuvio  ").getRecipes();

        // Validate that the requestRecipes method was called the expected number of times
        // In this case, we are assuming that it is only called once, modify if needed
        verify(mockHttpClient, times(2)).send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class));
        // Add more assertions based on the expected behavior
        assertNotNull(recipes);
        assertEquals(2, recipes.size());
        for (Recipe recipe : recipes) {
            assertEquals(recipe.label(), "Chicken Vesuvio");
            assertIterableEquals(recipe.dietLabels(), List.of(DietLabel.LOW_CARB));
            assertIterableEquals(recipe.cuisineType(), List.of(Cuisine.ITALIAN));
            assertIterableEquals(recipe.healthLabels(), List.of(HealthLabel.MOLLUSK_FREE, HealthLabel.KOSHER));
            assertIterableEquals(recipe.ingredientLines(), List.of("1/2 cup olive oil", "1 cup frozen peas, thawed"));
            assertIterableEquals(recipe.mealType(), List.of(MealType.LUNCH_DINNER));
            assertIterableEquals(recipe.dishType(), List.of(DishType.MAIN_COURSE));
        }
    }

    @Test
    public void testGetRecipesWithClientError() throws RecipeApiException, NoParametersException, IOException, InterruptedException {
        // Mocking behavior of HttpClient to simulate an API error
        when(mockHttpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenThrow(new IOException("HTTP request failed"));
        recipeClient.setKeywords("dasdas");
        assertThrows(RecipeApiException.class, () -> recipeClient.getRecipes());
    }

    @Test
    public void testGetRecipesWithApiError() throws RecipeApiException, NoParametersException, IOException, InterruptedException {
        // Mocking behavior of HttpClient to simulate an API error
        HttpResponse<String> mockResponse = mock(HttpResponse.class);
        when(mockResponse.statusCode()).thenReturn(500);
        when(mockResponse.body()).thenReturn("{ \"status\": \"error\", \"message\": \"Quota Error\" }");
        when(mockHttpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenReturn(mockResponse);
        recipeClient.setKeywords("chicken");
        assertThrows(RecipeApiException.class, () -> recipeClient.getRecipes());
    }

    @Test
    public void testSetKeywordsWithNullKeywords() {
        recipeClient.setKeywords((String) null);

    }

    @Test
    public void testSetMealTypesWithNullMealTypes() {
        recipeClient.setMealTypes((MealType[]) null);

    }

    @Test
    public void testSetHealthLabelsWithNullHealthLabels() {
        recipeClient.setHealthLabels((HealthLabel[]) null);


    }


}