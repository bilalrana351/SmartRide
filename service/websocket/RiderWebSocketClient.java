package service.websocket;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import model.Location;
import model.Ride;
import model.TimeStampedMapper;
import model.notification.NotificationMessage;
import model.notification.NotificationType;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class RiderWebSocketClient extends WebSocketClient {
    private final ObjectMapper mapper;
    private Consumer<NotificationMessage> messageHandler;

    public RiderWebSocketClient(String riderId, Consumer<NotificationMessage> messageHandler) throws Exception {
        super(new URI("ws://localhost:8887"), createHeaders(riderId));
        this.messageHandler = messageHandler;
        this.mapper = TimeStampedMapper.GetMapper();
        this.connect();
    }

    private static Map<String, String> createHeaders(String riderId) {
        Map<String, String> headers = new HashMap<>();
        headers.put("user-id", riderId);
        headers.put("user-type", "rider");
        return headers;
    }

    @Override
    public void onOpen(ServerHandshake handshake) {
        System.out.println("Connected to notification server");
    }


    @Override
    public void onMessage(String message) {
        try {
            ObjectMapper mapper = TimeStampedMapper.GetMapper();
            NotificationMessage notification = mapper.readValue(message, NotificationMessage.class);
            
            if (notification.getType() == NotificationType.RIDE_REQUEST && notification.getData() != null) {
                notification.setData(mapper.convertValue(notification.getData(), Ride.class));
            }
            
            if (messageHandler != null) {
                messageHandler.accept(notification);
            }
        } catch (JsonProcessingException e) {
            System.err.println("Error processing message: " + e.getMessage());
        }
    }

    public void sendRideResponse(String driverId, String clientId, String rideId, boolean accepted) {
        try {
            ObjectNode message = mapper.createObjectNode();
            message.put("route", "/ride/response");

            NotificationMessage notification = new NotificationMessage();
            notification.setSenderId(driverId);
            notification.setReceiverId(clientId);
            notification.setData(accepted);
            notification.setType(NotificationType.RIDE_RESPONSE);
            notification.setRideId(rideId);

            message.set("payload", mapper.valueToTree(notification));
            this.send(mapper.writeValueAsString(message));
        } catch (Exception e) {
            System.err.println("Error sending ride response: " + e.getMessage());
        }
    }

    public void sendLocationUpdate(String riderId, Location location) {
        try {
            ObjectNode message = mapper.createObjectNode();
            message.put("route", "/location/update");

            NotificationMessage notification = new NotificationMessage();
            notification.setSenderId(riderId);
            notification.setData(location);
            notification.setType(NotificationType.LOCATION_UPDATE);

            message.set("payload", mapper.valueToTree(notification));
            this.send(mapper.writeValueAsString(message));
        } catch (Exception e) {
            System.err.println("Error sending location update: " + e.getMessage());
        }
    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
        System.out.println("Connection closed: " + reason);
    }

    @Override
    public void onError(Exception ex) {
        System.err.println("WebSocket error: " + ex.getMessage());
    }

    public void setMessageHandler(Consumer<NotificationMessage> handler) {
        this.messageHandler = handler;
    }
}