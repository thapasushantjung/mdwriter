package com.mdwriter.app;

import com.vladsch.flexmark.ext.abbreviation.AbbreviationExtension;
import com.vladsch.flexmark.ext.tables.TablesExtension;
import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.util.ast.Node;
import com.vladsch.flexmark.util.data.MutableDataSet;
import java.util.Arrays;

/**
 * Simple markdown renderer for normal mode
 * Renders markdown to clean HTML without proposal-specific formatting
 */
public class NormalMarkdownRenderer {
    
    private final Parser parser;
    private final HtmlRenderer renderer;
    
    public NormalMarkdownRenderer() {
        MutableDataSet options = new MutableDataSet();
        options.set(Parser.EXTENSIONS, Arrays.asList(
            TablesExtension.create(),
            AbbreviationExtension.create()
        ));
        options.set(HtmlRenderer.SOFT_BREAK, "<br />\n");
        options.set(HtmlRenderer.GENERATE_HEADER_ID, true);
        
        this.parser = Parser.builder(options).build();
        this.renderer = HtmlRenderer.builder(options).build();
    }
    
    public String render(String markdownText) {
        Node document = parser.parse(markdownText);
        String htmlContent = renderer.render(document);
        
        String css = getCSS();
        return "<html><head>" + css + "</head><body>" + htmlContent + "</body></html>";
    }
    
    private String getCSS() {
        return """
            <style>
                body {
                    font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
                    line-height: 1.6;
                    padding: 20px;
                    max-width: 800px;
                    margin: 0 auto;
                    color: #333;
                }
                h1, h2, h3 { color: #2c3e50; }
                h1 { font-size: 2em; border-bottom: 2px solid #eee; padding-bottom: 0.3em; }
                h2 { font-size: 1.5em; border-bottom: 1px solid #eee; padding-bottom: 0.3em; }
                h3 { font-size: 1.2em; }
                code { 
                    background: #f4f4f4; 
                    padding: 2px 6px; 
                    border-radius: 3px;
                    font-family: 'Courier New', monospace;
                }
                pre { 
                    background: #f4f4f4; 
                    padding: 10px; 
                    overflow-x: auto;
                    border-radius: 4px;
                }
                pre code {
                    background: none;
                    padding: 0;
                }
                blockquote { 
                    border-left: 4px solid #ddd; 
                    margin: 0; 
                    padding-left: 16px; 
                    color: #666; 
                }
                table { 
                    border-collapse: collapse; 
                    width: 100%;
                    margin: 1em 0;
                }
                th, td { 
                    border: 1px solid #ddd; 
                    padding: 8px; 
                    text-align: left; 
                }
                th { 
                    background: #f4f4f4;
                    font-weight: bold;
                }
                img { 
                    max-width: 100%; 
                    height: auto;
                    display: block;
                    margin: 1em 0;
                }
                ul, ol {
                    padding-left: 2em;
                }
                li {
                    margin: 0.5em 0;
                }
            </style>
        """;
    }
}
