package bg.sofia.uni.fmi.mjt.space.mission;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public enum MissionStatus {
    SUCCESS("Success"),
    FAILURE("Failure"),
    PARTIAL_FAILURE("Partial Failure"),
    PRELAUNCH_FAILURE("Prelaunch Failure");

    private final String value;

    MissionStatus(String value) {
        this.value = value;
    }

    public String toString() {
        return value;
    }

    private static final Map<String, MissionStatus> ENUM_MAP;

    static {
        Map<String, MissionStatus> map = new HashMap<String, MissionStatus>();
        for (MissionStatus instance : MissionStatus.values()) {
            map.put(instance.toString(), instance);
        }
        ENUM_MAP = Collections.unmodifiableMap(map);
    }

    public static MissionStatus get(String name) {
        return ENUM_MAP.get(name);
    }
}