package model;

public class Rider {
    private String name;
    private String email;
    private String phoneNumber;
    private String password;
    private String vehicleDetails;
    private String licenseNumber;
    private String currentLocation;
    private boolean available = true; // By default, riders are available

    // Constructor
    public Rider(String name, String email, String phoneNumber, String password, String vehicleDetails, String licenseNumber, String currentLocation) {
        this.name = name;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.password = password;
        this.vehicleDetails = vehicleDetails;
        this.licenseNumber = licenseNumber;
        this.currentLocation = currentLocation;
        this.available = true; // By default, riders are available
    }

    // Default Constructor
    public Rider() {
        this.available = true; // By default, riders are available
    }

    // Getters and Setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getVehicleDetails() {
        return vehicleDetails;
    }

    public void setVehicleDetails(String vehicleDetails) {
        this.vehicleDetails = vehicleDetails;
    }

    public String getLicenseNumber() {
        return licenseNumber;
    }

    public void setLicenseNumber(String licenseNumber) {
        this.licenseNumber = licenseNumber;
    }

    public String getCurrentLocation() {
        return currentLocation;
    }

    public void setCurrentLocation(String currentLocation) {
        this.currentLocation = currentLocation;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public boolean isAvailable() {
        return available;
    }

    public void setAvailable(boolean available) {
        this.available = available;
    }

    // toString method for debugging and logging purposes
    @Override
    public String toString() {
        return "Rider{" +
                "name='" + name + '\'' +
                ", email='" + email + '\'' +
                ", phoneNumber='" + phoneNumber + '\'' +
                ", vehicleDetails='" + vehicleDetails + '\'' +
                ", licenseNumber='" + licenseNumber + '\'' +
                ", currentLocation='" + currentLocation + '\'' +
                ", available=" + available +
                '}';
    }
}
