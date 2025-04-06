package location;

import java.util.*;

public class Graph {
    private final Map<String, List<Edge>> adjList = new HashMap<>();

    public void addNode(String location) {
        adjList.putIfAbsent(location, new ArrayList<>());
    }

    public void addEdge(String from, String to, int weight) {
        // Ensure both nodes are initialized in the adjacency list
        adjList.putIfAbsent(from, new ArrayList<>());
        adjList.putIfAbsent(to, new ArrayList<>());

        // Add the edge for an undirected graph
        adjList.get(from).add(new Edge(to, weight));
        adjList.get(to).add(new Edge(from, weight));
    }

    public List<Edge> getEdges(String location) {
        return adjList.getOrDefault(location, new ArrayList<>());
    }

    public Map<String, List<Edge>> getAdjList() {
        return adjList;
    }
}
