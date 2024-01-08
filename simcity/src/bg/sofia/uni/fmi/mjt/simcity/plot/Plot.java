package bg.sofia.uni.fmi.mjt.simcity.plot;

import bg.sofia.uni.fmi.mjt.simcity.exception.BuildableAlreadyExistsException;
import bg.sofia.uni.fmi.mjt.simcity.exception.BuildableNotFoundException;
import bg.sofia.uni.fmi.mjt.simcity.exception.InsufficientPlotAreaException;
import bg.sofia.uni.fmi.mjt.simcity.property.buildable.Buildable;

import java.util.HashMap;
import java.util.Map;

public class Plot<E extends Buildable> implements PlotAPI<E> {
    int buildableArea;
    int takenArea;
    Map<String, E> buildables;

    public Plot(int buildableArea) {
        this.buildableArea = buildableArea;
        buildables = new HashMap<>();
    }

    /**
     * Constructs a buildable on the plot.
     *
     * @param address   the address where the buildable should be constructed.
     * @param buildable the buildable that should be constructed on the given address.
     * @throws IllegalArgumentException        if the address is null or blank.
     * @throws IllegalArgumentException        if the buildable is null.
     * @throws BuildableAlreadyExistsException if the address is already occupied on the plot.
     * @throws InsufficientPlotAreaException   if the required area exceeds the remaining plot area.
     */
    @Override
    public void construct(String address, E buildable) {
        if (address == null || address.isBlank() || buildable == null) {
            throw new IllegalArgumentException("the address is null or blank or the buildable is null");
        }
        if (buildables.containsKey(address)) {
            throw new BuildableAlreadyExistsException("the address is already occupied on the plot");
        }
        if (takenArea + buildable.getArea() > buildableArea) {
            throw new InsufficientPlotAreaException("the required area exceeds the remaining plot area");
        }
        takenArea += buildable.getArea();
        buildables.put(address, buildable);
    }

    /**
     * Constructs multiple buildables on the plot.
     * This method ensures that either all operations are successfully completed
     * or no changes are made to the plot's state.
     *
     * @param buildablesToConstruct a Map containing the addresses and corresponding buildable entities.
     * @throws IllegalArgumentException        if the map of buildablesToConstruct is null, Blank.
     * @throws BuildableAlreadyExistsException if any of the addresses is already occupied on the plot.
     * @throws InsufficientPlotAreaException   if the combined area of the provided buildablesToConstruct exceeds
     *                                         the remaining plot area.
     */
    @Override
    public void constructAll(Map<String, E> buildablesToConstruct) {
        if (buildablesToConstruct == null || buildablesToConstruct.isEmpty()) {
            throw new IllegalArgumentException("the map of buildablesToConstruct is null, Blank");
        }
        for (String addr : buildablesToConstruct.keySet()) {
            if (addr == null || addr.isBlank()) {
                throw new IllegalArgumentException("the address is null or blank or the buildable is null");
            }
            if (buildables.containsKey(addr)) {
                throw new BuildableAlreadyExistsException("one of the addresses is already occupied on the plot");
            }
        }
        int sumOfArea = 0;
        for (E buildable : buildablesToConstruct.values()) {
            sumOfArea += buildable.getArea();
        }
        if (sumOfArea + takenArea > buildableArea) {
            throw new InsufficientPlotAreaException("the combined area of the provided buildablesToConstruct "
                    + "exceeds the remaining plot area");
        }
        for (Map.Entry<String, E> e: buildablesToConstruct.entrySet()) {
            construct(e.getKey(), e.getValue());
        }
    }

    /**
     * Demolishes a buildable from the plot.
     *
     * @param address the address of the buildable which should be demolished.
     * @throws IllegalArgumentException   if the provided address is null or blank.
     * @throws BuildableNotFoundException if buildable with such address does not exist on the plot.
     */
    @Override
    public void demolish(String address) {
        if (address == null || address.isBlank()) {
            throw new IllegalArgumentException("the address is null or blank");
        }
        if (!buildables.containsKey(address)) {
            throw new BuildableNotFoundException("buildable with such address does not exist on the plot");
        }
        takenArea -= buildables.get(address).getArea();
        buildables.remove(address);
    }

    /**
     * Demolishes all buildables from the plot.
     */
    @Override
    public void demolishAll() {
        buildables.clear();
        takenArea = 0;
    }

    /**
     * Retrieves all buildables present on the plot.
     *
     * @return An unmodifiable copy of the buildables present on the plot.
     */
    @Override
    public Map<String, E> getAllBuildables() {
        return Map.copyOf(buildables);
    }

    /**
     * Retrieves the remaining buildable area on the plot.
     *
     * @return The remaining buildable area on the plot.
     */
    @Override
    public int getRemainingBuildableArea() {
        return buildableArea - takenArea;
    }
}
