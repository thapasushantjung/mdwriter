package com.mdwriter.app;

import atlantafx.base.theme.CupertinoDark;
import atlantafx.base.theme.CupertinoLight;
import atlantafx.base.theme.Dracula;
import atlantafx.base.theme.NordDark;
import atlantafx.base.theme.NordLight;
import atlantafx.base.theme.PrimerDark;
import atlantafx.base.theme.PrimerLight;
import javafx.application.Application;
import javafx.scene.control.Button;

/**
 * Dialog for selecting the application theme.
 * Uses AtlantaFX themes to provide a modern look and feel.
 */
public class ThemeSelector {

  public Dialog themes() {

    Button primerDark = new Button("Primar Dark");
    Button primerLight = new Button("Primer Light");
    Button nordLight = new Button("Nord Light");
    Button nordDark = new Button("Nord dark");
    Button cupertinoLight = new Button("Cupertino Light");
    Button cupertinoDark = new Button("Cupertino Dark");
    Button dracula = new Button("Dracula");
    primerDark.setOnAction((evt -> Application.setUserAgentStylesheet(new PrimerDark().getUserAgentStylesheet())));
    primerLight.setOnAction((evt -> Application.setUserAgentStylesheet(new PrimerLight().getUserAgentStylesheet())));
    nordLight.setOnAction((evt -> Application.setUserAgentStylesheet(new NordLight().getUserAgentStylesheet())));
    nordDark.setOnAction((evt -> Application.setUserAgentStylesheet(new NordDark().getUserAgentStylesheet())));
    dracula.setOnAction((evt -> Application.setUserAgentStylesheet(new Dracula().getUserAgentStylesheet())));
    cupertinoDark
        .setOnAction((evt -> Application.setUserAgentStylesheet(new CupertinoDark().getUserAgentStylesheet())));
    cupertinoLight
        .setOnAction(evt -> Application.setUserAgentStylesheet(new CupertinoLight().getUserAgentStylesheet()));
    var dialog = new Dialog(450, 450);
    dialog.getChildren().addAll(primerLight, primerDark, nordLight, nordDark, cupertinoLight, cupertinoDark, dracula);
    return dialog;

  }
}
