package bg.sofia.uni.fmi.mjt.gym.member;

import bg.sofia.uni.fmi.mjt.gym.workout.Exercise;
import bg.sofia.uni.fmi.mjt.gym.workout.Workout;

import java.time.DayOfWeek;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Member implements GymMember, Comparable<GymMember> {
    Address address;
    String name;
    int age;
    String personalIdNumber;
    Gender gender;

    Map<DayOfWeek, Workout> trainingProgram;

    public Member(Address address, String name, int age, String personalIdNumber, Gender gender) {
        this.address = address;
        this.name = name;
        this.age = age;
        this.personalIdNumber = personalIdNumber;
        this.gender = gender;
        trainingProgram = new HashMap<>(DayOfWeek.values().length);
    }

    /**
     * Returns the member's name.
     */
    @Override
    public String getName() {
        return name;
    }

    /**
     * Returns the member's age.
     */
    @Override
    public int getAge() {
        return age;
    }

    /**
     * Returns the member's id number.
     */
    @Override
    public String getPersonalIdNumber() {
        return personalIdNumber;
    }

    /**
     * Returns the member's gender.
     */
    @Override
    public Gender getGender() {
        return gender;
    }

    /**
     * Returns the member's address.
     */
    @Override
    public Address getAddress() {
        return address;
    }

    /**
     * Returns an immutable Map representing the workout a member does on the DayOfWeek.
     */
    @Override
    public Map<DayOfWeek, Workout> getTrainingProgram() {
        return trainingProgram;
    }

    /**
     * Sets the workout for a specific day.
     *
     * @param day     - DayOfWeek on which the workout will be trained
     * @param workout - the workout to be trained
     * @throws IllegalArgumentException if day or workout is null.
     */
    @Override
    public void setWorkout(DayOfWeek day, Workout workout) {
        if (day == null || workout == null) {
            throw new IllegalArgumentException("day or workout is null");
        }
        trainingProgram.put(day, workout);
    }

    /**
     * Returns a collection of days in undefined order on which the workout finishes with a specific exercise.
     *
     * @param exerciseName - the name of the exercise.
     * @throws IllegalArgumentException if exerciseName is null or empty.
     */
    @Override
    public Collection<DayOfWeek> getDaysFinishingWith(String exerciseName) {
        if (exerciseName == null || exerciseName.isEmpty()) {
            throw new IllegalArgumentException("exerciseName is null or empty");
        }
        List<DayOfWeek> result = new ArrayList<>(DayOfWeek.values().length);
        for (DayOfWeek day : DayOfWeek.values()) {
            if (trainingProgram.get(day) == null) {
                continue;
            }
            if (trainingProgram.get(day).exercises().isEmpty()) {
                continue;
            }
            if (trainingProgram.get(day).exercises().getLast().name().equals(exerciseName)) {
                result.add(day);
            }
        }
        return result;
    }

    /**
     * Adds an Exercise to the Workout trained on the given day. If there is no workout set for the day,
     * the day is considered a day off and no exercise can be added.
     * W
     *
     * @param day      - DayOfWeek to train the exercise.
     * @param exercise - the trained Exercise.
     * @throws DayOffException          if the Workout on this day is null.
     * @throws IllegalArgumentException if day or exercise is null
     */
    @Override
    public void addExercise(DayOfWeek day, Exercise exercise) {
        if (day == null || exercise == null) {
            throw new IllegalArgumentException("day or exercise is null");
        }
        if (trainingProgram.get(day) == null) {
            throw new DayOffException("the Workout on this day is null");
        }
        trainingProgram.get(day).exercises().add(exercise);
    }

    /**
     * Adds Exercises to the Workout trained on the given day. If there is no workout set for the day, the day is
     * considered a day off and no exercise can be added.
     *
     * @param day       - DayOfWeek to train the exercise.
     * @param exercises - list of the trained Exercises
     * @throws DayOffException          if the Workout on this day is null or the exercises list is empty.
     * @throws IllegalArgumentException if day is null or exercises is null or empty
     */
    @Override
    public void addExercises(DayOfWeek day, List<Exercise> exercises) {
        if (day == null || exercises == null || exercises.isEmpty()) {
            throw new IllegalArgumentException("day is null or exercises is null or empty");
        }
        for (Exercise exercise : exercises) {
            if (exercise != null) {
                addExercise(day, exercise);
            }
        }
    }

    @Override
    public int compareTo(GymMember o) {
        return personalIdNumber.compareTo(o.getPersonalIdNumber());
    }

    @Override
    public boolean equals(Object other) {
        return other.getClass() == Member.class && ((Member) other).getPersonalIdNumber().equals(personalIdNumber);
    }

    @Override
    public int hashCode() {
        return personalIdNumber.hashCode();
    }
}
