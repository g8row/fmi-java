package bg.sofia.uni.fmi.mjt.cookingcompass.client;

public class RecipeApiException extends RuntimeException {
    public RecipeApiException(String message) {
        super(message);
    }

    public RecipeApiException(String message, Throwable cause) {
        super(message, cause);
    }
}
