package se.su.inlupp;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

import javax.imageio.ImageIO;

import javafx.application.Application;
import javafx.event.Event;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.image.Image;
import javafx.scene.layout.BackgroundImage;
import javafx.scene.layout.BackgroundSize;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.Cursor;
import javafx.scene.control.Tooltip;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

public class Gui extends Application {
  private boolean hasUnsavedChanges = false;
  private File mapFile = null;
  private final Pane mapPane = new Pane();

  private boolean addingNewPlace = false;
  private final Graph<String> graph = new ListGraph<>();
  private final List<PlaceView> places = new ArrayList<>();
  private final List<PlaceView> selectedPlaces = new ArrayList<>();

  public void start(Stage stage) {
    VBox root = new VBox(10);
    root.setAlignment(Pos.TOP_CENTER);
    Scene scene = new Scene(root, 640, 480);

    Menu fileMenu = new Menu("File");

    MenuItem newMapItem = new MenuItem("New Map");
    newMapItem.setOnAction(e -> newMapHandler(root));
    fileMenu.getItems().add(newMapItem);

    MenuItem openItem = new MenuItem("Open");
    openItem.setOnAction(e -> openHandler(stage));
    fileMenu.getItems().add(openItem);

    MenuItem saveItem = new MenuItem("Save");
    saveItem.setOnAction(e -> saveHandler(stage));
    fileMenu.getItems().add(saveItem);

    MenuItem saveImageItem = new MenuItem("Save Image");
    saveImageItem.setOnAction(e -> saveImageHandler());
    fileMenu.getItems().add(saveImageItem);

    MenuItem exitItem = new MenuItem("Exit");
    exitItem.setOnAction(this::onCloseRequest);
    fileMenu.getItems().add(exitItem);

    MenuBar menuBar = new MenuBar();
    menuBar.getMenus().add(fileMenu);

    root.getChildren().add(0, menuBar);
    root.setAlignment(Pos.TOP_CENTER);

    stage.setOnCloseRequest(this::onCloseRequest);

    // Knapp för att skapa ny plats
    Button newPlaceButton = new Button("New Place");
    // knapp för skapa connection mellan två platser
    Button newConnectionButton = new Button("New Connection");
    Button showConnectionButton = new Button("Show Connection");
    Button changeConnectionButton = new Button("Change Connection");

    HBox buttonRow = new HBox(10, newPlaceButton, newConnectionButton, showConnectionButton, changeConnectionButton);
    buttonRow.setAlignment(Pos.CENTER);

    // Karta-panel (bakgrundsarea för platserna)
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
        showAlert("Fel", "Du måste markera exakt två platser.");
        return;
      }

      PlaceView from = selectedPlaces.get(0);
      PlaceView to = selectedPlaces.get(1);

      if (from.name.equals(to.name)) {
        showAlert("Fel", "En plats kan inte kopplas till sig själv.");
        return;
      }

      // Dialog för namn på förbindelsen
      TextInputDialog nameDialog = new TextInputDialog();
      nameDialog.setTitle("Ny Förbindelse");
      nameDialog.setHeaderText("Ange namn på förbindelsen:");
      nameDialog.setContentText("Namn:");

      String connectionName = nameDialog.showAndWait().orElse("").trim();
      if (connectionName.isEmpty()) {
        showAlert("Fel", "Förbindelsen måste ha ett namn.");
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
        if (time < 0)
          throw new NumberFormatException();
      } catch (NumberFormatException ex) {
        showAlert("Fel", "Restiden måste vara ett positivt heltal.");
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
        showAlert("Fel", "Det finns redan en förbindelse mellan dessa platser.");
      } catch (Exception ex) {
        showAlert("Något gick fel: ", ex.getMessage());
      }
    });

    showConnectionButton.setOnAction(e -> {
      if (selectedPlaces.size() != 2) {
        showAlert("Fel", "Du måste markera exakt två platser.");
        return;
      }

      PlaceView from = selectedPlaces.get(0);
      PlaceView to = selectedPlaces.get(1);

      try {
        Edge<String> edge = graph.getEdgeBetween(from.name, to.name);

        if (edge == null) {
          showAlert("Fel", "Det finns ingen förbindelse mellan dessa två platser.");
          return;
        }

        // Visa information i dialog
        Alert info = new Alert(Alert.AlertType.INFORMATION);
        info.setTitle("Förbindelseinformation");
        info.setHeaderText("Förbindelse mellan " + from.name + " och " + to.name);
        info.setContentText(
            "Namn: " + edge.getName() + "\n" +
                "Tid: " + edge.getWeight() + " enheter");
        info.showAndWait();

      } catch (NoSuchElementException ex) {
        showAlert("Fel", "En av platserna finns inte längre.");
      }
    });

    changeConnectionButton.setOnAction(e -> {
      if (selectedPlaces.size() != 2) {
        showAlert("Fel", "Du måste markera exakt två platser.");
        return;
      }

      PlaceView from = selectedPlaces.get(0);
      PlaceView to = selectedPlaces.get(1);

      try {
        Edge<String> edge = graph.getEdgeBetween(from.name, to.name);

        if (edge == null) {
          showAlert("Fel", "Det finns ingen förbindelse mellan dessa två platser.");
          return;
        }

        // Skapa fält: namn (icke-redigerbart), ny tid (redigerbart)
        TextField nameField = new TextField(edge.getName());
        nameField.setEditable(false);

        TextField timeField = new TextField();
        timeField.setPromptText("Ny tid");

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.add(new Label("Namn:"), 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(new Label("Tid:"), 0, 1);
        grid.add(timeField, 1, 1);

        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Ändra Förbindelse");
        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.showAndWait().ifPresent(result -> {
          if (result == ButtonType.OK) {
            try {
              int newTime = Integer.parseInt(timeField.getText().trim());
              if (newTime < 0)
                throw new NumberFormatException();

              graph.setConnectionWeight(from.name, to.name, newTime);

            } catch (NumberFormatException ex) {
              showAlert("Fel", "Tiden måste vara ett positivt heltal.");
            } catch (Exception ex) {
              showAlert("Fel", ex.getMessage());
            }
          }
        });

      } catch (NoSuchElementException ex) {
        showAlert("Fel", "En av platserna finns inte längre.");
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
            hasUnsavedChanges = true;

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

    stage.setTitle("Map Application");

    stage.setScene(scene);
    stage.show();
  }

  private void newMapHandler(VBox root) {
    FileChooser fileChooser = new FileChooser();
    fileChooser.setTitle("Open Map");
    fileChooser.getExtensionFilters().addAll(
        new ExtensionFilter("Image Files", "*.png", "*.jpg", "*.gif"),
        new ExtensionFilter("All Files", "*.*"));
    Stage stage = (Stage) root.getScene().getWindow();
    File selectedFile = fileChooser.showOpenDialog(stage);
    if (selectedFile != null) {
      mapFile = selectedFile;
      BackgroundImage backgroundImage = fileToBackgroundImage(selectedFile);
      setBackground(root, backgroundImage);
    }
  }

  private void openHandler(Stage stage) {
    if (mapFile != null) {
      Alert alert = new Alert(Alert.AlertType.CONFIRMATION,
          "You have unsaved changes. Do you want to discard changes and open a new graph?", ButtonType.YES,
          ButtonType.NO);
      alert.setTitle("Unsaved Changes");
      alert.setHeaderText(null);
      Optional<ButtonType> result = alert.showAndWait();
      if (result.isPresent() && result.get() == ButtonType.NO) {
        return;
      }
    }
    FileChooser fileChooser = new FileChooser();
    fileChooser.setTitle("Open Graph");
    fileChooser.getExtensionFilters().addAll(
        new ExtensionFilter("Graph files", "*.graph", "*.txt"),
        new ExtensionFilter("All Files", "*.*"));
    File selectedFile = fileChooser.showOpenDialog(stage);

    if (selectedFile == null) {
      return;
    }
    // Clear previous data
    places.clear();
    mapPane.getChildren().clear();
    selectedPlaces.clear();
    hasUnsavedChanges = false;

    FileReader fileReader = null;
    BufferedReader reader = null;
    try {
      fileReader = new FileReader(selectedFile);
      reader = new BufferedReader(fileReader);
      String line;

      // Background image
      line = reader.readLine();
      if (line != null && !line.isBlank()) {
        String filePath = line.split(":")[1].trim();
        mapFile = new File(filePath);
        BackgroundImage backgroundImage = fileToBackgroundImage(mapFile);
        setBackground((VBox) stage.getScene().getRoot(), backgroundImage);
      }

      // Places
      line = reader.readLine();
      if (line != null && !line.isBlank()) {
        String[] placeData = line.split(";");
        for (int i = 0; i < placeData.length; i += 3) {
          if (i + 2 < placeData.length) {
            String name = placeData[i];
            double x = Double.parseDouble(placeData[i + 1]);
            double y = Double.parseDouble(placeData[i + 2]);

            // Lägg till plats i grafen
            graph.add(name);
            hasUnsavedChanges = true;

            // Skapa och visa plats
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
        }
      }

      // Edges
      while ((line = reader.readLine()) != null) {
        String[] parts = line.split(";");
        if (parts.length == 4) {
          String fromName = parts[0];
          String toName = parts[1];
          String connectionName = parts[2];
          int weight = Integer.parseInt(parts[3]);

          // kolla så att det inte finns redan existerande kant
          if (graph.getEdgeBetween(fromName, toName) != null) {
            continue;
          }

          // Lägg till förbindelse i grafen
          graph.connect(fromName, toName, connectionName, weight);

          // Rita linje
          PlaceView fromPlace = places.stream().filter(p -> p.name.equals(fromName)).findFirst().orElse(null);
          PlaceView toPlace = places.stream().filter(p -> p.name.equals(toName)).findFirst().orElse(null);
          if (fromPlace != null && toPlace != null) {
            javafx.scene.shape.Line lineShape = new javafx.scene.shape.Line(fromPlace.x,
                fromPlace.y,
                toPlace.x, toPlace.y);
            lineShape.setStrokeWidth(2);
            mapPane.getChildren().add(lineShape);
          }
        }
      }
    } catch (Exception ex) {
      System.err.println("Error loading graph: " + ex.getMessage());
      showAlert("Fel", "Kunde inte läsa in grafen: " + ex.getMessage());
    } finally {
      if (fileReader != null) {
        try {
          fileReader.close();
        } catch (Exception ex) {
          System.err.println("Error closing file: " + ex.getMessage());
        }
      }
      if (reader != null) {
        try {
          reader.close();
        } catch (Exception ex) {
          System.err.println("Error closing reader: " + ex.getMessage());
        }
      }
    }
  }

  private void saveHandler(Stage stage) {
    FileChooser fileChooser = new FileChooser();
    fileChooser.setTitle("Save Graph");
    fileChooser.getExtensionFilters().addAll(
        new ExtensionFilter("Graph files", "*.graph", "*.txt"),
        new ExtensionFilter("All Files", "*.*"));
    File selectedFile = fileChooser.showSaveDialog(stage);
    if (selectedFile != null) {
      selectedFile = new File(selectedFile.getAbsolutePath() + ".graph");
      StringBuilder sb = new StringBuilder();
      // Add map file URI
      sb.append(mapFile.toURI().toString()).append("\n");

      // Add places
      for (PlaceView pv : places) {
        sb.append(pv.name).append(";")
            .append(pv.x).append(";")
            .append(pv.y).append(";");
      }
      if (sb.length() > 0) { // Remove trailing semicolon
        sb.setLength(sb.length() - 1);
      }
      sb.append("\n");
      // Add edges
      for (PlaceView pv : places) {
        for (Edge<String> edge : graph.getEdgesFrom(pv.name)) {
          sb.append(pv.name).append(";")
              .append(edge.getDestination()).append(";")
              .append(edge.getName()).append(";")
              .append(edge.getWeight()).append("\n");
        }
      }
      String content = sb.toString();

      hasUnsavedChanges = false;
      try (java.io.FileWriter writer = new java.io.FileWriter(selectedFile)) {
        writer.write(content);
      } catch (java.io.IOException ex) {
        System.err.println("Error saving graph: " + ex.getMessage());
      }
    }
  }

  private void saveImageHandler() {
    if (mapFile == null) {
      System.err.println("No map image to save.");
      return;
    }

    BufferedImage image = null;

    try {
      image = ImageIO.read(mapFile);
    } catch (Exception ex) {
      System.err.println("Error loading image: " + ex.getMessage());
    }

    Graphics2D g2d = image.createGraphics();
    int radius = 5;
    for (PlaceView pv : places) {
      g2d.setColor(java.awt.Color.BLUE);
      int x = (int) pv.x - radius;
      int y = (int) pv.y - radius;
      g2d.fillOval(x, y, radius * 2, radius * 2);
    }
    for (PlaceView pv : places) {
      for (Edge<String> edge : graph.getEdgesFrom(pv.name)) {
        PlaceView to = places.stream().filter(p -> p.name.equals(edge.getDestination())).findFirst().orElse(null);
        if (to != null) {
          g2d.setColor(java.awt.Color.RED);
          g2d.drawLine((int) pv.x, (int) pv.y, (int) to.x, (int) to.y);
          g2d.drawString(edge.getName(), (int) ((pv.x + to.x) / 2), (int) ((pv.y + to.y) / 2));
        }
      }
    }
    g2d.dispose();

    try {
      String currentDir = System.getProperty("user.dir");
      currentDir = new File(currentDir).getParent();
      File output = new File(currentDir, "capture.png");
      ImageIO.write(image, "png", output);
    } catch (Exception ex) {
      System.err.println("Error saving image: " + ex.getMessage());
    }
  }

  private BackgroundImage fileToBackgroundImage(File selectedFile) {
    String url = selectedFile.toURI().toString();
    Image image = new Image(url);
    double width = image.getWidth();
    double height = image.getHeight();
    BackgroundSize backgroundSize = new BackgroundSize(width, height, false, false, true, false);
    BackgroundImage backgroundImage = new BackgroundImage(image, null, null, null, backgroundSize);
    return backgroundImage;
  }

  private void setBackground(VBox root, BackgroundImage backgroundImage) {
    double width = backgroundImage.getSize().getWidth();
    double height = backgroundImage.getSize().getHeight();
    mapPane.setBackground(new javafx.scene.layout.Background(backgroundImage));
    mapPane.setMaxSize(width, height);
    mapPane.setMinSize(width, height);
    Stage stage = (Stage) root.getScene().getWindow();
    int padding = 20;
    int menuBarHeight = 120;
    stage.setWidth(width + padding);
    stage.setHeight(height + padding + menuBarHeight);
    stage.setResizable(false);
  }

  public void onCloseRequest(Event event) {
    if (!hasUnsavedChanges) {
      System.exit(0);
      return;
    }
    Alert alert = new Alert(Alert.AlertType.CONFIRMATION,
        "You have unsaved changes. Do you want to exit without saving?", ButtonType.YES, ButtonType.NO);
    alert.setTitle("Unsaved Changes");
    alert.setHeaderText(null);
    alert.showAndWait().ifPresent(response -> {
      if (response == ButtonType.NO) {
        event.consume();
        return;
      }
      System.exit(0);
    });
  }

  private void showAlert(String title, String message) {
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
