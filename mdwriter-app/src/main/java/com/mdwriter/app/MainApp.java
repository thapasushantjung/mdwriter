package com.mdwriter.app;

import com.mdwriter.api.ToolBarButton;
import com.mdwriter.app.plugins.ButtonPlugin;
import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.util.ast.Node;
import com.vladsch.flexmark.util.data.MutableDataSet;

import atlantafx.base.controls.ModalPane;
import atlantafx.base.theme.PrimerDark;
import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.MenuBar;
import javafx.scene.control.TextArea;
import javafx.scene.control.ToolBar;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import java.util.List;

import org.kordamp.ikonli.feather.Feather;
import org.kordamp.ikonli.javafx.FontIcon;

/**
 *
 * Main Application class for the Markdown Writer App.
 * 
 * @author Sushant Thapa
 * @Version 1.0
 */
public class MainApp extends Application {

  private final ModalPane modalPane = new ModalPane();

  private Parent createContent() throws Exception {

    WebView webview = new WebView();
    TextArea textarea = new Editor(webview);
    var toolBar = new Menu(textarea);

    var normalBtn = new Button(null, new FontIcon(Feather.SUN));
    var dialog = new ThemeSelector().themes();
    normalBtn.setOnAction((evt -> modalPane.show(dialog)));

    HBox menuBar = new HBox();
    Region spacer = new Region();
    HBox.setHgrow(spacer, Priority.ALWAYS);
    menuBar.getChildren().addAll(toolBar.toolbar, spacer, normalBtn);

    HBox container = new HBox(textarea, webview);
    HBox.setHgrow(textarea, Priority.ALWAYS);

    VBox root = new VBox();
    root.setPadding(new javafx.geometry.Insets(10));
    root.getChildren().addAll(menuBar, container);

    StackPane stack = new StackPane();
    stack.getChildren().addAll(root, modalPane);

    VBox.setVgrow(container, Priority.ALWAYS);
    return stack;
  }

  @Override
  public void start(Stage stage) throws Exception {
    Application.setUserAgentStylesheet(new PrimerDark().getUserAgentStylesheet());

    modalPane.setId("modalPane");
    modalPane.displayProperty().addListener((obs, old, val) -> {
      if (!val) {
        modalPane.setAlignment(Pos.CENTER);
        modalPane.usePredefinedTransitionFactories(null);
      }
    });

    stage.setScene(new Scene(createContent(), 600, 400));
    stage.setMaximized(true);
    stage.show();

  }

  public static void main(String[] args) {
    launch(args);
  }
}
