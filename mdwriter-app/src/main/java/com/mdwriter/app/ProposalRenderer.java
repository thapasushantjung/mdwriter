package com.mdwriter.app;

import com.vladsch.flexmark.ext.abbreviation.AbbreviationExtension;
import com.vladsch.flexmark.ext.tables.TablesExtension;
import com.vladsch.flexmark.ext.toc.TocExtension;
import com.vladsch.flexmark.ext.yaml.front.matter.AbstractYamlFrontMatterVisitor;
import com.vladsch.flexmark.ext.yaml.front.matter.YamlFrontMatterExtension;
import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.util.ast.Node;
import com.vladsch.flexmark.util.data.MutableDataSet;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Handles the rendering of Markdown into a formal project proposal structure.
 * 
 * Features:
 * - HTML Generation with academic styling (Times New Roman, margins).
 * - Table of Contents (TOC) with page numbers.
 * - Dynamic pagination logic via JavaScript.
 * - Front Matter parsing for title page generation.
 */
public class ProposalRenderer {

    /**
     * Renders the raw markdown content into a full HTML document.
     * 
     * @param newText The raw markdown content.
     * @return The complete HTML string ready for the WebView.
     */
    public String render(String newText) {
        MutableDataSet options = new MutableDataSet();
        options.set(Parser.EXTENSIONS, Arrays.asList(
            YamlFrontMatterExtension.create(),
            TocExtension.create(),
            TablesExtension.create(),
            AbbreviationExtension.create()
        ));
        options.set(HtmlRenderer.SOFT_BREAK, "<br />\n");
        options.set(HtmlRenderer.GENERATE_HEADER_ID, true);
        // Set custom TOC class to ensure robust regex matching
        options.set(TocExtension.DIV_CLASS, "pro-toc");
        // Include H1-H3 levels in TOC
        options.set(TocExtension.LEVELS, 7); // Binary: 0b0000111 = 7 means levels 1, 2, 3
        
        Parser parser = Parser.builder(options).build();
        HtmlRenderer renderer = HtmlRenderer.builder(options).build();
        
        String processedText = newText.replace("\n", "  \n");
        
        // Explicitly wrap [TOC] to ensure we can extract it reliably
        // We use comments to avoid nesting issues with divs
        if (processedText.contains("[TOC]")) {
            processedText = processedText.replace("[TOC]", "\n<!-- TOC_START -->\n[TOC]\n<!-- TOC_END -->\n");
        }
        
        Node document = parser.parse(processedText);
        
        AbstractYamlFrontMatterVisitor visitor = new AbstractYamlFrontMatterVisitor();
        visitor.visit(document);
        Map<String, List<String>> metadata = visitor.getData();
        
        String htmlContent = renderer.render(document);
        return buildFullHtml(metadata, htmlContent);
    }

    private String buildFullHtml(Map<String, List<String>> metadata, String content) {
        // Extract metadata for Title Page construction
        String title = getMeta(metadata, "title", "Project Title");
        String subject = getMeta(metadata, "subject", "Project Subject");
        String code = getMeta(metadata, "code", "Subject Code");
        String program = getMeta(metadata, "program", "Bachelor of Information Technology");
        String semester = getMeta(metadata, "semester", "IV");
        String university = "Purbanchal University";
        String college = "KANTIPUR CITY COLLEGE";
        String address = "Putalisadak, Kathmandu";
        String year = getMeta(metadata, "year", "2024");
        String supervisor = getMeta(metadata, "supervisor", "Rubim Shrestha");

        // Load external resources (Images, Fonts, Scripts)
        String fontRegular = ResourceLoader.getFontUrl("Times New Roman.ttf");
        String fontBold = ResourceLoader.getFontUrl("Times New Roman Bold.ttf");
        String fontItalic = ResourceLoader.getFontUrl("Times New Roman Italic.ttf");
        String fontBoldItalic = ResourceLoader.getFontUrl("Times New Roman Bold Italic.ttf");
        String logoUrl = ResourceLoader.getImageUrl("Purbanchal_University_Logo.png");
        String highlightJs = ResourceLoader.load("/highlight.js");
        String highlightCss = ResourceLoader.load("/highlight.css");
        
        List<String> students = metadata.get("students");
        StringBuilder studentsHtml = new StringBuilder();
        if (students != null) {
            for (String student : students) {
                studentsHtml.append("<div>").append(student).append("</div>");
            }
        } else {
            studentsHtml.append("<div>Student Name (Roll No)</div>");
        }

        // CSS Styles
        // Load from external file
        String css = "<style>" + ResourceLoader.loadContent("/proposal.css") + "</style>"
        .replace("{{FONT_REGULAR}}", fontRegular != null ? fontRegular : "")
        .replace("{{FONT_BOLD}}", fontBold != null ? fontBold : "")
        .replace("{{FONT_ITALIC}}", fontItalic != null ? fontItalic : "")
        .replace("{{FONT_BOLD_ITALIC}}", fontBoldItalic != null ? fontBoldItalic : "");

        // --- SECTION: PRELIMINARY PAGES ---
        
        // Page 1: Title Page with Logo
        String titlePageTemplate = ResourceLoader.loadContent("/title_page.html");
        String page1 = String.format(titlePageTemplate, title, subject, code, program, semester, logoUrl, university, studentsHtml.toString(), college, address, year);

        // Page 2: Supervisor / Approval Page (Detailed)
        String recommendationTemplate = ResourceLoader.loadContent("/recommendation.html");
        String page2 = String.format(recommendationTemplate, title, subject, code, program, semester, university, studentsHtml.toString(), supervisor, college, address, year);


        // Page 3: Approval Certificate
        String hodName = getMeta(metadata, "hod", "Saroj Pandey");
        String externalName = "_____________________"; 

        StringBuilder studentsComma = new StringBuilder();
        if (students != null && !students.isEmpty()) {
            for (int i = 0; i < students.size(); i++) {
                // Extract name only, remove (Roll No)
                String s = students.get(i);
                if (s.contains("(")) {
                    s = s.substring(0, s.indexOf("(")).trim();
                }
                studentsComma.append(s);
                if (i < students.size() - 2) studentsComma.append(", ");
                else if (i == students.size() - 2) studentsComma.append(" and ");
            }
        } else {
            studentsComma.append("Student Name");
        }

        String certificationTemplate = ResourceLoader.loadContent("/certification.html");
        String page3 = String.format(certificationTemplate, title, studentsComma.toString(), program, supervisor, college, hodName, college);

        // Page 4: Acknowledgment
        String acknowledgmentTemplate = ResourceLoader.loadContent("/acknowledgement.html");
        String page4 = String.format(acknowledgmentTemplate, college, supervisor);

        // Page 5: Abstract
        String abstractText = getMeta(metadata, "abstract", "The Blood Bank Management System is intended to meet the requirements of administrators and donors...");
        String abstractTemplate = ResourceLoader.loadContent("/abstract.html");
        String page5 = String.format(abstractTemplate, abstractText);

        // --- SECTION: BODY PROCESSING ---
        // Logic to extracting TOC and re-injecting it on the correct page.
        // Also splits the content into per-chapter pages.

        // 1. Clean Content: Remove the inline TOC if it exists
        String cleanBody = content;
        String extractedToc = "";
        
        // Robust TOC extraction using comment markers
        // Matches <!-- TOC_START --> ... <!-- TOC_END -->
        java.util.regex.Pattern tocPattern = java.util.regex.Pattern.compile("<!-- TOC_START -->(.*?)<!-- TOC_END -->", java.util.regex.Pattern.DOTALL);
        java.util.regex.Matcher tocMatcher = tocPattern.matcher(content);
        if (tocMatcher.find()) {
            extractedToc = tocMatcher.group(1); // Extract inner content
            cleanBody = tocMatcher.replaceAll(""); // Remove block from body
        }

        // Re-inject the extracted TOC into Page 6
        String tocPrelims = """
            <div class="toc-entry"><a href="#page-approval" class="toc-link"><span class="toc-text">Approval Certificate</span><span class="toc-dots"></span><span class="toc-page">I</span></a></div>
            <div class="toc-entry"><a href="#page-acknowledgment" class="toc-link"><span class="toc-text">Acknowledgment</span><span class="toc-dots"></span><span class="toc-page">II</span></a></div>
            <div class="toc-entry"><a href="#page-abstract" class="toc-link"><span class="toc-text">Abstract</span><span class="toc-dots"></span><span class="toc-page">III</span></a></div>
            <div class="toc-entry"><a href="#page-figures" class="toc-link"><span class="toc-text">List of Figures</span><span class="toc-dots"></span><span class="toc-page">V</span></a></div>
            <div class="toc-entry"><a href="#page-abbreviations" class="toc-link"><span class="toc-text">List of Abbreviations</span><span class="toc-dots"></span><span class="toc-page">VI</span></a></div>
        """;
        
        String page6 = String.format("""
             <div class="page" id="page-toc">
                <div class="center">
                    <h2 class="bold">Table of Contents</h2>
                </div>
                <br>
                <div class="toc-list" id="dynamic-toc">
                    %s
                    <div class="toc-content">
                        %s
                    </div>
                </div>
                <div class="footer-number">
                    <p>IV</p>
                </div>
             </div>
             """, tocPrelims, extractedToc);

        // Page 7: List of Figures
        StringBuilder figuresHtml = new StringBuilder();
        java.util.regex.Pattern imagePattern = java.util.regex.Pattern.compile("!\\[(.*?)\\]\\((.*?)\\)");
        java.util.regex.Matcher imageMatcher = imagePattern.matcher(content);
        int figCount = 1;
        while (imageMatcher.find()) {
            String caption = imageMatcher.group(1);
            String imageUrl = imageMatcher.group(2);
            if (caption.isEmpty()) caption = "Figure " + figCount;
            figuresHtml.append(String.format("""
                <div class="toc-entry">
                    <span class="toc-text">Figure %d: %s</span>
                    <span class="toc-dots"></span>
                    <span class="toc-count-page" data-target-image="%s"></span>
                </div>
            """, figCount, caption, imageUrl));
            figCount++;
        }
        
        String page7 = String.format("""
            <div class="page" id="page-figures">
                <div class="center">
                    <h2 class="bold">List of Figures</h2>
                </div>
                <br>
                <div class="toc-list">
                    %s
                </div>
                <div class="footer-number">
                    <p>V</p>
                </div>
            </div>
            """, figuresHtml.toString());

        // Page 8: Abbreviations
        List<String> abbreviations = metadata.get("abbreviations");
        StringBuilder abbrHtml = new StringBuilder();
        if (abbreviations != null) {
            abbrHtml.append("<table style='width: 100%; border-collapse: collapse;'>");
            for (String abbr : abbreviations) {
                String[] parts = abbr.split(":", 2);
                if (parts.length == 2) {
                    abbrHtml.append(String.format("<tr><td style='width: 150px; padding: 5px; vertical-align: top;'>%s</td><td style='padding: 5px; vertical-align: top;'>%s</td></tr>", parts[0].trim(), parts[1].trim()));
                } else {
                    abbrHtml.append(String.format("<tr><td colspan='2'>%s</td></tr>", abbr));
                }
            }
            abbrHtml.append("</table>");
        }
        String page8 = String.format("""
            <div class="page" id="page-abbreviations">
                <div class="center">
                    <h2 class="bold">List of Abbreviations</h2>
                </div>
                <br>
                <div class="abbr-list">
                    %s
                </div>
                <div class="footer-number">
                    <p>VI</p>
                </div>
            </div>
            """, abbrHtml.toString());

        // SPLIT BODY CONTENT BY <h1>
        String[] chapters = cleanBody.split("(?=<h1)");
        StringBuilder bodyPagesBuilder = new StringBuilder();
        int bodyPageCount = 1;
        
        for (String chapterHtml : chapters) {
            if (chapterHtml.trim().isEmpty()) continue;
            
            bodyPagesBuilder.append(String.format("""
                <div class="page body-page" id="body-page-%d">
                    <div class="main-content">
                        %s
                    </div>
                    <div class="footer-number">
                        <p class="dynamic-page-number">%d</p> 
                    </div>
                </div>
            """, bodyPageCount, chapterHtml, bodyPageCount));
            
            bodyPageCount++; 
        }

        // JS logic
        String proposalJs = ResourceLoader.loadContent("/proposal.js");
        // We prepend the <script> tag opening and append closing tag as the resource file is pure JS
        String script = "<script>" + proposalJs + "</script>";

        return "<html><head>" + css + "<link rel=\"stylesheet\" href=\"" + highlightCss + "\"></head><body>" + page1 + page2 + page3 + page4 + page5 + page6 + page7 + page8 + bodyPagesBuilder.toString() + "<script src=\"" + highlightJs + "\"></script>" + script + "</body></html>";
    }

    private String getMeta(Map<String, List<String>> metadata, String key, String defaultValue) {
        if (metadata.containsKey(key) && !metadata.get(key).isEmpty()) {
            return metadata.get(key).get(0);
        }
        return defaultValue;
    }
}
