package bg.sofia.uni.fmi.mjt.order.server.repository.exception;

public class OrderNotFoundException extends Exception {
    OrderNotFoundException(String message) {
        super(message);
    }

    OrderNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
