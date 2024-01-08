package bg.sofia.uni.fmi.mjt.itinerary;

import bg.sofia.uni.fmi.mjt.itinerary.exception.CityNotKnownException;
import bg.sofia.uni.fmi.mjt.itinerary.exception.NoPathToDestinationException;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.SequencedCollection;
import bg.sofia.uni.fmi.mjt.itinerary.Journey;

import static bg.sofia.uni.fmi.mjt.itinerary.vehicle.VehicleType.BUS;
import static bg.sofia.uni.fmi.mjt.itinerary.vehicle.VehicleType.PLANE;

public class Main {
    static void print(SequencedCollection<Journey> path) {
        try {
            System.out.println(path.getFirst().from().name());
        } catch(Exception e) {

        }

        for (Journey journey : path) {
            System.out.println(journey.to().name());
        }
        System.out.println(path);

    }

    public static void main(String[] args) {
        City sofia = new City("Sofia", new Location(0, 2000));
        City plovdiv = new City("Plovdiv", new Location(4000, 1000));
        City varna = new City("Varna", new Location(9000, 3000));
        City burgas = new City("Burgas", new Location(9000, 1000));
        City ruse = new City("Ruse", new Location(7000, 4000));
        City blagoevgrad = new City("Blagoevgrad", new Location(0, 1000));
        City kardzhali = new City("Kardzhali", new Location(3000, 0));
        City tarnovo = new City("Tarnovo", new Location(5000, 3000));
        City blagoevgrad2 = new City("Blagoevgrad", new Location(4000, 1000));

        List<Journey> schedule = List.of(
                new Journey(BUS, sofia, blagoevgrad, new BigDecimal("20")),
                new Journey(BUS, blagoevgrad, sofia, new BigDecimal("20")),
                new Journey(BUS, sofia, plovdiv, new BigDecimal("90")),
                new Journey(BUS, plovdiv, sofia, new BigDecimal("90")),
                new Journey(BUS, plovdiv, kardzhali, new BigDecimal("50")),
                new Journey(BUS, kardzhali, plovdiv, new BigDecimal("50")),
                new Journey(BUS, plovdiv, burgas, new BigDecimal("90")),
                new Journey(BUS, burgas, plovdiv, new BigDecimal("90")),
                new Journey(BUS, burgas, varna, new BigDecimal("60")),
                new Journey(BUS, varna, burgas, new BigDecimal("60")),
                new Journey(BUS, sofia, tarnovo, new BigDecimal("150")),
                new Journey(BUS, tarnovo, sofia, new BigDecimal("150")),
                new Journey(BUS, plovdiv, tarnovo, new BigDecimal("40")),
                new Journey(BUS, tarnovo, plovdiv, new BigDecimal("40")),
                new Journey(BUS, tarnovo, ruse, new BigDecimal("70")),
                new Journey(BUS, ruse, tarnovo, new BigDecimal("70")),
                new Journey(BUS, varna, ruse, new BigDecimal("70")),
                new Journey(BUS, ruse, varna, new BigDecimal("70")),
                new Journey(PLANE, varna, burgas, new BigDecimal("200")),
                new Journey(PLANE, burgas, varna, new BigDecimal("200")),
                new Journey(PLANE, burgas, sofia, new BigDecimal("150")),
                new Journey(PLANE, sofia, burgas, new BigDecimal("250")),
                new Journey(PLANE, varna, sofia, new BigDecimal("290")),
                new Journey(PLANE, sofia, varna, new BigDecimal("300"))
        );

        List<Journey> schedule2 = List.of(
                new Journey(BUS, sofia, plovdiv, new BigDecimal(20)),
                new Journey(BUS, sofia, blagoevgrad2, new BigDecimal(20)),
                new Journey(BUS, plovdiv, varna, new BigDecimal(20)),
                new Journey(BUS, blagoevgrad2, varna, new BigDecimal(20))
                );

        RideRight rideRight = new RideRight(schedule);

        try {
            print(rideRight.findCheapestPath(sofia, varna, true));
            System.out.println();
            print(rideRight.findCheapestPath(varna, sofia, true));
            System.out.println();
            //print(rideRight.findCheapestPath(sofia, null, true));
            System.out.println();
            print(rideRight.findCheapestPath(sofia, sofia, true));
            System.out.println();
            print(rideRight.findCheapestPath(varna, kardzhali, true));
            System.out.println();
            print(rideRight.findCheapestPath(varna, burgas, false));
            System.out.println();
            print(rideRight.findCheapestPath(varna, burgas, true));
            System.out.println();
            print(rideRight.findCheapestPath(varna, kardzhali, false));
        } catch (CityNotKnownException | NoPathToDestinationException e) {
            throw new RuntimeException(e);
        }
    }
}
