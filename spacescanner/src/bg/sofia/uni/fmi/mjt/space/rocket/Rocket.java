package bg.sofia.uni.fmi.mjt.space.rocket;

import java.io.Serializable;
import java.util.Optional;

public record Rocket(String id, String name, Optional<String> wiki, Optional<Double> height) implements Serializable {
    public static final int NUM_OF_ATTRIBUTES = 4;
    private static Optional<Double> parseCost(String str) {
        if (str.isBlank()) {
            return Optional.empty();
        } else {
            return Optional.of(Double.parseDouble(str.replaceAll("[^0-9.]", "")));
        }
    }

    private static Optional<String> parseWiki(String str) {
        if (str.isBlank()) {
            return Optional.empty();
        } else {
            return Optional.of(str);
        }
    }

    public static Rocket of(String[] arr) {
        int i = 0;
        return new Rocket(arr[i++], arr[i++], parseWiki(arr[i++]), parseCost(arr[i]));
    }

}
