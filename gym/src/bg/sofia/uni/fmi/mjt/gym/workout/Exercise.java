package bg.sofia.uni.fmi.mjt.gym.workout;

public record Exercise(String name, int sets, int repetitions) {
    @Override
    public boolean equals(Object o) {
        return o.getClass() == Exercise.class && ((Exercise) o).name.equals(name);
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }
}
