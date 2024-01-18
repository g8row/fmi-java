package bg.sofia.uni.fmi.mjt.cookingcompass.client;

public class NoParametersException extends RuntimeException {
    public NoParametersException(String message) {
        super(message);
    }

    public NoParametersException(String message, Throwable cause) {
        super(message, cause);
    }
}
