package com.mdwriter.app;

import atlantafx.base.controls.ModalPane;
import atlantafx.base.theme.PrimerDark;
import javafx.application.Application;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
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

  private Parent createContent() throws Exception {

    WebView webview = new WebView();
    TextArea textarea = new Editor(webview);
    var toolBar = new Menu(textarea);
    modalPane = toolBar.modalPane;

    HBox container = new HBox(textarea, webview);
    HBox.setHgrow(textarea, Priority.ALWAYS);

    TabPane tabPane = new TabPane();
    Tab tab1 = new Tab("Dashboard");
    VBox root = new VBox();
    root.setPadding(new javafx.geometry.Insets(10));
    root.getChildren().addAll(toolBar.toolbar, container);

    tab1.setContent(root);
    tabPane.getTabs().add(tab1);
    StackPane stack = new StackPane();
    stack.getChildren().addAll(root, modalPane);

    VBox.setVgrow(container, Priority.ALWAYS);

    return stack;
  }

  @Override
  public void start(Stage stage) throws Exception {
    Application.setUserAgentStylesheet(new PrimerDark().getUserAgentStylesheet());

    stage.setScene(new Scene(createContent(), 600, 400));
    stage.setMaximized(true);
    stage.show();

  }

  public static void main(String[] args) {
    launch(args);
  }
}
