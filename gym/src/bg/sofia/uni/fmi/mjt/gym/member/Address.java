package bg.sofia.uni.fmi.mjt.gym.member;

public record Address(double longitude, double latitude) {
    public double getDistanceTo(Address other) {
        return Math.sqrt((latitude - other.latitude) * (latitude - other.latitude) +
                (longitude - other.longitude) * (longitude - other.longitude));
    }
}