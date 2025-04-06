package model;

public enum RideStatus {
    REQUESTED,    // Initial state when client requests a ride
    ACCEPTED,     // Driver has accepted the ride
    REJECTED,     // Driver has rejected the ride
    CANCELLED,    // Client has cancelled the ride
    IN_PROGRESS,  // Driver has picked up the client
    COMPLETED,    // Ride has been completed
    FAILED        // Ride failed for some reason
}