package bg.sofia.uni.fmi.mjt.authserver.exception;

public class HashException extends RuntimeException {
    public HashException(String message) {
        super(message);
    }

    public HashException(String message, Throwable cause) {
        super(message, cause);
    }
}

