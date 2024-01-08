package bg.sofia.uni.fmi.mjt.order.server.tshirt;

public enum Color {
    BLACK("BLACK"),
    WHITE("WHITE"),
    RED("RED"),
    UNKNOWN("UNKNOWN");

    private final String name;

    Color(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public static Color of(String name) {
        Color colorE;
        try {
            colorE = Color.valueOf(name);
        } catch (IllegalArgumentException e) {
            colorE = Color.UNKNOWN;
        }
        return colorE;
    }
}