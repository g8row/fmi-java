package bg.sofia.uni.fmi.mjt.order.server.destination;

public enum Destination {
    EUROPE("EUROPE"),
    NORTH_AMERICA("NORTH_AMERICA"),
    AUSTRALIA("AUSTRALIA"),
    UNKNOWN("UNKNOWN");

    private final String name;

    Destination(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public static Destination of(String name) {
        Destination destinationE;
        try {
            destinationE = Destination.valueOf(name);
        } catch (IllegalArgumentException e) {
            destinationE = Destination.UNKNOWN;
        }
        return destinationE;
    }
}