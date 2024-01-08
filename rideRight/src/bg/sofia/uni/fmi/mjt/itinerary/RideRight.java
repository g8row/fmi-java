package bg.sofia.uni.fmi.mjt.itinerary;

import bg.sofia.uni.fmi.mjt.itinerary.exception.CityNotKnownException;
import bg.sofia.uni.fmi.mjt.itinerary.exception.NoPathToDestinationException;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.SequencedCollection;

public class RideRight implements ItineraryPlanner {
    private final List<Journey> schedule;
    private final List<City> cities;

    public RideRight(List<Journey> schedule) {
        this.schedule = schedule;
        cities = new ArrayList<>();
        for (Journey journey : schedule) {
            if (!cities.contains(journey.from())) {
                cities.add(journey.from());
            }
            if (!cities.contains(journey.to())) {
                cities.add(journey.to());
            }
        }
    }

    BigDecimal manhattanDistance(City a, City b) {
        final double priceForKm = 20;
        final double kilometer = 1000;
        return new BigDecimal((Math.abs(a.location().x() - b.location().x())
                + Math.abs(a.location().y() - b.location().y())) * priceForKm / kilometer);
    }

    SequencedCollection<Journey> reconstructPath(Map<City, Journey> cameFrom, City current) {
        List<Journey> toReturn = new ArrayList<>();
        while (cameFrom.containsKey(current)) {
            toReturn.addFirst(cameFrom.get(current));
            current = cameFrom.get(current).from();
        }
        return toReturn;
    }

    SequencedCollection<Journey> getNeighbourEdges(City current) {
        List<Journey> toReturn = new ArrayList<>();
        for (Journey journey : schedule) {
            if (journey.from().name().equals(current.name())) {
                toReturn.add(journey);
            }
        }
        return toReturn;
    }

    Journey findCheapestEdge(City start, City destination) {
        for (Journey journey : schedule) {
            if (journey.from().name().equals(start.name()) && journey.to().name().equals(destination.name())) {
                return journey;
            }
        }
        return null;
    }

    void initMapNulls(Map<City, BigDecimal> map) {
        for (City city : cities) {
            map.put(city, null); // null represents infinity
        }
    }

    BigDecimal calculateTentativeScore(Map<City, BigDecimal> gScore, City current, Journey edge) {
        return gScore.get(current).add(edge.price().add(edge.price().multiply(edge.vehicleType().getGreenTax())));
    }

    SequencedCollection<Journey> findCheapestTransferPath(City start, City destination) {
        Map<City, Journey> cameFrom = new HashMap<>(cities.size());
        Map<City, BigDecimal> gScore = new HashMap<>(cities.size()); //price of the cheapest path to every city
        initMapNulls(gScore);
        gScore.put(start, BigDecimal.valueOf(0));
        Map<City, BigDecimal> fScore = new HashMap<>(cities.size());
        initMapNulls(fScore);
        fScore.put(start, manhattanDistance(start, destination));
        PriorityQueue<City> openSet = new PriorityQueue<>(cities.size(), new FileFScoreComparator(fScore));
        openSet.add(start);
        while (!openSet.isEmpty()) {
            City current = openSet.poll();
            if (current.name().equals(destination.name())) {
                return reconstructPath(cameFrom, current);
            }

            for (Journey edge : getNeighbourEdges(current)) {
                BigDecimal tentativeGScore = calculateTentativeScore(gScore, current, edge);
                if (gScore.get(edge.to()) == null || tentativeGScore.compareTo(gScore.get(edge.to())) < 0) {
                    cameFrom.put(edge.to(), edge);
                    gScore.put(edge.to(), tentativeGScore);
                    fScore.put(edge.to(), tentativeGScore.add(manhattanDistance(edge.to(), destination)));
                    if (!openSet.contains(edge.to())) {
                        openSet.add(edge.to());
                    }
                }
            }
        }
        return null;
    }

    /**
     * Returns a sequenced collection of Journeys representing the cheapest path from the start to the destination City.
     *
     * @param start         - City, from which the itinerary begins
     * @param destination   - the City that needs to be reached
     * @param allowTransfer - a flag parameter whether multiple Journeys with transfer can be returned as a result, or
     *                      only a direct route is expected
     * @throws CityNotKnownException        if the start or destination City is not present
     *                                      in the list of provided Journeys
     * @throws NoPathToDestinationException if there is no path satisfying the conditions
     *
     * @throws IllegalArgumentException     if start or destination is null
     */

    @Override
    public SequencedCollection<Journey> findCheapestPath(City start, City destination, boolean allowTransfer)
            throws CityNotKnownException, NoPathToDestinationException {
        if (!cities.contains(start) || !cities.contains(destination)) {
            throw new CityNotKnownException("start or destination City is not known");
        }
        if (start == null || destination == null ) {
            throw new IllegalArgumentException("start or destination is null");
        }
        if (!allowTransfer) {
            Journey edge = findCheapestEdge(start, destination);
            if (edge == null) {
                throw new NoPathToDestinationException("if there is no path satisfying the conditions");
            }
            return List.of(edge);
        } else {
            SequencedCollection<Journey> path = findCheapestTransferPath(start, destination);
            if (path == null) {
                throw new NoPathToDestinationException("if there is no path satisfying the conditions");
            } else {
                return path;
            }
        }
    }
}
