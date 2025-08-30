
/*
 * Copyright (C) 2012-present the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.mdwriter.app;

import org.apache.commons.lang3.StringUtils;
import org.pf4j.CompoundPluginDescriptorFinder;
import org.pf4j.ManifestPluginDescriptorFinder;
import org.pf4j.PropertiesPluginDescriptorFinder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.kordamp.ikonli.*;
import org.pf4j.DefaultPluginManager;
import org.pf4j.PluginManager;
import org.pf4j.PluginWrapper;
import com.mdwriter.api.Greeting;
import atlantafx.base.theme.PrimerDark;
import atlantafx.base.theme.PrimerLight;
import atlantafx.base.theme.Styles;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Separator;
import javafx.scene.control.TextArea;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.ToolBar;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.web.HTMLEditor;
import javafx.stage.Stage;
import java.net.URL;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.kordamp.ikonli.feather.Feather;
import org.kordamp.ikonli.javafx.FontIcon;

/**
 * A boot class that start the demo.
 *
 * @author Decebal Suiu
 */
public class MainApp extends Application {

  public static Button iconButton(Ikon icon) {
    var btn = new Button(null);
    if (icon != null) {
      btn.setGraphic(new FontIcon(icon));
    }
    btn.getStyleClass().addAll(Styles.BUTTON_ICON);
    return btn;
  }

  public ToggleButton toggleIconButton(Ikon icon, String... styleClasses) {
    return toggleIconButton(icon, null, false, styleClasses);
  }

  public ToggleButton toggleIconButton(Ikon icon, boolean selected, String... styleClasses) {
    return toggleIconButton(icon, null, selected, styleClasses);
  }

  public ToggleButton toggleIconButton(Ikon icon, ToggleGroup group, boolean selected,
      String... styleClasses) {
    var btn = new ToggleButton("");
    if (icon != null) {
      btn.setGraphic(new FontIcon(icon));
    }
    if (group != null) {
      btn.setToggleGroup(group);
    }
    btn.getStyleClass().addAll(styleClasses);
    btn.setSelected(selected);
    return btn;
  }

  private static final Logger logger = LoggerFactory.getLogger(MainApp.class);

  private Parent createContent() throws Exception {

    // BorderPane
    // BorderPane borderPane = new BorderPane();

    var fontFamilyCmb = new ComboBox<>(
        FXCollections.observableArrayList(Font.getFamilies()));
    fontFamilyCmb.setPrefWidth(150);
    fontFamilyCmb.getSelectionModel().selectFirst();

    var fontSizeCmb = new ComboBox<>(
        IntStream.range(6, 32).mapToObj(String::valueOf).collect(
            Collectors.toCollection(FXCollections::observableArrayList)));
    fontSizeCmb.getSelectionModel().select(6);

    final var toolbar2 = new ToolBar(
        fontFamilyCmb,
        fontSizeCmb,
        new Separator(Orientation.VERTICAL),
        toggleIconButton(Feather.BOLD, true),
        toggleIconButton(Feather.ITALIC),
        toggleIconButton(Feather.UNDERLINE),
        new Separator(Orientation.VERTICAL),
        toggleIconButton(Feather.ALIGN_LEFT),
        toggleIconButton(Feather.ALIGN_CENTER),
        toggleIconButton(Feather.ALIGN_RIGHT),
        new Separator(Orientation.VERTICAL),
        iconButton(Feather.IMAGE));

    FXMLLoader loader = new FXMLLoader();
    loader.setLocation(MainApp.class.getResource("Navbar.fxml"));
    // HBox hbox = loader.<HBox>load();
    TextArea textarea = new TextArea();
    TextArea textarea2 = new TextArea();
    // borderPane.setTop(toolbar2);
    // borderPane.setLeft(textarea);

    // borderPane.setRight(new TextArea());
    //
    HBox hbox = new HBox(textarea, textarea2);
    HBox.setHgrow(textarea, Priority.ALWAYS);
    HBox.setHgrow(textarea2, Priority.ALWAYS);

    VBox root = new VBox();
    root.setPadding(new javafx.geometry.Insets(10));
    root.getChildren().addAll(toolbar2, hbox);
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
    // print logo
    launch(args);

    // create the plugin manager
    final PluginManager pluginManager = new DefaultPluginManager() {
      @Override
      protected CompoundPluginDescriptorFinder createPluginDescriptorFinder() {
        return new CompoundPluginDescriptorFinder()
            // Demo is using the Manifest file
            // PropertiesPluginDescriptorFinder is commented out just to avoid error log
            // .add(new PropertiesPluginDescriptorFinder())
            .add(new ManifestPluginDescriptorFinder());
      }
    };

    // load the plugins
    pluginManager.loadPlugins();

    // enable a disabled plugin
    // pluginManager.enablePlugin("welcome-plugin");

    // start (active/resolved) the plugins
    pluginManager.startPlugins();

    logger.info("Plugindirectory: ");
    logger.info("\t" + System.getProperty("pf4j.pluginsDir", "plugins") + "\n");

    // retrieves the extensions for Greeting extension point
    List<Greeting> greetings = pluginManager.getExtensions(Greeting.class);
    logger.info(
        String.format("Found %d extensions for extension point '%s'", greetings.size(), Greeting.class.getName()));
    for (Greeting greeting : greetings) {
      logger.info(">>> " + greeting.getGreeting());
    }

    // // print extensions from classpath (non plugin)
    // logger.info(String.format("Extensions added by classpath:"));
    // Set<String> extensionClassNames = pluginManager.getExtensionClassNames(null);
    // for (String extension : extensionClassNames) {
    // logger.info(" " + extension);
    // }

    // print extensions for each started plugin
    List<PluginWrapper> startedPlugins = pluginManager.getStartedPlugins();
    for (PluginWrapper plugin : startedPlugins) {
      String pluginId = plugin.getDescriptor().getPluginId();
      logger.info(String.format("Extensions added by plugin '%s':", pluginId));
      // extensionClassNames = pluginManager.getExtensionClassNames(pluginId);
      // for (String extension : extensionClassNames) {
      // logger.info(" " + extension);
      // }
    }

    // stop the plugins
    pluginManager.stopPlugins();
    /*
     * Runtime.getRuntime().addShutdownHook(new Thread() {
     * 
     * @Override
     * public void run() {
     * pluginManager.stopPlugins();
     * }
     * 
     * });
     */
  }

  private static void printLogo() {
    logger.info(StringUtils.repeat("#", 40));
    logger.info(StringUtils.center("PF4J-DEMO", 40));
    logger.info(StringUtils.repeat("#", 40));
  }

}
