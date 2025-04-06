package cli.utils;

import java.util.Stack;
import java.util.Scanner;

public class NavigationManager {
    private static Stack<Runnable> navigationStack = new Stack<>();
    private static final String BACK_KEY = "\b";  // Backspace character

    public static void navigate(Runnable nextPage) {
        navigationStack.push(nextPage);
        nextPage.run();
    }

    public static boolean goBack() {
        if (navigationStack.size() > 1) {
            navigationStack.pop(); // Remove current page
            Runnable previousPage = navigationStack.peek();
            previousPage.run();
            return true;
        }
        return false;
    }

    public static String readInput(Scanner scanner) {
        String input = scanner.nextLine();
        try {
            if (input.equals(BACK_KEY) || input.toLowerCase().equals("back")) {
                goBack();
                return null;
            }
        }
        catch (Exception e) {
            return null;
        }
        return input;
    }

    public static void clearHistory() {
        navigationStack.clear();
    }
}