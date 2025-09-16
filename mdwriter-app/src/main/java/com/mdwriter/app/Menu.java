package com.mdwriter.app;

import java.util.List;

import org.kordamp.ikonli.feather.Feather;
import org.kordamp.ikonli.javafx.FontIcon;

import com.mdwriter.api.ToolBarButton;
import com.mdwriter.app.plugins.ButtonPlugin;

import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.ToolBar;

public class Menu {
  ToolBar toolbar = new ToolBar();

  public Menu(TextArea textarea) {

    ButtonPlugin menu = new ButtonPlugin();
    var undo = new Button(null, new FontIcon(Feather.ARROW_UP_LEFT));
    undo.setOnAction((event -> {
      textarea.undo();
    }));
    var redo = new Button(null, new FontIcon(Feather.ARROW_UP_RIGHT));
    redo.setOnAction((event -> textarea.redo()));
    this.toolbar.getItems().addAll(undo, redo);

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

  }

}
