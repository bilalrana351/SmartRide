package cli.pages;

import cli.utils.Formatter;
import cli.utils.GlobalState;
import cli.utils.NavigationManager;
import model.Client;
import model.Ride;
import model.notification.NotificationMessage;
import model.notification.RideRequestQueue;
import service.UserService;
import service.websocket.RiderWebSocketClient;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RequestsListPage {
    private final UserService userService;
    private final String userEmail;
    private final RiderWebSocketClient webSocketClient;
    private final java.util.Scanner scanner;
    private Map<String, NotificationMessage> pendingRequests = new HashMap<>();


    public RequestsListPage(String userEmail, RiderWebSocketClient webSocketClient) {
        this.userService = GlobalState.getInstance().getUserService();
        this.userEmail = userEmail;
        this.webSocketClient = webSocketClient;
        this.scanner = new java.util.Scanner(System.in);
    }
    

    public void display() {
        while (true) {
            List<NotificationMessage> requests = new ArrayList<>();
            RideRequestQueue queue = RideRequestQueue.getInstance();

            // Get all current requests
            while (queue.hasRequests()) {
                requests.add(queue.getNextRequest());
                queue.removeCurrentRequest();
            }

            // Add them back to preserve order
            for (NotificationMessage request : requests) {
                queue.addRequest(request);
            }
            Formatter.clearScreen();

            if (requests.isEmpty()) {
                Formatter.printHeader(new String[]{
                    "PENDING RIDE REQUESTS",
                    "No pending ride requests at the moment.",
                    "",
                    "Check back later!",
                    "",
                    "Press Enter to go back..."
                }, 10);
                scanner.nextLine();
                return;
            }

            // Display header
            Formatter.printHeader(new String[]{
                "PENDING RIDE REQUESTS",
                "You have " + requests.size() + " pending requests",
                "",
                "Select a request number to respond, or 'b' to go back:"
            }, 10);

            // Display each request
            for (int i = 0; i < requests.size(); i++) {
                NotificationMessage request = requests.get(i);
                Ride ride = (Ride) request.getData();
                Client client = userService.getClientById(request.getSenderId());

                System.out.println("\n" + (i + 1) + ". Request from " + client.getName());
                System.out.println("   From: " + ride.getPickupLocation());
                System.out.println("   To: " + ride.getDestination());
                System.out.println("   Phone: " + client.getPhoneNumber());
            }

            System.out.print("\nEnter your choice: ");
            String input = scanner.nextLine().trim().toLowerCase();

            if (input.equals("b")) {
                return;
            }

            try {
                int choice = Integer.parseInt(input);
                if (choice > 0 && choice <= requests.size()) {
                    handleRequest(requests.get(choice - 1));
                } else {
                    System.out.println("\n❌ Invalid choice. Press Enter to continue...");
                    scanner.nextLine();
                }
            } catch (NumberFormatException e) {
                System.out.println("\n❌ Invalid input. Press Enter to continue...");
                scanner.nextLine();
            }
        }
    }

    private void handleRequest(NotificationMessage notification) {
        Ride ride = (Ride) notification.getData();
        String clientId = notification.getSenderId();
        Client client = userService.getClientById(clientId);

        Formatter.clearScreen();
        Formatter.printHeader(new String[]{
            "RIDE REQUEST DETAILS",
            "From: " + client.getName(),
            "Phone: " + client.getPhoneNumber(),
            "",
            "Pickup: " + ride.getPickupLocation(),
            "Destination: " + ride.getDestination(),
            "",
            "Would you like to accept this ride?",
            "1. Accept",
            "2. Decline",
            ""
        }, 10);

        System.out.println("Enter your choice (1-2): ");
        String choice = NavigationManager.readInput(scanner);

        if (choice != null) {
            boolean accepted = choice.equals("1");
            webSocketClient.sendRideResponse(userEmail, clientId, notification.getRideId(), accepted);
            
            if (accepted) {
                System.out.println("\n✅ Ride accepted! Please contact the client to coordinate pickup.");
            } else {
                System.out.println("\n❌ Ride declined.");
            }
            
            // Remove the request from the queue
            RideRequestQueue.getInstance().removeRequest(clientId);
        }

        System.out.println("\nPress Enter to continue...");
        scanner.nextLine();
    }

    private void handleNotification(NotificationMessage notification) {
        switch (notification.getType()) {
            case RIDE_REQUEST:
                Ride ride = (Ride) notification.getData();
                pendingRequests.put(ride.getId(), notification);
                break;
            case RIDE_CANCELLED:
                // Remove the cancelled ride from pending requests
                String rideId = (String) notification.getData();
                pendingRequests.remove(rideId);
                break;
            case RIDE_ACCEPTED:
                // Remove the ride if another driver accepted it
                rideId = (String) notification.getData();
                pendingRequests.remove(rideId);
                break;
        }
    }
} 