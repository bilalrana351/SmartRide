package service;

import java.net.InetSocketAddress;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import VoiceChat.VoiceChatClient;
import VoiceChat.VoiceChatServer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

import model.TimeStampedMapper;
import model.Ride;
import model.RideStatus;
import model.Rider;
import model.notification.NotificationMessage;
import config.Config;
import model.notification.NotificationType;

public class RideNotificationServer extends WebSocketServer {
    private static RideNotificationServer instance;
    private final Map<String, WebSocket> userConnections;
    private ObjectMapper objectMapper;
    private static final Object lock = new Object();
    private boolean isRunning = false;
    private UserService userService;
    private LocationService locationService;
    private HashMap<String, Ride> rides = new HashMap<String, Ride>();
    private final Map<String, List<String>> rideRequestDrivers = new ConcurrentHashMap<>();
    private RidePricingService pricingService = new RidePricingService(Config.PRICE_FILE);
    private final RideService rideService;  // Add RideService
    private String userType;
//for bilal


    // Define message routes
    private static final String ROUTE_RIDE_REQUEST = "/ride/request";
    private static final String ROUTE_RIDE_RESPONSE = "/ride/response";
    private static final String ROUTE_LOCATION_UPDATE = "/location/update";
    private static final String ROUTE_RIDE_CANCEL = "/ride/cancel";

    private RideNotificationServer(int port) {
        super(new InetSocketAddress(port));
        setReuseAddr(true); // Allow port reuse
        this.userConnections = new ConcurrentHashMap<>();
        this.objectMapper = TimeStampedMapper.GetMapper();

        // Initialize services
        this.userService = new UserServiceImpl();
        this.locationService = new LocationServiceImpl();
        this.rideService = new RideService();  // Initialize RideService

    }

    public static RideNotificationServer getInstance() {
        if (instance == null) {
            instance = new RideNotificationServer(8887); // Default WebSocket port
            instance.start();
        }
        return instance;
    }

    @Override
    public void onOpen(WebSocket conn, ClientHandshake handshake) {
        String userId = handshake.getFieldValue("user-id");
        userType = handshake.getFieldValue("user-type");

        if (userId != null) {
            userConnections.put(userId, conn);
            System.out.println("New connection established for user: " + userId);

            // If the user is a rider, mark them as available
            if ("rider".equalsIgnoreCase(userType)) {
                try {
                    Rider rider = userService.getRiderById(userId);
                    if (rider != null) {
                        rider.setAvailable(true);
                        System.out.println("Rider " + userId + " is now available");
                    }
                } catch (IllegalArgumentException e) {
                    System.err.println("Error updating rider availability: " + e.getMessage());
                }
            }
        }
    }

    @Override
    public void onClose(WebSocket conn, int code, String reason, boolean remote) {
        // Find the user ID associated with this connection
        String userId = userConnections.entrySet()
            .stream()
            .filter(entry -> entry.getValue().equals(conn))
            .map(Map.Entry::getKey)
            .findFirst()
            .orElse(null);

        if (userId != null) {
            // If it's a rider, mark them as unavailable
            try {
                Rider rider = userService.getRiderById(userId);
                if (rider != null) {
                    rider.setAvailable(false);
                    System.out.println("Rider " + userId + " is now unavailable");
                }
            } catch (IllegalArgumentException e) {
                // Not a rider or rider not found, ignore
            }

            userConnections.remove(userId);
        }

        System.out.println("Connection closed: " + reason);
    }

    @Override
    public void onMessage(WebSocket conn, String message) {
        try {
            // Parse the incoming message
            ObjectMapper mapper =  TimeStampedMapper.GetMapper();
            JsonNode jsonNode = mapper.readTree(message);

            // Extract route and payload
            String route = jsonNode.get("route").asText();
            JsonNode payload = jsonNode.get("payload");

            // Route the message based on the specified path
            switch (route) {
                case ROUTE_RIDE_REQUEST:
                    handleRideRequest(mapper.treeToValue(payload, NotificationMessage.class));
                    break;
                case ROUTE_RIDE_RESPONSE:
                    handleRideResponse(mapper.treeToValue(payload, NotificationMessage.class));
                    break;
                case ROUTE_LOCATION_UPDATE:
                    handleLocationUpdate(mapper.treeToValue(payload, NotificationMessage.class));
                    break;
                case ROUTE_RIDE_CANCEL:
                    handleRideCancellation(mapper.treeToValue(payload, NotificationMessage.class));
                    break;
                default:
                    System.err.println("Unknown route: " + route);
            }
        } catch (Exception e) {
            System.err.println("Error processing message on the server: " + e.getMessage());
        }
    }

    @Override
    public void onError(WebSocket conn, Exception ex) {
        System.err.println("WebSocket error: " + ex.getMessage());
    }

    @Override
    public void onStart() {
        this.isRunning = true;
        System.out.println("WebSocket server started on port: " + getPort());

        // Mark all riders as unavailable at server start
        try {
            for (HashMap<String, Rider> riderMap : ((UserServiceImpl)userService).riders.values()) {
                for (Rider rider : riderMap.values()) {
                    rider.setAvailable(false);
                }
            }
        } catch (Exception e) {
            System.err.println("Error initializing rider availability: " + e.getMessage());
        }
    }

    public boolean isRunning() {
        return isRunning;
    }

    public void sendNotification(String userId, NotificationMessage notification) {
        WebSocket conn = userConnections.get(userId);
        if (conn != null && conn.isOpen()) {
            try {
                String jsonMessage = objectMapper.writeValueAsString(notification);
                conn.send(jsonMessage);
            } catch (Exception e) {
                System.err.println("Error sending notification: " + e.getMessage());
            }
        }
    }

    private void handleMessage(NotificationMessage message) {
        // Handle different types of messages
        switch (message.getType()) {
            case RIDE_REQUEST:
                handleRideRequest(message);
                break;
            case RIDE_RESPONSE:
                handleRideResponse(message);
                break;
            case LOCATION_UPDATE:
                handleLocationUpdate(message);
                break;
        }
    }

    private void handleRideRequest(NotificationMessage message) {
        try {
            // Extract ride details from the message
            Ride ride = objectMapper.convertValue(message.getData(), Ride.class);
            rides.put(ride.getId(), ride);
            String clientId = message.getSenderId();

            // Find nearest available drivers (configurable count, default 3)
            int maxDrivers = ride.getMaxDrivers() != 0 ? ride.getMaxDrivers() : 3;
            List<String> nearestDriverIds = userService.findNearestAvailableDrivers(ride.getPickupLocation(), maxDrivers);

            if (nearestDriverIds.isEmpty()) {
                // No available drivers found - send special server response
                NotificationMessage response = new NotificationMessage();
                response.setType(NotificationType.RIDE_RESPONSE);
                response.setSenderId("server");
                response.setReceiverId(clientId);
                response.setData(false);
                response.setContent("No available drivers found");
                response.setRideId(ride.getId());
                sendNotification(clientId, response);
                return;
            }

            // Store the ride request and associated drivers
            rideRequestDrivers.put(ride.getId(), nearestDriverIds);

            // Create notification for drivers
            NotificationMessage driverNotification = new NotificationMessage();
            driverNotification.setType(NotificationType.RIDE_REQUEST);
            driverNotification.setSenderId(clientId);
            driverNotification.setData(ride);
            driverNotification.setRideId(ride.getId());

            // Send to all nearest drivers
            for (String driverId : nearestDriverIds) {
                driverNotification.setReceiverId(driverId);
                sendNotification(driverId, driverNotification);
            }
        } catch (Exception e) {
            System.err.println("Error handling ride request: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void handleRideResponse(NotificationMessage message) {
        try {
            String driverId = message.getSenderId();
            String clientId = message.getReceiverId();
            boolean accepted = (boolean) message.getData();
            String rideId = message.getRideId();
            Map<String, Object> rideDetails = new HashMap<>();


            if (accepted) {
                // Get the ride details
                Ride ride = rides.get(rideId);
                if (ride != null) {
                    rideDetails.put("pickupLocation", ride.getPickupLocation());
                    rideDetails.put("destination", ride.getDestination());

                    try {
                        // Calculate base distance
                        double distance = locationService.calculateDistance(ride.getPickupLocation(), ride.getDestination());

                        // Get traffic intensity between locations
                        double trafficIntensity = locationService.getTrafficIntensity(ride.getPickupLocation(), ride.getDestination());

                        // Get demand from destination location
                        Map<String, Object> destLocation = locationService.getLocationDetails(ride.getDestination());
                        double demandFactor = destLocation != null && destLocation.get("demand") != null ?
                                ((Number) destLocation.get("demand")).doubleValue() : 1.0;

                        // Calculate final price
                        Double price = pricingService.getPrice(distance, (int)trafficIntensity, (int)demandFactor);
                        if (price == null) {
                            price = (distance * (trafficIntensity / 50) * (demandFactor / 50) * Config.PETROL_PRICE) / Config.AVG_CAR_CONSUMPTION;
                        }

                        // Update ride details
                        ride.setFare(price);
                        ride.setDistance(distance);
                        ride.setTrafficIntensity(trafficIntensity);
                        ride.setDemandFactor(demandFactor);
                        ride.setDriverId(driverId);
                        ride.setStatus(RideStatus.ACCEPTED);

                        // Save the ride before sending notifications
                        rideService.saveRide(ride);

                        rideDetails.put("price", price);
                        rideDetails.put("distance", distance);
                        rideDetails.put("trafficIntensity", trafficIntensity);
                        rideDetails.put("demandFactor", demandFactor);
                        System.out.println(userType);

                        if ("rider".equalsIgnoreCase(userType)) {
                            new Thread(() -> {
                                try {
                                    String command = "java -cp out/production/SmartRide VoiceChat.VoiceChatServer";
                                    Runtime.getRuntime().exec(command);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }).start();

                            new Thread(() -> {
                                try {
                                    String command = "java -cp out/production/SmartRide VoiceChat.VoiceChatClient";
                                    Runtime.getRuntime().exec(command);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }).start();

                        }

                        // Notify other drivers to remove the request
                        List<String> driversToNotify = rideRequestDrivers.get(rideId);
                        if (driversToNotify != null) {
                            NotificationMessage cancelNotification = new NotificationMessage();
                            cancelNotification.setType(NotificationType.RIDE_CANCELLED);
                            cancelNotification.setSenderId(clientId);
                            cancelNotification.setData(rideId);

                            for (String otherDriverId : driversToNotify) {
                                if (!otherDriverId.equals(driverId)) {
                                    cancelNotification.setReceiverId(otherDriverId);
                                    sendNotification(otherDriverId, cancelNotification);
                                }
                            }

                            rideRequestDrivers.remove(rideId);
                        }
                    } catch (Exception e) {
                        System.err.println("Error calculating ride details: " + e.getMessage());
                        e.printStackTrace();
                    }
                }
            }

            // Send response to client
            NotificationMessage clientNotification = new NotificationMessage();
            clientNotification.setContent(objectMapper.writeValueAsString(rideDetails));
            clientNotification.setType(NotificationType.RIDE_RESPONSE);
            clientNotification.setSenderId(driverId);
            clientNotification.setReceiverId(clientId);
            clientNotification.setData(accepted);
            clientNotification.setRideId(rideId);

            sendNotification(clientId, clientNotification);

        } catch (Exception e) {
            System.err.println("Error handling ride response: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void handleLocationUpdate(NotificationMessage message) {
        // Broadcast location update to relevant parties
        sendNotification(message.getReceiverId(), message);
    }

    private void handleRideCancellation(NotificationMessage message) {
        try {
            // Try to get rideId from message, fallback to data if null
            String rideId = message.getRideId();
            if (rideId == null && message.getData() != null) {
                rideId = message.getData().toString();
            }

            if (rideId == null) {
                System.err.println("Error: No ride ID found in cancellation message");
                return;
            }

            String clientId = message.getSenderId();
            List<String> driversToNotify = rideRequestDrivers.get(rideId);

            if (driversToNotify != null) {
                NotificationMessage cancelNotification = new NotificationMessage();
                cancelNotification.setType(NotificationType.RIDE_CANCELLED);
                cancelNotification.setSenderId(clientId);
                cancelNotification.setData(rideId);

                for (String driverId : driversToNotify) {
                    cancelNotification.setReceiverId(driverId);
                    sendNotification(driverId, cancelNotification);
                }

                // Remove the ride request tracking
                rideRequestDrivers.remove(rideId);
            }
        } catch (Exception e) {
            System.err.println("Error handling ride cancellation: " + e.getMessage());
            e.printStackTrace();
        }
    }
}