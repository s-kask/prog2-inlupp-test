package se.su.inlupp;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileReader;

import javax.imageio.ImageIO;

import javafx.application.Application;
import javafx.event.Event;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.Cursor;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

public class Gui extends Application {
  private boolean hasUnsavedChanges = true; // TODO: Track unsaved changes
  private File mapFile = null;

  private boolean addingNewPlace = false;
  private final Graph<String> graph = new ListGraph<>();
  private final List<PlaceView> places = new ArrayList<>();
  private final List<PlaceView> selectedPlaces = new ArrayList<>();

  public void start(Stage stage) {
    String javaVersion = System.getProperty("java.version");
    String javafxVersion = System.getProperty("javafx.version");
    Label label = new Label("Hello, JavaFX " + javafxVersion + ", running on Java " + javaVersion + ".");

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
    exitItem.setOnAction(e -> handleExit());
    fileMenu.getItems().add(exitItem);

    MenuBar menuBar = new MenuBar();
    menuBar.getMenus().add(fileMenu);

    root.getChildren().add(0, menuBar);
    root.setAlignment(Pos.TOP_CENTER);

    // Knapp för att skapa ny plats
    Button newPlaceButton = new Button("New Place");
    // knapp för skapa connection mellan två platser
    Button newConnectionButton = new Button("New Connection");
    Button showConnectionButton = new Button("Show Connection");
    Button changeConnectionButton = new Button("Change Connection");
    Button findPathButton = new Button("Find Path");

    HBox buttonRow = new HBox(10, newPlaceButton, newConnectionButton, showConnectionButton, changeConnectionButton,
        findPathButton);
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

    findPathButton.setOnAction(e -> {
      if (selectedPlaces.size() != 2) {
        showAlert("Error", "You must select exactly two places.");
        return;
      }

      PlaceView from = selectedPlaces.get(0);
      PlaceView to = selectedPlaces.get(1);

      try {
        List<Edge<String>> path = graph.getPath(from.name, to.name);

        if (path == null || path.isEmpty()) {
          showAlert("No Path", "There is no path between " + from.name + " and " + to.name);
          return;
        }

        StringBuilder pathInfo = new StringBuilder();
        pathInfo.append("Path from ").append(from.name).append(" to ").append(to.name).append(":\n\n");

        int totalTime = 0;
        String currentPlace = from.name;

        for (Edge<String> edge : path) {
          pathInfo.append(currentPlace).append(" --[")
              .append(edge.getName()).append(" (")
              .append(edge.getWeight()).append(" min)]--> ")
              .append(edge.getDestination()).append("\n");
          totalTime += edge.getWeight();
          currentPlace = edge.getDestination();
        }

        pathInfo.append("\nTotal travel time: ").append(totalTime).append(" hours");

        Alert info = new Alert(Alert.AlertType.INFORMATION);
        info.setTitle("Path Information");
        info.setHeaderText("Path between " + from.name + " and " + to.name);
        info.setContentText(pathInfo.toString());
        info.setResizable(true);
        info.getDialogPane().setPrefSize(400, 300);
        info.showAndWait();

      } catch (NoSuchElementException ex) {
        showAlert("Error", "One of the places no longer exists.");
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
    // TODO: If a map is already open, prompt to save changes
    FileChooser fileChooser = new FileChooser();
    fileChooser.setTitle("Open Graph");
    fileChooser.getExtensionFilters().addAll(
        new ExtensionFilter("Graph files", "*.graph", "*.txt"),
        new ExtensionFilter("All Files", "*.*"));
    File selectedFile = fileChooser.showOpenDialog(stage);
    if (selectedFile != null) {
      // TODO: Load the graph from the selected file
      FileReader reader = null;
      try {
        reader = new FileReader(selectedFile);
        // TODO: Parse the graph from the file
        // graph.loadFromFile(reader);
        hasUnsavedChanges = false; // Reset unsaved changes flag
      } catch (Exception ex) {
        System.err.println("Error loading graph: " + ex.getMessage());
      } finally {
        if (reader != null) {
          try {
            reader.close();
          } catch (Exception ex) {
            System.err.println("Error closing file: " + ex.getMessage());
          }
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
      String content = graph.toString();
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
    // TODO: Draw the graph on the image
    g2d.setColor(java.awt.Color.RED);
    int radius = 5;
    g2d.fillOval(100 - radius, 100 - radius, radius * 2, radius * 2);
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
    root.setBackground(new javafx.scene.layout.Background(backgroundImage));
    Stage stage = (Stage) root.getScene().getWindow();
    stage.setWidth(width);
    stage.setHeight(height);
    stage.setResizable(false);
  }

  private void handleExit() {
    if (hasUnsavedChanges) {
      Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
      alert.setTitle("Osparade ändringar");
      alert.setHeaderText("Du har osparade ändringar.");
      alert.setContentText("Vill du avsluta utan att spara?");
      alert.getButtonTypes().setAll(ButtonType.OK, ButtonType.CANCEL);

      alert.showAndWait().ifPresent(response -> {
        if (response == ButtonType.OK) {
          System.exit(0); // Avsluta
        }
        // Annars gör vi inget – användaren valde att stanna kvar
      });
    } else {
      System.exit(0); // Inga ändringar → avsluta direkt
    }
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