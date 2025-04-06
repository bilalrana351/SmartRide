package service;

import model.Ride;
import java.util.HashMap;

/**
 * Adapted from the original Navigationer class to handle ride history navigation
 */
public class RideHistoryNavigator {
    private Node head = null;
    private Node tail = null;
    private final HashMap<String, Node> hashMap = new HashMap<>();
    private final String userEmail;

    private class Node {
        private final Ride ride;
        Node nextInList;
        Node previousInList;
        
        Node(Ride ride) {
            this.ride = ride;
            this.nextInList = null;
            this.previousInList = null;
        }
    }

    public RideHistoryNavigator(String userEmail) {
        this.userEmail = userEmail;
    }

    public void add(Ride ride) {
        // Create the node
        Node node = new Node(ride);

        // Check if it is the first node
        if (hashMap.isEmpty()) {
            head = node;
            tail = node;
        } else {
            tail.nextInList = node;
            node.previousInList = tail;
            tail = node;
        }

        // Add the node to the hashmap
        hashMap.put(ride.getId(), node);
    }

    public Ride getCurrentRide() {
        return tail != null ? tail.ride : null;
    }

    public Ride getNextRide() throws Exception {
        if (tail == null || tail.nextInList == null) {
            throw new Exception("No next ride available");
        }
        tail = tail.nextInList;
        return tail.ride;
    }

    public Ride getPreviousRide() throws Exception {
        if (tail == null || tail.previousInList == null) {
            throw new Exception("No previous ride available");
        }
        tail = tail.previousInList;
        return tail.ride;
    }

    public boolean hasNext() {
        return tail != null && tail.nextInList != null;
    }

    public boolean hasPrevious() {
        return tail != null && tail.previousInList != null;
    }
}