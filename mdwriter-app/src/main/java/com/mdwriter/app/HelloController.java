package com.mdwriter.app;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
public class HelloController {

  @FXML
  private Label WelcomeText;

  @FXML
  protected void onHelloButtonClick() {
    WelcomeText.setText("Welcome to JavaFX Application!");
  }
}
