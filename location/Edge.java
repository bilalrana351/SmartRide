package location;

public class Edge {
    private final String destination;
    private final int weight;

    public Edge(String destination, int weight) {
        this.destination = destination;
        this.weight = weight;
    }

    public String getDestination() {
        return destination;
    }

    public int getWeight() {
        return weight;
    }

    @Override
    public String toString() {
        return "Edge{" +
                "destination='" + destination + '\'' +
                ", weight=" + weight +
                '}';
    }
}
