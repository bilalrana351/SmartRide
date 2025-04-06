package cli.pages;

import cli.utils.*;
import service.LocationService;
import service.UserService;
import model.Rider;

public class RiderAccountManagementPage {
    private final UserService userService;
    private final String userEmail;
    private final java.util.Scanner scanner;

    public RiderAccountManagementPage(String userEmail) {
        this.userService = GlobalState.getInstance().getUserService();
        this.userEmail = userEmail;
        this.scanner = new java.util.Scanner(System.in);
    }

    public void display() {
        while (true) {
            Formatter.clearScreen();
            Rider rider = userService.getRiderById(userEmail);
            
            Formatter.printHeader(new String[]{
                "RIDER ACCOUNT MANAGEMENT",
                "Current Settings:",
                "Name: " + rider.getName(),
                "Email: " + rider.getEmail(),
                "Phone: " + rider.getPhoneNumber(),
                "Vehicle Details: " + rider.getVehicleDetails(),
                "License Number: " + rider.getLicenseNumber(),
                "Current Location: " + rider.getCurrentLocation()
            }, 10);

            String[] menuItems = {
                "",
                "1. Update Phone Number",
                "2. Update Vehicle Details",
                "3. Update License Number",
                "4. Update Current Location",
                "5. Delete Account",
                "6. Back to Dashboard",
                ""
            };
            Formatter.printMenu(menuItems, 6);

            System.out.print("\nPlease select an option (1-6): ");
            String choice = NavigationManager.readInput(scanner);

            if (choice == null) return;

            switch (choice) {
                case "1":
                    updatePhoneNumber();
                    break;
                case "2":
                    updateVehicleDetails();
                    break;
                case "3":
                    updateLicenseNumber();
                    break;
                case "4":
                    updateCurrentLocation();
                    break;
                case "5":
                    if (deleteAccount()) {
                        return; // Return to welcome page after successful deletion
                    }
                    break;
                case "6":
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
        if (!userService.login(userEmail, password, "RIDER")) {
            System.out.println("\n❌ Incorrect password. Account deletion cancelled.");
            System.out.println("\nPress Enter to continue...");
            scanner.nextLine();
            return false;
        }
        
        // Delete account
        boolean deleted = userService.deleteRider(userEmail);
        
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

        userService.updateRiderPhone(userEmail, newPhone);
        
        System.out.println("\n✅ Phone number updated successfully!");
        System.out.println("\nPress Enter to continue...");
        scanner.nextLine();
    }

    private void updateVehicleDetails() {
        Formatter.clearScreen();
        Formatter.printHeader(new String[]{"UPDATE VEHICLE DETAILS"}, 10);
        
        System.out.print("\nEnter new vehicle details: ");
        String newVehicleDetails = NavigationManager.readInput(scanner);
        
        if (newVehicleDetails == null) return;
        
        userService.updateRiderVehicleDetails(userEmail, newVehicleDetails);
        
        System.out.println("\n✅ Vehicle details updated successfully!");
        System.out.println("\nPress Enter to continue...");
        scanner.nextLine();
    }

    private void updateLicenseNumber() {
        Formatter.clearScreen();
        Formatter.printHeader(new String[]{"UPDATE LICENSE NUMBER"}, 10);
        
        System.out.print("\nEnter new license number: ");
        String newLicenseNumber = NavigationManager.readInput(scanner);
        
        if (newLicenseNumber == null) return;
        
        userService.updateRiderLicenseNumber(userEmail, newLicenseNumber);
        
        System.out.println("\n✅ License number updated successfully!");
        System.out.println("\nPress Enter to continue...");
        scanner.nextLine();
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
} 