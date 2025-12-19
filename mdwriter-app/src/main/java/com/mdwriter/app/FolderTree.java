package com.mdwriter.app;

import java.io.File;
import java.util.Comparator;
import java.util.Optional;

import org.apache.commons.io.FileUtils;

import javafx.concurrent.Task;
import javafx.scene.Cursor;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.control.cell.TextFieldTreeCell;
import javafx.util.StringConverter;

/**
 * Manages the file system tree view.
 * Handles background loading of directories and file operations (create, rename, delete).
 */
public class FolderTree {
  private TreeView<FileItem> treeView;

  // NOTE: Bug fix here. Fields are no longer final and setters now work
  // correctly.
  public static class FileItem implements Comparable<FileItem> {
    private String name;
    private final String location;
    private final boolean isDirectory;
    private boolean isNewFileCreated;
    private boolean isNewFolderCreated;
    private boolean isRenamed;

    public FileItem(String name, String location, boolean isDirectory) {
      this.name = name;
      this.location = location;
      this.isDirectory = isDirectory;
      this.isNewFileCreated = false;
      this.isNewFolderCreated = false;
      this.isRenamed = false;
    }

    // ... Getters for name, location, isDirectory ...
    public String getName() {
      return name;
    }

    public String getLocation() {
      return location;
    }

    public boolean isDirectory() {
      return isDirectory;
    }

    public boolean isNewFileCreated() {
      return isNewFileCreated;
    }

    public boolean isNewFolderCreated() {
      return isNewFolderCreated;
    }

    public boolean isRenamed() {
      return isRenamed;
    }

    public void setName(String name) {
      this.name = name;
    }

    public void setIsNewFileCreated(boolean bool) {
      this.isNewFileCreated = bool;
    }

    public void setIsRenamed(boolean bool) {
      this.isRenamed = bool;
    }

    public void setIsNewFolderCreated(boolean bool) {
      this.isNewFolderCreated = bool;
    }

    @Override
    public int compareTo(FileItem other) {
      // Directories should come before files
      if (this.isDirectory && !other.isDirectory) {
        return -1;
      } else if (!this.isDirectory && other.isDirectory) {
        return 1;
      }
      return this.name.compareToIgnoreCase(other.getName());
    }

    @Override
    public String toString() {
      return name;
    }
  }

  // NOTE 1: This is a placeholder item to show the expansion arrow on folders
  // that haven't been loaded yet.
  private final TreeItem<FileItem> DUMMY_NODE = new TreeItem<>();

  private TreeItem<FileItem> createNode(File file) {
    FileItem fileItem = new FileItem(file.getName(), file.getPath(), file.isDirectory());
    TreeItem<FileItem> item = new TreeItem<>(fileItem);

    // NOTE 2: If it's a directory, add the dummy node so it can be expanded.
    if (file.isDirectory()) {
      item.getChildren().add(DUMMY_NODE);

      // NOTE 3: Add a listener to load contents on expansion.
      item.expandedProperty().addListener((obs, wasExpanded, isNowExpanded) -> {
        // Load children only when expanded for the first time.
        if (isNowExpanded && item.getChildren().contains(DUMMY_NODE)) {
          loadChildrenInBackground(item);
        }
      });
    }
    return item;
  }

  /**
   * Loads children of a directory on a background thread to prevent UI freezing.
   * @param parent The parent TreeItem to populate.
   */
  private void loadChildrenInBackground(TreeItem<FileItem> parent) {
    // Show a temporary "Loading..." message
    FileItem loadingItem = new FileItem("Loading...", "", false);
    TreeItem<FileItem> loadingNode = new TreeItem<>(loadingItem);
    parent.getChildren().setAll(loadingNode);

    // NOTE 4: This Task runs on a background thread, preventing UI freeze.
    Task<Void> task = new Task<>() {
      @Override
      protected Void call() throws Exception {
        File file = new File(parent.getValue().getLocation());
        File[] files = file.listFiles();

        if (files != null) {
          // This runs in the background
          var children = new java.util.ArrayList<TreeItem<FileItem>>();
          for (File f : files) {
            if (!f.isHidden()) { // Optional: hide hidden files
              children.add(createNode(f));
            }
          }
          children.sort(Comparator.comparing(TreeItem::getValue));

          // NOTE 5: This updates the UI safely on the JavaFX thread when done.
          javafx.application.Platform.runLater(() -> {
            parent.getChildren().setAll(children);
          });
        } else {
          javafx.application.Platform.runLater(() -> {
            parent.getChildren().clear(); // Clear loading message on failure
          });
        }
        return null;
      }
    };

    new Thread(task).start();
  }

  public TreeView<FileItem> getFolder(File dir) {
    TreeItem<FileItem> rootItem = createNode(dir);
    rootItem.setExpanded(true); // Start with the root expanded

    // The initial load is now also done in the background
    loadChildrenInBackground(rootItem);

    treeView = new TreeView<>(rootItem);
    treeView.setEditable(true);

    // CellFactory remains mostly the same
    treeView.setCellFactory(tv -> {
      TextFieldTreeCell<FileItem> cell = new TextFieldTreeCell<>();

      cell.setConverter(new StringConverter<>() {
        @Override
        public String toString(FileItem item) {
          return item == null ? "" : item.getName();
        }

        @Override
        public FileItem fromString(String name) {
          FileItem originalItem = cell.getItem();
          // Mark as renamed for the onEditCommit handler
          originalItem.setIsRenamed(true);
          originalItem.setName(name);
          return originalItem;
        }
      });

      return cell;
    });

    this.treeView = treeView;
    return treeView;

  }

  /**
   * Refresh the tree by reloading the root's children
   */
  public void refresh() {
    if (treeView != null && treeView.getRoot() != null) {
      loadChildrenInBackground(treeView.getRoot());
    }
  }

  public void createNewFile() {
    TreeItem<FileItem> selectedItem = treeView.getSelectionModel().getSelectedItem();
    if (selectedItem == null) {
      // Show alert if nothing is selected
      Alert alert = new Alert(Alert.AlertType.WARNING);
      alert.setTitle("No Selection");
      alert.setHeaderText("No folder selected");
      alert.setContentText("Please select a folder first to create a new file.");
      alert.showAndWait();
      return;
    }

    // Determine the parent directory for the new file
    TreeItem<FileItem> parentDir = selectedItem.getValue().isDirectory()
        ? selectedItem
        : selectedItem.getParent();

    // Ensure the parent's children are loaded before adding a new one
    if (parentDir.getChildren().contains(DUMMY_NODE)) {
      parentDir.getChildren().remove(DUMMY_NODE);
    }

    FileItem tempItem = new FileItem("Untitled.md", parentDir.getValue().getLocation(), false);
    tempItem.setIsNewFileCreated(true); // Mark as new
    TreeItem<FileItem> newItem = new TreeItem<>(tempItem);

    parentDir.getChildren().add(newItem);
    parentDir.setExpanded(true);

    // Defer selection and editing to ensure UI is ready
    javafx.application.Platform.runLater(() -> {
      treeView.getSelectionModel().select(newItem);
      treeView.edit(newItem);
    });
  }

  // createNewFolder would be very similar
  public void createNewFolder() {
    TreeItem<FileItem> selectedItem = treeView.getSelectionModel().getSelectedItem();
    if (selectedItem == null) {
      // Show alert if nothing is selected
      Alert alert = new Alert(Alert.AlertType.WARNING);
      alert.setTitle("No Selection");
      alert.setHeaderText("No folder selected");
      alert.setContentText("Please select a folder first to create a new folder.");
      alert.showAndWait();
      return;
    }

    // Determine the parent directory for the new file
    TreeItem<FileItem> parentDir = selectedItem.getValue().isDirectory()
        ? selectedItem
        : selectedItem.getParent();

    // Ensure the parent's children are loaded before adding a new one
    if (parentDir.getChildren().contains(DUMMY_NODE)) {
      parentDir.getChildren().remove(DUMMY_NODE);
    }

    FileItem tempItem = new FileItem("NewFolder", parentDir.getValue().getLocation(), true);
    tempItem.setIsNewFolderCreated(true); // Mark as new
    TreeItem<FileItem> newItem = new TreeItem<>(tempItem);

    parentDir.getChildren().add(newItem);
    parentDir.setExpanded(true);

    // Defer selection and editing to ensure UI is ready
    javafx.application.Platform.runLater(() -> {
      treeView.getSelectionModel().select(newItem);
      treeView.edit(newItem);
    });
  }


  public void rename() {
    TreeItem<FileItem> selectedItem = treeView.getSelectionModel().getSelectedItem();
    if (selectedItem == null) {
      return; // Nothing selected
    }

    // Optional: Prevent renaming the root node
    if (selectedItem.getParent() == null) {
      System.out.println("Cannot rename the root directory.");
      return;
    }

    // Trigger the built-in edit mechanism
    treeView.edit(selectedItem);
  }

  public void delete() {
    TreeItem<FileItem> selectedItem = treeView.getSelectionModel().getSelectedItem();
    if (selectedItem == null || selectedItem.getParent() == null) {
      return; // Nothing selected or root is selected
    }

    Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
    confirmation.setTitle("Delete Item");
    confirmation.setHeaderText("Delete '" + selectedItem.getValue().getName() + "'?");
    confirmation.setContentText("This action is permanent and cannot be undone.");

    Optional<ButtonType> result = confirmation.showAndWait();

    if (result.isPresent() && result.get() == ButtonType.OK) {
      // Create a Task for the background deletion process
      Task<Void> deleteTask = new Task<>() {
        @Override
        protected Void call() throws Exception {
          // This is the long-running code that runs on a background thread
          File fileToDelete = new File(selectedItem.getValue().getLocation());
          FileUtils.forceDelete(fileToDelete);
          return null; // Return null because we don't need a result value
        }
      };

      // What to do when the task SUCCEEDS (runs on the UI thread)
      deleteTask.setOnSucceeded(event -> {
        // Remove the item from the TreeView
        selectedItem.getParent().getChildren().remove(selectedItem);
        // Restore the normal cursor
        treeView.getScene().setCursor(Cursor.DEFAULT);
      });

      // What to do when the task FAILS (runs on the UI thread)
      deleteTask.setOnFailed(event -> {
        Throwable e = deleteTask.getException(); // Get the exception from the task
        Alert error = new Alert(Alert.AlertType.ERROR);
        error.setTitle("Error");
        error.setHeaderText("Failed to delete '" + selectedItem.getValue().getName() + "'");
        error.setContentText("Please check file permissions and try again.\n" + e.getMessage());
        error.showAndWait();
        // Restore the normal cursor
        treeView.getScene().setCursor(Cursor.DEFAULT);
      });

      // Give immediate visual feedback to the user
      treeView.getScene().setCursor(Cursor.WAIT);

      // Start the task on a new thread
      new Thread(deleteTask).start();
    }
  }
}
