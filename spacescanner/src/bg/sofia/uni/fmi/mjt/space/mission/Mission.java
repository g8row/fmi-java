package bg.sofia.uni.fmi.mjt.space.mission;

import bg.sofia.uni.fmi.mjt.space.rocket.RocketStatus;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Optional;

public record Mission(String id, String company, String location, LocalDate date, Detail detail,
                      RocketStatus rocketStatus, Optional<Double> cost, MissionStatus missionStatus) {
    public static final int NUM_OF_ATTRIBUTES = 8;

    private static Optional<Double> parseCost(String str) {
        if (str.isBlank()) {
            return Optional.empty();
        } else {
            return Optional.of(Double.parseDouble(str.replaceAll("[^0-9.]", "")));
        }
    }

    public static Mission of(String[] attributes) {
        int i = 0;
        return new Mission(attributes[i++], attributes[i++],
                attributes[i].substring(1, attributes[i++].length() - 1),
                LocalDate.parse(attributes[i].substring(1, attributes[i++].length() - 1),
                        DateTimeFormatter.ofPattern("EEE MMM dd, yyyy", Locale.ENGLISH)),
                Detail.of(attributes[i++]),
                RocketStatus.get(attributes[i++]),
                parseCost(attributes[i++]),
                MissionStatus.get(attributes[i])
        );
    }

    public String country() {
        String[] arr = location.split(",");
        return arr[arr.length - 1].strip();
    }
}
