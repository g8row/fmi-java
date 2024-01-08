package bg.sofia.uni.fmi.mjt.space;

import bg.sofia.uni.fmi.mjt.space.algorithm.Rijndael;
import bg.sofia.uni.fmi.mjt.space.mission.MaxCostComparator;
import bg.sofia.uni.fmi.mjt.space.mission.MinCostComparator;
import bg.sofia.uni.fmi.mjt.space.mission.Mission;
import bg.sofia.uni.fmi.mjt.space.mission.MissionStatus;
import bg.sofia.uni.fmi.mjt.space.rocket.MaxHeightComparator;
import bg.sofia.uni.fmi.mjt.space.rocket.Rocket;
import bg.sofia.uni.fmi.mjt.space.rocket.RocketStatus;
import bg.sofia.uni.fmi.mjt.space.exception.TimeFrameMismatchException;
import bg.sofia.uni.fmi.mjt.space.exception.CipherException;

import javax.crypto.SecretKey;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BinaryOperator;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class MJTSpaceScanner implements SpaceScannerAPI {
    Collection<Mission> missions;
    Collection<Rocket> rockets;
    SecretKey secretKey;

    public MJTSpaceScanner(Reader missionsReader, Reader rocketsReader, SecretKey secretKey) {
        try (BufferedReader mReader = new BufferedReader(missionsReader);
             BufferedReader rReader = new BufferedReader(rocketsReader)) {
            missions = mReader
                    .lines()
                    .skip(1)
                    .map(x -> Mission.of(x.split(",(?=([^\"]*\"[^\"]*\")*[^\"]*$)"))).toList();
            rockets = rReader
                    .lines()
                    .skip(1)
                    .map(x -> Rocket.of(x.split(",(?=([^\"]*\"[^\"]*\")*[^\"]*$)"))).toList();
        } catch (IOException e) {
            throw new RuntimeException("invalid data");
        }
        this.secretKey = secretKey;
    }

    /**
     * Returns all missions in the dataset.
     * If there are no missions, return an empty collection.
     */
    @Override
    public Collection<Mission> getAllMissions() {
        return List.copyOf(missions);
    }

    /**
     * Returns all missions in the dataset with a given status.
     * If there are no missions, return an empty collection.
     *
     * @param missionStatus the status of the missions
     * @throws IllegalArgumentException if missionStatus is null
     */

    @Override
    public Collection<Mission> getAllMissions(MissionStatus missionStatus) {
        if (missionStatus == null) {
            throw new IllegalArgumentException("missionStatus is null");
        }
        return missions.stream().filter(x -> x.missionStatus() == missionStatus).toList();
    }

    /**
     * Returns the company with the most successful missions in a given time period.
     * If there are no missions, return an empty string.
     *
     * @param from the inclusive beginning of the time frame
     * @param to   the inclusive end of the time frame
     * @throws IllegalArgumentException   if from or to is null
     * @throws TimeFrameMismatchException if to is before from
     */
    @Override
    public String getCompanyWithMostSuccessfulMissions(LocalDate from, LocalDate to) {
        if (from == null || to == null) {
            throw new IllegalArgumentException("from or to is null");
        }
        if (to.isBefore(from)) {
            throw new TimeFrameMismatchException("to is before from");
        }
        return missions
                .stream()
                .filter(x -> x.date().isAfter(from) && x.date().isBefore(to))
                .collect(Collectors.toMap(Mission::company, x -> 1, (x, y) -> x + 1))
                .entrySet()
                .stream()
                .max(Map.Entry.comparingByValue())
                .orElse(new AbstractMap.SimpleEntry<>("", 0))
                .getKey();
    }

    /**
     * Groups missions by country.
     * If there are no missions, return an empty map.
     */
    @Override
    public Map<String, Collection<Mission>> getMissionsPerCountry() {
        return missions
                .stream()
                .collect(Collectors.toMap(Mission::country,
                        x -> {
                            ArrayList<Mission> arr = new ArrayList<>();
                            arr.add(x);
                            return arr;
                        },
                        (x, y) -> {
                            x.addAll(y);
                            return x;
                        }));
    }

    /**
     * Returns the top N least expensive missions, ordered from cheapest to more expensive.
     * If there are no missions, return an empty list.
     *
     * @param n             the number of missions to be returned
     * @param missionStatus the status of the missions
     * @param rocketStatus  the status of the rockets
     * @throws IllegalArgumentException if n is less than ot equal to 0, missionStatus or rocketStatus is null
     */
    @Override
    public List<Mission> getTopNLeastExpensiveMissions(int n, MissionStatus missionStatus, RocketStatus rocketStatus) {
        if (n <= 0 || missionStatus == null || rocketStatus == null) {
            throw new IllegalArgumentException("n is less than ot equal to 0, missionStatus or rocketStatus is null");
        }
        return missions
                .stream()
                .filter(x -> x.missionStatus() == missionStatus && x.rocketStatus() == rocketStatus)
                .sorted(new MinCostComparator())
                .limit(n)
                .toList();
    }

    /**
     * Returns the most desired location for missions per company.
     * If there are no missions, return an empty map.
     */
    @Override
    public Map<String, String> getMostDesiredLocationForMissionsPerCompany() {
        return missions
                .stream()
                .collect(Collectors.toMap(Mission::company,
                        x -> {
                            ArrayList<String> arr = new ArrayList<>();
                            arr.add(x.location());
                            return arr;
                        },
                        (x, y) -> {
                            x.addAll(y);
                            return x;
                        }))
                .entrySet()
                .stream()
                .collect(Collectors.toMap(Map.Entry::getKey,
                        x -> x.getValue()
                                .stream()
                                .reduce(BinaryOperator.maxBy(Comparator
                                        .comparingInt(o -> Collections.frequency(x.getValue(), o))))
                                .orElse("")
                        ));
    }

    /**
     * Returns the company mapped to its location with most successful missions.
     * If there are no missions, return an empty map.
     *
     * @param from the inclusive beginning of the time frame
     * @param to   the inclusive end of the time frame
     * @throws IllegalArgumentException   if from or to is null
     * @throws TimeFrameMismatchException if to is before from
     */
    @Override
    public Map<String, String> getLocationWithMostSuccessfulMissionsPerCompany(LocalDate from, LocalDate to) {
        if (from == null || to == null) {
            throw new IllegalArgumentException("from or to is null");
        }
        if (to.isBefore(from)) {
            throw new TimeFrameMismatchException("to is before from");
        }
        Collection<Mission> filteredMissions = missions
                .stream()
                .filter(x -> x.date().isAfter(from)
                        && x.date().isBefore(to)
                        && x.missionStatus() == MissionStatus.SUCCESS)
                        .toList();
        Collection<String> companies = missions
                .stream()
                .collect(HashSet::new, (x, y) -> x.add(y.company()), HashSet::addAll);
        return companies
                .stream()
                .collect(Collectors.toMap(x -> x, x -> {
                    Collection<String> locations = filteredMissions
                                    .stream()
                                    .filter(y -> y.company().equals(x))
                                    .collect(ArrayList::new, (a, b) -> a.add(b.location()), ArrayList::addAll);
                    return locations
                                    .stream()
                                    .reduce(BinaryOperator.maxBy(Comparator
                                            .comparingInt(o -> Collections.frequency(locations, o))))
                                    .orElse("");
                }));
    }

    /**
     * Returns all rockets in the dataset.
     * If there are no rockets, return an empty collection.
     */
    @Override
    public Collection<Rocket> getAllRockets() {
        return List.copyOf(rockets);
    }

    /**
     * Returns the top N tallest rockets, in decreasing order.
     * If there are no rockets, return an empty list.
     *
     * @param n the number of rockets to be returned
     * @throws IllegalArgumentException if n is less than ot equal to 0
     */
    @Override
    public List<Rocket> getTopNTallestRockets(int n) {
        if (n <= 0) {
            throw new IllegalArgumentException("n is less than ot equal to 0");
        }
        return rockets
                .stream()
                .sorted(new MaxHeightComparator())
                .limit(n)
                .toList();
    }

    /**
     * Returns a mapping of rockets (by name) to their respective wiki page (if present).
     * If there are no rockets, return an empty map.
     */
    @Override
    public Map<String, Optional<String>> getWikiPageForRocket() {
        return rockets.stream().collect(Collectors.toMap(Rocket::name, Rocket::wiki));
    }

    /**
     * Returns the wiki pages for the rockets used in the N most expensive missions.
     * If there are no missions, return an empty list.
     *
     * @param n             the number of missions to be returned
     * @param missionStatus the status of the missions
     * @param rocketStatus  the status of the rockets
     * @throws IllegalArgumentException if n is less than ot equal to 0, or missionStatus or rocketStatus is null
     */
    @Override
    public List<String> getWikiPagesForRocketsUsedInMostExpensiveMissions(int n, MissionStatus missionStatus,
                                                                          RocketStatus rocketStatus) {
        if (n <= 0 || missionStatus == null || rocketStatus == null) {
            throw new IllegalArgumentException("n is less than ot equal to 0, missionStatus or rocketStatus is null");
        }
        return missions
                .stream()
                .filter(x -> x.missionStatus() == missionStatus && x.rocketStatus() == rocketStatus)
                .sorted(new MaxCostComparator())
                .limit(n)
                .collect(ArrayList::new,
                        (x, y) -> x.add(rockets
                                .stream()
                                .filter(r -> r.name().equals(y.detail().rocketName()))
                                .findFirst()
                                .orElse(new Rocket("", "", Optional.of(""), Optional.of(0.0)))
                                .wiki()
                                .orElse("")),
                        ArrayList::addAll);
    }

    private double getReliability(Rocket rocket, Stream<Mission> missionStream) {
        Collection<Mission> rocketMissions = missionStream
                .filter(x -> x.detail().rocketName().equals(rocket.name()))
                .toList();
        if (rocketMissions.isEmpty()) {
            return 0;
        }
        long succ = rocketMissions.stream().filter(x -> x.missionStatus() == MissionStatus.SUCCESS).count();
        return (double) (2 * succ
                + rocketMissions.size() - succ)
                / rocketMissions.size();
    }

    /**
     * Saves the name of the most reliable rocket in a given time period in an encrypted format.
     *
     * @param outputStream the output stream where the encrypted result is written into
     * @param from         the inclusive beginning of the time frame
     * @param to           the inclusive end of the time frame
     * @throws IllegalArgumentException   if outputStream, from or to is null
     * @throws CipherException            if the encrypt/decrypt operation cannot be completed successfully
     * @throws TimeFrameMismatchException if to is before from
     */
    @Override
    public void saveMostReliableRocket(OutputStream outputStream, LocalDate from, LocalDate to) throws CipherException {
        if (outputStream == null || from == null || to == null) {
            throw new IllegalArgumentException("from or to is null");
        }
        if (to.isBefore(from)) {
            throw new TimeFrameMismatchException("to is before from");
        }
        Rocket rocket = rockets
                .stream()
                .reduce(BinaryOperator
                        .maxBy(Comparator.comparingDouble(x -> getReliability(x, missions
                                .stream()
                                .filter(y -> y.date().isAfter(from)
                                        && y.date().isBefore(to))))))
                .orElse(null);
        if (rocket != null) {
            Rijndael rijndael = new Rijndael(secretKey);
            InputStream stream = new ByteArrayInputStream(rocket.name().getBytes(StandardCharsets.UTF_8));
            rijndael.encrypt(stream, outputStream);
        }
    }
}
