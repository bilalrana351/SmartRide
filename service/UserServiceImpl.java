package service;

import com.fasterxml.jackson.core.type.TypeReference;
import model.Client;
import model.Rider;
import util.FileUtils;
import util.PasswordUtils;
import config.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class UserServiceImpl implements UserService {
    public HashMap<String, HashMap<String, Client>> clients;
    HashMap<String, HashMap<String, Rider>> riders;
    private LocationService locationService;

    public UserServiceImpl() {
        this.clients = FileUtils.readJsonFile(
                Config.CLIENTS_FILE, new TypeReference<HashMap<String, HashMap<String, Client>>>() {});

        this.riders = FileUtils.readJsonFile(
                Config.RIDERS_FILE, new TypeReference<HashMap<String, HashMap<String, Rider>>>() {});

        this.locationService = new LocationServiceImpl();
    }
    
    public UserServiceImpl(HashMap<String, HashMap<String, Client>> clients, HashMap<String, HashMap<String, Rider>> riders) {
        this.clients = clients;
        this.riders = riders;
        this.locationService = new LocationServiceImpl();
    }



    @Override
    public String registerClient(Client client) {
        if (client == null || client.getEmail() == null || client.getPassword() == null) {
            return "Invalid client details provided!";
        }


        String key = getKeyFromEmail(client.getEmail());

        HashMap<String, Client> innerMap = clients.getOrDefault(key, new HashMap<>());
        if (innerMap.containsKey(client.getEmail())) {
            return "Client with email " + client.getEmail() + " is already registered!";
        }

        client.setPassword(PasswordUtils.hashPassword(client.getPassword()));
        innerMap.put(client.getEmail(), client);

        clients.put(key, innerMap);
        FileUtils.writeJsonFile(Config.CLIENTS_FILE, clients);

        return "Client registered successfully!";
    }

    @Override
    public String registerRider(Rider rider) {
        if (rider == null || rider.getEmail() == null || rider.getPassword() == null) {
            return "Invalid rider details provided!";
        }

        String key = getKeyFromEmail(rider.getEmail());

        HashMap<String, Rider> innerMap = riders.getOrDefault(key, new HashMap<>());
        if (innerMap.containsKey(rider.getEmail())) {
            return "Rider with email " + rider.getEmail() + " is already registered!";
        }

        rider.setPassword(PasswordUtils.hashPassword(rider.getPassword()));
        innerMap.put(rider.getEmail(), rider);

        riders.put(key, innerMap);
        FileUtils.writeJsonFile(Config.RIDERS_FILE, riders);

        return "Rider registered successfully!";
    }

    @Override
    public boolean login(String email, String password, String userType) {
        if (email == null || email.isEmpty() || password == null || password.isEmpty()) {
            System.out.println("Login failed: Invalid email or password");
            return false;
        }

        String key;
        try {
            key = getKeyFromEmail(email);
        } catch (IllegalArgumentException e) {
            System.out.println("Login failed: " + e.getMessage());
            return false;
        }

        try {
            // Dynamically reload the data from the file based on the user type
            if ("client".equalsIgnoreCase(userType)) {
                clients = FileUtils.readJsonFile(
                        Config.CLIENTS_FILE, new TypeReference<HashMap<String, HashMap<String, Client>>>() {});
            } else if ("rider".equalsIgnoreCase(userType)) {
                riders = FileUtils.readJsonFile(
                        Config.RIDERS_FILE, new TypeReference<HashMap<String, HashMap<String, Rider>>>() {});
            } else {
                System.out.println("Login failed: Invalid user type");
                return false;
            }
        } catch (Exception e) {
            System.out.println("Failed to read user data: " + e.getMessage());
            return false;
        }

        HashMap<String, ?> innerMap;
        try {
            if ("client".equalsIgnoreCase(userType)) {
                innerMap = clients.getOrDefault(key, new HashMap<>());
            } else {
                innerMap = riders.getOrDefault(key, new HashMap<>());
            }
        } catch (Exception e) {
            System.out.println("Login failed: Error accessing user data - " + e.getMessage());
            return false;
        }

        Object user = innerMap.get(email);

        if (user == null) {
            System.out.println("Login failed: User not found");
            return false;
        }

        try {
            if ("client".equalsIgnoreCase(userType) && user instanceof Client) {
                return PasswordUtils.verifyPassword(password, ((Client) user).getPassword());
            } else if ("rider".equalsIgnoreCase(userType) && user instanceof Rider) {
                return PasswordUtils.verifyPassword(password, ((Rider) user).getPassword());
            } else {
                System.out.println("Login failed: User type mismatch");
                return false;
            }
        } catch (Exception e) {
            System.out.println("Login failed: Error verifying password - " + e.getMessage());
            return false;
        }
    }


    @Override
    public Rider getRiderById(String riderId) {
        String key = getKeyFromEmail(riderId);

        if (!riders.containsKey(key)) {
            throw new IllegalArgumentException("Rider not found: " + riderId);
        }

        HashMap<String, Rider> innerMap = riders.get(key);
        Rider rider = innerMap.get(riderId);

        if (rider == null) {
            throw new IllegalArgumentException("Rider not found: " + riderId);
        }

        return rider;
    }

    @Override
    public Client getClientById(String clientId) {
        String key = getKeyFromEmail(clientId);

        if (!clients.containsKey(key)) {
            throw new IllegalArgumentException("Client not found: " + clientId);
        }

        HashMap<String, Client> innerMap = clients.get(key);
        Client client = innerMap.get(clientId);

        if (client == null) {
            throw new IllegalArgumentException("Client not found: " + clientId);
        }

        return client;
    }

    @Override
    public boolean deleteRider(String riderId) {
        String key = getKeyFromEmail(riderId);

        HashMap<String, Rider> innerMap = riders.getOrDefault(key, new HashMap<>());
        if (!innerMap.containsKey(riderId)) {
            return false;
        }

        innerMap.remove(riderId);
        if (innerMap.isEmpty()) {
            riders.remove(key);
        } else {
            riders.put(key, innerMap);
        }

        FileUtils.writeJsonFile(Config.RIDERS_FILE, riders);
        return true;
    }

    @Override
    public boolean deleteClient(String clientId) {
        String key = getKeyFromEmail(clientId);

        HashMap<String, Client> innerMap = clients.getOrDefault(key, new HashMap<>());
        if (!innerMap.containsKey(clientId)) {
            return false;
        }

        innerMap.remove(clientId);
        if (innerMap.isEmpty()) {
            clients.remove(key);
        } else {
            clients.put(key, innerMap);
        }

        FileUtils.writeJsonFile(Config.CLIENTS_FILE, clients);
        return true;
    }

    private String getKeyFromEmail(String email) {
        if (email == null || email.isEmpty()) {
            throw new IllegalArgumentException("Email cannot be null or empty");
        }

        char firstChar = email.toLowerCase().charAt(0);

        if (firstChar >= 'a' && firstChar <= 'z') {
            return String.valueOf(firstChar);
        }

        throw new IllegalArgumentException("Email must start with a letter from 'a' to 'z'");
    }

    @Override
    public void updateClientPhone(String clientId, String newPhoneNumber) {
        String key = getKeyFromEmail(clientId);
    
        if (!clients.containsKey(key)) {
            throw new IllegalArgumentException("Client not found: " + clientId);
        }
    
        HashMap<String, Client> innerMap = clients.get(key);
        Client client = innerMap.get(clientId);
    
        if (client == null) {
            throw new IllegalArgumentException("Client not found: " + clientId);
        }
    
        client.setPhoneNumber(newPhoneNumber);
        innerMap.put(clientId, client);
        clients.put(key, innerMap);
        
        // Persist changes to file
        FileUtils.writeJsonFile(Config.CLIENTS_FILE, clients);
    }
    
    @Override
    public void updateClientPreferredLocation(String clientId, String newLocation) {
        String key = getKeyFromEmail(clientId);
    
        if (!clients.containsKey(key)) {
            throw new IllegalArgumentException("Client not found: " + clientId);
        }
    
        HashMap<String, Client> innerMap = clients.get(key);
        Client client = innerMap.get(clientId);
    
        if (client == null) {
            throw new IllegalArgumentException("Client not found: " + clientId);
        }
    
        client.setPreferredPickupLocation(newLocation);
        innerMap.put(clientId, client);
        clients.put(key, innerMap);
        
        // Persist changes to file
        FileUtils.writeJsonFile(Config.CLIENTS_FILE, clients);
    }

    @Override
    public void updateRiderPhone(String riderId, String newPhoneNumber) {
        String key = getKeyFromEmail(riderId);
    
        if (!riders.containsKey(key)) {
            throw new IllegalArgumentException("Rider not found: " + riderId);
        }
    
        HashMap<String, Rider> innerMap = riders.get(key);
        Rider rider = innerMap.get(riderId);
    
        if (rider == null) {
            throw new IllegalArgumentException("Rider not found: " + riderId);
        }
    
        rider.setPhoneNumber(newPhoneNumber);
        innerMap.put(riderId, rider);
        riders.put(key, innerMap);
        
        // Persist changes to file
        FileUtils.writeJsonFile(Config.RIDERS_FILE, riders);
    }

    @Override
    public void updateRiderVehicleDetails(String riderId, String newVehicleDetails) {
        String key = getKeyFromEmail(riderId);
    
        if (!riders.containsKey(key)) {
            throw new IllegalArgumentException("Rider not found: " + riderId);
        }
    
        HashMap<String, Rider> innerMap = riders.get(key);
        Rider rider = innerMap.get(riderId);
    
        if (rider == null) {
            throw new IllegalArgumentException("Rider not found: " + riderId);
        }
    
        rider.setVehicleDetails(newVehicleDetails);
        innerMap.put(riderId, rider);
        riders.put(key, innerMap);
        
        // Persist changes to file
        FileUtils.writeJsonFile(Config.RIDERS_FILE, riders);
    }

    @Override
    public void updateRiderLicenseNumber(String riderId, String newLicenseNumber) {
        String key = getKeyFromEmail(riderId);
    
        if (!riders.containsKey(key)) {
            throw new IllegalArgumentException("Rider not found: " + riderId);
        }
    
        HashMap<String, Rider> innerMap = riders.get(key);
        Rider rider = innerMap.get(riderId);
    
        if (rider == null) {
            throw new IllegalArgumentException("Rider not found: " + riderId);
        }
    
        rider.setLicenseNumber(newLicenseNumber);
        innerMap.put(riderId, rider);
        riders.put(key, innerMap);
        
        // Persist changes to file
        FileUtils.writeJsonFile(Config.RIDERS_FILE, riders);
    }

    @Override
    public void updateRiderCurrentLocation(String riderId, String newLocation) {
        String key = getKeyFromEmail(riderId);
    
        if (!riders.containsKey(key)) {
            throw new IllegalArgumentException("Rider not found: " + riderId);
        }
    
        HashMap<String, Rider> innerMap = riders.get(key);
        Rider rider = innerMap.get(riderId);
    
        if (rider == null) {
            throw new IllegalArgumentException("Rider not found: " + riderId);
        }
    
        rider.setCurrentLocation(newLocation);
        innerMap.put(riderId, rider);
        riders.put(key, innerMap);
        
        // Persist changes to file
        FileUtils.writeJsonFile(Config.RIDERS_FILE, riders);
    }

    @Override
    public String findNearestAvailableDriver(String pickupLocation) {
        // Get all riders' current locations
        Map<String, String> driverLocations = new HashMap<>();
        
        // Iterate through all riders and collect their current locations
        for (HashMap<String, Rider> riderMap : riders.values()) {
            for (Rider rider : riderMap.values()) {
                if (rider.getCurrentLocation() != null && rider.isAvailable()) {
                    driverLocations.put(rider.getEmail(), rider.getCurrentLocation());
                }
            }
        }

        try {
            return locationService.findNearestDriver(pickupLocation, driverLocations);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    @Override
    public List<String> findNearestAvailableDrivers(String pickupLocation, int maxDrivers) {
        // Get all riders' current locations
        Map<String, String> driverLocations = new HashMap<>();
        
        // Iterate through all riders and collect their current locations
        for (HashMap<String, Rider> riderMap : riders.values()) {
            for (Rider rider : riderMap.values()) {
                if (rider.getCurrentLocation() != null && rider.isAvailable()) {
                    driverLocations.put(rider.getEmail(), rider.getCurrentLocation());
                }
            }
        }

        try {
            return locationService.findNearestDrivers(pickupLocation, driverLocations, maxDrivers);
        } catch (IllegalArgumentException e) {
            return new ArrayList<>();
        }
    }
}
