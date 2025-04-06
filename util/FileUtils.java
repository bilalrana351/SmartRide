package util;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import location.Graph;
import model.Location;

import config.*;
import model.TimeStampedMapper;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class FileUtils {
    private static final ObjectMapper objectMapper = new ObjectMapper();

    // Utility method to create a file if it doesn't exist
    private static void createFileIfNotExists(String filePath) {
        try {
            File file = new File(filePath);
            if (!file.exists()) {
                file.createNewFile();
                // Initialize the file with an empty JSON object or array based on the expected structure
                if (filePath.equals(Config.LOCATION_FILE)) {
                    objectMapper.writeValue(file, new ArrayList<>()); // Empty list for locations
                } else if (filePath.equals(Config.CLIENTS_FILE) || filePath.equals(Config.RIDERS_FILE)) {
                    objectMapper.writeValue(file, new HashMap<>()); // Empty map for users
                } else if (filePath.equals(Config.DRIVER_LOCATIONS_FILE)) {
                    objectMapper.writeValue(file, new HashMap<>()); // Empty map for driver locations
                } else {
                    objectMapper.writeValue(file, new Object());
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Generic method to read JSON file and convert to specified type
     * @param filePath Path to the JSON file
     * @param typeReference TypeReference for the target type
     * @return Parsed object of specified type, or null if error occurs
     */
    public static <T> T readJsonFile(String filePath, TypeReference<T> typeReference) {
        try {
            ObjectMapper mapper = TimeStampedMapper.GetMapper();
            File file = new File(filePath);
            
            if (!file.exists()) {
                return null;
            }
            
            return mapper.readValue(file, typeReference);
        } catch (IOException e) {
            System.err.println("Error reading JSON file: " + e.getMessage());
            return null;
        }
    }

    /**
     * Generic method to write object to JSON file
     * @param filePath Path where to save the JSON file
     * @param object Object to be written to file
     * @return true if successful, false otherwise
     */
    public static <T> boolean writeJsonFile(String filePath, T object) {
        try {
            ObjectMapper mapper = TimeStampedMapper.GetMapper();
            File file = new File(filePath);
            
            // Create parent directories if they don't exist
            file.getParentFile().mkdirs();
            
            mapper.writeValue(file, object);
            return true;
        } catch (IOException e) {
            System.err.println("Error writing JSON file: " + e.getMessage());
            return false;
        }
    }

    /**
     * Reads a list of Location objects from the JSON file.
     *
     * @return A list of Location objects.
     */
    public static List<Location> readLocations() {
        createFileIfNotExists(Config.LOCATION_FILE);

        try {
            return objectMapper.readValue(new File(Config.LOCATION_FILE), new TypeReference<List<Location>>() {});
        } catch (IOException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    /**
     * Writes a list of Location objects to the JSON file.
     *
     * @param locations The list of Location objects to write.
     */
    public static void writeLocations(List<Location> locations) {
        try {
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(new File(Config.LOCATION_FILE), locations);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Reads a map of driver locations from the JSON file.
     *
     * @return A map of driver locations.
     */
    public static Map<String, String> readDriverLocations(String filePath) {
        createFileIfNotExists(filePath);

        try {
            return objectMapper.readValue(new File(filePath), new TypeReference<Map<String, String>>() {});
        } catch (IOException e) {
            e.printStackTrace();
            return new HashMap<>();
        }
    }

    /**
     * Writes a map of driver locations to the JSON file.
     *
     * @param filePath        The file path to write to.
     * @param driverLocations The map of driver locations to write.
     */
    public static void writeDriverLocations(String filePath, Map<String, String> driverLocations) {
        try {
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(new File(filePath), driverLocations);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Reads a map of clients or riders from the JSON file.
     *
     * @param filePath The file path to read from.
     * @param <T>      The type of user (Client or Rider).
     * @return A nested map of users.
     */
    public static <T> HashMap<String, HashMap<String, T>> readUsers(String filePath) {
        createFileIfNotExists(filePath);

        try {
            return objectMapper.readValue(new File(filePath), new TypeReference<HashMap<String, HashMap<String, T>>>() {});
        } catch (IOException e) {
            e.printStackTrace();
            return new HashMap<>();
        }
    }

    /**
     * Writes a map of clients or riders to the JSON file.
     *
     * @param filePath The file path to write to.
     * @param users    The nested map of users to write.
     * @param <T>      The type of user (Client or Rider).
     */
    public static <T> void writeUsers(String filePath, HashMap<String, HashMap<String, T>> users) {
        try {
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(new File(filePath), users);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public static Graph buildGraphFromLocations(Map<String, Map<String, Object>> locations, Map<String, Map<String, Object>> connections) {
        Graph graph = new Graph();
    
        // First add all locations as nodes
        for (String locationName : locations.keySet()) {
            graph.addNode(locationName);
        }
    
        // Then add all edges based on connections
        for (Map.Entry<String, Map<String, Object>> entry : connections.entrySet()) {
            Map<String, Object> connection = entry.getValue();
            String node1 = (String) connection.get("node1");
            String node2 = (String) connection.get("node2");
            int distance = ((Number) connection.get("distance")).intValue();
            
            graph.addEdge(node1, node2, distance);
        }
    
        return graph;
    }
}
