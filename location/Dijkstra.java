package location;

import java.util.*;

public class Dijkstra {

    private final Graph graph;

    public Dijkstra(Graph graph) {
        this.graph = graph;
    }

    public Map<String, Integer> shortestPath(String start) {
        PriorityQueue<Node> pq = new PriorityQueue<>(Comparator.comparingInt(n -> n.distance));
        Map<String, Integer> distances = new HashMap<>();
        Set<String> visited = new HashSet<>();

        // Initialize distances with infinity
        for (String location : graph.getAdjList().keySet()) {
            distances.put(location, Integer.MAX_VALUE);
        }

        // Distance to the starting point is 0
        distances.put(start, 0);
        pq.add(new Node(start, 0));

        while (!pq.isEmpty()) {
            Node current = pq.poll();

            if (visited.contains(current.name)) continue;
            visited.add(current.name);

            for (Edge edge : graph.getEdges(current.name)) {
                int newDist = distances.get(current.name) + edge.getWeight();
                if (newDist < distances.get(edge.getDestination())) {
                    distances.put(edge.getDestination(), newDist);
                    pq.add(new Node(edge.getDestination(), newDist));
                }
            }
        }

        return distances;
    }

    private static class Node {
        String name;
        int distance;

        public Node(String name, int distance) {
            this.name = name;
            this.distance = distance;
        }
    }
}
