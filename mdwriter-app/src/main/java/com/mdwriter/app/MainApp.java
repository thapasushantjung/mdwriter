package com.mdwriter.app;

import com.mdwriter.api.ToolBarButton;
import atlantafx.base.theme.PrimerDark;
import javafx.application.Application;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TextArea;
import javafx.scene.control.ToolBar;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import java.util.List;

/**
 *
 * Main Application class for the Markdown Writer App.
 * 
 * @author Sushant Thapa
 * @Version 1.0
 */
public class MainApp extends Application {

  // A toolbar to hold the buttons
  ToolBar toolbar = new ToolBar();

  private Parent createContent() throws Exception {

    MenuBar menu = new MenuBar();
    List<ToolBarButton> buttons = menu.buttons;
    for (ToolBarButton button : buttons) {
      this.toolbar.getItems().add(button.iconButton());
    }
    TextArea textarea = new TextArea();
    TextArea textarea2 = new TextArea();
    HBox hbox = new HBox(textarea, textarea2);
    HBox.setHgrow(textarea, Priority.ALWAYS);
    HBox.setHgrow(textarea2, Priority.ALWAYS);

    VBox root = new VBox();
    root.setPadding(new javafx.geometry.Insets(10));
    root.getChildren().addAll(toolbar, hbox);
    VBox.setVgrow(hbox, Priority.ALWAYS);
    return root;
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
