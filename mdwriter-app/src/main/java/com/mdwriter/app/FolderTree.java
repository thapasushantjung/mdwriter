package com.mdwriter.app;

import java.io.File;
import java.util.Comparator;

import org.kordamp.ikonli.feather.Feather;
import org.kordamp.ikonli.javafx.FontIcon;

import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;

public class FolderTree {

  public TreeItem<String> scan(File dir, int depth) {
    var parent = new TreeItem<>(
        dir.getName(),
        new FontIcon(Feather.FOLDER));
    File[] files = dir.listFiles();
    depth--;

    if (files != null) {
      for (File f : files) {
        if (f.isDirectory() && depth > 0) {
          parent.getChildren().add(scan(f, depth));
        } else {
          var leaf = new TreeItem<>(
              f.getName(),
              new FontIcon(Feather.FILE));
          parent.getChildren().add(leaf);
        }
      }
      parent.getChildren().sort(
          Comparator.comparing(TreeItem::getValue));
    }

    return parent;
  }
}
