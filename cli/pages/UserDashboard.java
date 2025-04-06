package cli.pages;

import cli.utils.*;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import config.Config;
import model.*;
import model.notification.NotificationMessage;
import model.notification.NotificationType;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import service.*;
import service.websocket.UserWebSocketClient;

import java.net.URI;
import java.net.URISyntaxException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import static cli.utils.GlobalState.RIDE_REQUEST_TIMEOUT_SECONDS;

public class UserDashboard {
    private final UserService userService;
    private Scanner scanner;
    private String userEmail;
    private UserWebSocketClient webSocketClient;
    private final ObjectMapper mapper = TimeStampedMapper.GetMapper();
    private boolean[] rideAccepted = { false };
    private boolean[] requestActive = { true };
    private RideHistoryNavigator rideHistory;
    private RideService rideService;

    public UserDashboard(String userEmail) {
        this.userService = GlobalState.getInstance().getUserService();
        this.userEmail = userEmail;
        this.scanner = new Scanner(System.in);
        try {
            this.webSocketClient = new UserWebSocketClient(userEmail, "client", this::handleIncomingMessage);
        } catch (Exception e) {
            System.err.println("Error creating WebSocket client: " + e.getMessage());
        }

        this.rideService = new RideService();
        this.rideHistory = new RideHistoryNavigator(userEmail);
        loadRideHistory();
    }

    private void loadRideHistory() {
        // Load rides from RideService
        HashMap<String, Ride> userRides = rideService.getUserRides(userEmail);

        // Add rides to navigator
        for (Ride ride : userRides.values()) {
            rideHistory.add(ride);
        }
    }

    private void handleIncomingMessage(NotificationMessage notification) {
        switch (notification.getType()) {
            case RIDE_RESPONSE:
                handleRideResponse(notification);
                break;
        }
    }

    private void handleRideResponse(NotificationMessage notification) {
        boolean accepted = (Boolean) notification.getData();
        String senderId = notification.getSenderId();
        Rider driver = userService.getRiderById(senderId);


        synchronized (System.out) {
            Formatter.clearScreen();
            if (accepted) {
                // Store the rating request for later processing
                GlobalState.getInstance().setPendingRating(new PendingRating(senderId));

                rideAccepted[0] = true;
                requestActive[0] = false;

                // Get ride details from notification content
                Map<String, Object> rideDetails = null;
                try {
                    String content = notification.getContent();
                    if (content != null && !content.trim().isEmpty()) {
                        rideDetails = mapper.readValue(content, new TypeReference<Map<String, Object>>() {
                        });
                    }
                } catch (Exception e) {
                    System.err.println("Error parsing ride details: " + e.getMessage());
                }

                // Extract information from ride details with safe defaults
                String pickupLocation = rideDetails != null ? (String) rideDetails.get("pickupLocation") : null;
                String destination = rideDetails != null ? (String) rideDetails.get("destination") : null;
                Double trafficIntensity = rideDetails != null && rideDetails.get("trafficIntensity") != null
                        ? ((Number) rideDetails.get("trafficIntensity")).doubleValue()
                        : 1.0;
                Double demandFactor = rideDetails != null && rideDetails.get("demandFactor") != null
                        ? ((Number) rideDetails.get("demandFactor")).doubleValue()
                        : 1.0;

                // Calculate distances using LocationService if needed
                LocationService locationService = GlobalState.getInstance().getLocationService();
                String driverLocation = driver.getCurrentLocation();

                double driverToPickupDistance = 0;
                double pickupToDestinationDistance = 0;

                Double price = ((Number) rideDetails.get("price")).doubleValue();

                try {
                    driverToPickupDistance = locationService.calculateDistance(driverLocation, pickupLocation);
                    pickupToDestinationDistance = locationService.calculateDistance(pickupLocation, destination);
                } catch (Exception e) {
                    System.err.println("Error calculating distances: " + e.getMessage());
                }

                if (price == 0.0) {
                    // Use a default calculation if price service returns null
                    price = (pickupToDestinationDistance * (trafficIntensity / 50) * (demandFactor / 50)
                            * Config.PETROL_PRICE) / Config.AVG_CAR_CONSUMPTION;
                }

                Formatter.printHeader(new String[] {
                        "RIDE REQUEST ACCEPTED",
                        "✅ Your ride request has been accepted!",
                        "",
                        "Driver Details:",
                        "Name: " + driver.getName(),
                        "Phone: " + driver.getPhoneNumber(),
                        "Vehicle: " + driver.getVehicleDetails(),
                        "",
                        "Trip Information:",
                        "🚗 Driver is " + String.format("%.1f", driverToPickupDistance) + " km away",
                        "🛣️ Trip distance will be " + String.format("%.1f", pickupToDestinationDistance) + " km",
                        "",
                        "Price Information:",
                        "💰 Estimated fare: Rs. " + String.format("%.2f", price),
                        "",
                        "Please contact your driver to coordinate pickup.",
                        "",
                        "Press Enter to continue..."
                }, 10);
            }
        }
    }

    private void viewRideHistory() {
        Ride currentRide = rideHistory.getCurrentRide();
        if (currentRide == null) {
            Formatter.clearScreen();
            Formatter.printHeader(new String[] {
                    "📖 RIDE HISTORY",
                    "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━",
                    "No rides found in your history.",
                    "",
                    "Press Enter to return to dashboard..."
            }, 10);
            scanner.nextLine();
            return;
        }

        boolean viewing = true;
        while (viewing) {
            Formatter.clearScreen();
            displayRideCard(currentRide);

            // Navigation menu with emoji indicators
            String[] options = {
                    "",
                    "Navigation Options:",
                    "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━",
                    "1. ⬅️  Previous Ride" + (rideHistory.hasPrevious() ? "" : " (Not available)"),
                    "2. ➡️  Next Ride" + (rideHistory.hasNext() ? "" : " (Not available)"),
                    "3. 🔙 Return to Dashboard",
                    "",
                    "Enter your choice (1-3): "
            };

            Formatter.printMenu(options, 6);
            String choice = NavigationManager.readInput(scanner);

            try {
                switch (choice) {
                    case "1":
                        if (rideHistory.hasPrevious()) {
                            currentRide = rideHistory.getPreviousRide();
                        } else {
                            System.out.println("\n❌ No previous rides available.");
                            Thread.sleep(1000);
                        }
                        break;
                    case "2":
                        if (rideHistory.hasNext()) {
                            currentRide = rideHistory.getNextRide();
                        } else {
                            System.out.println("\n❌ No more rides available.");
                            Thread.sleep(1000);
                        }
                        break;
                    case "3":
                        viewing = false;
                        break;
                    default:
                        System.out.println("\n❌ Invalid choice. Press Enter to continue...");
                        scanner.nextLine();
                }
            } catch (Exception e) {
                System.out.println("\n❌ Error: " + e.getMessage());
                scanner.nextLine();
            }
        }
    }

    private void displayRideCard(Ride ride) {
        String status = ride.getStatus().toString();
        String completionTime = ride.getCompletionTime() != null ? ride.getCompletionTime().toString() : "Pending";

        String statusEmoji = switch (ride.getStatus()) {
            case COMPLETED -> "✅";
            case CANCELLED -> "❌";
            case REQUESTED -> "🕒";
            case ACCEPTED -> "🚗";
            default -> "❓";
        };

        Formatter.printHeader(new String[] {
                "🎫 RIDE DETAILS",
                "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━",
                "",
                "🆔 Ride ID: " + ride.getId(),
                "",
                "📍 Pickup Location: " + ride.getPickupLocation(),
                "🎯 Destination: " + ride.getDestination(),
                "",
                "💰 Fare: Rs. " + String.format("%.2f", ride.getFare()),
                "📏 Distance: " + String.format("%.1f km", ride.getDistance()),
                "",
                "🚦 Traffic Intensity: " + String.format("%.1f", ride.getTrafficIntensity()),
                "📊 Demand Factor: " + String.format("%.1f", ride.getDemandFactor()),
                "",
                "⏰ Timeline",
                "├─ Requested: " + formatInstant(ride.getRequestTime()),
                "└─ Completed: "
                        + (ride.getCompletionTime() != null ? formatInstant(ride.getCompletionTime()) : "Pending"),
                "",
                "👤 Driver ID: " + (ride.getDriverId() != null ? ride.getDriverId() : "Not assigned"),
                statusEmoji + " Status: " + status,
                "",
                "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
        }, 10);
    }

    private String formatInstant(Instant instant) {
        return DateTimeFormatter
                .ofPattern("MMM dd, yyyy HH:mm:ss")
                .withZone(ZoneId.systemDefault())
                .format(instant);
    }

    public void display() {
        while (true) {
            PendingRating pendingRating = GlobalState.getInstance().getPendingRating();
            if (pendingRating != null && !pendingRating.isProcessed()) {
                handlePendingRating(pendingRating);
                GlobalState.getInstance().clearPendingRating();
            }

            Formatter.clearScreen();
            Formatter.printHeader(new String[]{
                "🏠 USER DASHBOARD",
                "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━",
                "Welcome, " + userService.getClientById(userEmail).getName()
            }, 10);
    
            String[] menuItems = {
                "",
                "1. 🚗 Request a Ride",
                "2. 📖 View Ride History",
                "3. 👤 Account Management",
                "4. 🚪 Logout",
                "",
                "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
            };
            Formatter.printMenu(menuItems, 6);
    
            System.out.print("\nPlease select an option (1-4): ");
            String choice = NavigationManager.readInput(scanner);
    
            if (choice == null) return;
    
            switch (choice) {
                case "1":
                    requestRide();
                    break;
                case "2":
                    viewRideHistory();
                    break;
                case "3":
                    NavigationManager.navigate(() -> new AccountManagementPage(userEmail).display());
                    break;
                case "4":
                    logout();
                    return;
                default:
                    System.out.println("\n❌ Invalid choice. Press Enter to continue...");
                    scanner.nextLine();
            }
        }
    }

    private void handlePendingRating(PendingRating pendingRating) {
        Formatter.clearScreen();
        Formatter.printHeader(new String[]{
            "⭐ RATE YOUR RIDE",
            "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━",
            "Please rate your experience with the driver",
            "Rate from 1 (poor) to 10 (excellent)"
        }, 10);
    
        try {
            System.out.print("\nYour rating (1-10): ");
            String input = NavigationManager.readInput(scanner);
            int rating = Integer.parseInt(input);
            
            if (rating >= 1 && rating <= 10) {
                Rider driver = userService.getRiderById(pendingRating.getDriverId());
                DriverRatingSystem ratingSystem = GlobalState.getInstance().getDriverRatingSystem();
                ratingSystem.addOrUpdateDriver(driver.getEmail(), driver.getName(), rating);
                
                System.out.println("\n✅ Thank you for your rating!");
            } else {
                System.out.println("\n❌ Invalid rating. Must be between 1 and 10.");
            }
        } catch (NumberFormatException e) {
            System.out.println("\n❌ Invalid input. Please enter a number.");
        }
        
        System.out.println("\nPress Enter to continue...");
        scanner.nextLine();
    }

    private void requestRide() {
        Formatter.clearScreen();
        Formatter.printHeader(new String[] { "REQUEST A RIDE" }, 10);

        // Get client and location service
        Client client = userService.getClientById(userEmail);

        // Handle pickup location selection
        String pickupLocation;
        if (client.getPreferredPickupLocation() != null) {
            System.out.println("\nUsing preferred pickup location: " + client.getPreferredPickupLocation());
            System.out.print("Would you like to use a different location? (y/n): ");
            String choice = NavigationManager.readInput(scanner);
            if (choice == null) {
                return;
            }

            if (choice.equalsIgnoreCase("y")) {
                pickupLocation = selectLocation("pickup");
                if (pickupLocation == null) {
                    return;
                }
            } else {
                pickupLocation = client.getPreferredPickupLocation();
            }
        } else {
            pickupLocation = selectLocation("pickup");
            if (pickupLocation == null) {
                return;
            }
        }

        // Handle destination selection
        String destination = selectLocation("destination");
        if (destination == null) {
            return;
        }

        try {
            // Reset flags for new request
            rideAccepted[0] = false;
            requestActive[0] = true;

            // Create ride request
            Ride ride = new Ride();
            ride.setClientId(userEmail);
            ride.setPickupLocation(pickupLocation);
            ride.setDestination(destination);
            ride.setStatus(RideStatus.REQUESTED);
            ride.setMaxDrivers(3); // Set default number of drivers to notify

            // Send ride request to server and let it find the nearest driver
            webSocketClient.sendRideRequest(userEmail, null, ride);

            System.out.println("\n✅ Ride request sent! Waiting for drivers...");
            System.out.println("(Request will timeout after 60 seconds if no driver accepts)");

            // Set up a timeout thread
            Thread timeoutThread = new Thread(() -> {
                try {
                    Thread.sleep(RIDE_REQUEST_TIMEOUT_SECONDS * 1000); // 60 seconds timeout
                    if (requestActive[0] && !rideAccepted[0]) {
                        synchronized (System.out) {
                            // Cancel the ride request
                            NotificationMessage cancelNotification = new NotificationMessage();
                            cancelNotification.setType(NotificationType.RIDE_CANCELLED);
                            cancelNotification.setSenderId(userEmail);
                            cancelNotification.setData(ride.getId());

                            try {
                                ObjectNode message = mapper.createObjectNode();
                                message.put("route", "/ride/cancel");
                                message.set("payload", mapper.valueToTree(cancelNotification));
                                webSocketClient.send(mapper.writeValueAsString(message));
                            } catch (Exception e) {
                                System.err.println("Error sending cancellation: " + e.getMessage());
                            }

                            // Show timeout message
                            Formatter.clearScreen();
                            Formatter.printHeader(new String[] {
                                    "REQUEST TIMED OUT",
                                    "❌ No drivers accepted your request within 60 seconds.",
                                    "",
                                    "Please try again later when more drivers are available.",
                                    "",
                                    "Press Enter to continue..."
                            }, 10);
                            requestActive[0] = false;
                        }
                    }
                } catch (InterruptedException e) {
                    // Thread was interrupted, request was either accepted or cancelled
                }
            });
            timeoutThread.setDaemon(true); // Make it a daemon thread so it doesn't prevent program exit
            timeoutThread.start();

            // Wait for response
            System.out.println("\nWaiting for driver response...");
            Thread.sleep(1000); // Small delay to ensure messages are displayed in order
            scanner.nextLine();

        } catch (Exception e) {
            System.out.println("\n❌ Error: " + e.getMessage());
        }

        System.out.println("\nPress Enter to continue...");
        scanner.nextLine();
    }

    private String selectLocation(String type) {
        LocationService locationService = GlobalState.getInstance().getLocationService();

        System.out.println("\nAvailable locations for " + type + ":");
        locationService.displayLocations();

        System.out.print("\nSelect " + type + " location (enter number): ");
        try {
            int locationIndex = Integer.parseInt(NavigationManager.readInput(scanner));
            String location = locationService.getLocationByIndex(locationIndex);

            if (location == null) {
                System.out.println("\n❌ Invalid location selection.");
                System.out.println("\nPress Enter to continue...");
                scanner.nextLine();
                return null;
            }

            return location;
        } catch (NumberFormatException e) {
            System.out.println("\n❌ Invalid input. Please enter a number.");
            System.out.println("\nPress Enter to continue...");
            scanner.nextLine();
            return null;
        }
    }

    // Update the logout method to close WebSocket
    private void logout() {
        if (webSocketClient != null) {
            webSocketClient.close();
        }
        GlobalState.getInstance().clearSession();
        System.out.println("\nLogging out...");
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

}