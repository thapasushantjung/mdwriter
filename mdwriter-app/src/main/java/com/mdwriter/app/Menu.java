package com.mdwriter.app;

import java.util.List;
import org.kordamp.ikonli.feather.Feather;
import org.kordamp.ikonli.javafx.FontIcon;
import com.mdwriter.api.ToolBarButton;
import com.mdwriter.app.plugins.ButtonPlugin;

import atlantafx.base.controls.ModalPane;
import atlantafx.base.theme.Styles;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.geometry.Side;
import javafx.scene.control.Button;
import javafx.scene.control.Separator;
import javafx.scene.control.TextArea;
import javafx.scene.control.ToolBar;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;

/**
 * Manages the Application Toolbar and Menu.
 * Handles actions like Undo, Redo, Theme Switching, PDF Export,
 * and opening the Sidebar.
 */
public class Menu {
  ToolBar toolbar = new ToolBar();

  public ModalPane modalPane = new ModalPane();

  public Menu(TextArea textarea, java.io.File rootDirectory) {

    modalPane.setId("modalPane");
    modalPane.displayProperty().addListener((obs, old, val) -> {
      if (!val) {
        modalPane.setAlignment(Pos.CENTER);
        modalPane.usePredefinedTransitionFactories(null);
      }
    });

    ButtonPlugin menu = new ButtonPlugin();

    var themeChanger = new Button(null, new FontIcon(Feather.SUN));
    themeChanger.getStyleClass().add(Styles.FLAT);
    var dialog = new ThemeSelector().themes();
    themeChanger.setOnAction((evt -> modalPane.show(dialog)));
    var left_dialog = new Sidebar(textarea, rootDirectory);

    var folder = new Button(null, new FontIcon(Feather.FOLDER));
    folder.getStyleClass().add(Styles.FLAT);
    folder.setOnAction((event -> {
      modalPane.setAlignment(Pos.TOP_LEFT);
      modalPane.usePredefinedTransitionFactories(Side.LEFT);
      modalPane.show(left_dialog);
    }));

    var undo = new Button(null, new FontIcon(Feather.ARROW_UP_LEFT));
    undo.setOnAction((event -> textarea.undo()));

    var redo = new Button(null, new FontIcon(Feather.ARROW_UP_RIGHT));
    redo.setOnAction((event -> textarea.redo()));

    this.toolbar.getItems().addAll(folder, new Separator(Orientation.VERTICAL), undo, redo);
    this.toolbar.setMaxWidth(Double.MAX_VALUE);

    List<ToolBarButton> buttons = menu.buttons;
    for (ToolBarButton button : buttons) {
      var iconButton = button.iconButton();
      this.toolbar.getItems().add(iconButton);
      iconButton.setOnMousePressed(event -> {
        String selectedText = textarea.getSelectedText();
        String changedText = button.changeText(selectedText);
        textarea.replaceSelection(changedText);
      });
    }
    Region spacer = new Region();
    HBox.setHgrow(spacer, Priority.ALWAYS);

    // Proposal Template Button
    // Creates a new proposal.md file and switches to proposal mode
    var proposalTemplate = new Button("Proposal Template", new FontIcon(Feather.FILE_TEXT));
    proposalTemplate.getStyleClass().addAll(Styles.FLAT, Styles.ACCENT);
    proposalTemplate.setOnAction((evt -> {
        // Enable proposal mode and load template
        if (textarea instanceof Editor) {
            Editor editor = (Editor) textarea;
            String templateContent = ProposalTemplate.getDefaultTemplate();
            
            // Create proposal file in root directory
            java.io.File proposalFile = new java.io.File(rootDirectory, "proposal.md");
            int counter = 1;
            // Find unique filename if proposal.md exists
            while (proposalFile.exists()) {
                proposalFile = new java.io.File(rootDirectory, "proposal-" + counter + ".md");
                counter++;
            }
            
            try {
                // Write template to file
                java.nio.file.Files.writeString(proposalFile.toPath(), templateContent);
                
                // Enable proposal mode and show template
                editor.enableProposalMode();
                
                // Refresh the sidebar to show the new file
                left_dialog.refresh();

            } catch (java.io.IOException e) {
                javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.ERROR);
                alert.setTitle("Error");
                alert.setHeaderText("Failed to create proposal file");
                alert.setContentText(e.getMessage());
                alert.showAndWait();
                
                // Still enable proposal mode even if file creation fails
                editor.enableProposalMode();
            }
        }
    }));

    // Export / Print Button - exports HTML and opens in browser for printing
    // This allows users to use the browser's native print-to-PDF functionality
    var printButton = new Button("Export / Print", new FontIcon(Feather.PRINTER));
    printButton.getStyleClass().addAll(Styles.FLAT, Styles.ACCENT);
    printButton.setOnAction((evt -> {
        if (textarea instanceof Editor) {
            javafx.scene.web.WebView webView = ((Editor) textarea).getWebView();
            if (webView == null) {
                javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.ERROR);
                alert.setTitle("Export Error");
                alert.setHeaderText("Cannot Export");
                alert.setContentText("WebView is not available. Please load some content first.");
                alert.showAndWait();
                return;
            }
            
            // Get HTML content from WebView
            String htmlContent = (String) webView.getEngine().executeScript("document.documentElement.outerHTML");
            
            // Show file save dialog
            javafx.stage.FileChooser fileChooser = new javafx.stage.FileChooser();
            fileChooser.setTitle("Export HTML for Printing");
            fileChooser.setInitialFileName("proposal.html");
            fileChooser.getExtensionFilters().add(
                new javafx.stage.FileChooser.ExtensionFilter("HTML Files", "*.html")
            );
            
            java.io.File file = fileChooser.showSaveDialog(webView.getScene().getWindow());
            if (file != null) {
                try {
                    java.nio.file.Files.writeString(file.toPath(), htmlContent);
                    
                    // Open the HTML file in default browser
                    try {
                        // Use xdg-open on Linux (more reliable than Desktop.browse)
                        String os = System.getProperty("os.name").toLowerCase();
                        if (os.contains("linux")) {
                            Runtime.getRuntime().exec(new String[]{"xdg-open", file.getAbsolutePath()});
                        } else if (java.awt.Desktop.isDesktopSupported() && java.awt.Desktop.getDesktop().isSupported(java.awt.Desktop.Action.BROWSE)) {
                            java.awt.Desktop.getDesktop().browse(file.toURI());
                        }
                    } catch (Exception ignored) {
                        // Browser opening is optional, don't fail if it doesn't work
                    }
                    
                    javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.INFORMATION);
                    alert.setTitle("Export Successful");
                    alert.setHeaderText("HTML Exported");
                    alert.setContentText("File saved and opened in browser.\nUse Ctrl+P in your browser to print or save as PDF.");
                    alert.showAndWait();
                } catch (java.io.IOException e) {
                    javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.ERROR);
                    alert.setTitle("Export Error");
                    alert.setHeaderText("Failed to Export");
                    alert.setContentText("Error: " + e.getMessage());
                    alert.showAndWait();
                }
            }
        } else {
            javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.WARNING);
            alert.setTitle("Export");
            alert.setHeaderText("Cannot Export");
            alert.setContentText("Editor is not available.");
            alert.showAndWait();
        }
    }));

    this.toolbar.getItems().addAll(spacer, proposalTemplate, printButton, themeChanger);


  }

}
