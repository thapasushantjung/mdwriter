package com.mdwriter.app;

import java.util.List;
import org.kordamp.ikonli.feather.Feather;
import org.kordamp.ikonli.javafx.FontIcon;
import com.mdwriter.api.ToolBarButton;
import com.mdwriter.app.plugins.ButtonPlugin;

import atlantafx.base.controls.ModalPane;
import atlantafx.base.theme.Styles;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.geometry.Side;
import javafx.scene.control.Button;
import javafx.scene.control.Separator;
import javafx.scene.control.TextArea;
import javafx.scene.control.ToolBar;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;

public class Menu {
  ToolBar toolbar = new ToolBar();

  public ModalPane modalPane = new ModalPane();

  public Menu(TextArea textarea) {

    modalPane.setId("modalPane");
    modalPane.displayProperty().addListener((obs, old, val) -> {
      if (!val) {
        modalPane.setAlignment(Pos.CENTER);
        modalPane.usePredefinedTransitionFactories(null);
      }
    });

    ButtonPlugin menu = new ButtonPlugin();

    var themeChanger = new Button(null, new FontIcon(Feather.SUN));
    themeChanger.getStyleClass().add(Styles.FLAT);
    var dialog = new ThemeSelector().themes();
    themeChanger.setOnAction((evt -> modalPane.show(dialog)));
    var left_dialog = new Sidebar(textarea);

    var folder = new Button(null, new FontIcon(Feather.FOLDER));
    folder.getStyleClass().add(Styles.FLAT);
    folder.setOnAction((event -> {
      modalPane.setAlignment(Pos.TOP_LEFT);
      modalPane.usePredefinedTransitionFactories(Side.LEFT);
      modalPane.show(left_dialog);
    }));

    var undo = new Button(null, new FontIcon(Feather.ARROW_UP_LEFT));
    undo.setOnAction((event -> textarea.undo()));

    var redo = new Button(null, new FontIcon(Feather.ARROW_UP_RIGHT));
    redo.setOnAction((event -> textarea.redo()));

    this.toolbar.getItems().addAll(folder, new Separator(Orientation.VERTICAL), undo, redo);
    this.toolbar.setMaxWidth(Double.MAX_VALUE);

    List<ToolBarButton> buttons = menu.buttons;
    for (ToolBarButton button : buttons) {
      var iconButton = button.iconButton();
      this.toolbar.getItems().add(iconButton);
      iconButton.setOnMousePressed(event -> {
        String selectedText = textarea.getSelectedText();
        String changedText = button.changeText(selectedText);
        textarea.replaceSelection(changedText);
      });
    }
    Region spacer = new Region();
    HBox.setHgrow(spacer, Priority.ALWAYS);

    this.toolbar.getItems().addAll(spacer, themeChanger);

  }

}
