package service;

import java.util.List;
import java.util.Map;

public interface LocationService {
    /**
     * Finds the nearest driver to a specified pickup location.
     *
     * @param pickupLocation The pickup location.
     * @param driverLocations A map of driver IDs to their current locations.
     * @return The ID of the nearest driver.
     */
    String findNearestDriver(String pickupLocation, Map<String, String> driverLocations);

    /**
     * Checks if a location is valid (exists in the system).
     *
     * @param locationName The name of the location to validate.
     * @return True if the location exists, false otherwise.
     */
    boolean isValidLocation(String locationName);

    public List<String> getAllLocations();

    public void displayLocations();

    public String getLocationByIndex(int index);

        /**
     * Finds the nearest drivers to a specified pickup location.
     *
     * @param pickupLocation The pickup location
     * @param driverLocations A map of driver IDs to their current locations
     * @param maxDrivers Maximum number of drivers to return (default 3)
     * @return List of driver IDs sorted by proximity
     */
    List<String> findNearestDrivers(String pickupLocation, Map<String, String> driverLocations, int maxDrivers);

    // Add overloaded method with default value
    default List<String> findNearestDrivers(String pickupLocation, Map<String, String> driverLocations) {
        return findNearestDrivers(pickupLocation, driverLocations, 3);
    }

    /**
     * Calculates the distance between two locations using Dijkstra's algorithm.
     *
     * @param fromLocation the starting location
     * @param toLocation the destination location
     * @return the distance between the locations in kilometers
     * @throws IllegalArgumentException if either location is invalid
     */
    double calculateDistance(String fromLocation, String toLocation);

    /**
     * Gets the traffic intensity between two locations.
     *
     * @param fromLocation the starting location
     * @param toLocation the destination location
     * @return the traffic intensity value between the locations
     * @throws IllegalArgumentException if either location is invalid
     */
    double getTrafficIntensity(String fromLocation, String toLocation);

    /**
     * Gets the details of a location including demand and other properties.
     *
     * @param locationName the name of the location
     * @return Map containing location details
     * @throws IllegalArgumentException if location is invalid
     */
    Map<String, Object> getLocationDetails(String locationName);
}
