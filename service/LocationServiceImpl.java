package service;

import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.fasterxml.jackson.core.type.TypeReference;

import config.Config;
import location.Dijkstra;
import location.Graph;
import model.Rider;
import util.FileUtils;

public class LocationServiceImpl implements LocationService {

    private final Map<String, Map<String, Object>> locations;
    private final Map<String, Map<String, Object>> connections;
    private final HashMap<String, HashMap<String, Rider>> riders;
    private final Graph graph;

    public LocationServiceImpl() {
        // Read riders data
        this.riders = FileUtils.readJsonFile(
                Config.RIDERS_FILE, new TypeReference<HashMap<String, HashMap<String, Rider>>>() {});

        // Read locations and connections from JSON files
        this.locations = FileUtils.readJsonFile(
                Config.LOCATION_FILE,
                new TypeReference<Map<String, Map<String, Object>>>() {}
        );

        this.connections = FileUtils.readJsonFile(
                Config.CONNECTIONS_FILE,
                new TypeReference<Map<String, Map<String, Object>>>() {}
        );
        this.graph = FileUtils.buildGraphFromLocations(locations, connections);
    }
    
    // Constructor to initialize with data
    public LocationServiceImpl(Map<String, Map<String, Object>> locations,
                               Map<String, Map<String, Object>> connections,
                               HashMap <String, HashMap<String, Rider>> riders){
        this.locations = locations;
        this.connections = connections;
        this.riders = riders;
        this.graph = FileUtils.buildGraphFromLocations(locations, connections);
    }
    

    @Override
    public boolean isValidLocation(String locationName) {
        return locations.containsKey(locationName);
    }

    /**
     * Get all available locations as a list
     * @return List of location names
     */
    @Override
    public List<String> getAllLocations() {
        return new ArrayList<>(locations.keySet());
    }

    /**
     * Display available locations with indices
     */
    @Override
    public void displayLocations() {
        List<String> locationList = getAllLocations();
        System.out.println("\nAvailable Locations:");
        for (int i = 0; i < locationList.size(); i++) {
            System.out.printf("%d. %s%n", i + 1, locationList.get(i));
        }
    }

    /**
     * Get location name by index
     * @param index The selected index
     * @return Location name or null if invalid index
     */
    @Override
    public String getLocationByIndex(int index) {
        List<String> locationList = getAllLocations();
        if (index >= 1 && index <= locationList.size()) {
            return locationList.get(index - 1);
        }
        return null;
    }

    @Override
    public String findNearestDriver(String pickupLocation, Map<String, String> driverLocations) {
        List<String> drivers = findNearestDrivers(pickupLocation, driverLocations, 1);
        return drivers.isEmpty() ? null : drivers.get(0);
    }

    @Override
    public List<String> findNearestDrivers(String pickupLocation, Map<String, String> driverLocations, int maxDrivers) {
        if (pickupLocation == null || driverLocations == null || driverLocations.isEmpty()) {
            return new ArrayList<>();
        }

        // Use the class's graph instance and run Dijkstra's algorithm
        Dijkstra dijkstra = new Dijkstra(graph);
        Map<String, Integer> distances = dijkstra.shortestPath(pickupLocation);

        // Sort drivers by distance to pickup location
        return driverLocations.entrySet().stream()
            .filter(entry -> distances.containsKey(entry.getValue()))
            .sorted(Comparator.comparingInt(entry -> distances.get(entry.getValue())))
            .limit(maxDrivers)
            .map(Map.Entry::getKey)
            .collect(Collectors.toList());
    }

    @Override
    public double calculateDistance(String fromLocation, String toLocation) {
        if (!isValidLocation(fromLocation) || !isValidLocation(toLocation)) {
            throw new IllegalArgumentException("Invalid location provided");
        }

        // Use the class's graph instance and run Dijkstra's algorithm
        Dijkstra dijkstra = new Dijkstra(graph);
        Map<String, Integer> distances = dijkstra.shortestPath(fromLocation);
        
        Integer distance = distances.get(toLocation);
        if (distance == null) {
            throw new IllegalArgumentException("No route found between " + fromLocation + " and " + toLocation);
        }
        
        // Convert distance to kilometers (assuming the stored distances are in kilometers)
        return distance.doubleValue();
    }
    @Override
    public double getTrafficIntensity(String location1, String location2) {
        // Get the connections list from the location
        List<Integer> connectionsTo = (List<Integer>) locations.get(location1).get("connections");
        List<Integer> connectionsFrom = (List<Integer>) locations.get(location2).get("connections");
    
        double totalIntensity = 0;
        int totalConnections = 0;
    
        // Calculate average for first location's connections
        for (Integer connectionId : connectionsTo) {
            Map<String, Object> connection = connections.get(String.valueOf(connectionId));
            if (connection != null) {
                double intensity = ((Number) connection.get("traffic_intensity")).doubleValue();
                totalIntensity += intensity;
                totalConnections++;
            }
        }
    
        // Calculate average for second location's connections
        for (Integer connectionId : connectionsFrom) {
            Map<String, Object> connection = connections.get(String.valueOf(connectionId));
            if (connection != null) {
                double intensity = ((Number) connection.get("traffic_intensity")).doubleValue();
                totalIntensity += intensity;
                totalConnections++;
            }
        }
    
        // Return average intensity, or -1 if no connections found
        return totalConnections > 0 ? totalIntensity / totalConnections : -1;
    }

    @Override
    public Map<String, Object> getLocationDetails(String locationName) {
        if (!isValidLocation(locationName)) {
            throw new IllegalArgumentException("Invalid location: " + locationName);
        }
        return locations.get(locationName);
    }

    public static void main(String[] args) {
        LocationServiceImpl locationService = new LocationServiceImpl();
        locationService.getTrafficIntensity("26 Number Chungi", "G-13 Markaz");
    }
}
