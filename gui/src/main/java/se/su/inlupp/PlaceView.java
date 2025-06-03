package se.su.inlupp;

import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

class PlaceView {
    String name;
    double x, y;
    Circle circle;
    boolean isSelected = false;

    // skapar en ny plats
    PlaceView(String name, double x, double y) {
        this.name = name;
        this.x = x;
        this.y = y;
        this.circle = new Circle(x, y, 6);
        updateColor();
    }

    // Växlar markeringsstatus för platsen.
    void toggleSelected() {
        isSelected = !isSelected;
        updateColor();
    }

    // Uppdaterar cirkelns färg baserat på om platsen är markerad eller inte
    void updateColor() {
        circle.setFill(isSelected ? Color.RED : Color.BLUE);
    }
}