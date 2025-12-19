package com.mdwriter.app;

import javafx.geometry.Pos;
import javafx.scene.layout.VBox;

/**
 * A custom VBox-based dialog component.
 * Used as a base for modal dialogs like the Sidebar and ThemeSelector.
 */
public class Dialog extends VBox {

  public Dialog(int width, int height) {
    super();
    setSpacing(10);
    setAlignment(Pos.CENTER);
    setMinSize(width, height);
    setMaxSize(width, height);
    setStyle("-fx-background-color: -color-bg-default");
  }

}
