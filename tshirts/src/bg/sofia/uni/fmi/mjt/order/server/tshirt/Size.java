package bg.sofia.uni.fmi.mjt.order.server.tshirt;

public enum Size {
    S("S"),
    M("M"),
    L("L"),
    XL("XL"),
    UNKNOWN("UNKNOWN");

    private final String name;

    Size(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public static Size of(String name) {
        Size sizeE;
        try {
            sizeE = Size.valueOf(name);
        } catch (IllegalArgumentException e) {
            sizeE = Size.UNKNOWN;
        }
        return sizeE;
    }
}