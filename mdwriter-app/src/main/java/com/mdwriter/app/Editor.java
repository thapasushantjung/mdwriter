package com.mdwriter.app;

import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.util.ast.Node;
import com.vladsch.flexmark.util.data.MutableDataSet;
import javafx.scene.control.TextArea;
import javafx.scene.web.WebView;

public class Editor extends TextArea {

  public Editor(WebView webview) {

    textProperty().addListener((obs, oldText, newText) -> {
      MutableDataSet options = new MutableDataSet();
      options.set(HtmlRenderer.SOFT_BREAK, "<br />\n");
      Parser parser = Parser.builder().build();
      HtmlRenderer renderer = HtmlRenderer.builder().build();
      String processedText = newText.replace("\n", "  \n");
      Node document = parser.parse(processedText);
      String html = renderer.render(document);
      webview.getEngine().loadContent(html);
    });
  }

}
