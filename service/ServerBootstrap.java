package service;

public class ServerBootstrap {
    private static boolean isServerStarted = false;
    private static final Object lock = new Object();
    
    public static void ensureServerRunning() {
        synchronized (lock) {
            if (!isServerStarted) {
                RideNotificationServer server = RideNotificationServer.getInstance();
                isServerStarted = true;
                System.out.println("Notification server started on port 8887");
            }
        }
    }
}