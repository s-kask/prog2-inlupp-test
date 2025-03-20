package se.su.inlupp;

public interface Edge<T> {

  int getWeight();

  T getDestination();

  String getName();
}
