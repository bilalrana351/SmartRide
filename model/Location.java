package model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Location {
    private String name;
    private List<Connection> connections;

    // Default Constructor
    public Location() {
        this.connections = new ArrayList<>();
    }

    // Parameterized Constructor
    public Location(String name, List<Connection> connections) {
        this.name = name;
        this.connections = connections != null ? connections : new ArrayList<>();
    }

    // Getter and Setter for name
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    // Getter and Setter for connections
    public List<Connection> getConnections() {
        return connections;
    }

    public void setConnections(List<Connection> connections) {
        this.connections = connections != null ? connections : new ArrayList<>();
    }

    // Adds a connection to the list
    public void addConnection(Connection connection) {
        if (connections == null) {
            connections = new ArrayList<>();
        }
        connections.add(connection);
    }

    // Connection Inner Class
    public static class Connection {
        private String to;
        private int distance;

        // Default Constructor
        public Connection() {}

        // Parameterized Constructor
        public Connection(String to, int distance) {
            this.to = to;
            this.distance = distance;
        }

        // Getter and Setter for 'to'
        public String getTo() {
            return to;
        }

        public void setTo(String to) {
            this.to = to;
        }

        // Getter and Setter for 'distance'
        public int getDistance() {
            return distance;
        }

        public void setDistance(int distance) {
            this.distance = distance;
        }

        // Override equals and hashCode for proper comparison
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Connection that = (Connection) o;
            return distance == that.distance && Objects.equals(to, that.to);
        }

        @Override
        public int hashCode() {
            return Objects.hash(to, distance);
        }

        @Override
        public String toString() {
            return "Connection{" +
                    "to='" + to + '\'' +
                    ", distance=" + distance +
                    '}';
        }
    }

    // Override equals and hashCode for proper comparison
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Location location = (Location) o;
        return Objects.equals(name, location.name) &&
                Objects.equals(connections, location.connections);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, connections);
    }

    @Override
    public String toString() {
        return "Location{" +
                "name='" + name + '\'' +
                ", connections=" + connections +
                '}';
    }
}
