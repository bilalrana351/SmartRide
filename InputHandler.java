import java.util.Scanner;

public class InputHandler {
    private static volatile boolean notificationActive = false;
    private static Thread inputThread;

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        // Start a thread to simulate notification
        Thread notificationThread = new Thread(() -> {
            try {
                Thread.sleep(5000); // Simulate a delay before notification
                displayNotification(scanner);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
        notificationThread.start();

        // Start main input process
        handleUserInput(scanner);
    }

    private static void handleUserInput(Scanner scanner) {
        inputThread = new Thread(() -> {
            try {
                System.out.println("Type something: ");
                while (!notificationActive) {
                    if (scanner.hasNext()) {
                        String input = scanner.nextLine();
                        System.out.println("You typed: " + input);
                    }
                }
            } catch (Exception e) {
                System.out.println("Input process interrupted.");
            }
        });
        inputThread.start();
    }

    private static void displayNotification(Scanner scanner) {
        notificationActive = true;
        if (inputThread != null && inputThread.isAlive()) {
            inputThread.interrupt(); // Interrupt the old input process
        }

        System.out.println("\n⚠️  Notification received! Please respond.");
        System.out.print("Notification Input: ");
        if (scanner.hasNext()) {
            String notificationInput = scanner.nextLine();
            System.out.println("Notification Response: " + notificationInput);
        }

        // Reset to allow normal input after notification
        notificationActive = false;
        handleUserInput(scanner);
    }
}
