package se.su.inlupp;

import java.io.File;

import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Scene;
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

  public void start(Stage stage) {
    Graph<String> graph = new ListGraph<String>();
    String javaVersion = System.getProperty("java.version");
    String javafxVersion = System.getProperty("javafx.version");
    Label label = new Label("Hello, JavaFX " + javafxVersion + ", running on Java " + javaVersion + ".");

    VBox root = new VBox(30, label);
    root.setAlignment(Pos.CENTER);
    Scene scene = new Scene(root, 640, 480);

    Menu fileMenu = new Menu("File");

    MenuItem newMapItem = new MenuItem("New Map");
    newMapItem.setOnAction(e -> {
      FileChooser fileChooser = new FileChooser();
      fileChooser.setTitle("Open Map");
      fileChooser.getExtensionFilters().addAll(
          new ExtensionFilter("Image Files", "*.png", "*.jpg", "*.gif"),
          new ExtensionFilter("All Files", "*.*"));
      File selectedFile = fileChooser.showOpenDialog(stage);
      if (selectedFile != null) {
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
      FileChooser fileChooser = new FileChooser();
      fileChooser.setTitle("Open Graph");
      fileChooser.getExtensionFilters().addAll(
          new ExtensionFilter("Graph files", "*.graph", "*.txt"),
          new ExtensionFilter("All Files", "*.*"));
      File selectedFile = fileChooser.showOpenDialog(stage);
      if (selectedFile != null) {
        // TODO: Load the graph from the selected file
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
        String content = "Some serialized graph data"; // TODO: Serialize the graph data
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
      System.out.println("Save Image dialog!");
    });
    fileMenu.getItems().add(saveImageItem);

    MenuItem exitItem = new MenuItem("Exit");
    exitItem.setOnAction(e -> System.exit(0));

    fileMenu.getItems().add(exitItem);

    MenuBar menuBar = new MenuBar();
    menuBar.getMenus().add(fileMenu);

    root.getChildren().add(0, menuBar);
    root.setAlignment(Pos.TOP_CENTER);

    stage.setScene(scene);
    stage.show();
  }

  public static void main(String[] args) {
    launch(args);
  }
}
