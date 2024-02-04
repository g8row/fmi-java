package bg.sofia.uni.fmi.mjt.authserver.response;

public record Response(boolean success, String message) {
    @Override
    public String toString() {
        return ((success) ? "Success" : "Failure") + ": " + message;
    }
}
