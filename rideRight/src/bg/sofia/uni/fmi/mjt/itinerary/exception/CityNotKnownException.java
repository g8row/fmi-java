package bg.sofia.uni.fmi.mjt.itinerary.exception;

public class CityNotKnownException extends Exception {

    public CityNotKnownException(String message) {
        super(message);
    }

    CityNotKnownException(String message, Throwable cause) {
        super(message, cause);
    }
}
