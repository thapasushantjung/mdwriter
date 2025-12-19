package com.mdwriter.app;

import atlantafx.base.controls.ModalPane;
import atlantafx.base.theme.PrimerDark;
import javafx.application.Application;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.geometry.Pos;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebView;
import javafx.stage.Stage;

/**
 * Main Application class for the Markdown Writer App.
 * Initializes the JavaFX application, sets up the main window (Stage),
 * and coordinates the primary UI components (Editor, WebView, Toolbar).
 * 
 * @author Sushant Thapa
 * @version 1.0
 */
public class MainApp extends Application {

  private ModalPane modalPane;

  /**
   * Creates the main content area of the application.
   * Initializes the Editor, WebView, and Toolbar.
   * 
   * @param rootDirectory The root directory of the workspace.
   * @return The Parent node containing the application layout.
   * @throws Exception If initialization fails.
   */
  private Parent createContent(java.io.File rootDirectory) throws Exception {

    // Initialize WebView for preview and Editor for input
    WebView webview = new WebView();
    Editor textarea = new Editor(webview, rootDirectory);
    
    // Initialize the Toolbar/Menu system
    var toolBar = new Menu(textarea, rootDirectory);
    modalPane = toolBar.modalPane;

    HBox container = new HBox(textarea, webview);
    HBox.setHgrow(textarea, Priority.ALWAYS);

    VBox root = new VBox();
    root.setPadding(new javafx.geometry.Insets(10));
    // Status Bar Configuration
    HBox statusBar = new HBox();
    statusBar.setPadding(new javafx.geometry.Insets(5));
    statusBar.setAlignment(Pos.CENTER_RIGHT);
    Label wordCountLabel = new Label("Words: 0");
    statusBar.getChildren().add(wordCountLabel);
    
    // Bind word count label
    wordCountLabel.textProperty().bind(textarea.wordCountProperty().asString("Words: %d"));

    root.getChildren().addAll(toolBar.toolbar, container, statusBar);

    StackPane stack = new StackPane();
    stack.getChildren().addAll(root, modalPane);

    VBox.setVgrow(container, Priority.ALWAYS);

    return stack;
  }

  @Override
  public void start(Stage stage) throws Exception {
    // Load the dark theme stylesheet
    Application.setUserAgentStylesheet(new PrimerDark().getUserAgentStylesheet());

    // Prompt user to select a workspace directory
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
