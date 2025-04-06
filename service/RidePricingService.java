package service;

import com.fasterxml.jackson.core.type.TypeReference;

import config.Config;
import util.FileUtils;



import java.time.LocalDateTime;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;
import javax.xml.crypto.Data;

class RidePricingService {
    static class PriceEntry implements Comparable<PriceEntry> {
        private LocalDateTime dateTime;
        private double price;

        // Default constructor for Jackson
        public PriceEntry() {
        }

        public PriceEntry(LocalDateTime dateTime, double price) {
            this.dateTime = dateTime;
            this.price = price;
        }

        // Getters and setters
        public LocalDateTime getDateTime() {
            return dateTime;
        }

        public void setDateTime(LocalDateTime dateTime) {
            this.dateTime = dateTime;
        }

        public double getPrice() {
            return price;
        }

        public void setPrice(double price) {
            this.price = price;
        }

        @Override
        public int compareTo(PriceEntry other) {
            return this.dateTime.compareTo(other.dateTime);
        }
    }

    private final HashMap<String, PriorityQueue<PriceEntry>> priceMap;
    private final int MAX_HEAP_SIZE = 3; // Limit for the min-heap size

    public RidePricingService() {
        this.priceMap = new HashMap<>();
    }

    public RidePricingService(String filePath) {
        this.priceMap = this.readPricesFromJson(filePath);
    }

    public void addPricesFromJson(String locationPath) {
        // Read locations from JSON file with correct type reference
        Map<String, Map<String, Object>> locations = FileUtils.readJsonFile(
                locationPath,
                new TypeReference<Map<String, Map<String, Object>>>() {}
        );
    
        List<LocalDateTime> times = new ArrayList<>();
    
        // Add all the hours to the data structure
        for (int hour = 0; hour < 24; hour++) {
            LocalDateTime dateTime = LocalDateTime.of(LocalDate.now(), LocalTime.of(hour, 0));
            times.add(dateTime);
        }
    
        // Generate distance range from 1 to 30
        List<Integer> distances = new ArrayList<>();
        for (int d = 1; d <= 30; d++) {
            distances.add(d);
        }
    
        // Extract demands correctly from locations
        Set<Integer> demands = new HashSet<>();
        for (Map.Entry<String, Map<String, Object>> entry : locations.entrySet()) {
            Integer demand = ((Number) entry.getValue().get("demand")).intValue();
            demands.add(demand);
        }
    
        // Rest of your code remains the same
        for (LocalDateTime dateTime : times) {
            for (int distance : distances) {
                for (int demand : demands) {
                    double price = getPriceIslamabadMap(distance, demand);
                    addPrice(dateTime, distance, demand, price);
                }
            }
        }
    
        // Write the priceMap to a JSON file
        FileUtils.writeJsonFile(Config.PRICE_FILE, priceMap);
    }

    public HashMap<String, PriorityQueue<PriceEntry>> readPricesFromJson(String filePath) {
        // Read the priceMap from a JSON file
        return FileUtils.readJsonFile(filePath, new TypeReference<HashMap<String, PriorityQueue<PriceEntry>>>() {});
    }

    private double getPriceIslamabadMap(int distance, double  demand) {

        // Scale demand to 50
        demand = demand / 50;
        // THe current price will depend on the petrol price
        double price = (Config.PETROL_PRICE * distance * demand) / Config.AVG_CAR_CONSUMPTION;

        return price;
    }

    public void addPrice(LocalDateTime dateTime, int traffic, int demand, double price) {
        String timeOfDay = mapToTimeOfDay(dateTime.getHour());
        String key = dateTime.toString() + "_" + traffic + "_" + demand;

        // Add the price entry
        PriceEntry entry = new PriceEntry(dateTime, price);
        priceMap.putIfAbsent(key, new PriorityQueue<>());
        PriorityQueue<PriceEntry> heap = priceMap.get(key);

        // Maintain min-heap size
        heap.offer(entry);
        if (heap.size() > MAX_HEAP_SIZE) {
            heap.poll(); // Remove the oldest entry
        }

        // Rehash if necessary (optional implementation based on custom conditions)
        if (priceMap.size() > 1000) { // Example condition for rehashing
            rehash();
        }
    }


    public void addPrice(LocalDateTime dateTime, double distance, double time, int users, int drivers, double price) {
        // Generate the key
        int traffic = (int) Math.floor(distance / time);
        int demand = (int) Math.floor((double) users / drivers);

        addPrice(dateTime, traffic, demand, price);
    }

    public Double getPrice(double distance, double time, int users, int drivers) {
        // Generate the key
        int traffic = (int) Math.floor(distance / time);
        int demand = (int) Math.floor((double) users / drivers);

        return getPrice(distance, traffic, demand);

    }

    public Double getPrice(double distance, int traffic, int demand) {
        String timeOfDay = mapToTimeOfDay(LocalDateTime.now().getHour());

        String key = timeOfDay + "_" + traffic + "_" + demand;

        // Retrieve the prices
        PriorityQueue<PriceEntry> heap = priceMap.get(key);
        if (heap == null || heap.isEmpty()) {
            return null; // No prices available for this key
        }

        // Calculate the average of the top 3 prices
        List<PriceEntry> temp = new ArrayList<>(heap);
        double total = 0;
        for (PriceEntry entry : temp) {
            total += entry.price;
        }
        return total / temp.size();

    }

    private String mapToTimeOfDay(int hour) {
        if (hour >= 6 && hour <= 10) return "morning";
        if (hour >= 11 && hour <= 13) return "noon";
        if (hour >= 14 && hour <= 16) return "afternoon";
        if (hour >= 17 && hour <= 19) return "evening";
        if (hour >= 20 && hour <= 23) return "night";
        return "dawn"; // 0-5
    }

    private void rehash() {
        HashMap<String, PriorityQueue<PriceEntry>> newMap = new HashMap<>();
        for (Map.Entry<String, PriorityQueue<PriceEntry>> entry : priceMap.entrySet()) {
            newMap.put(entry.getKey(), new PriorityQueue<>(entry.getValue()));
        }
        priceMap.clear();
        priceMap.putAll(newMap);
    }

    public static void main(String[] args) {
//        // Print hte current working directory
        RidePricingService pricing = new RidePricingService();

//
        pricing.addPricesFromJson(Config.LOCATION_FILE);
//
//        HashMap<String, PriorityQueue<PriceEntry>> priceMap = pricing.readPricesFromJson(Config.PRICE_FILE);

//        // Adding some example entries
//        pricing.addPrice(LocalDateTime.now(), 10, 0.5, 50, 10, 25.0);
//        pricing.addPrice(LocalDateTime.now(), 10, 0.5, 50, 10, 30.0);
//        pricing.addPrice(LocalDateTime.now(), 10, 0.5, 50, 10, 35.0);
//        pricing.addPrice(LocalDateTime.now(), 10, 0.5, 50, 10, 40.0);
//
//        // Retrieving the price for similar conditions
//        Double price = pricing.getPrice(10, 0.5, 50, 10);
//        System.out.println("Average Price: " + (price != null ? price : "No data available"));
    }
}