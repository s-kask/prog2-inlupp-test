package se.su.inlupp;

import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.scene.Cursor;
import javafx.scene.control.Tooltip;
import javafx.scene.shape.Circle;

import java.util.ArrayList;
import java.util.List;

public class Gui extends Application {

  private boolean addingNewPlace = false;
  private final Graph<String> graph = new ListGraph<>();
  private final List<PlaceView> places = new ArrayList<>();
  private final List<PlaceView> selectedPlaces = new ArrayList<>();

  public void start(Stage stage) {
    VBox root = new VBox(10);
    root.setAlignment(Pos.TOP_CENTER);

    // Knapp för att skapa ny plats
    Button newPlaceButton = new Button("New Place");

    HBox buttonRow = new HBox(10, newPlaceButton);
    buttonRow.setAlignment(Pos.CENTER);

    // Karta-panel (bakgrundsarea för platserna)
    Pane mapPane = new Pane();
    mapPane.setPrefSize(640, 400);
    mapPane.setStyle("-fx-background-color: lightgray;");

    root.getChildren().addAll(buttonRow, mapPane);

    // Klick på "New Place" aktiverar platsläget
    newPlaceButton.setOnAction(e -> {
      addingNewPlace = true;
      mapPane.setCursor(Cursor.CROSSHAIR);
      newPlaceButton.setDisable(true);
    });

    // Klick på kartan
    mapPane.addEventHandler(MouseEvent.MOUSE_CLICKED, event -> {
      if (addingNewPlace) {
        int x = (int) event.getX();
        int y = (int) event.getY();

        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("New Place");
        dialog.setHeaderText("Enter a name for the new place:");
        dialog.setContentText("Name:");

        dialog.showAndWait().ifPresent(name -> {
          if (!name.isBlank()) {
            // Lägg till i grafen
            graph.add(name);

            // Skapa plats och visa
            PlaceView pv = new PlaceView(name, x, y);
            places.add(pv);
            Tooltip.install(pv.circle, new Tooltip(name));
            mapPane.getChildren().add(pv.circle);

            // Klickbarhet på plats
            pv.circle.setOnMouseClicked(circleEvent -> {
              circleEvent.consume(); // förhindrar att kartan får klicket
              handlePlaceClick(pv);
            });
          }
        });

        // Återställ läge
        addingNewPlace = false;
        mapPane.setCursor(Cursor.DEFAULT);
        newPlaceButton.setDisable(false);
      }
    });

    Scene scene = new Scene(root, 640, 480);
    stage.setTitle("Map Application");
    stage.setScene(scene);
    stage.show();
  }

  // Hanterar klick på en plats: markerar eller avmarkerar.
  private void handlePlaceClick(PlaceView pv) {
    if (pv.isSelected) {
      pv.toggleSelected();
      selectedPlaces.remove(pv);
    } else {
      if (selectedPlaces.size() == 2) {
        return; // tillåt max två markerade
      }
      pv.toggleSelected();
      selectedPlaces.add(pv);
    }
  }

  public static void main(String[] args) {
    launch(args);
  }
}
