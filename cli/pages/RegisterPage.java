package cli.pages;

import cli.utils.*;

import java.text.Normalizer;
import java.util.Scanner;
import model.Client;
import model.Rider;
import service.LocationService;
import service.UserService;
import service.UserServiceImpl;

public class RegisterPage {
    private Scanner scanner;
    private UserService userService;

    public RegisterPage() {
        scanner = new Scanner(System.in);
        userService = new UserServiceImpl();
    }

    public void display(String userType) {
        Formatter.clearScreen();
        printHeader(userType);
    
        System.out.print("\nEnter email: ");
        String email = NavigationManager.readInput(scanner);
        if (email == null) return;
    
        System.out.print("Enter name: ");
        String name = NavigationManager.readInput(scanner);
        if (name == null) return;
    
        System.out.print("Enter phone number: ");
        String phone = NavigationManager.readInput(scanner);
        if (phone == null) return;
    
        // Additional fields for Rider registration
        String vehicleDetails = null;
        String licenseNumber = null;
        String currentLocation = null;
        String locationPreference = null;
        
        if (userType.equalsIgnoreCase("RIDER")) {
            System.out.print("Enter vehicle details: ");
            vehicleDetails = NavigationManager.readInput(scanner);
            if (vehicleDetails == null) return;

            System.out.print("Enter license number: ");
            licenseNumber = NavigationManager.readInput(scanner);
            if (licenseNumber == null) return;

            // Display available locations
            LocationService locationService = GlobalState.getInstance().getLocationService();
            locationService.displayLocations();
            
            System.out.print("\nSelect your current location (enter number): ");
            try {
                int locationIndex = Integer.parseInt(NavigationManager.readInput(scanner));
                currentLocation = locationService.getLocationByIndex(locationIndex);
                if (currentLocation == null) {
                    System.out.println("Invalid location selection. Registration cancelled.");
                    return;
                }
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a number.");
                return;
            }
        } else {
            // For client registration
            LocationService locationService = GlobalState.getInstance().getLocationService();
            locationService.displayLocations();
            
            System.out.print("\nSelect your preferred pickup location (enter number): ");
            try {
                int locationIndex = Integer.parseInt(NavigationManager.readInput(scanner));
                locationPreference = locationService.getLocationByIndex(locationIndex);
                if (locationPreference == null) {
                    System.out.println("Invalid location selection. Registration cancelled.");
                    return;
                }
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a number.");
                return;
            }
        }
    
        String password;
        String confirmPassword;

        do {
            System.out.print("Enter Password: ");
            password = NavigationManager.readInput(scanner);
            if (password == null) return;

            System.out.print("Confirm Password: ");
            confirmPassword = NavigationManager.readInput(scanner);
            if (confirmPassword == null) return;

            if (!password.equals(confirmPassword)) {
                System.out.println("\nPasswords do not match! Please try again.");
            }
        } while (!password.equals(confirmPassword));


        System.out.println("\nProcessing registration...");
        try {
            Thread.sleep(500);
            String result;
            if (userType.equalsIgnoreCase("CLIENT")) {
                Client client = new Client();
                client.setEmail(email);
                client.setName(name);
                client.setPhoneNumber(phone);
                client.setPassword(password);
                client.setPreferredPickupLocation(locationPreference);
                result = userService.registerClient(client);
            } else {
                Rider rider = new Rider();
                rider.setEmail(email);
                rider.setName(name);
                rider.setPhoneNumber(phone);
                rider.setPassword(password);
                rider.setVehicleDetails(vehicleDetails);
                rider.setLicenseNumber(licenseNumber);
                rider.setCurrentLocation(currentLocation);
                result = userService.registerRider(rider);
            }

            if (result.contains("successfully")) {
                System.out.println("\n✅ " + result);
            } else {
                System.out.println("\n❌ " + result);
            }

            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        System.out.println("\nPress Enter to continue or Backspace to go back...");
        NavigationManager.readInput(scanner);
    }


    private void printHeader(String userType) {
        String[] rows = {
                "REGISTRATION PAGE",
                userType + " REGISTRATION"
        };
        Formatter.printHeader(rows, 6);
    }
}