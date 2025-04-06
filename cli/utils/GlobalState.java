package cli.utils;

import service.*;
import model.*;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.type.TypeReference;
import util.FileUtils;
import config.Config;

public class GlobalState {
    private static GlobalState instance;
    private UserService userService;
    private LocationService locationService;
    private String currentUserEmail;
    private String userType;
    public static final int RIDE_REQUEST_TIMEOUT_SECONDS = 120; // 30 seconds to respond
    public HashMap <String, HashMap<String, Client>> clients;
    public HashMap <String, HashMap<String, Rider>> riders;
    public Map<String, Map<String, Object>> locations;
    public Map<String, Map<String, Object>> connections;
    private DriverRatingSystem driverRatingSystem;
    private PendingRating pendingRating;


    private GlobalState() {
        // Initialize the clients and the riders here
        this.clients = FileUtils.readJsonFile(
                Config.CLIENTS_FILE, new TypeReference<HashMap<String, HashMap<String, Client>>>() {});

        this.riders = FileUtils.readJsonFile(
                Config.RIDERS_FILE, new TypeReference<HashMap<String, HashMap<String, Rider>>>() {});


        // Read locations and connections from JSON files using TypeReference for HashMap
        this.locations = FileUtils.readJsonFile(
            Config.LOCATION_FILE,
            new TypeReference<Map<String, Map<String, Object>>>() {}
        );
        
        this.connections = FileUtils.readJsonFile(
            Config.CONNECTIONS_FILE,
            new TypeReference<Map<String, Map<String, Object>>>() {}
        );

        userService = new UserServiceImpl(clients, riders);
        locationService = new LocationServiceImpl(locations, connections, riders);

        this.driverRatingSystem = new DriverRatingSystem();
    }

    public DriverRatingSystem getDriverRatingSystem() {
        return driverRatingSystem;
    }

    public static GlobalState getInstance() {
        if (instance == null) {
            instance = new GlobalState();
        }
        return instance;
    }

    public UserService getUserService() {
        return userService;
    }

    public String getCurrentUserEmail() {
        return currentUserEmail;
    }

    public void setPendingRating(PendingRating rating) {
        this.pendingRating = rating;
    }

    public PendingRating getPendingRating() {
        return pendingRating;
    }

    public void clearPendingRating() {
        this.pendingRating = null;
    }

    public void setCurrentUserEmail(String email) {
        this.currentUserEmail = email;
    }

    public String getUserType() {
        return userType;
    }

    public void setUserType(String userType) {
        this.userType = userType;
    }

    public void clearSession() {
        currentUserEmail = null;
        userType = null;
    }
    
    public LocationService getLocationService() {
        return locationService;
    }
}