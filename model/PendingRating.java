package model;

public class PendingRating {
    private final String driverId;
    private boolean processed;

    public PendingRating(String driverId) {
        this.driverId = driverId;
        this.processed = false;
    }

    public String getDriverId() {
        return driverId;
    }

    public boolean isProcessed() {
        return processed;
    }

    public void setProcessed(boolean processed) {
        this.processed = processed;
    }
}