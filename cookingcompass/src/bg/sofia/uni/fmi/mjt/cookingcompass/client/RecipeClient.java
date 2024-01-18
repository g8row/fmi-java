package bg.sofia.uni.fmi.mjt.cookingcompass.client;

import bg.sofia.uni.fmi.mjt.cookingcompass.recipe.HealthLabel;
import bg.sofia.uni.fmi.mjt.cookingcompass.recipe.MealType;
import bg.sofia.uni.fmi.mjt.cookingcompass.recipe.Recipe;
import bg.sofia.uni.fmi.mjt.cookingcompass.recipe.RecipeDeserializer;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Objects;

public class RecipeClient implements RecipeClientAPI {
    private static final String URL = "https://api.edamam.com/api/recipes/v2?type=public";
    private final HttpClient client;
    private final GsonBuilder gsonBuilder;
    private final Gson gson;
    private static final int PAGES = 2;
    private static final int SUCCESS = 200;
    private final String appId;
    private final String appKey;
    private Collection<String> keywords;
    private Collection<MealType> mealTypes;
    private Collection<HealthLabel> healthLabels;

    public RecipeClient(String appId, String appKey) {
        this.appId = appId;
        this.appKey = appKey;
        client = HttpClient.newBuilder().build();
        gsonBuilder = new GsonBuilder()
                .registerTypeAdapter(Recipe.class, new RecipeDeserializer());
        gson = gsonBuilder.create();
    }

    public RecipeClient(String appId, String appKey, HttpClient client) {
        this.appId = appId;
        this.appKey = appKey;
        this.client = client;
        gsonBuilder = new GsonBuilder()
                .registerTypeAdapter(Recipe.class, new RecipeDeserializer());
        gson = gsonBuilder.create();
    }

    public RecipeClient setKeywords(String... keywords) {
        if (keywords == null) {
            this.keywords = null;
        } else {
            this.keywords = Arrays.stream(keywords).filter(Objects::nonNull).filter(x -> !x.isBlank()).toList();
        }
        return this;
    }

    public RecipeClient setMealTypes(MealType... mealTypes) {
        if (mealTypes == null) {
            this.mealTypes = null;
        } else {
            this.mealTypes = Arrays.stream(mealTypes).filter(Objects::nonNull).toList();
        }
        return this;
    }

    public RecipeClient setHealthLabels(HealthLabel... healthLabels) {
        if (healthLabels == null) {
            this.healthLabels = null;
        } else {
            this.healthLabels = Arrays.stream(healthLabels).filter(Objects::nonNull).toList();
        }
        return this;
    }

    private URI createUri() {
        if ((keywords == null || keywords.isEmpty()) && (mealTypes == null || mealTypes.isEmpty())
                && (healthLabels == null || healthLabels.isEmpty())) {
            throw new NoParametersException("keyword is required if no other parameters are given");
        }
        StringBuilder str = new StringBuilder(URL)
                .append("&app_id=").append(appId)
                .append("&app_key=").append(appKey);
        if (keywords != null && !keywords.isEmpty()) {
            str.append("&q=");
            str.append(
                    keywords.parallelStream()
                            .map(x -> x.replaceAll(" ", "%20"))
                            .collect(StringBuilder::new, StringBuilder::append,
                                    (x, y) -> x.append("%2c").append(y)).toString());
        }
        if (mealTypes != null && !mealTypes.isEmpty()) {
            for (MealType mealType : mealTypes) {
                str.append("&mealType=").append(mealType.getParameter());
            }
        }
        if (healthLabels != null && !healthLabels.isEmpty()) {
            for (HealthLabel healthLabel : healthLabels) {
                str.append("&health=").append(healthLabel.getParameter());
            }
        }
        System.out.println(str);
        return URI.create(str.toString());
    }

    private Collection<Recipe> parseHits(JsonElement hits) {
        return hits.getAsJsonArray()
                .asList()
                .stream()
                .map(x -> gson.fromJson(x.getAsJsonObject().get("recipe"), Recipe.class))
                .toList();
    }

    private String parseError(JsonElement error) {
        JsonObject jsonObject = error.getAsJsonObject();
        return "Error code: " +
                jsonObject.get("status").getAsString() +
                ", Message is: " +
                jsonObject.get("message").getAsString();
    }

    private Collection<Recipe> requestRecipes(URI uri, int pages) throws RecipeApiException, NoParametersException {
        HttpRequest request = HttpRequest.newBuilder().uri(uri).build();
        Collection<Recipe> recipes = new ArrayList<>();
        if (pages != 0) {
            try {
                HttpResponse<String> resp = client.send(request, HttpResponse.BodyHandlers.ofString());
                if (resp.statusCode() == SUCCESS) {
                    JsonObject response = JsonParser.parseString(resp.body()).getAsJsonObject();
                    recipes.addAll(parseHits(response.get("hits")));
                    URI next;
                    if (response.has("_links") && response.getAsJsonObject("_links").has("next")) {
                        next = URI.create(response.getAsJsonObject("_links")
                                .getAsJsonObject("next").get("href").getAsString());
                        recipes.addAll(requestRecipes(next, pages - 1));
                    }
                } else {
                    JsonElement response = JsonParser.parseString(resp.body());
                    throw new RecipeApiException(parseError(response));
                }
            } catch (IOException | InterruptedException e) {
                throw new RecipeApiException("API error. ", e);
            }
        }
        return recipes;
    }

    public Collection<Recipe> getRecipes() {
        return requestRecipes(createUri(), PAGES);
    }
}
