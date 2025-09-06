package com.mdwriter.app;

import com.mdwriter.api.ToolBarButton;
import com.mdwriter.app.plugins.ButtonPlugin;
import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.util.ast.Node;
import com.vladsch.flexmark.util.data.MutableDataSet;

import atlantafx.base.theme.PrimerDark;
import javafx.application.Application;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TextArea;
import javafx.scene.control.ToolBar;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebView;
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

  ToolBar toolbar = new ToolBar();

  private Parent createContent() throws Exception {

    TextArea textarea = new TextArea();
    TextArea textarea2 = new TextArea();
    WebView webview = new WebView();
    ButtonPlugin menu = new ButtonPlugin();
    List<ToolBarButton> buttons = menu.buttons;
    for (ToolBarButton button : buttons) {

      System.out.println("Button: " + button.getIcon());
      var iconButton = button.iconButton();

      this.toolbar.getItems().add(iconButton);

      iconButton.setOnMousePressed(event -> {
        String selectedText = textarea.getSelectedText();

        String changedText = button.changeText(selectedText);

        textarea.replaceSelection(changedText);
      });
    }
    textarea.textProperty().addListener((obs, oldText, newText) -> {
      MutableDataSet options = new MutableDataSet();
      options.set(HtmlRenderer.SOFT_BREAK, "<br />\n");
      Parser parser = Parser.builder().build();
      HtmlRenderer renderer = HtmlRenderer.builder().build();
      String processedText = newText.replace("\n", "  \n");
      System.out.println(processedText);
      Node document = parser.parse(processedText);
      String html = renderer.render(document);
      webview.getEngine().loadContent(html);
    });
    HBox container = new HBox(textarea, webview);
    HBox.setHgrow(textarea, Priority.ALWAYS);
    HBox.setHgrow(textarea2, Priority.ALWAYS);

    VBox root = new VBox();
    root.setPadding(new javafx.geometry.Insets(10));
    root.getChildren().addAll(toolbar, container);
    VBox.setVgrow(container, Priority.ALWAYS);
    return root;
    // :TODO - Add WebView and Markdown Parser
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
