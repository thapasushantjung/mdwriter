package com.mdwriter.app;

import java.io.File;

import org.kordamp.ikonli.feather.Feather;
import org.kordamp.ikonli.javafx.FontIcon;

import atlantafx.base.theme.Styles;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ToolBar;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

public class Sidebar extends Dialog {

  public Sidebar() {
    super(250, -1);

    var newFile = new Button(null, new FontIcon(Feather.FILE_PLUS));
    newFile.getStyleClass().add(Styles.FLAT);
    var newFolder = new Button(null, new FontIcon(Feather.FOLDER_PLUS));
    newFolder.getStyleClass().add(Styles.FLAT);
    var delete = new Button(null, new FontIcon(Feather.TRASH_2));
    delete.getStyleClass().add(Styles.FLAT);
    var rename = new Button(null, new FontIcon(Feather.EDIT_2));
    rename.getStyleClass().add(Styles.FLAT);

    ToolBar sideToolBar = new ToolBar(newFile, newFolder, delete, rename);
    var tree = new FolderTree().getFolder(new File(System.getProperty("user.home")));
    // Add this inside the start() method from the example above
    var folderContextMenu = new FolderContextMenu();
    tree.setContextMenu(folderContextMenu);

    tree.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
      if (newValue != null) {
        // newValue is the selected TreeItem<String>
        System.out.println("Selected Item: " + newValue.getValue());
      }
    });
    tree.getStyleClass().add(Styles.DENSE);
    tree.setShowRoot(false);
    VBox sidebar = new VBox(sideToolBar, tree);
    sidebar.prefHeightProperty().bind(heightProperty());
    VBox.setVgrow(tree, Priority.ALWAYS);
    setAlignment(Pos.TOP_LEFT);
    getChildren().add(sidebar);

  }

}
