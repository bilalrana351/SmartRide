package model.notification;

import java.util.LinkedList;
import model.Ride;

public class RideRequestQueue {
    private LinkedList<NotificationMessage> pendingRequests;
    private static RideRequestQueue instance;

    private RideRequestQueue() {
        pendingRequests = new LinkedList<>();
    }

    public static RideRequestQueue getInstance() {
        if (instance == null) {
            instance = new RideRequestQueue();
        }
        return instance;
    }

    public void addRequest(NotificationMessage notification) {
        pendingRequests.addLast(notification);
    }

    public void removeRequest(String clientId) {
        pendingRequests.removeIf(notification -> 
            notification.getSenderId().equals(clientId)
        );
    }

    public NotificationMessage getNextRequest() {
        return pendingRequests.isEmpty() ? null : pendingRequests.getFirst();
    }

    public void removeCurrentRequest() {
        if (!pendingRequests.isEmpty()) {
            pendingRequests.removeFirst();
        }
    }

    public boolean hasRequests() {
        return !pendingRequests.isEmpty();
    }

    public int getRequestCount() {
        return pendingRequests.size();
    }
} 