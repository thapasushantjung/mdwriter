package com.mdwriter.app;

import java.io.File;
import java.util.Comparator;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import org.kordamp.ikonli.feather.Feather;
import org.kordamp.ikonli.javafx.FontIcon;

public class FolderTree {
  public static class FileItem implements Comparable<FileItem> {
    private final String name;
    private final String location;
    private final boolean isDirectory;

    public FileItem(String name, String location, boolean isDirectory) {
      this.name = name;
      this.location = location;
      this.isDirectory = isDirectory;
    }

    public String getName() {
      return name;
    }

    public String getLocation() {
      return location;
    }

    public boolean isDirectory() {
      return isDirectory;
    }

    @Override
    public int compareTo(FileItem other) {
      return this.name.compareToIgnoreCase(other.getName());
    }
  }

  private TreeItem<FileItem> scan(File dir, int depth) {
    var par = new FileItem(dir.getName(), dir.getPath(), dir.isDirectory());
    // The graphic here is redundant because the CellFactory handles it,
    // but it doesn't cause harm.
    var parent = new TreeItem<>(par);
    File[] files = dir.listFiles();
    depth--;

    if (files != null) {
      for (File f : files) {
        // We don't need to add icons here because the CellFactory will do it
        if (f.isDirectory() && depth > 0) {
          parent.getChildren().add(scan(f, depth));
        } else if (!f.isDirectory()) { // Make sure we only add files, not deep dirs
          var le = new FileItem(f.getName(), f.getPath(), f.isDirectory());
          var leaf = new TreeItem<>(le);
          parent.getChildren().add(leaf);
        }
      }
      parent.getChildren().sort(Comparator.comparing(TreeItem::getValue));
    }

    return parent;
  }

  public TreeView<FileItem> getFolder(File dir) {
    var tr = this.scan(dir, 3);

    TreeView<FileItem> tre = new TreeView<>(tr);
    tre.setCellFactory(tv -> new TreeCell<FileItem>() {
      @Override
      protected void updateItem(FileItem item, boolean empty) {
        super.updateItem(item, empty); // Essential call

        if (empty || item == null) {
          setText(null);
          setGraphic(null);
        } else {
          // Set the text to the item's name
          setText(item.getName());

          // Set a graphic (icon) based on the isDirectory property
          FontIcon icon;
          if (item.isDirectory()) {
            icon = new FontIcon(Feather.FOLDER); // 📁 Folder Icon
          } else {
            icon = new FontIcon(Feather.FILE_TEXT); // 📄 File Icon
          }
          setGraphic(icon);
        }
      }
    }); // Semicolon was missing here

    return tre; // Return statement was missing
  }
}
