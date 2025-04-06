package service;

import com.fasterxml.jackson.core.type.TypeReference;
import config.Config;
import model.Rider;
import util.FileUtils;
import java.util.*;

public class DriverRatingSystem {
    private static final String RATINGS_FILE = Config.DATA_DIR + "/ratings.json";
    private final TreeHashMap<String, Driver> driverMap;  // Changed to String key for email
    private final CustomHeap<Driver> driverHeap;

    public static class Driver {
        String email;  // Changed from id to email
        String name;
        List<Integer> ratings;

        Driver(String email, String name) {
            this.email = email;
            this.name = name;
            this.ratings = new ArrayList<>();
        }

        void addRating(int rating) {
            ratings.add(rating);
        }

        double getAverageRating() {
            return ratings.stream().mapToInt(Integer::intValue).average().orElse(0.0);
        }

        // For JSON serialization
        public Map<String, Object> toMap() {
            Map<String, Object> map = new HashMap<>();
            map.put("email", email);
            map.put("name", name);
            map.put("ratings", ratings);
            return map;
        }

        // For JSON deserialization
        public static Driver fromMap(Map<String, Object> map) {
            Driver driver = new Driver(
                (String) map.get("email"),
                (String) map.get("name")
            );
            List<Integer> ratings = (List<Integer>) map.get("ratings");
            driver.ratings.addAll(ratings);
            return driver;
        }

        @Override
        public String toString() {
            return String.format("⭐ %.2f | 👤 %s", getAverageRating(), name);
        }
    }

    public DriverRatingSystem() {
        this.driverMap = new TreeHashMap<>();
        this.driverHeap = new CustomHeap<>();
        loadRatings();
    }

    private void loadRatings() {
        // Clear existing data
        driverMap.clear();
        driverHeap.clear();

        Map<String, Map<String, Object>> ratingsData = FileUtils.readJsonFile(
            RATINGS_FILE,
            new TypeReference<Map<String, Map<String, Object>>>() {}
        );

        if (ratingsData != null) {
            for (Map.Entry<String, Map<String, Object>> entry : ratingsData.entrySet()) {
                Driver driver = Driver.fromMap(entry.getValue());
                driverMap.put(driver.email, driver);
                driverHeap.add(driver, driver.getAverageRating());
            }
        }
    }

    private void saveRatings() {
        Map<String, Map<String, Object>> ratingsData = new HashMap<>();
        for (String email : driverMap.keySet()) {
            Driver driver = driverMap.get(email);
            ratingsData.put(email, driver.toMap());
        }
        FileUtils.writeJsonFile(RATINGS_FILE, ratingsData);
    }

    public void addOrUpdateDriver(String email, String name, int rating) {
        Driver driver = driverMap.get(email);
        if (driver == null) {
            driver = new Driver(email, name);
            driverMap.put(email, driver);
        }
        driver.addRating(rating);
        driverHeap.add(driver, driver.getAverageRating());
        saveRatings();
    }

    public List<Driver> getTopDrivers(int limit) {
        // Reload ratings before getting top drivers
        loadRatings();
        
        // Create a temporary heap to avoid modifying the original
        CustomHeap<Driver> tempHeap = new CustomHeap<>();
        for (String email : driverMap.keySet()) {
            Driver driver = driverMap.get(email);
            tempHeap.add(driver, driver.getAverageRating());
        }
        
        List<Driver> topDrivers = new ArrayList<>();
        Set<String> addedDrivers = new HashSet<>();  // Track added drivers
        
        while (topDrivers.size() < limit && !tempHeap.isEmpty()) {
            Driver driver = tempHeap.poll();  // Use poll() instead of peek()
            if (!addedDrivers.contains(driver.email)) {
                topDrivers.add(driver);
                addedDrivers.add(driver.email);
            }
        }
        return topDrivers;
    }

    public double getDriverRating(String email) {
        Driver driver = driverMap.get(email);
        return driver != null ? driver.getAverageRating() : 0.0;
    }


}