package se.su.inlupp;

import java.util.*;

public class ListGraph<N> implements Graph<N> {
  private Map<N, List<Edge<N>>> adjacencyList = new HashMap<>();

  private boolean dfs(N current, N target, Set<N> visited) {
    if (current.equals(target))
      return true;
    visited.add(current);

    for (Edge<N> edge : adjacencyList.get(current)) {
      N neighbour = edge.getDestination();
      if (!visited.contains(neighbour)) {
        if (dfs(neighbour, target, visited))
          return true;
      }
    }
    return false;
  }

  private boolean dfsPath(N current, N target, Set<N> visited, List<Edge<N>> path) {
    if (current.equals(target)) {
      return true;
    }

    visited.add(current);

    for (Edge<N> edge : adjacencyList.get(current)) {
      N neighbour = edge.getDestination();

      if (!visited.contains(neighbour)) {
        path.add(edge); //Lägg till kant innan rekursion

        if (dfsPath(neighbour, target, visited, path)) {
          return true;
        }

        path.remove(path.size() - 1); //Ta bort om vi inte hittade vägen
      }
    }

    return false;
  }

  // Om nod redan finns gör inget annar lägg till ny nod
  public void add(N node) {
    if (!adjacencyList.containsKey(node)) {
      adjacencyList.put(node, new ArrayList<>());
    }
  }

  // Om nod inte finns printa att den inte finns, annars tar bort noden
  public void remove(N node) {
    // element check så att noden finns
    if (!adjacencyList.containsKey(node)) {
      throw new NoSuchElementException("Noden finns inte i grafen" + node);
    }

    // hämtar alla kanter från noden
    List<Edge<N>> edges = getEdgesFrom(node);

    // går igenom alla kopplingar och tar bort dom
    for (Edge<N> edge : edges) {
      N neighbour = edge.getDestination();
      disconnect(node, neighbour);
    }

    // tar tillslut bort noden helt
    adjacencyList.remove(node);
  }

  // länkar samman två noder
  public void connect(N node1, N node2, String name, int weigth) {
    // kolla att noderna finns
    if (!adjacencyList.containsKey(node1) || !adjacencyList.containsKey(node2)) {
      throw new NoSuchElementException("En eller båda noderna saknas.");
    }

    // kolla så att vikt inte är negativ
    if (weigth < 0) {
      throw new IllegalArgumentException("Vikt negativ.");
    }

    // kolla så att det inte finns redan existerande kant
    if (getEdgeBetween(node1, node2) != null) {
      throw new IllegalStateException("Det finns redan en kant mellan dessa noder.");
    }

    // lägger till och skapar kanterna
    adjacencyList.get(node1).add(new Edge<>(node2, name, weigth));
    adjacencyList.get(node2).add(new Edge<>(node1, name, weigth));
  }

  // avkopplar två noder
  public void disconnect(N node1, N node2) {
    // element check så att båda noderna som skrivs in faktiskt finns
    if (!adjacencyList.containsKey(node1) || !adjacencyList.containsKey(node2)) {
      throw new NoSuchElementException("En eller båda noderna finns inte i grafen.");
    }

    // hämtar edge mellan noderna
    Edge<N> edge1 = getEdgeBetween(node1, node2);
    Edge<N> edge2 = getEdgeBetween(node2, node1);

    // kollar så att kanterna inte är null
    if (edge1 == null || edge2 == null) {
      throw new IllegalStateException("Det finns ingen koppling mellan noderna.");
    }

    // tar bort kopplingen
    adjacencyList.get(node1).remove(edge1);
    adjacencyList.get(node2).remove(edge2);
  }

  // hämtar edge mellan två noder
  public Edge<N> getEdgeBetween(N node1, N node2) {
    // kollar om noderna finns
    if (!adjacencyList.containsKey(node1) || !adjacencyList.containsKey(node2)) {
      throw new NoSuchElementException("En eller flera noder saknas.");
    }

    // variabel för nod1:s edge
    List<Edge<N>> edgesNode1 = adjacencyList.get(node1);

    // for loop för att kolla igenom alla edges i edge listan och returnerar edge om
    // den är lika med nod2:s destination
    // som är samma sak som edge i detta sammanhang
    for (Edge<N> edge : edgesNode1) {
      if (edge.getDestination().equals(node2)) {
        return edge;
      }
    }
    // metoden hittade inte det den letade efter
    return null;
  }

  // hämtar lista på alla kopplingar en nod har
  public List<Edge<N>> getEdgesFrom(N node) {
    // element check
    if (!adjacencyList.containsKey(node)) {
      throw new NoSuchElementException("Noden finns inte i grafen.");
    }
    // returnerar en lista med kopplingar
    return new ArrayList<>(adjacencyList.get(node));
  }

  // returnerar alla noder i grafen
  public Set<N> getNodes() {
    return new HashSet<>(adjacencyList.keySet());
  }

  // ändrar kopplings vikt
  public void setConnectionWeight(N node1, N node2, int newWeight) {
    // element check för noderna
    if (!adjacencyList.containsKey(node1) || !adjacencyList.containsKey(node2)) {
      throw new NoSuchElementException("En eller båda noderna finns inte i grafen.");
    }

    // variabler för att kolla så att det finns en koppling mellan noderna
    Edge<N> edge1 = getEdgeBetween(node1, node2);
    Edge<N> edge2 = getEdgeBetween(node2, node1);

    // element check för kopplingen
    if (edge1 == null || edge2 == null) {
      throw new NoSuchElementException("Det finns ingen koppling mellan noderna.");
    }

    // kollar om den nya vikten är negativ
    if (newWeight < 0) {
      throw new IllegalArgumentException("Vikt kan inte vara negativ.");
    }

    // sätter den nya vikten på kopplingen
    edge1.setWeight(newWeight);
    edge2.setWeight(newWeight);
  }

  public boolean pathExists(N start, N end) {
    if (!adjacencyList.containsKey(start) || !adjacencyList.containsKey(end)) {
      return false;
    }

    Set<N> visited = new HashSet<>();
    return dfs(start, end, visited);
  }

  public List<Edge<N>> getPath(N start, N end) {
    if (!adjacencyList.containsKey(start) || !adjacencyList.containsKey(end)) {
      return null;
    }

    Set<N> visited = new HashSet<>();
    List<Edge<N>> path = new ArrayList<>();

    boolean found = dfsPath(start, end, visited, path);
    return found ? path : null;
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();

    for (N node : adjacencyList.keySet()) {
      sb.append(node.toString() + "\n");

      for (Edge<N> edge : adjacencyList.get(node)) {
        sb.append(edge.toString() + "\n");
      }
    }
    return sb.toString();
  }
}