package se.su.inlupp;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileReader;
import java.util.concurrent.atomic.AtomicReference;

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

public class Gui extends Application {
  private boolean hasUnsavedChanges = false; // TODO: Track unsaved changes

  public void start(Stage stage) {
    Graph<String> graph = new ListGraph<String>();
    String javaVersion = System.getProperty("java.version");
    String javafxVersion = System.getProperty("javafx.version");
    Label label = new Label("Hello, JavaFX " + javafxVersion + ", running on Java " + javaVersion + ".");

    VBox root = new VBox(30, label);
    root.setAlignment(Pos.CENTER);
    Scene scene = new Scene(root, 640, 480);

    Menu fileMenu = new Menu("File");
    AtomicReference<File> mapFile = new AtomicReference<>(null);

    MenuItem newMapItem = new MenuItem("New Map");
    newMapItem.setOnAction(e -> {
      FileChooser fileChooser = new FileChooser();
      fileChooser.setTitle("Open Map");
      fileChooser.getExtensionFilters().addAll(
          new ExtensionFilter("Image Files", "*.png", "*.jpg", "*.gif"),
          new ExtensionFilter("All Files", "*.*"));
      File selectedFile = fileChooser.showOpenDialog(stage);
      if (selectedFile != null) {
        mapFile.set(selectedFile);
        String url = selectedFile.toURI().toString();
        Image image = new Image(url);
        double width = image.getWidth();
        double height = image.getHeight();
        BackgroundSize backgroundSize = new BackgroundSize(width, height, false, false, true, false);
        BackgroundImage backgroundImage = new BackgroundImage(image, null, null, null, backgroundSize);
        root.setBackground(new javafx.scene.layout.Background(backgroundImage));
        stage.setWidth(width);
        stage.setHeight(height);
        stage.setResizable(false);
      }
    });
    fileMenu.getItems().add(newMapItem);

    MenuItem openItem = new MenuItem("Open");
    openItem.setOnAction(e -> {
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
    });
    fileMenu.getItems().add(openItem);

    MenuItem saveItem = new MenuItem("Save");
    saveItem.setOnAction(e -> {
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
    });
    fileMenu.getItems().add(saveItem);

    MenuItem saveImageItem = new MenuItem("Save Image");
    saveImageItem.setOnAction(e -> {
      if (mapFile.get() == null) {
        System.err.println("No map image to save.");
        return;
      }

      BufferedImage image = null;

      try {
        image = ImageIO.read(mapFile.get());
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
    });
    fileMenu.getItems().add(saveImageItem);

    MenuItem exitItem = new MenuItem("Exit");
    exitItem.setOnAction(this::onCloseRequest);

    fileMenu.getItems().add(exitItem);

    MenuBar menuBar = new MenuBar();
    menuBar.getMenus().add(fileMenu);

    root.getChildren().add(0, menuBar);
    root.setAlignment(Pos.TOP_CENTER);

    stage.setOnCloseRequest(this::onCloseRequest);

    stage.setScene(scene);
    stage.show();
  }

  public void onCloseRequest(Event event) {
    if (!hasUnsavedChanges) {
      return;
    }
    Alert alert = new Alert(Alert.AlertType.CONFIRMATION,
        "You have unsaved changes. Do you want to save before exiting?", ButtonType.YES, ButtonType.NO);
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

  public static void main(String[] args) {
    launch(args);
  }
}
