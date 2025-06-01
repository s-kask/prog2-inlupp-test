package se.su.inlupp;

import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.scene.Cursor;
import javafx.scene.control.Tooltip;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

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
    // knapp för skapa connection mellan två platser
    Button newConnectionButton = new Button("New Connection");
    Button showConnectionButton = new Button("Show Connection");

    HBox buttonRow = new HBox(10, newPlaceButton, newConnectionButton, showConnectionButton);
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

    newConnectionButton.setOnAction(e -> {
      if (selectedPlaces.size() != 2) {
        showAlert("Du måste markera exakt två platser.");
        return;
      }

      PlaceView from = selectedPlaces.get(0);
      PlaceView to = selectedPlaces.get(1);

      if (from.name.equals(to.name)) {
        showAlert("En plats kan inte kopplas till sig själv.");
        return;
      }

      // Dialog för namn på förbindelsen
      TextInputDialog nameDialog = new TextInputDialog();
      nameDialog.setTitle("Ny Förbindelse");
      nameDialog.setHeaderText("Ange namn på förbindelsen:");
      nameDialog.setContentText("Namn:");

      String connectionName = nameDialog.showAndWait().orElse("").trim();
      if (connectionName.isEmpty()) {
        showAlert("Förbindelsen måste ha ett namn.");
        return;
      }

      // Dialog för tid
      TextInputDialog timeDialog = new TextInputDialog();
      timeDialog.setTitle("Restid");
      timeDialog.setHeaderText("Hur lång tid tar förbindelsen?");
      timeDialog.setContentText("Tid (heltal):");

      String timeStr = timeDialog.showAndWait().orElse("").trim();
      int time;

      try {
        time = Integer.parseInt(timeStr);
        if (time < 0) throw new NumberFormatException();
      } catch (NumberFormatException ex) {
        showAlert("Restiden måste vara ett positivt heltal.");
        return;
      }

      try {
        // Lägg till i grafen
        graph.connect(from.name, to.name, connectionName, time);

        // Rita linje
        javafx.scene.shape.Line line = new javafx.scene.shape.Line(from.x, from.y, to.x, to.y);
        line.setStrokeWidth(2);
        mapPane.getChildren().add(line);

      } catch (IllegalStateException ex) {
        showAlert("Det finns redan en förbindelse mellan dessa platser.");
      } catch (Exception ex) {
        showAlert("Något gick fel: " + ex.getMessage());
      }
    });

    showConnectionButton.setOnAction(e -> {
      if (selectedPlaces.size() != 2) {
        showAlert("Fel: Du måste markera exakt två platser.");
        return;
      }

      PlaceView from = selectedPlaces.get(0);
      PlaceView to = selectedPlaces.get(1);

      try {
        Edge<String> edge = graph.getEdgeBetween(from.name, to.name);

        if (edge == null) {
          showAlert("Fel: Det finns ingen förbindelse mellan dessa två platser.");
          return;
        }

        // Visa information i dialog
        Alert info = new Alert(Alert.AlertType.INFORMATION);
        info.setTitle("Förbindelseinformation");
        info.setHeaderText("Förbindelse mellan " + from.name + " och " + to.name);
        info.setContentText(
                "Namn: " + edge.getName() + "\n" +
                        "Tid: " + edge.getWeight() + " enheter"
        );
        info.showAndWait();

      } catch (NoSuchElementException ex) {
        showAlert("Fel: En av platserna finns inte längre.");
      }
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

  private void showAlert(String message) {
    Alert alert = new Alert(Alert.AlertType.ERROR);
    alert.setTitle("Fel");
    alert.setHeaderText(null);
    alert.setContentText(message);
    alert.showAndWait();
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
