package com.mdwriter.app;

import atlantafx.base.controls.ModalPane;
import atlantafx.base.theme.PrimerDark;
import javafx.application.Application;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TextArea;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebView;
import javafx.stage.Stage;

/**
 *
 * Main Application class for the Markdown Writer App.
 * 
 * @author Sushant Thapa
 * @Version 1.0
 */
public class MainApp extends Application {

  private ModalPane modalPane;

  private Parent createContent(java.io.File rootDirectory) throws Exception {

    WebView webview = new WebView();
    TextArea textarea = new Editor(webview, rootDirectory);
    var toolBar = new Menu(textarea, rootDirectory);
    modalPane = toolBar.modalPane;

    HBox container = new HBox(textarea, webview);
    HBox.setHgrow(textarea, Priority.ALWAYS);

    VBox root = new VBox();
    root.setPadding(new javafx.geometry.Insets(10));
    root.getChildren().addAll(toolBar.toolbar, container);

    StackPane stack = new StackPane();
    stack.getChildren().addAll(root, modalPane);

    VBox.setVgrow(container, Priority.ALWAYS);

    return stack;
  }

  @Override
  public void start(Stage stage) throws Exception {
    Application.setUserAgentStylesheet(new PrimerDark().getUserAgentStylesheet());

    javafx.stage.DirectoryChooser directoryChooser = new javafx.stage.DirectoryChooser();
    directoryChooser.setTitle("Authorize Workspace Folder");
    java.io.File initialDirectory = new java.io.File(System.getProperty("user.home"));
    if (initialDirectory.exists()) {
        directoryChooser.setInitialDirectory(initialDirectory);
    }
    
    java.io.File selectedDirectory = directoryChooser.showDialog(stage);
    
    if (selectedDirectory == null) {
        // User cancelled, maybe exit or default to home?
        // Let's default to user home for now if they cancel, or exit.
        // Better to just exit if authorization is mandatory.
        System.exit(0);
        return; 
    }

    stage.setScene(new Scene(createContent(selectedDirectory), 600, 400));
    stage.setMaximized(true);
    stage.show();

  }

  public static void main(String[] args) {
    launch(args);
  }
}
