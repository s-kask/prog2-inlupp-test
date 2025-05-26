package se.su.inlupp;

import java.util.*;

public class ListGraph<N> {
  private Map<N, List<Edge<N>>> adjacencyList = new HashMap<>();


  //Om nod redan finns gör inget annar lägg till ny nod
  public void add(N node) {
    if (!adjacencyList.containsKey(node)) {
      adjacencyList.put(node, new ArrayList<>());
    }
  }

  //Om nod inte finns printa att den inte finns, annars tar bort noden TODO: Implementera så att den också tar bort förbinndelser
  public void remove(N node) {
    if (!adjacencyList.containsKey(node)) {
      throw new NoSuchElementException("Noden finns inte i grafen" + node);
    }
    adjacencyList.remove(node);
  }

  public void connect(N node1, N node2, String name, int weigth) {
    //kolla att noderna finns
    if (!adjacencyList.containsKey(node1) || !adjacencyList.containsKey(node2)) {
      throw new NoSuchElementException("En eller båda noderna saknas.");
    }

    //kolla så att vikt inte är negativ
    if (weigth < 0) {
      throw new IllegalArgumentException("Vikt negativ.");
    }

    //kolla så att det inte finns redan existerande kant
    List<Edge<N>> edgesNode1 = adjacencyList.get(node1);
    for (Edge<N> edge : edgesNode1) {
      if (edge.getDestination().equals(node2)) {
        throw new IllegalStateException("Kant finns redan mellan dessa noder.");
      }
    }

    //lägger till och skapar kanterna
    edgesNode1.add(new Edge<>(node2, name, weigth));
    adjacencyList.get(node2).add(new Edge<>(node1, name, weigth));
  }
}