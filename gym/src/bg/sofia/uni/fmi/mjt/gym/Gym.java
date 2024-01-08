package bg.sofia.uni.fmi.mjt.gym;

import bg.sofia.uni.fmi.mjt.gym.member.Address;
import bg.sofia.uni.fmi.mjt.gym.member.GymMember;
import bg.sofia.uni.fmi.mjt.gym.workout.Exercise;

import java.time.DayOfWeek;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

public class Gym implements GymAPI {

    int capacity;
    Address address;

    SortedSet<GymMember> members;

    public Gym(int capacity, Address address) {
        this.capacity = capacity;
        this.address = address;
        members = new TreeSet<>();
    }

    /**
     * Returns an unmodifiable copy of all members of the gym.
     * If there are no members, return an empty collection.
     */
    @Override
    public SortedSet<GymMember> getMembers() {
        return Collections.unmodifiableSortedSet(members);
    }

    /**
     * Returns an unmodifiable copy of all members of the gym sorted by their name in lexicographic order.
     * If there are no members, return an empty collection.
     */
    @Override
    public SortedSet<GymMember> getMembersSortedByName() {
        SortedSet<GymMember> result = new TreeSet<>(Comparator.comparing(GymMember::getName));
        result.addAll(members);
        return result;
    }

    /**
     * Returns an unmodifiable copy of all members of the gym sorted by their proximity to the
     * gym in increasing order. If there are no members, return an empty collection.
     */
    @Override
    public SortedSet<GymMember> getMembersSortedByProximityToGym() {
        SortedSet<GymMember> result = new TreeSet<>(new Comparator<>() {
            @Override
            public int compare(GymMember o1, GymMember o2) {
                return (int) (o1.getAddress().getDistanceTo(address) - o2.getAddress().getDistanceTo(address));
            }
        });
        result.addAll(members);
        return result;
    }

    /**
     * Adds a single member to the gym.
     *
     * @param member the member to add
     * @throws GymCapacityExceededException - if the gym is full
     * @throws IllegalArgumentException     if member is null
     */
    @Override
    public void addMember(GymMember member) throws GymCapacityExceededException {
        if (member == null) {
            throw new IllegalArgumentException("member is null");
        }
        if (members.size() >= capacity) {
            throw new GymCapacityExceededException("gym is full");
        }
        members.add(member);
    }

    /**
     * Adds a group of members to the gym. If the gym does not have the capacity to accept all the
     * new members then no members are added
     *
     * @param members the members to add
     * @throws GymCapacityExceededException if the gym is full
     * @throws IllegalArgumentException     if members is null or empty
     */
    @Override
    public void addMembers(Collection<GymMember> members) throws GymCapacityExceededException {
        if (members == null || members.isEmpty()) {
            throw new IllegalArgumentException("members is null or empty");
        }
        if (members.size() >= capacity) {
            throw new GymCapacityExceededException("gym is full");
        }
        for (GymMember member : members) {
            addMember(member);
        }
    }

    /**
     * Checks if a given member is member of the gym.
     *
     * @param member - the member
     * @throws IllegalArgumentException if member is null
     */
    @Override
    public boolean isMember(GymMember member) {
        if (member == null) {
            throw new IllegalArgumentException("member is null");
        }
        return members.contains(member);
    }

    /**
     * Checks if an Exercise is trained on a given day.
     *
     * @param exerciseName - the name of the Exercise
     * @param day          - the day for which the check is done
     * @throws IllegalArgumentException if day is null or if exerciseName is null or empty
     */
    @Override
    public boolean isExerciseTrainedOnDay(String exerciseName, DayOfWeek day) {
        if (day == null || exerciseName == null || exerciseName.isEmpty()) {
            throw new IllegalArgumentException("day is null or  exerciseName is null or empty");
        }
        for (GymMember member : members) {
            if (member.getTrainingProgram().get(day) == null) {
                continue;
            }
            for (Exercise exercise : member.getTrainingProgram().get(day).exercises()) {
                if (exercise.name().equals(exerciseName)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Returns an unmodifiable Map representing each day and the names of the members that do this exercise on it.
     *
     * @param exerciseName - the name of the exercise being done
     * @throws IllegalArgumentException if exerciseName is null or empty
     */
    @Override
    public Map<DayOfWeek, List<String>> getDailyListOfMembersForExercise(String exerciseName) {
        Map<DayOfWeek, List<String>> dailyList = new HashMap<>(DayOfWeek.values().length);
        if (exerciseName == null || exerciseName.isEmpty()) {
            throw new IllegalArgumentException("exerciseName is null or empty");
        }
        for (DayOfWeek day : DayOfWeek.values()) {
            List<String> filteredMembers = new ArrayList<>();
            for (GymMember member : members) {

                if (member.getTrainingProgram().get(day) == null) {
                    continue;
                }
                for (Exercise exercise : member.getTrainingProgram().get(day).exercises()) {
                    if (exercise.name().equals(exerciseName)) {
                        filteredMembers.add(member.getName());
                    }
                }
            }
            if (!filteredMembers.isEmpty()) {
                dailyList.put(day, filteredMembers);
            }
        }
        return dailyList;
    }
}
