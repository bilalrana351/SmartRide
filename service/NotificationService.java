package service;

import model.notification.NotificationMessage;
import model.notification.NotificationType;
import model.Ride;
import model.Location;

public class NotificationService {
    private final RideNotificationServer notificationServer;

    public NotificationService() {
        this.notificationServer = RideNotificationServer.getInstance();
    }

    public void sendRideRequest(String clientId, String driverId, Ride ride) {
        NotificationMessage notification = new NotificationMessage();
        notification.setType(NotificationType.RIDE_REQUEST);
        notification.setSenderId(clientId);
        notification.setReceiverId(driverId);
        notification.setContent("New ride request");
        notification.setData(ride);

        notificationServer.sendNotification(driverId, notification);
    }

    public void sendRideResponse(String driverId, String clientId, boolean accepted, String message) {
        NotificationMessage notification = new NotificationMessage();
        notification.setType(NotificationType.RIDE_RESPONSE);
        notification.setSenderId(driverId);
        notification.setReceiverId(clientId);
        notification.setContent(message);
        notification.setData(accepted);

        notificationServer.sendNotification(clientId, notification);
    }

    public void sendLocationUpdate(String senderId, String receiverId, Location location) {
        NotificationMessage notification = new NotificationMessage();
        notification.setType(NotificationType.LOCATION_UPDATE);
        notification.setSenderId(senderId);
        notification.setReceiverId(receiverId);
        notification.setContent("Location updated");
        notification.setData(location);

        notificationServer.sendNotification(receiverId, notification);
    }
}