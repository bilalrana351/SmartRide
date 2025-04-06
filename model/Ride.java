package model;

import java.time.Instant;
import java.util.UUID;

public class Ride {
    private String id;
    private String clientId;
    private String driverId;
    private String pickupLocation;
    private String destination;
    private double distance = 0.0;
    private double trafficIntensity = 0.0;
    private double demandFactor = 0.0;
    private RideStatus status;
    private Instant requestTime;
    private Instant completionTime;
    private double fare;
    private int maxDrivers = 3; // Default value

    public Ride() {
        this.id = UUID.randomUUID().toString();
        this.requestTime = Instant.now();
        this.status = RideStatus.REQUESTED;
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getClientId() { return clientId; }
    public void setClientId(String clientId) { this.clientId = clientId; }

    public String getDriverId() { return driverId; }
    public void setDriverId(String driverId) { this.driverId = driverId; }

    public String getPickupLocation() { return pickupLocation; }
    public void setPickupLocation(String pickupLocation) { this.pickupLocation = pickupLocation; }

    public String getDestination() { return destination; }
    public void setDestination(String destination) { this.destination = destination; }

    public RideStatus getStatus() { return status; }
    public void setStatus(RideStatus status) { this.status = status; }

    public Instant getRequestTime() { return requestTime; }
    public void setRequestTime(Instant requestTime) { this.requestTime = requestTime; }

    public double getDistance() { return distance; }
    public void setDistance(double distance) { this.distance = distance; }

    public double getTrafficIntensity() { return trafficIntensity; }
    public void setTrafficIntensity(double trafficIntensity) { this.trafficIntensity = trafficIntensity; }

    public double getDemandFactor() { return demandFactor; }
    public void setDemandFactor(double demandFactor) { this.demandFactor = demandFactor; }

    public Instant getCompletionTime() { return completionTime; }
    public void setCompletionTime(Instant completionTime) { this.completionTime = completionTime; }

    public double getFare() { return fare; }
    public void setFare(double fare) { this.fare = fare; }

    public int getMaxDrivers() { return maxDrivers; }
    public void setMaxDrivers(int maxDrivers) { this.maxDrivers = maxDrivers; }

    @Override
    public String toString() {
        return "Ride{" +
                "id='" + id + '\'' +
                ", clientId='" + clientId + '\'' +
                ", driverId='" + driverId + '\'' +
                ", pickupLocation='" + pickupLocation + '\'' +
                ", destination='" + destination + '\'' +
                ", distance=" + distance +
                ", status=" + status +
                ", requestTime=" + requestTime +
                ", completionTime=" + completionTime +
                ", fare=" + fare +
                '}';
    }
}