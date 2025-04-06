package cli.pages;

import cli.utils.*;
import java.util.Scanner;
import service.UserService;

public class LoginPage {
    private Scanner scanner;
    private UserService userService;

    public LoginPage() {
        scanner = new Scanner(System.in);
        userService = GlobalState.getInstance().getUserService();
    }

    public void display(String userType) {
        int attempts = 0;
        boolean loginSuccess = false;

        while (!loginSuccess) {
            Formatter.clearScreen();

            if (attempts == 0) {
                printHeader(userType);
            }

            System.out.print("\n📧 Email: ");
            String email = NavigationManager.readInput(scanner);
            if (email == null) return;

            System.out.print("🔒 Password: ");
            String password = NavigationManager.readInput(scanner);
            if (password == null) return;

            System.out.println("\nProcessing login...");
            try {
                Thread.sleep(500);
                loginSuccess = userService.login(email, password, userType);
                
                if (loginSuccess) {
                    System.out.println("\n✅ Login successful!");
                    Thread.sleep(1000);
                    
                    // Set global state
                    GlobalState.getInstance().setCurrentUserEmail(email);
                    GlobalState.getInstance().setUserType(userType);

                    // Navigate to appropriate dashboard
                    if (userType.equalsIgnoreCase("CLIENT")) {
                        NavigationManager.navigate(() -> new UserDashboard(email).display());
                    } else {
                        NavigationManager.navigate(() -> new RiderDashboard(email).display());
                    }
                } else {
                    System.out.println("\n❌ Invalid email or password.");
                    System.out.println("Press Enter to try again...");
                    scanner.nextLine();
            }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            attempts++;
        }
    }

    private void printHeader(String userType) {
        String[] rows = {
                "LOGIN PAGE",
                userType + " LOGIN"
        };
        Formatter.printHeader(rows, 6);
    }
}