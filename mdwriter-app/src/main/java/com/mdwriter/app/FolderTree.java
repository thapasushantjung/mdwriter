package com.mdwriter.app;

import java.io.File;
import java.util.Comparator;

import org.kordamp.ikonli.feather.Feather;
import org.kordamp.ikonli.javafx.FontIcon;

import javafx.concurrent.Task;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.control.cell.TextFieldTreeCell;
import javafx.util.StringConverter;

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

  public void createNewFile() {
    TreeItem<FileItem> selectedItem = treeView.getSelectionModel().getSelectedItem();
    if (selectedItem == null)
      return; // Nothing selected

    // Determine the parent directory for the new file
    TreeItem<FileItem> parentDir = selectedItem.getValue().isDirectory()
        ? selectedItem
        : selectedItem.getParent();

    // Ensure the parent's children are loaded before adding a new one
    if (parentDir.getChildren().contains(DUMMY_NODE)) {
      // If not loaded, expand it to trigger loading, then try again.
      // This is a complex scenario, for simplicity we'll just add it.
      // A more robust solution would wait for the load to complete.
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
    if (selectedItem == null)
      return; // Nothing selected

    // Determine the parent directory for the new file
    TreeItem<FileItem> parentDir = selectedItem.getValue().isDirectory()
        ? selectedItem
        : selectedItem.getParent();

    // Ensure the parent's children are loaded before adding a new one
    if (parentDir.getChildren().contains(DUMMY_NODE)) {
      // If not loaded, expand it to trigger loading, then try again.
      // This is a complex scenario, for simplicity we'll just add it.
      // A more robust solution would wait for the load to complete.
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
}
