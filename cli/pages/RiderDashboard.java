package cli.pages;

import cli.utils.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import model.Client;
import model.Ride;
import model.Rider;
import model.notification.NotificationMessage;
import model.notification.NotificationType;
import model.notification.RideRequestQueue;
import org.java_websocket.WebSocket;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import service.DriverRatingSystem;
import service.LocationService;
import service.RideNotificationServer;
import service.UserService;
import service.websocket.RiderWebSocketClient;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RiderDashboard {
    private final UserService userService;
    private final String userEmail;
    private final java.util.Scanner scanner;
    private RiderWebSocketClient webSocketClient;
    private boolean notified = false;

    public RiderDashboard(String userEmail) {
        this.userService = GlobalState.getInstance().getUserService();
        this.userEmail = userEmail;
        this.scanner = new java.util.Scanner(System.in);

        try {
            this.webSocketClient = new RiderWebSocketClient(userEmail, this::handleIncomingMessage);
        } catch (Exception e) {
            System.err.println("Error creating WebSocket client: " + e.getMessage());
        }
    }

    private void handleIncomingMessage(NotificationMessage notification) {
        switch (notification.getType()) {
            case RIDE_REQUEST:
                // Add to queue instead of showing immediately
                RideRequestQueue.getInstance().addRequest(notification);
                synchronized (System.out) {
                    System.out.println("\n🔔 New ride request received! Check pending requests menu to respond.");
                }
                break;
            case RIDE_CANCELLED:
                // Remove from queue if client cancels
                RideRequestQueue.getInstance().removeRequest(notification.getSenderId());
                break;
            case RIDE_ACCEPTED:
                // Remove from queue if another driver accepts
                RideRequestQueue.getInstance().removeRequest(notification.getData().toString());
                break;
        }
    }

    private void viewPendingRequests() {
        NavigationManager.navigate(() -> {
            new RequestsListPage(userEmail, webSocketClient).display();
        });
    }

    public void display() {
        while (true) {
            Formatter.clearScreen();
            Rider rider = userService.getRiderById(userEmail);

            int pendingRequests = RideRequestQueue.getInstance().getRequestCount();
            String pendingLabel = pendingRequests > 0 ? " (" + pendingRequests + " pending)" : "";

            Formatter.printHeader(new String[]{
                    "🏍️ RIDER DASHBOARD",
                    "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━",
                    "Welcome, " + rider.getName() + "!",
                    "",
                    "Current Status:",
                    "📍 Location: " + rider.getCurrentLocation(),
                    "🚗 Vehicle: " + rider.getVehicleDetails(),
                    "📱 Phone: " + rider.getPhoneNumber()
            }, 10);

            String[] menuItems = {
                    "",
                    "1. 👤 View/Update Account Settings",
                    "2. 📍 Update Current Location",
                    "3. 🔔 View Pending Ride Requests" + pendingLabel,
                    "4. 🏆 View Top Rated Drivers",
                    "5. 🚪 Logout",
                    "",
                    "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
            };
            Formatter.printMenu(menuItems, 6);

            System.out.print("\nPlease select an option (1-5): ");
            String choice = NavigationManager.readInput(scanner);

            if (choice == null) return;

            switch (choice) {
                case "1":
                    NavigationManager.navigate(() -> new RiderAccountManagementPage(userEmail).display());
                    break;
                case "2":
                    updateCurrentLocation();
                    break;
                case "3":
                    viewPendingRequests();
                    break;
                case "4":
                    viewDriverLeaderboard();  // Call the leaderboard method here
                    break;
                case "5":
                    logout();
                    return;
                default:
                    System.out.println("\n❌ Invalid choice. Press Enter to continue...");
                    scanner.nextLine();
            }
        }
    }

    private void updateCurrentLocation() {
        Formatter.clearScreen();
        Formatter.printHeader(new String[]{"UPDATE CURRENT LOCATION"}, 10);
        
        // Display available locations
        LocationService locationService = GlobalState.getInstance().getLocationService();
        locationService.displayLocations();
        
        System.out.print("\nSelect your new current location (enter number): ");
        try {
            int locationIndex = Integer.parseInt(NavigationManager.readInput(scanner));
            String newLocation = locationService.getLocationByIndex(locationIndex);

            if (newLocation == null) {
                System.out.println("\n❌ Invalid location selection.");
            } else {
                userService.updateRiderCurrentLocation(userEmail, newLocation);
                System.out.println("\n✅ Current location updated successfully!");
            }
        } catch (NumberFormatException e) {
            System.out.println("\n❌ Invalid input. Please enter a number.");
        }
        
        System.out.println("\nPress Enter to continue...");
        scanner.nextLine();
    }

    private void viewDriverLeaderboard() {
        Formatter.clearScreen();
        Formatter.printHeader(new String[]{
                "🏆 TOP RATED DRIVERS",
                "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━",
                ""
        }, 10);

        DriverRatingSystem ratingSystem = GlobalState.getInstance().getDriverRatingSystem();
        List<DriverRatingSystem.Driver> topDrivers = ratingSystem.getTopDrivers(5);

        if (topDrivers.isEmpty()) {
            System.out.println("No ratings available yet.");
        } else {
            for (int i = 0; i < topDrivers.size(); i++) {
                System.out.printf("%d. %s%n", i + 1, topDrivers.get(i));
            }
        }

        System.out.println("\nPress Enter to continue...");
        scanner.nextLine();
    }

    public void logout() {
        if (webSocketClient != null) {
            webSocketClient.close();
        }
    }
} 