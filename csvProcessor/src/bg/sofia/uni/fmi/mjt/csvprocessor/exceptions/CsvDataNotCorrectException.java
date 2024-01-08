package bg.sofia.uni.fmi.mjt.csvprocessor.exceptions;

public class CsvDataNotCorrectException extends Exception {
    public CsvDataNotCorrectException(String message) {
        super(message);
    }

    public CsvDataNotCorrectException(String message, Exception cause) {
        super(message, cause);
    }
}
