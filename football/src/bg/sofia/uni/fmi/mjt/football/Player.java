package bg.sofia.uni.fmi.mjt.football;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;

public record Player(String name, String fullName, LocalDate birthDate, int age, double heightCm, double weightKg,
                     List<Position> positions, String nationality, int overallRating, int potential, long valueEuro,
                     long wageEuro, Foot preferredFoot) {

    static final int NUM_OF_ATTRIBUTES = 13;

    public static Player of(String line) {
        String[] values = line.split("\\Q" + ';' + "\\E");
        int i = 0;
        return new Player(values[i++],
                values[i++],
                LocalDate.parse(values[i++], DateTimeFormatter.ofPattern("M/d/yyyy")),
                Integer.parseInt(values[i++]),
                Double.parseDouble(values[i++]),
                Double.parseDouble(values[i++]),
                Arrays.stream(values[i++].split("\\Q" + ',' + "\\E")).map(Position::valueOf).toList(),
                values[i++],
                Integer.parseInt(values[i++]),
                Integer.parseInt(values[i++]),
                Long.parseLong(values[i++]),
                Long.parseLong(values[i++]),
                Foot.valueOf(values[i++].toUpperCase())
        );
    }
}
