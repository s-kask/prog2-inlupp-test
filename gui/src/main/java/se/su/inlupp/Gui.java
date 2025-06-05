package se.su.inlupp;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

import javax.imageio.ImageIO;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.Cursor;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

import java.util.Optional;

public class Gui extends Application {
  private boolean hasUnsavedChanges = false;
  private File mapFile = null;
  private final Pane mapPane = new Pane();
  private ImageView mapImageView = new ImageView();

  private boolean addingNewPlace = false;
  private final Graph<String> graph = new ListGraph<>();
  private final List<PlaceView> places = new ArrayList<>();
  private final List<PlaceView> selectedPlaces = new ArrayList<>();

  // knappar så att jag kan enable/disable dem
  private Button newPlaceButton;
  private Button newConnectionButton;
  private Button showConnectionButton;
  private Button changeConnectionButton;
  private Button findPathButton;

  public void start(Stage stage) {
    VBox root = new VBox(10);
    root.setAlignment(Pos.TOP_CENTER);
    root.setPadding(new Insets(0, 20, 0, 20)); // skapar padding runt root

    // Skapar menu bar
    MenuBar menuBar = createMenuBar(stage, root);

    // Skapar button row
    HBox buttonRow = createButtonRow();

    // Ställer in mapPane med bildvy
    mapPane.getChildren().add(mapImageView);

    // Lägger till allt i root
    root.getChildren().addAll(menuBar, buttonRow, mapPane);

    Scene scene = new Scene(root);
    stage.setScene(scene);
    stage.setTitle("Map Application");
    stage.show();
  }

  private MenuBar createMenuBar(Stage stage, VBox root) {
    Menu fileMenu = new Menu("File");

    MenuItem newMapItem = new MenuItem("New Map");
    newMapItem.setOnAction(e -> newMapHandler(stage));

    MenuItem openItem = new MenuItem("Open");
    openItem.setOnAction(e -> openHandler(stage));

    MenuItem saveItem = new MenuItem("Save");
    saveItem.setOnAction(e -> saveHandler(stage));

    MenuItem saveImageItem = new MenuItem("Save Image");
    saveImageItem.setOnAction(e -> saveImageHandler());

    MenuItem exitItem = new MenuItem("Exit");
    exitItem.setOnAction(e -> handleExit());

    fileMenu.getItems().addAll(newMapItem, openItem, saveItem, saveImageItem, exitItem);

    MenuBar menuBar = new MenuBar();
    menuBar.getMenus().add(fileMenu);

    // Lägg till padding här:
    menuBar.setPadding(new Insets(5, 20, 5, 20)); // top, right, bottom, left

    stage.setOnCloseRequest(e -> {
      e.consume(); // hindrar automatisk stängning
      handleExit(); // vi hanterar det själva
    });
    return menuBar;
  }

  private HBox createButtonRow() {
    newPlaceButton = new Button("New Place"); // Knapp för att skapa ny plats
    newConnectionButton = new Button("New Connection"); // knapp för skapa connection mellan två platser
    showConnectionButton = new Button("Show Connection");
    changeConnectionButton = new Button("Change Connection");
    findPathButton = new Button("Find Path");

    // avaktivera knapparna initialt
    disableAllButtons();

    HBox buttonRow = new HBox(10, newPlaceButton, newConnectionButton, showConnectionButton, changeConnectionButton,
        findPathButton);
    buttonRow.setAlignment(Pos.CENTER);

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
      timeDialog.setContentText("Tid (heltal i timmar):");

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
                "Tid: " + edge.getWeight() + " Timmar");
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
        timeField.setPromptText("Ny tid (i timmar)");

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
              .append(edge.getWeight()).append(" h)]--> ")
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

    return buttonRow;
  }

  private void newMapHandler(Stage stage) {
    FileChooser fileChooser = new FileChooser();
    fileChooser.setTitle("Open Map");
    fileChooser.getExtensionFilters().addAll(
        new ExtensionFilter("Image Files", "*.png", "*.jpg", "*.gif"),
        new ExtensionFilter("All Files", "*.*"));

    File selectedFile = fileChooser.showOpenDialog(stage);
    if (selectedFile != null) {
      mapFile = selectedFile;
      try {
        Image image = new Image(selectedFile.toURI().toString());
        mapImageView.setImage(image);

        // Rensar existerande places and connections
        mapPane.getChildren().clear();
        mapPane.getChildren().add(mapImageView);
        places.clear();
        selectedPlaces.clear();
        graph.clear();

        // Ändrar storlek på window så den passar image
        Stage primaryStage = (Stage) mapPane.getScene().getWindow();
        primaryStage.setWidth(image.getWidth() + 40);
        primaryStage.setHeight(image.getHeight() + 100); // Extra space for menu and buttons

      } catch (Exception ex) {
        showAlert("Error", "Kunde inte ladda karta: " + ex.getMessage());
      }
    }
    enableAllButtons(); // Aktivera knappar efter ny karta
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
    graph.clear();
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
        String fileName = line.split(":", 2)[1].trim();

        // Resolve image path relative to the selected .graph file
        File imageFile = new File(selectedFile.getParentFile(), fileName);
        mapFile = imageFile;

        try {
          Image image = new Image(imageFile.toURI().toString());
          mapImageView.setImage(image);

          mapPane.getChildren().add(mapImageView);

          // Adjust window size to image
          Stage primaryStage = (Stage) mapPane.getScene().getWindow();
          primaryStage.setWidth(image.getWidth() + 40);
          primaryStage.setHeight(image.getHeight() + 100);
        } catch (Exception ex) {
          showAlert("Error", "Kunde inte ladda kartbilden: " + ex.getMessage());
        }
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
    enableAllButtons(); // Aktivera knappar efter inläsning
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
      sb.append("file:").append(mapFile.getName()).append("\n");

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
      Platform.exit(); // Inga ändringar → avsluta direkt
    }

  }

  // Inaktiverar alla knappar
  private void disableAllButtons() {
    newPlaceButton.setDisable(true);
    newConnectionButton.setDisable(true);
    showConnectionButton.setDisable(true);
    changeConnectionButton.setDisable(true);
    findPathButton.setDisable(true);
  }

  // Aktiverar knapparna
  private void enableAllButtons() {
    newPlaceButton.setDisable(false);
    newConnectionButton.setDisable(false);
    showConnectionButton.setDisable(false);
    changeConnectionButton.setDisable(false);
    findPathButton.setDisable(false);
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