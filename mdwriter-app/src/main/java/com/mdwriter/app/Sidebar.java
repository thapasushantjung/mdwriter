package com.mdwriter.app;

import java.io.File;

import org.kordamp.ikonli.feather.Feather;
import org.kordamp.ikonli.javafx.FontIcon;

import com.mdwriter.app.FolderTree.FileItem;

import atlantafx.base.theme.Styles;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ToolBar;
import javafx.scene.control.TreeItem;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

public class Sidebar extends Dialog {
  public Sidebar() {
    super(250, -1);
    // ... (Toolbar button setup remains the same)
    var newFile = new Button(null, new FontIcon(Feather.FILE_PLUS));
    newFile.getStyleClass().add(Styles.FLAT);
    var newFolder = new Button(null, new FontIcon(Feather.FOLDER_PLUS));
    newFolder.getStyleClass().add(Styles.FLAT);
    var delete = new Button(null, new FontIcon(Feather.TRASH_2));
    delete.getStyleClass().add(Styles.FLAT);
    var rename = new Button(null, new FontIcon(Feather.EDIT_2));
    rename.getStyleClass().add(Styles.FLAT);
    ToolBar sideToolBar = new ToolBar(newFile, newFolder, delete, rename);

    var folder = new FolderTree();

    // THIS CALL IS NOW NON-BLOCKING AND RETURNS INSTANTLY 🚀
    var tree = folder.getFolder(new File(System.getProperty("user.home")));

    // The onEditCommit logic needs to be updated to handle renaming properly
    tree.setOnEditCommit(event -> {
      TreeItem<FileItem> editedItem = event.getTreeItem();
      FileItem updatedFileItem = event.getNewValue(); // Use getNewValue()
      FileItem oldFileItem = event.getOldValue();

      if (updatedFileItem.isNewFileCreated()) {
        // Logic to create a new file on disk
        File parentDir = new File(editedItem.getParent().getValue().getLocation());
        File file = new File(parentDir, updatedFileItem.getName());
        try {
          if (file.createNewFile()) {
            FileItem finalItem = new FileItem(file.getName(), file.getPath(), false);
            editedItem.setValue(finalItem);
          } else {
            // Handle error: file already exists
            editedItem.getParent().getChildren().remove(editedItem);
          }
        } catch (Exception e) {
          // Handle I/O error
          editedItem.getParent().getChildren().remove(editedItem);
        }

      } else if (updatedFileItem.isRenamed()) {
        // Logic to rename the file on disk
        File oldFile = new File(oldFileItem.getLocation());
        File file = new File(oldFile.getParent(), updatedFileItem.getName());
        if (oldFile.renameTo(file)) {
          // Update the item in the tree with the new path
          FileItem finalItem = new FileItem(file.getName(), file.getPath(), file.isDirectory());
          editedItem.setValue(finalItem);
        } else {
          // Handle rename failure, revert to old name
          editedItem.setValue(oldFileItem);
        }
      }
    });
    // Add this inside the start() method from the example above
    var folderContextMenu = new FolderContextMenu(folder);
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
