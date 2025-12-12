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
import javafx.scene.control.TextArea;
import javafx.scene.web.WebView;

import java.util.Arrays;
import java.io.File;
import java.util.List;
import java.util.Map;

public class Editor extends TextArea {

    // Flag to determine rendering mode
    private boolean proposalMode = false;
    private WebView webview;
    private File rootDirectory;

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
    }
    
    /**
     * Enable proposal mode with template
     */
    public void enableProposalMode() {
        this.proposalMode = true;
        setText(getDefaultTemplate());
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
    private void renderProposalMode(String newText) {
        // ... (existing code for renderProposalMode start) ...
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
        String fullHtml = buildFullHtml(metadata, htmlContent);
        
        webview.getEngine().loadContent(fullHtml);
    }

    private String buildFullHtml(Map<String, List<String>> metadata, String content) {
        // ... (metadata getters) ...
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
        
        List<String> students = metadata.get("students");
        StringBuilder studentsHtml = new StringBuilder();
        if (students != null) {
            for (String student : students) {
                studentsHtml.append("<div>").append(student).append("</div>");
            }
        } else {
            studentsHtml.append("<div>Student Name (Roll No)</div>");
        }

        // CSS
        String css = """
            <style>
                /* Times New Roman - Regular */
                @font-face {
                    font-family: 'Times New Roman';
                    src: url('file:///home/hancy/mdwriter/mdwriter-app/src/main/resources/fonts/Times New Roman.ttf') format('truetype');
                    font-weight: normal;
                    font-style: normal;
                }
                
                /* Times New Roman - Bold */
                @font-face {
                    font-family: 'Times New Roman';
                    src: url('file:///home/hancy/mdwriter/mdwriter-app/src/main/resources/fonts/Times New Roman Bold.ttf') format('truetype');
                    font-weight: bold;
                    font-style: normal;
                }
                
                /* Times New Roman - Italic */
                @font-face {
                    font-family: 'Times New Roman';
                    src: url('file:///home/hancy/mdwriter/mdwriter-app/src/main/resources/fonts/Times New Roman Italic.ttf') format('truetype');
                    font-weight: normal;
                    font-style: italic;
                }
                
                /* Times New Roman - Bold Italic */
                @font-face {
                    font-family: 'Times New Roman';
                    src: url('file:///home/hancy/mdwriter/mdwriter-app/src/main/resources/fonts/Times New Roman Bold Italic.ttf') format('truetype');
                    font-weight: bold;
                    font-style: italic;
                }
                body {
                    font-family: 'Times New Roman', serif;
                    line-height: 1.5;
                    margin: 0;
                    padding: 20px;
                    background: #f0f0f0;
                }

                .page {
                    background: white;
                    width: 21cm;
                    min-height: 29.7cm;
                    padding: 1in 1.25in; 
                    margin: 20px auto;
                    box-shadow: 0 0 10px rgba(0,0,0,0.1);
                    box-sizing: border-box;
                    page-break-after: always;
                    position: relative;
                    word-wrap: break-word;
                    overflow-wrap: break-word;
                    display: flex;
                    flex-direction: column;
                }
                .main-content {
                    flex: 1 1 auto;
                }
                .center { text-align: center; }
                .bold { font-weight: bold; }
                .uppercase { text-transform: uppercase; }
                
                .logo {
                    width: 150px;
                    height: auto;
                    margin: 5px 0;
                }
                
                .title-section { margin-top: 10px; margin-bottom: 10px; }
                .submission-section { margin-bottom: 20px; }
                .submitted-by { margin-top: 20px; margin-bottom: 20px; }
                .college-section { margin-top: auto; text-align: center; margin-bottom: 10px; }
                
                .footer-number {
                    position: absolute;
                    bottom: 0.5in;
                    width: 100%;
                    text-align: center;
                    left: 0;
                }
                
                h1 { font-size: 16pt; margin: 10px 0; }
                h2 { font-size: 14pt; margin: 10px 0; }
                h3 { font-size: 13pt; margin: 5px 0; }
                p { font-size: 12pt; margin: 5px 0; }

                
                /* TOC Styles */
                .toc-entry { display: flex; align-items: flex-end; margin-bottom: 5px; }
                .toc-text { flex: 0 0 auto; }
                .toc-dots { flex: 1 1 auto; border-bottom: 1px dotted #000; margin: 0 5px; position: relative; top: -4px;}
                .toc-page { flex: 0 0 auto; }
                
                /* Helper for Flexmark generated TOC */
                .toc-list ul { list-style: none; padding: 0; margin: 0; }
                .toc-list li { margin-left: 0; margin-top: 8px; } /* L1 Spacing */
                .toc-list li ul { margin-left: 25px; margin-top: 5px; }
                .toc-list li ul li ul { margin-left: 25px; } /* L3 Indentation */
                .toc-list a, .toc-content a { 
                    text-decoration: none; 
                    color: #000; 
                    cursor: pointer; 
                }
                .toc-list a:hover, .toc-content a:hover { 
                    text-decoration: underline; 
                    color: #0066cc; 
                }
                /* Active link styling after JS processes */
                .toc-link { display: flex; width: 100%; align-items: flex-end; }
                .toc-link .toc-text { flex: 0 0 auto; }
                .toc-link .toc-dots { flex: 1 1 auto; border-bottom: 1px dotted #000; margin: 0 5px; position: relative; top: -4px; }
                .toc-link .toc-page { flex: 0 0 auto; }
                
                @media print {
                    * {
                        margin: 0 !important;
                        padding: 0 !important;
                    }
                    html, body {
                        background: none !important;
                        -webkit-print-color-adjust: exact;
                    }
                    .page {
                        padding: 1cm !important;
                        box-shadow: none !important;
                        border: none !important;
                        width: 100% !important;
                        min-height: auto !important;
                        page-break-after: always;
                        page-break-inside: avoid !important;
                        position: relative !important;
                    }
                    .page:last-child {
                        page-break-after: auto !important;
                    }
                    .footer-number {
                        position: absolute !important;
                        bottom: 0.5cm !important;
                        left: 0 !important;
                        width: 100% !important;
                        text-align: center !important;
                    }
                    @page {
                        margin: 0;
                        size: A4;
                    }
                }
            </style>
        """;

        // Page 1: Title Page with Logo
        String page1 = String.format("""
            <div class="page center" id="page-title">
                <div class="title-section">
                    <p>A Project Report on</p>
                    <h2 class="bold">%s</h2>
                </div>
                
                <div class="submission-section">
                    <p>Submitted in partial fulfillment of the requirement of</p>
                    <p>%s</p>
                    <p>%s</p>
                    <p>%s - %s</p>
                </div>
                
                <div class="submission-to">
                    <p class="bold">Submitted to</p>
                    <img src="file:///home/hancy/mdwriter/Purbanchal_University_Logo.png" class="logo" alt="University Logo"/>
                    <p>%s</p>
                    <p>Biratnagar, Nepal</p>
                </div>
                
                <div class="submitted-by">
                    <p class="bold">Submitted by</p>
                    %s
                </div>
                
                <div class="college-section">
                    <h2 class="bold uppercase">%s</h2>
                    <p>%s</p>
                    <br>
                    <p>%s</p>
                </div>
            </div>
            """, title, subject, code, program, semester, university, studentsHtml.toString(), college, address, year);

        // Page 2: Supervisor / Approval Page (Detailed)
        String page2 = String.format("""
            <div class="page center" id="page-supervisor">
                <div class="title-section">
                    <p>A Project Report on</p>
                    <h2 class="bold">%s</h2>
                </div>
                
                <div class="submission-section">
                    <p>Submitted in partial fulfillment of the requirement of</p>
                    <p>%s</p>
                    <p>%s</p>
                    <p>%s - %s Semester</p>
                </div>
                
                <div class="submission-to">
                    <p class="bold">Submitted to</p>
                    <p>%s</p>
                    <p>Biratnagar, Nepal</p>
                </div>
                
                <div class="submitted-by">
                    <p class="bold">Submitted by</p>
                    %s
                </div>
                
                <div class="supervisor-section">
                    <p class="bold">Project Supervisor</p>
                    <p>%s</p>
                </div>
                
                <div class="college-section">
                    <h2 class="bold uppercase">%s</h2>
                    <p>%s</p>
                    <br>
                    <p>%s</p>
                    <br>
                    <p style="font-size: 10pt;">2</p>
                </div>
            </div>
            """, title, subject, code, program, semester, university, studentsHtml.toString(), supervisor, college, address, year);


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

        String page3 = String.format("""
            <div class="page" id="page-approval">
                <div class="center">
                    <h2 class="bold">Approval Certificate</h2>
                </div>
                <br>
                <div style="text-align: justify; margin-bottom: 40px;">
                    <p>The undersigned certify that they have read and recommended to the Department of Information Technology for acceptance, a project report entitled <span class="bold">"%s"</span> submitted by <span class="bold">%s</span> in partial fulfillment for the Degree of %s.</p>
                </div>
                
                <div class="signatures" style="margin-top: 60px;">
                    <div class="signature-block" style="margin-bottom: 40px;">
                        <p>................................................</p>
                        <p class="bold">%s</p>
                        <p>Project Supervisor</p>
                        <p>%s</p>
                    </div>
                    
                    <div class="signature-block" style="margin-bottom: 40px;">
                        <p>................................................</p>
                        <p>(External Examiner Name)</p>
                        <p>External Examiner</p>
                        <br>
                        <p><span class="bold">Date:</span> .......................................</p>
                    </div>
                    
                    <div class="signature-block">
                        <p>................................................</p>
                        <p class="bold">%s</p>
                        <p>Head of Department</p>
                        <p>%s</p>
                    </div>
                </div>
                
                <div class="footer-number">
                    <p>I</p>
                </div>
            </div>
            """, title, studentsComma.toString(), program, supervisor, college, hodName, college);

        // Page 4: Acknowledgment
        String page4 = String.format("""
            <div class="page" id="page-acknowledgment">
                <div class="center">
                    <h2 class="bold">Acknowledgment</h2>
                </div>
                <br>
                <div style="text-align: justify;">
                    <p>We'd like to express our heartfelt gratitude to everyone who has encouraged us to work on this project. First and foremost, we'd like to express our gratitude to the entire team of %s for this chance, especially the professors of science and technology, who assisted us in furthering our knowledge in this field.</p>
                    <br>
                    <p>And we'd like to express our special gratitude to our supervisor, <span class="bold">Mr. %s</span>, who has consistently encouraged, inspired and provided us with a wealth of information that has been beneficial. His advice was helpful in completing this assignment. We could not have asked for a better supervisor, counselor, or mentor.</p>
                    <br>
                    <p>This project would not have been possible without the assistance of each member, as well as the entire class, who offered suggestions, shared their experiences, and provided advice throughout the project. We are grateful for this. Finally, we'd like to express our gratitude to our friends and colleagues who supported us directly and indirectly throughout this project.</p>
                </div>
                
                <div class="footer-number">
                    <p>II</p>
                </div>
            </div>
            """, college, supervisor);

        // Page 5: Abstract
        String abstractText = getMeta(metadata, "abstract", "The Blood Bank Management System is intended to meet the requirements of administrators and donors...");
        String page5 = String.format("""
             <div class="page" id="page-abstract">
                <div class="center">
                    <h2 class="bold">Abstract</h2>
                </div>
                <br>
                <div style="text-align: justify;">
                    <p>%s</p>
                </div>
                <div class="footer-number">
                    <p>III</p>
                </div>
             </div>
             """, abstractText);

        // Body Pages Logic
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

        // ... [List of Figures and Abbreviations logic remains similar but needs to be below or above?]
        // Actually, previous code had Page 7 and 8 logic. I need to keep that.
        // I will focus on replacing the "Append Main Body" section mainly, but I need to reconstruct the pages flow.

        // Page 7: List of Figures (Recalculating to ensure variables are available)
        StringBuilder figuresHtml = new StringBuilder();
        java.util.regex.Pattern imagePattern = java.util.regex.Pattern.compile("!\\[(.*?)\\]\\((.*?)\\)");
        java.util.regex.Matcher imageMatcher = imagePattern.matcher(content); // Use original content to find images
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

        // Page 8: Abbreviations (Same as before)
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
        // Logic: The first chunk before the first <h1> is likely "Introduction" if h1 is used, 
        // OR it might be empty if [TOC] was first.
        // We want every <h1> to start a NEW <div class="page">.
        // But H1 itself should be IN that page.
        // So we split by (?=<h1) lookahead.

        String[] chapters = cleanBody.split("(?=<h1)");
        StringBuilder bodyPagesBuilder = new StringBuilder();
        int bodyPageCount = 1; // Used for footer
        
        for (String chapterHtml : chapters) {
            if (chapterHtml.trim().isEmpty()) continue;
            
            // We need a unique ID for each page to help JS, though our JS currently estimates based on height.
            // Splitting by pages effectively means we "force" page breaks.
            // But we also need to allow content to overflow if a chapter is huge? 
            // For this version (Simplistic), we assume a Chapter fits in a Page OR effectively scrolls.
            // The constraint "Separate pages for each chapter" is physically separate .page divs.
            
            // IMPORTANT: If a chapter is longer than one page, it will just stretch the .page div.
            // In a real word processor we'd flow text. Here we rely on the user or Auto-page-break logic (complex).
            // For now, ONE chapter = ONE .page div (which might be tall).
            // Styling handles `min-height: 29.7cm`.
            
            bodyPagesBuilder.append(String.format("""
                <div class="page body-page" id="body-page-%d">
                    <div class="main-content">
                        %s
                    </div>
                    <div class="footer-number">
                        <p class="dynamic-page-number">%d</p> 
                    </div>
                </div>
            """, bodyPageCount, chapterHtml, bodyPageCount)); // Initial estimation, JS will correct if needed
            
            // Note: Since we are creating distinct DIVs, we can't easily auto-increment page number for *overflow*.
            // The JS logic "1 + floor(overflow)" relies on a continuous flow.
            // If we break into separate divs, checking "which page number" becomes "Index of this div + Sub-pages".
            // Implementation shortcut: For now, each Chapter starts purely on a new Page count? 
            // No, the user wants continuous numbering.
            // The footer number `%d` here is just a placeholder. 
            // The JS script handles the numbering update? 
            // My previous JS script looked for `.toc-page`. It didn't update the footers.
            // I should update the JS to also update these footer numbers if I want them dynamic?
            // Actually, for simplicity, let's just increment `bodyPageCount` here.
            
            bodyPageCount++; 
        }

        // Updated JS with pagination logic
        String script = """
            <script>
                document.addEventListener("DOMContentLoaded", function() {
                    // Delay to ensure DOM is fully rendered in WebView
                    setTimeout(function() {
                        // Constants for A4 page content area
                        const MAX_CONTENT_HEIGHT = 800; // ~800px content area
                        
                        console.log('=== Starting Pagination ===');
                        console.log('Max content height:', MAX_CONTENT_HEIGHT + 'px');
                        
                        // 1. PAGINATION: Split overflow content into new pages
                        function paginateContent() {
                            let totalNewPages = 0;
                            let iterations = 0;
                            const maxIterations = 50;
                            
                            while (iterations < maxIterations) {
                                iterations++;
                                let madeChanges = false;
                                
                                const bodyPages = document.querySelectorAll('.body-page');
                                console.log('Iteration ' + iterations + ': Found ' + bodyPages.length + ' body pages');
                                
                                for (let i = 0; i < bodyPages.length; i++) {
                                    const page = bodyPages[i];
                                    const mainContent = page.querySelector('.main-content');
                                    if (!mainContent) continue;
                                    
                                    // Use getBoundingClientRect for accurate measurement
                                    const rect = mainContent.getBoundingClientRect();
                                    const contentHeight = rect.height;
                                    
                                    console.log('Page ' + (i+1) + ' height: ' + contentHeight.toFixed(0) + 'px');
                                    
                                    if (contentHeight > MAX_CONTENT_HEIGHT) {
                                        console.log('  -> OVERFLOW detected!');
                                        
                                        const children = mainContent.children;
                                        if (children.length <= 1) {
                                            console.log('  -> Only 1 child, cannot split');
                                            continue;
                                        }
                                        
                                        // Find where to split
                                        let splitAt = -1;
                                        let runningHeight = 0;
                                        
                                        for (let j = 0; j < children.length; j++) {
                                            const childRect = children[j].getBoundingClientRect();
                                            runningHeight += childRect.height;
                                            
                                            if (runningHeight > MAX_CONTENT_HEIGHT && j > 0) {
                                                splitAt = j;
                                                console.log('  -> Split at child ' + j);
                                                break;
                                            }
                                        }
                                        
                                        if (splitAt > 0) {
                                            // Create new page
                                            const newPage = document.createElement('div');
                                            newPage.className = 'page body-page';
                                            newPage.innerHTML = '<div class=\"main-content\"></div><div class=\"footer-number\"><p class=\"dynamic-page-number\">?</p></div>';
                                            
                                            // Move elements to new page
                                            const newContent = newPage.querySelector('.main-content');
                                            while (mainContent.children.length > splitAt) {
                                                newContent.appendChild(mainContent.children[splitAt]);
                                            }
                                            
                                            // Insert after current page
                                            page.parentNode.insertBefore(newPage, page.nextSibling);
                                            totalNewPages++;
                                            madeChanges = true;
                                            console.log('  -> Created new page');
                                            break; // Restart iteration
                                        }
                                    }
                                }
                                
                                if (!madeChanges) {
                                    console.log('No changes in iteration ' + iterations + ', done!');
                                    break;
                                }
                            }
                            
                            console.log('=== Pagination Complete: Created ' + totalNewPages + ' new pages ===');
                        }
                        
                        // Run pagination
                        paginateContent();
                        
                        // 2. Update page numbers after pagination
                    function updatePageNumbers() {
                        const allBodyPages = document.querySelectorAll('.body-page');
                        allBodyPages.forEach((page, index) => {
                            const pageNum = page.querySelector('.dynamic-page-number');
                            if (pageNum) {
                                pageNum.textContent = (index + 1);
                            }
                            page.id = 'body-page-' + (index + 1);
                        });
                        console.log('Total body pages after pagination:', allBodyPages.length);
                        return allBodyPages;
                    }
                    
                    const pages = updatePageNumbers();
                    
                    // 3. Update TOC Page Numbers for chapter links
                    const tocLinks = document.querySelectorAll('.toc-content a');
                    console.log('Found ' + tocLinks.length + ' TOC content links');
                    
                    tocLinks.forEach(link => {
                        const href = link.getAttribute('href');
                        if (href && href.startsWith('#')) {
                            const targetId = href.substring(1);
                            const target = document.getElementById(targetId);
                            if (target) {
                                let pageNum = 0;
                                pages.forEach((page, index) => {
                                    if (page.contains(target)) {
                                        pageNum = index + 1;
                                    }
                                });
                                
                                if (pageNum > 0) {
                                    const text = link.innerText;
                                    link.innerHTML = '<span class="toc-text">' + text + '</span><span class="toc-dots"></span><span class="toc-page">' + pageNum + '</span>';
                                    link.classList.add('toc-link');
                                }
                            }
                        }
                    });
                    
                    // 4. Figures Page Numbering
                    document.querySelectorAll('.toc-count-page').forEach(span => {
                        const url = span.getAttribute('data-target-image');
                        const img = document.querySelector('img[src="' + url + '"]');
                        if (img) {
                            pages.forEach((page, index) => {
                                if (page.contains(img)) {
                                     span.innerText = (index + 1);
                                }
                            });
                        }
                    });
                    
                    // 5. Smooth scroll using event delegation
                    document.body.addEventListener('click', function(e) {
                        const anchor = e.target.closest('a[href^="#"]');
                        if (anchor) {
                            e.preventDefault();
                            const targetId = anchor.getAttribute('href');
                            const targetElement = document.querySelector(targetId);
                            if (targetElement) {
                                targetElement.scrollIntoView({ behavior: 'smooth' });
                            } else {
                                console.warn('Target not found for link:', targetId);
                            }
                        }
                    });
                    
                    console.log('Page setup complete');
                    }, 100); // End setTimeout with 100ms delay
                });
            </script>
        """;

        return "<html><head>" + css + "</head><body>" + page1 + page2 + page3 + page4 + page5 + page6 + page7 + page8 + bodyPagesBuilder.toString() + script + "</body></html>";
    }

    // Helper to extract TOC if [TOC] is present or just return empty
    private String extractTocFromContent(String htmlContent) {
       // This is a naive heuristic since we don't have easy AST here without re-parsing.
       // However, if we enabled TOC extension, and user put [TOC], it would be in htmlContent.
       // But we want it on Page 6.
       // If the user didn't put [TOC], we might miss it.
       // For this iteration, let's rely on the user putting [TOC] at the start of their markdown, 
       // AND we need to hide it from the body pages?
       // Actually, let's just create a simple Javascript-based TOC or css-styled TOC if present.
       // For now, I'll return an empty string or simple instructions if not found.
       // Better: The user asked for "Proposal Document", likely they want it auto-generated.
       // But without AST access in this method (it was parsed in constructor), I can't easily walk it.
       // I'll stick to formatting what flexmark gives.
       // If htmlContent contains <div class="toc">, we extract it.
       if (htmlContent.contains("class=\"toc\"")) {
           int start = htmlContent.indexOf("<div class=\"toc\">");
           int end = htmlContent.indexOf("</div>", start) + 6;
           return htmlContent.substring(start, end);
       }
       return "";
    }
    
    private String cleanContent(String htmlContent) {
        // Remove TOC if we extracted it
       if (htmlContent.contains("class=\"toc\"")) {
           int start = htmlContent.indexOf("<div class=\"toc\">");
           int end = htmlContent.indexOf("</div>", start) + 6;
           return htmlContent.replace(htmlContent.substring(start, end), "");
       }
       return htmlContent;
    }

    private String getMeta(Map<String, List<String>> metadata, String key, String defaultValue) {
        if (metadata.containsKey(key) && !metadata.get(key).isEmpty()) {
            return metadata.get(key).get(0);
        }
        return defaultValue;
    }

    public static String getDefaultTemplate() {
        return """
---
title: Blood Bank Management System
subject: Project - IV
code: BIT256CO
program: Bachelor of Information Technology
semester: IV
year: 2024
supervisor: Rubim Shrestha
hod: Saroj Pandey
students:
  - Anish Kumar Goit (320886)
  - Kusal Rimal (320900)
  - Dinesh Rimal (320894)
abstract: The Blood Bank Management System is intended to meet the requirements of administrators and donors by offering a stable platform for maintaining blood donation records. This Java programming project is aimed for intermediate Java developers who want to improve their coding abilities while supporting a vital component of the healthcare system.
abbreviations:
  - "RDBMS: Relational Database Management System"
  - "IDE: Integrated Development Environment"
  - "DFD: Data Flow Diagram"
  - "SDLC: Software Development Life Cycle"
---

[TOC]

# Chapter 1: Introduction
## 1.1 Background
Enter background here...

## 1.2 Problem Statement
Describe the problem...

## 1.3 Objectives
### 1.3.1 General Objectives
*   To develop...

### 1.3.2 Specific Objectives
*   To implement...

## 1.4 Scope and Limitation
...

# Chapter 2: Literature Review
## 2.1 Existing System Analysis
...

## 2.2 Proposed System
...

# Chapter 3: Methodology
## 3.1 Software Development Life Cycle
...

## 3.2 System Design
### 3.2.1 Use Case Diagram
![Use Case](https://via.placeholder.com/600x400 "Use Case Diagram")

### 3.2.2 Data Flow Diagram (DFD)
![DFD](https://via.placeholder.com/600x400 "Data Flow Diagram")

# Chapter 4: Expected Output
...

# References
*   Reference 1
*   Reference 2
""";
    }
}
