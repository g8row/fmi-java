package bg.sofia.uni.fmi.mjt.space.mission;

public record Detail(String rocketName, String payload) {
    public static Detail of(String str) {
        String[] arr = str.split("\\|");
        return  new Detail(arr[0].strip(), arr[1].strip());
    }
}
