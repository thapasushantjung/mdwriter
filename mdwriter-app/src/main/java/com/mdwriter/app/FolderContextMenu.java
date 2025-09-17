package com.mdwriter.app;

import org.jetbrains.annotations.Nullable;
import org.kordamp.ikonli.Ikon;
import org.kordamp.ikonli.feather.Feather;
import org.kordamp.ikonli.javafx.FontIcon;

import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;

public class FolderContextMenu extends ContextMenu {
  public FolderContextMenu() {

    var newFile = createItem("_New File", Feather.FILE_PLUS);
    var newFolder = createItem("_New Folder", Feather.FOLDER_PLUS);
    var delete = createItem("_Delete", Feather.TRASH_2);
    var rename = createItem("_Rename", Feather.EDIT_2);

    getItems().addAll(
        newFile, newFolder, delete, rename);

  }

  private MenuItem createItem(@Nullable String text,
      @Nullable Ikon graphic) {

    var item = new MenuItem(text);

    if (graphic != null) {
      item.setGraphic(new FontIcon(graphic));
    }

    return item;
  }
}
