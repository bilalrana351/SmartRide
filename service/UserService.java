package service;
import java.util.List;
import model.Client;
import model.Rider;

public interface UserService {
    /**
     * Registers a new client.
     *
     * @param client the client object containing registration details
     * @return a message indicating the result of the registration
     */
    String registerClient(Client client);

    /**
     * Registers a new rider.
     *
     * @param rider the rider object containing registration details
     * @return a message indicating the result of the registration
     */
    String registerRider(Rider rider);

    /**
     * Authenticates a user login based on email, password, and user type.
     *
     * @param email    the user's email address
     * @param password the user's password
     * @param userType the type of user (e.g., "client" or "rider")
     * @return true if authentication is successful, false otherwise
     */
    boolean login(String email, String password, String userType);

    /**
     * Retrieves a rider object by its ID.
     *
     * @param riderId the unique identifier of the rider
     * @return the Rider object if found, or null if not
     */
    Rider getRiderById(String riderId);

    /**
     * Deletes a rider by its ID.
     *
     * @param riderId the unique identifier of the rider
     * @return true if deletion is successful, false otherwise
     */
    boolean deleteRider(String riderId);

    /**
     * Deletes a client by its ID.
     *
     * @param clientId the unique identifier of the client
     * @return true if deletion is successful, false otherwise
     */
    boolean deleteClient(String clientId);

    /**
     * Retrieves a client object by its ID.
     *
     * @param clientId the unique identifier of the client
     * @return the Client object if found, or null if not
     */
    Client getClientById(String clientId);

    /**
     * Updates the phone number of a client.
     *
     * @param clientId the unique identifier of the client
     * @param newPhoneNumber the new phone number to set
     */
    void updateClientPhone(String clientId, String newPhoneNumber);

    /**
     * Updates the preferred pickup location of a client.
     *
     * @param clientId the unique identifier of the client
     * @param newPreferredLocation the new preferred pickup location to set
     */
    void updateClientPreferredLocation(String clientId, String newPreferredLocation);

    /**
     * Updates the phone number of a rider.
     *
     * @param riderId the unique identifier of the rider
     * @param newPhoneNumber the new phone number to set
     */
    void updateRiderPhone(String riderId, String newPhoneNumber);

    /**
     * Updates the vehicle details of a rider.
     *
     * @param riderId the unique identifier of the rider
     * @param newVehicleDetails the new vehicle details to set
     */
    void updateRiderVehicleDetails(String riderId, String newVehicleDetails);

    /**
     * Updates the license number of a rider.
     *
     * @param riderId the unique identifier of the rider
     * @param newLicenseNumber the new license number to set
     */
    void updateRiderLicenseNumber(String riderId, String newLicenseNumber);

    /**
     * Updates the current location of a rider.
     *
     * @param riderId the unique identifier of the rider
     * @param newLocation the new current location to set
     */
    void updateRiderCurrentLocation(String riderId, String newLocation);

    /**
     * Finds the nearest available driver to a given pickup location.
     *
     * @param pickupLocation the location where the client needs pickup
     * @return the ID of the nearest available driver, or null if no drivers are available
     */
    String findNearestAvailableDriver(String pickupLocation);

        /**
     * Finds the nearest available drivers to a given pickup location.
     *
     * @param pickupLocation the location where the client needs pickup
     * @param maxDrivers maximum number of drivers to return (default 3)
     * @return List of IDs of the nearest available drivers, or empty list if no drivers are available
     */
    List<String> findNearestAvailableDrivers(String pickupLocation, int maxDrivers);

    // Add overloaded method with default value
    default List<String> findNearestAvailableDrivers(String pickupLocation) {
        return findNearestAvailableDrivers(pickupLocation, 4);
    }
}