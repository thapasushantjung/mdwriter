package com.mdwriter.app;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

import org.apache.commons.io.FileUtils;

import org.kordamp.ikonli.feather.Feather;
import org.kordamp.ikonli.javafx.FontIcon;

import com.mdwriter.app.FolderTree.FileItem;

import atlantafx.base.theme.Styles;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.ToolBar;
import javafx.scene.control.TreeItem;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

/**
 * The Sidebar component (File Explorer).
 * Displays the project file structure, handles file/folder creation, deletion, renaming,
 * and auto-saves the currently open file.
 */
public class Sidebar extends Dialog {
  private File file;
  private Timeline autoSaveTimeline;
  private FolderTree folderTree;

  public Sidebar(TextArea textarea, File rootDirectory) {
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

    this.folderTree = new FolderTree();

    // THIS CALL IS NOW NON-BLOCKING AND RETURNS INSTANTLY 🚀
    // IMPORTANT: getFolder() initializes the internal treeView, must be called first
    var tree = folderTree.getFolder(rootDirectory);

    // Wire up buttons AFTER tree is initialized
    newFile.setOnAction(e -> {
      System.out.println("DEBUG: New File button clicked");
      folderTree.createNewFile();
    });
    newFolder.setOnAction(e -> {
      System.out.println("DEBUG: New Folder button clicked");
      folderTree.createNewFolder();
    });
    delete.setOnAction(e -> {
      System.out.println("DEBUG: Delete button clicked");
      folderTree.delete();
    });
    rename.setOnAction(e -> {
      System.out.println("DEBUG: Rename button clicked");
      folderTree.rename();
    });

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
            tree.getSelectionModel().clearSelection();
            tree.getSelectionModel().select(editedItem);
          } else {
            // Handle error: file already exists
            editedItem.getParent().getChildren().remove(editedItem);
          }
        } catch (Exception e) {
          // Handle I/O error
          editedItem.getParent().getChildren().remove(editedItem);
        }

      } else if (updatedFileItem.isNewFolderCreated()) {
        // Logic to create a new file on disk
        File parentDir = new File(editedItem.getParent().getValue().getLocation());
        File file = new File(parentDir, updatedFileItem.getName());
        try {
          if (file.mkdir()) {
            FileItem finalItem = new FileItem(file.getName(), file.getPath(), true);
            editedItem.setValue(finalItem);
            tree.getSelectionModel().clearSelection();
            tree.getSelectionModel().select(editedItem);
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
          tree.refresh();
        } else {
          // Handle rename failure, revert to old name
          editedItem.setValue(oldFileItem);
        }
      }
    });
    // Add this inside the start() method from the example above
    var folderContextMenu = new FolderContextMenu(folderTree);
    tree.setContextMenu(folderContextMenu);
    autoSaveTimeline = new Timeline(new KeyFrame(Duration.seconds(10), event -> {
      if (this.file != null) {
        try {
          // Get content from TextArea and write to the file
          String content = textarea.getText();
          FileUtils.writeStringToFile(this.file, content, StandardCharsets.UTF_8);

          // Update status label to give user feedback
          // Note: This prints to stdout, could be improved to update a UI label
          String time = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
          System.out.println("File auto-saved at " + time);

        } catch (IOException e) {
          e.printStackTrace();
          // Stop the timeline if saving fails to prevent repeated errors
          autoSaveTimeline.stop();
        }
      }
    }));
    autoSaveTimeline.setCycleCount(Animation.INDEFINITE);

    tree.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
      if (newValue == null) {
        return;
      }

      FileItem selectedItem = newValue.getValue();
      File file = new File(selectedItem.getLocation());

      if (!file.isDirectory() && file != null) {
        this.file = file;

        try {

          String content = FileUtils.readFileToString(file, StandardCharsets.UTF_8);
          // Use setContent for auto mode detection if it's an Editor
          if (textarea instanceof Editor) {
            ((Editor) textarea).setContent(content);
          } else {
            textarea.setText(content);
          }
          autoSaveTimeline.playFromStart();

        } catch (IOException e) {
          e.printStackTrace();
        }
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

  /**
   * Refresh the file tree to show newly created files
   */
  public void refresh() {
    if (folderTree != null) {
      folderTree.refresh();
    }
  }

}
