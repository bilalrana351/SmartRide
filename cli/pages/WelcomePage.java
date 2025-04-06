package cli.pages;

import cli.utils.*;

import java.text.Normalizer;
import java.util.Scanner;

public class WelcomePage {
    private Scanner scanner;
    private LoginPage loginPage;
    private RegisterPage registerPage;

    public WelcomePage() {
        scanner = new Scanner(System.in);
        loginPage = new LoginPage();
        registerPage = new RegisterPage();
    }

    public void display() {
        NavigationManager.clearHistory(); // Clear navigation history when starting fresh
        NavigationManager.navigate(() -> displayMenu());
    }

    private void displayMenu() {
        Formatter.clearScreen();
        printHeader();
        printMenu();

        String choice = NavigationManager.readInput(scanner);

        if (choice == null) {
            return; // User pressed back
        }

        switch (choice) {
            case "1":
                NavigationManager.navigate(() -> loginPage.display("CLIENT"));
                break;
            case "2":
                NavigationManager.navigate(() -> loginPage.display("RIDER"));
                break;
            case "3":
                NavigationManager.navigate(() -> registerPage.display("CLIENT"));
                break;
            case "4":
                NavigationManager.navigate(() -> registerPage.display("RIDER"));
                break;
            case "5":
                System.out.println("\nThank you for using SmartRide. Goodbye!");
                System.exit(0);
            default:
                System.out.println("\nInvalid choice. Please try again.");
                displayMenu();
        }
    }

    protected void printHeader() {
        String[] rows = {
                "SMART RIDE",
                "Your Journey, Your Way"
        };
        Formatter.printHeader(rows, 6);
    }

    protected void printMenu() {
        String[] menuItems = {
                "",
                "1. Login as Client",
                "2. Login as Rider",
                "3. Register as Client",
                "4. Register as Rider",
                "5. Exit",
                ""
        };
        Formatter.printMenu(menuItems, 6);
        System.out.print("\nPlease select an option (1-5): ");
    }
}