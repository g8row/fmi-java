package bg.sofia.uni.fmi.mjt.space.rocket;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public enum RocketStatus {
    STATUS_RETIRED("StatusRetired"),
    STATUS_ACTIVE("StatusActive");

    private final String value;

    RocketStatus(String value) {
        this.value = value;
    }

    public String toString() {
        return value;
    }

    private static final Map<String, RocketStatus> ENUM_MAP;

    static {
        Map<String, RocketStatus> map = new HashMap<>();
        for (RocketStatus instance : RocketStatus.values()) {
            map.put(instance.toString(), instance);
        }
        ENUM_MAP = Collections.unmodifiableMap(map);
    }

    public static RocketStatus get(String name) {
        return ENUM_MAP.get(name);
    }
}
