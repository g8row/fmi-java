package bg.sofia.uni.fmi.mjt.simcity.utility;

import bg.sofia.uni.fmi.mjt.simcity.property.billable.Billable;

import java.util.HashMap;
import java.util.Map;

public class UtilityService implements UtilityServiceAPI {
    Map<UtilityType, Double> taxRates;

    public UtilityService(Map<UtilityType, Double> taxRates) {
        this.taxRates = taxRates;
    }

    /**
     * Retrieves the costs of a specific utility for a given billable building.
     *
     * @param utilityType The utility type used for the costs' calculation.
     * @param billable    The billable building for which the utility costs will be calculated.
     * @return The cost of the specified utility for the billable building, rounded up to two decimal places.
     * @throws IllegalArgumentException if the utility or billable is null.
     */
    @Override
    public <T extends Billable> double getUtilityCosts(UtilityType utilityType, T billable) {
        if (billable == null) {
            throw new IllegalArgumentException("billable is null");
        }
        switch (utilityType) {
            case WATER -> {
                return billable.getWaterConsumption() * taxRates.get(utilityType);
            }
            case ELECTRICITY -> {
                return billable.getElectricityConsumption() * taxRates.get(utilityType);
            }
            case NATURAL_GAS -> {
                return billable.getNaturalGasConsumption() * taxRates.get(utilityType);
            }
            case null, default -> {
                throw new IllegalArgumentException("the utility or billable is null");
            }
        }
    }

    /**
     * Calculates the total utility costs for a given billable building.
     *
     * @param billable The billable building for which total utility costs are calculated.
     * @return The total cost of all utilities for the billable building, rounded up to two decimal places.
     * @throws IllegalArgumentException if the billable is null.
     */
    @Override
    public <T extends Billable> double getTotalUtilityCosts(T billable) {
        if (billable == null) {
            throw new IllegalArgumentException("billable is null");
        }
        return getUtilityCosts(UtilityType.ELECTRICITY, billable)
                + getUtilityCosts(UtilityType.WATER, billable)
                + getUtilityCosts(UtilityType.NATURAL_GAS, billable);
    }

    /**
     * Computes the absolute difference in utility costs between two billable buildings for each utility type.
     *
     * @param firstBillable  The first billable building used for the cost comparison.
     * @param secondBillable The second billable building used for the cost comparison.
     * @return An unmodifiable map containing the absolute difference in costs between the buildings for each
     * utility. Each cost difference is rounded up to two decimal places.
     * @throws IllegalArgumentException if any billable is null.
     */
    @Override
    public <T extends Billable> Map<UtilityType, Double> computeCostsDifference(T firstBillable, T secondBillable) {
        if (firstBillable == null || secondBillable == null) {
            throw new IllegalArgumentException("some of the billables is null");
        }
        Map<UtilityType, Double> map = new HashMap<>(UtilityType.values().length);
        for (UtilityType type : UtilityType.values()) {
            map.put(type, Math.abs(getUtilityCosts(type, firstBillable) - getUtilityCosts(type, secondBillable)));
        }
        return Map.copyOf(map);
    }
}
