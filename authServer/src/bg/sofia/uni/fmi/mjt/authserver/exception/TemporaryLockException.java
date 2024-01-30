package bg.sofia.uni.fmi.mjt.authserver.exception;

public class TemporaryLockException extends RuntimeException{
    public TemporaryLockException(String message) {
        super(message);
    }

    public TemporaryLockException(String message, Throwable cause) {
        super(message, cause);
    }
}
