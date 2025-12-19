package com.mdwriter.app;

import javafx.scene.control.TextArea;
import javafx.scene.web.WebView;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.concurrent.Worker;

import java.io.File;

public class Editor extends TextArea {

    // Flag to determine rendering mode
    private boolean proposalMode = false;
    private WebView webview;
    private File rootDirectory;
    private final IntegerProperty wordCount = new SimpleIntegerProperty(0);

    public Editor(WebView webview, File rootDirectory) {
        this.webview = webview;
        this.rootDirectory = rootDirectory;
        // Start with empty editor - template is loaded via button click
        setText("");
        getStyleClass().add("editor");
        setWrapText(true);
        
        textProperty().addListener((obs, oldText, newText) -> {
            if (proposalMode) {
                renderProposalMode(newText);
            } else {
                renderNormalMode(newText);
            }
        });
        
        // Listen for WebView load completion to calculate word count
        webview.getEngine().getLoadWorker().stateProperty().addListener((obs, oldState, newState) -> {
            if (newState == Worker.State.SUCCEEDED) {
                calculateWordCount();
            }
        });
    }
    
    public IntegerProperty wordCountProperty() {
        return wordCount;
    }

    public int getWordCount() {
        return wordCount.get();
    }

    private void calculateWordCount() {
        try {
            // Execute JS to get innerText of body and count words
            Object result = webview.getEngine().executeScript(
                "document.body.innerText.trim().split(/\\s+/).filter(word => word.length > 0).length"
            );
            
            if (result instanceof Integer) {
                wordCount.set((Integer) result);
            } else if (result instanceof Number) {
                wordCount.set(((Number) result).intValue());
            } else {
                 wordCount.set(0); 
            }
        } catch (Exception e) {
            // Ignore errors during script execution (e.g. if page not fully loaded)
        }
    }
    
    /**
     * Enable proposal mode with template
     */
    public void enableProposalMode() {
        this.proposalMode = true;
        setText(ProposalTemplate.getDefaultTemplate());
    }
    
    /**
     * Enable proposal mode without setting template (for loading existing proposal files)
     */
    public void enableProposalModeOnly() {
        this.proposalMode = true;
    }
    
    /**
     * Disable proposal mode, return to normal markdown
     */
    public void disableProposalMode() {
        this.proposalMode = false;
        // Re-render current content in normal mode
        String currentText = getText();
        renderNormalMode(currentText);
    }
    
    /**
     * Set content and auto-detect mode based on content
     * If content has proposal YAML front matter, enable proposal mode
     * Otherwise, use normal mode
     */
    public void setContent(String content) {
        if (isProposalContent(content)) {
            this.proposalMode = true;
        } else {
            this.proposalMode = false;
        }
        setText(content);
    }
    
    /**
     * Check if content is a proposal file based on YAML front matter
     */
    public static boolean isProposalContent(String content) {
        if (content == null || content.isEmpty()) {
            return false;
        }
        // Check for YAML front matter with proposal-specific keys
        return content.startsWith("---") && 
               (content.contains("title:") && 
                content.contains("subject:") && 
                content.contains("students:"));
    }
    
    public WebView getWebView() {
        return webview;
    }

    private void renderNormalMode(String newText) {
        NormalMarkdownRenderer renderer = new NormalMarkdownRenderer();
        String html = renderer.render(newText);
        webview.getEngine().loadContent(html);
    }
    
    /**
     * Proposal mode rendering - full proposal formatting with pages, TOC, JS
     */
    /**
     * Proposal mode rendering - full proposal formatting with pages, TOC, JS
     */
    private void renderProposalMode(String newText) {
        String fullHtml = new ProposalRenderer().render(newText);
        webview.getEngine().loadContent(fullHtml);
    }


}
