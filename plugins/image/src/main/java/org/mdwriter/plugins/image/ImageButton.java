package org.mdwriter.plugins.image;

import com.mdwriter.api.ToolBarButton;
import org.kordamp.ikonli.Ikon;
import org.kordamp.ikonli.feather.Feather;
import org.pf4j.Extension;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import java.io.File;
import java.util.Optional;

@Extension(ordinal = 2)
public class ImageButton implements ToolBarButton {

    @Override
    public Ikon getIcon() {
        return Feather.IMAGE;
    }

    @Override
    public String changeText(String text) {
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("Insert Image");
        dialog.setHeaderText("Enter Image Details");

        ButtonType insertButtonType = new ButtonType("Insert", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(insertButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        
        TextField altText = new TextField(text);
        TextField source = new TextField();
        Button browse = new Button("Browse");
        
        browse.setOnAction(e -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Select Image");
            fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif", "*.svg")
            );
            File file = fileChooser.showOpenDialog(browse.getScene().getWindow());
            if (file != null) {
                source.setText(file.toURI().toString());
            }
        });

        grid.add(new Label("Alt Text:"), 0, 0);
        grid.add(altText, 1, 0);
        grid.add(new Label("Source:"), 0, 1);
        grid.add(source, 1, 1);
        grid.add(browse, 2, 1);

        dialog.getDialogPane().setContent(grid);

        // Request focus on the source field by default
        javafx.application.Platform.runLater(source::requestFocus);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == insertButtonType) {
                String alt = altText.getText() == null ? "" : altText.getText();
                String src = source.getText() == null ? "" : source.getText();
                return "![" + alt + "](" + src + ")";
            }
            return null;
        });

        Optional<String> result = dialog.showAndWait();
        return result.orElse(text);
    }
}
