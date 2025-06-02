package se.su.inlupp;

import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class Gui extends Application {

  public void start(Stage stage) {
    Graph<String> graph = new ListGraph<String>();
    String javaVersion = System.getProperty("java.version");
    String javafxVersion = System.getProperty("javafx.version");
    Label label = new Label("Hello, JavaFX " + javafxVersion + ", running on Java " + javaVersion + ".");

    VBox root = new VBox(30, label);
    root.setAlignment(Pos.CENTER);
    Scene scene = new Scene(root, 640, 480);

    // add a "file" menu
    // MenuBar menuBar = new MenuBar();
    Menu fileMenu = new Menu("File");

    MenuItem newMapItem = new MenuItem("New Map");
    newMapItem.setOnAction(e -> {
      System.out.println("New Map created!");
    });
    fileMenu.getItems().add(newMapItem);

    MenuItem openItem = new MenuItem("Open");
    openItem.setOnAction(e -> {
      System.out.println("Open Map dialog!");
      // Here you would typically open a file chooser dialog to select a map file
    });
    fileMenu.getItems().add(openItem);

    MenuItem saveItem = new MenuItem("Save");
    saveItem.setOnAction(e -> {
      System.out.println("Save Map dialog!");
      // Here you would typically open a file chooser dialog to save the map file
    });
    fileMenu.getItems().add(saveItem);

    MenuItem saveImageItem = new MenuItem("Save Image");
    saveImageItem.setOnAction(e -> {
      System.out.println("Save Image dialog!");
      // Here you would typically open a file chooser dialog to save the image
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
