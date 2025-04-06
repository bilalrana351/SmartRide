package service;

import com.fasterxml.jackson.core.type.TypeReference;
import config.Config;
import model.Ride;
import util.FileUtils;

import java.io.File;
import java.util.HashMap;

public class RideService {
    private HashMap<String, Ride> rides;
    private static final String RIDES_BASE_DIR = Config.DATA_DIR + "/rides";
    private static final String USER_RIDES_DIR = RIDES_BASE_DIR + "/users";
    private static final String DRIVER_RIDES_DIR = RIDES_BASE_DIR + "/drivers";
    private static final String USER_RIDES_FILE = "/rides.json";
    private static final String DRIVER_RIDES_FILE = "/rides.json";

    public RideService() {
        // Create necessary directories if they don't exist
        new File(USER_RIDES_DIR).mkdirs();
        new File(DRIVER_RIDES_DIR).mkdirs();
        this.rides = new HashMap<>();
    }

    public void saveRide(Ride ride) {
        rides.put(ride.getId(), ride);

        // Save to user's rides file
        String userDir = USER_RIDES_DIR + "/" + ride.getClientId();
        new File(userDir).mkdirs();
        String userRidesFile = userDir + USER_RIDES_FILE;
        HashMap<String, Ride> userRides = getUserRides(ride.getClientId());
        userRides.put(ride.getId(), ride);
        FileUtils.writeJsonFile(userRidesFile, userRides);

        // Save to driver's rides file if driver is assigned
        if (ride.getDriverId() != null) {
            String driverDir = DRIVER_RIDES_DIR + "/" + ride.getDriverId();
            new File(driverDir).mkdirs();
            String driverRidesFile = driverDir + DRIVER_RIDES_FILE;
            HashMap<String, Ride> driverRides = getDriverRides(ride.getDriverId());
            driverRides.put(ride.getId(), ride);
            FileUtils.writeJsonFile(driverRidesFile, driverRides);
        }
    }

    public Ride getRide(String id) {
        return rides.get(id);
    }

    public HashMap<String, Ride> getUserRides(String userEmail) {
        String userRidesFile = USER_RIDES_DIR + "/" + userEmail + USER_RIDES_FILE;
        File file = new File(userRidesFile);
        
        if (!file.exists()) {
            return new HashMap<>();
        }

        HashMap<String, Ride> userRides = FileUtils.readJsonFile(
            userRidesFile, 
            new TypeReference<HashMap<String, Ride>>() {}
        );
        
        return userRides != null ? userRides : new HashMap<>();
    }

    public HashMap<String, Ride> getDriverRides(String driverEmail) {
        String driverRidesFile = DRIVER_RIDES_DIR + "/" + driverEmail + DRIVER_RIDES_FILE;
        File file = new File(driverRidesFile);
        
        if (!file.exists()) {
            return new HashMap<>();
        }

        HashMap<String, Ride> driverRides = FileUtils.readJsonFile(
            driverRidesFile, 
            new TypeReference<HashMap<String, Ride>>() {}
        );
        
        return driverRides != null ? driverRides : new HashMap<>();
    }
}