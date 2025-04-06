package service.websocket;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
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

public class UserWebSocketClient extends WebSocketClient {
    private ObjectMapper mapper;
    private Consumer<NotificationMessage> messageHandler;

    public UserWebSocketClient(String userId, String userType, Consumer<NotificationMessage> messageHandler) throws Exception {
        super(new URI("ws://localhost:8887"), createHeaders(userId));
        this.messageHandler = messageHandler;
        this.mapper = TimeStampedMapper.GetMapper();
        this.connect();
    }

    private static Map<String, String> createHeaders(String userId) {
        Map<String, String> headers = new HashMap<>();
        headers.put("user-id", userId);
        headers.put("user-type", "client");
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
            
            // Convert data to proper type based on notification type
            if (notification.getType() == NotificationType.RIDE_RESPONSE && notification.getData() != null) {
                // For ride response, data is a boolean
                notification.setData(Boolean.valueOf(notification.getData().toString()));
            }
            
            messageHandler.accept(notification);
        } catch (JsonProcessingException e) {
            System.err.println("Error processing message: " + e.getMessage());
        }
    }

    public void sendRideRequest(String clientId, String driverId, Ride ride) {
        try {
            ObjectNode message = mapper.createObjectNode();
            message.put("route", "/ride/request");

            NotificationMessage notification = new NotificationMessage();
            notification.setSenderId(clientId);
            notification.setReceiverId(driverId);
            notification.setData(ride);
            notification.setType(NotificationType.RIDE_REQUEST);

            message.set("payload", mapper.valueToTree(notification));
            this.send(mapper.writeValueAsString(message));
        } catch (Exception e) {
            System.err.println("Error sending ride request: " + e.getMessage());
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
}