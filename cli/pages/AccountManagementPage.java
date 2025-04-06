package cli.pages;

import cli.utils.*;
import service.LocationService;
import service.UserService;
import model.Client;

public class AccountManagementPage {
    private final UserService userService;
    private final String userEmail;
    private final java.util.Scanner scanner;

    public AccountManagementPage(String userEmail) {
        this.userService = GlobalState.getInstance().getUserService();
        this.userEmail = userEmail;
        this.scanner = new java.util.Scanner(System.in);
    }

    public void display() {
        while (true) {
            Formatter.clearScreen();
            Client client = userService.getClientById(userEmail);
            
            Formatter.printHeader(new String[]{
                "ACCOUNT MANAGEMENT",
                "Current Settings:",
                "Name: " + client.getName(),
                "Email: " + client.getEmail(),
                "Phone: " + client.getPhoneNumber(),
                "Preferred Location: " + client.getPreferredPickupLocation()
            }, 10);

            String[] menuItems = {
                "",
                "1. Update Phone Number",
                "2. Update Preferred Pickup Location",
                "3. Delete Account",
                "4. Back to Dashboard",
                ""
            };
            Formatter.printMenu(menuItems, 6);

            System.out.print("\nPlease select an option (1-4): ");
            String choice = NavigationManager.readInput(scanner);

            if (choice == null) return;

            switch (choice) {
                case "1":
                    updatePhoneNumber();
                    break;
                case "2":
                    updatePreferredLocation();
                    break;
                case "3":
                    if (deleteAccount()) {
                        return; // Return to welcome page after successful deletion
                    }
                    break;
                case "4":
                    return;
                default:
                    System.out.println("\nInvalid choice. Press Enter to continue...");
                    scanner.nextLine();
            }
        }
    }

    private boolean deleteAccount() {
        Formatter.clearScreen();
        Formatter.printHeader(new String[]{"DELETE ACCOUNT"}, 10);
        
        System.out.println("\n⚠️  WARNING: This action cannot be undone!");
        System.out.println("To confirm deletion, please enter your password.");
        
        System.out.print("\n🔒 Password: ");
        String password = NavigationManager.readInput(scanner);
        
        if (password == null) return false;
        
        // Verify password
        if (!userService.login(userEmail, password, "CLIENT")) {
            System.out.println("\n❌ Incorrect password. Account deletion cancelled.");
            System.out.println("\nPress Enter to continue...");
            scanner.nextLine();
            return false;
        }
        
        // Delete account
        boolean deleted = userService.deleteClient(userEmail);
        
        if (deleted) {
            System.out.println("\n✅ Account deleted successfully.");
            System.out.println("We're sorry to see you go!");
            System.out.println("\nPress Enter to continue...");
            scanner.nextLine();
            
            // Navigate back to welcome page
            NavigationManager.navigate(() -> new WelcomePage().display());
            return true;
        } else {
            System.out.println("\n❌ Failed to delete account. Please try again later.");
            System.out.println("\nPress Enter to continue...");
            scanner.nextLine();
            return false;
        }
    }

    private void updatePhoneNumber() {
        Formatter.clearScreen();
        Formatter.printHeader(new String[]{"UPDATE PHONE NUMBER"}, 10);
        
        System.out.print("\nEnter new phone number: ");
        String newPhone = NavigationManager.readInput(scanner);
        
        if (newPhone == null) return;

        userService.updateClientPhone(userEmail, newPhone);

        // Print success message
        System.out.println("\n✅ Phone number updated successfully!");
        
        System.out.println("\nPress Enter to continue...");
        scanner.nextLine();
    }

    private void updatePreferredLocation() {
        Formatter.clearScreen();
        Formatter.printHeader(new String[]{"UPDATE PREFERRED LOCATION"}, 10);
        
        // Display available locations
        LocationService locationService = GlobalState.getInstance().getLocationService();
        locationService.displayLocations();
        
        System.out.print("\nSelect your new preferred pickup location (enter number): ");
        try {
            int locationIndex = Integer.parseInt(NavigationManager.readInput(scanner));
            String newLocation = locationService.getLocationByIndex(locationIndex);
            
            if (newLocation == null) {
                System.out.println("\n❌ Invalid location selection.");
            } else {
                userService.updateClientPreferredLocation(userEmail, newLocation);
                System.out.println("\n✅ Preferred location updated successfully!");
            }
        } catch (NumberFormatException e) {
            System.out.println("\n❌ Invalid input. Please enter a number.");
        }
        
        System.out.println("\nPress Enter to continue...");
        scanner.nextLine();
    }
}