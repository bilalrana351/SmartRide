package config;

public class Config {
    public static final String DATA_DIR = "data";

    // Add the data dir as a prefix to the file names
    public static final String CLIENTS_FILE = DATA_DIR + "/client.json";
    public static final String RIDERS_FILE = DATA_DIR + "/rider.json";
    public static  final String DRIVER_LOCATIONS_FILE = DATA_DIR + "/driversLocation.json";
    public static final String LOCATION_FILE = DATA_DIR + "/locations.json";
    public static final String CONNECTIONS_FILE = DATA_DIR + "/connections.json";
    public static final double PETROL_PRICE = 270;
    public static final double AVG_CAR_CONSUMPTION = 10;
    public static final String PRICE_FILE = DATA_DIR + "/price.json";
    public static final String RIDES_BASE_DIR = DATA_DIR + "/rides";
}
