package com.abundance.naivety.epub

import com.abundance.naivety.ui.components.epub.modals.EpubDisplaySettings
import com.abundance.naivety.ui.components.epub.modals.EpubReadingMode
import com.abundance.naivety.ui.components.epub.modals.EpubTypographySettings

/**
 * Helper class that generates HTML/CSS/JS wrapper for EPUB content
 */
object EpubHtmlWrapper {

    /**
     * Wraps EPUB content with styling and pagination JavaScript
     */
    fun wrapHtmlContent(
        content: String,
        displaySettings: EpubDisplaySettings,
        typographySettings: EpubTypographySettings,
        readingMode: EpubReadingMode,
        currentPage: Int
    ): String {
        val bgColor = String.format("#%06X", 0xFFFFFF and displaySettings.pageTheme.backgroundColor.toInt())
        // Use custom font color if set, otherwise use theme text color
        val textColor = if (typographySettings.fontColor != null) {
            String.format("#%06X", 0xFFFFFF and typographySettings.fontColor.colorValue.toInt())
        } else {
            String.format("#%06X", 0xFFFFFF and displaySettings.pageTheme.textColor.toInt())
        }
        val margin = displaySettings.pageMargin.toInt()
        val fontSize = typographySettings.fontSize.toInt()
        val fontFamily = typographySettings.fontFamily.fontFamily
        val lineHeight = typographySettings.lineSpacing
        val fontWeight = typographySettings.fontWeight.toInt()
        val textAlign = typographySettings.textAlignment.cssValue
        val topPadding = margin + 48
        val bottomPadding = margin + 64
        val sidePadding = margin + 16

        val googleFontImport = if (typographySettings.fontFamily.googleFontName.isNotEmpty()) {
            "@import url('https://fonts.googleapis.com/css2?family=${typographySettings.fontFamily.googleFontName}&display=swap');"
        } else ""

        // Determine page mode for column-based pagination
        val isPageByPageVertical = readingMode == EpubReadingMode.PAGE_HORIZONTAL
        val isPageByPageHorizontal = readingMode == EpubReadingMode.CONTINUOUS_HORIZONTAL
        val isContinuousVertical = readingMode == EpubReadingMode.CONTINUOUS_VERTICAL
        val isChapterScroll = readingMode == EpubReadingMode.CHAPTER_SCROLL

        val styleTag = buildStyleTag(
            googleFontImport, bgColor, textColor, topPadding, bottomPadding, sidePadding,
            fontFamily, fontSize, fontWeight, lineHeight, textAlign,
            isPageByPageVertical, isPageByPageHorizontal, isContinuousVertical, isChapterScroll
        )

        val scriptTag = buildScriptTag(
            isPageByPageVertical, isPageByPageHorizontal, isContinuousVertical, isChapterScroll,
            readingMode.name, topPadding, bottomPadding, sidePadding, currentPage
        )

        val fullStyleTag = "$styleTag\n$scriptTag"

        val bodyClass = when (readingMode) {
            EpubReadingMode.PAGE_HORIZONTAL -> "page-vertical"
            EpubReadingMode.CONTINUOUS_HORIZONTAL -> "page-horizontal"
            EpubReadingMode.CONTINUOUS_VERTICAL -> "continuous-vertical"
            EpubReadingMode.CHAPTER_SCROLL -> "chapter-scroll"
        }

        return wrapContent(content, fullStyleTag, bodyClass)
    }

    /**
     * Wraps multiple chapters for continuous vertical reading
     */
    fun wrapContinuousContent(
        chapters: List<Pair<String, String>>, // List of (chapterTitle, content)
        displaySettings: EpubDisplaySettings,
        typographySettings: EpubTypographySettings
    ): String {
        val bgColor = String.format("#%06X", 0xFFFFFF and displaySettings.pageTheme.backgroundColor.toInt())
        val textColor = if (typographySettings.fontColor != null) {
            String.format("#%06X", 0xFFFFFF and typographySettings.fontColor.colorValue.toInt())
        } else {
            String.format("#%06X", 0xFFFFFF and displaySettings.pageTheme.textColor.toInt())
        }
        val margin = displaySettings.pageMargin.toInt()
        val fontSize = typographySettings.fontSize.toInt()
        val fontFamily = typographySettings.fontFamily.fontFamily
        val lineHeight = typographySettings.lineSpacing
        val fontWeight = typographySettings.fontWeight.toInt()
        val textAlign = typographySettings.textAlignment.cssValue
        val topPadding = margin + 48
        val bottomPadding = margin + 64
        val sidePadding = margin + 16

        val googleFontImport = if (typographySettings.fontFamily.googleFontName.isNotEmpty()) {
            "@import url('https://fonts.googleapis.com/css2?family=${typographySettings.fontFamily.googleFontName}&display=swap');"
        } else ""

        // Combine all chapter content
        val combinedContent = StringBuilder()
        chapters.forEachIndexed { index, (_, content) ->
            // Extract body content from HTML
            val bodyContent = extractBodyContent(content)
            combinedContent.append("""<div class="chapter-content" data-chapter="$index">$bodyContent</div>""")
        }

        val style = buildContinuousVerticalStyle(
            googleFontImport, bgColor, textColor, topPadding, bottomPadding, sidePadding,
            fontFamily, fontSize, fontWeight, lineHeight, textAlign
        )

        val script = buildContinuousVerticalScript()

        return """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=3.0, user-scalable=yes">
                $style
                $script
            </head>
            <body class="continuous-vertical">
                $combinedContent
            </body>
            </html>
        """.trimIndent()
    }

    private fun extractBodyContent(html: String): String {
        val bodyStart = html.indexOf("<body", ignoreCase = true)
        val bodyEnd = html.indexOf("</body>", ignoreCase = true)

        var content = if (bodyStart >= 0 && bodyEnd > bodyStart) {
            val contentStart = html.indexOf(">", bodyStart) + 1
            html.substring(contentStart, bodyEnd)
        } else {
            html
        }

        // Remove inline width/height styles that might cause layout issues
        content = content.replace(Regex("""style\s*=\s*["'][^"']*width\s*:\s*[^;"']+[;"']""", RegexOption.IGNORE_CASE), "")
        content = content.replace(Regex("""style\s*=\s*["'][^"']*column[^;"']+[;"']""", RegexOption.IGNORE_CASE), "")

        return content
    }

    private fun buildContinuousVerticalStyle(
        googleFontImport: String,
        bgColor: String,
        textColor: String,
        topPadding: Int,
        bottomPadding: Int,
        sidePadding: Int,
        fontFamily: String,
        fontSize: Int,
        fontWeight: Int,
        lineHeight: Float,
        textAlign: String
    ): String {
        return """
            <style>
                $googleFontImport
                
                * { 
                    box-sizing: border-box !important; 
                    -webkit-text-size-adjust: 100% !important;
                    max-width: 100% !important;
                }
                
                html {
                    margin: 0 !important;
                    padding: 0 !important;
                    width: 100% !important;
                    height: auto !important;
                    background-color: $bgColor !important;
                    overflow-x: hidden !important;
                    overflow-y: auto !important;
                }
                
                body, body.continuous-vertical {
                    margin: 0 !important;
                    padding: ${topPadding}px ${sidePadding}px ${bottomPadding}px ${sidePadding}px !important;
                    width: 100% !important;
                    max-width: 100% !important;
                    min-width: 0 !important;
                    background-color: $bgColor !important;
                    color: $textColor !important;
                    font-family: $fontFamily !important;
                    font-size: ${fontSize}px !important;
                    font-weight: $fontWeight !important;
                    line-height: $lineHeight !important;
                    text-align: $textAlign !important;
                    word-wrap: break-word !important;
                    overflow-wrap: break-word !important;
                    -webkit-overflow-scrolling: touch;
                    overflow-x: hidden !important;
                    overflow-y: auto !important;
                    height: auto !important;
                    min-height: 100vh;
                }
                
                .chapter-content {
                    width: 100% !important;
                    max-width: 100% !important;
                    margin: 0 !important;
                    margin-bottom: 2em !important;
                    padding: 0 !important;
                    padding-bottom: 1em !important;
                    border-bottom: 1px solid rgba(128,128,128,0.2);
                }
                
                .chapter-content:last-child {
                    border-bottom: none;
                }
                
                /* Override ALL element widths that might be set by EPUB */
                .chapter-content * {
                    max-width: 100% !important;
                    width: auto !important;
                }
                
                .chapter-content p,
                .chapter-content div,
                .chapter-content span,
                .chapter-content section,
                .chapter-content article {
                    width: auto !important;
                    max-width: 100% !important;
                    margin-left: 0 !important;
                    margin-right: 0 !important;
                    padding-left: 0 !important;
                    padding-right: 0 !important;
                    column-count: unset !important;
                    column-width: unset !important;
                    columns: unset !important;
                }
                
                p { 
                    margin: 0 0 1em 0 !important; 
                    color: $textColor !important;
                    text-align: $textAlign !important;
                    font-size: ${fontSize}px !important;
                    line-height: $lineHeight !important;
                }
                
                h1, h2, h3, h4, h5, h6 { 
                    color: $textColor !important; 
                    margin: 1.5em 0 0.5em 0 !important; 
                    text-align: center !important;
                    width: 100% !important;
                }
                
                h1 { font-size: 1.5em !important; }
                h2 { font-size: 1.3em !important; }
                h3 { font-size: 1.2em !important; }
                
                a { color: #8B5CF6 !important; text-decoration: none; }
                
                img { 
                    max-width: 100% !important; 
                    height: auto !important; 
                    display: block;
                    margin: 1em auto;
                }
                
                /* Ensure tables and other elements don't overflow */
                table, pre, code {
                    max-width: 100% !important;
                    overflow-x: auto;
                }
                
                /* Reset any column styles from epub */
                div, section, article {
                    column-count: unset !important;
                    column-width: unset !important;
                    columns: unset !important;
                }
                
                /* Hide any embedded stylesheets that might interfere */
                .chapter-content style {
                    display: none !important;
                }
            </style>
        """.trimIndent()
    }

    private fun buildContinuousVerticalScript(): String {
        return """
            <script>
                var currentChapter = 0;
                var totalChapters = 0;
                
                function log(msg) {
                    if (typeof AndroidBridge !== 'undefined') AndroidBridge.log(msg);
                    console.log(msg);
                }
                
                function initContinuousScroll() {
                    var chapters = document.querySelectorAll('.chapter-content');
                    totalChapters = chapters.length;
                    log('Initialized continuous scroll with ' + totalChapters + ' chapters');
                    
                    // Report initial state
                    if (typeof AndroidBridge !== 'undefined') {
                        AndroidBridge.onContinuousScrollInit(totalChapters);
                    }
                    
                    // Track scroll position to update current chapter
                    window.addEventListener('scroll', function() {
                        var scrollTop = window.scrollY || document.documentElement.scrollTop;
                        var newChapter = 0;
                        
                        chapters.forEach(function(chapter, index) {
                            if (chapter.offsetTop <= scrollTop + 100) {
                                newChapter = index;
                            }
                        });
                        
                        if (newChapter !== currentChapter) {
                            currentChapter = newChapter;
                            if (typeof AndroidBridge !== 'undefined') {
                                AndroidBridge.onChapterChanged(currentChapter);
                            }
                        }
                    }, { passive: true });
                }
                
                function scrollToChapter(chapterIndex) {
                    var chapters = document.querySelectorAll('.chapter-content');
                    if (chapterIndex >= 0 && chapterIndex < chapters.length) {
                        chapters[chapterIndex].scrollIntoView({ behavior: 'smooth' });
                        currentChapter = chapterIndex;
                    }
                }
                
                function getScrollProgress() {
                    var scrollTop = window.scrollY || document.documentElement.scrollTop;
                    var scrollHeight = document.body.scrollHeight - window.innerHeight;
                    return scrollHeight > 0 ? scrollTop / scrollHeight : 0;
                }
                
                if (document.readyState === 'complete') {
                    initContinuousScroll();
                } else {
                    window.addEventListener('load', initContinuousScroll);
                }
            </script>
        """.trimIndent()
    }

    private fun buildStyleTag(
        googleFontImport: String,
        bgColor: String,
        textColor: String,
        topPadding: Int,
        bottomPadding: Int,
        sidePadding: Int,
        fontFamily: String,
        fontSize: Int,
        fontWeight: Int,
        lineHeight: Float,
        textAlign: String,
        isPageByPageVertical: Boolean,
        isPageByPageHorizontal: Boolean,
        isContinuousVertical: Boolean,
        isChapterScroll: Boolean
    ): String {
        // Calculate the content area dimensions
        // Using CSS calc() and viewport units for accurate page dimensions
        val pageWidthCalc = "calc(100vw - ${sidePadding * 2}px)"
        val pageHeightCalc = "calc(100vh - ${topPadding + bottomPadding}px)"

        // Determine overflow based on mode
        val htmlOverflow = if (isChapterScroll || isContinuousVertical) "auto" else "hidden"

        return """
            <style>
                $googleFontImport
                
                * { 
                    box-sizing: border-box; 
                    -webkit-text-size-adjust: 100%;
                }
                
                html {
                    background-color: $bgColor !important;
                    min-height: 100%;
                    width: 100%;
                    overflow-x: hidden;
                    overflow-y: $htmlOverflow;
                    margin: 0;
                    padding: 0;
                }
                
                body {
                    margin: 0 !important;
                    padding: 0 !important;
                    background-color: $bgColor !important;
                    color: $textColor !important;
                    font-family: $fontFamily !important;
                    font-size: ${fontSize}px !important;
                    font-weight: $fontWeight !important;
                    line-height: $lineHeight !important;
                    text-align: $textAlign !important;
                    word-wrap: break-word;
                    overflow-wrap: break-word;
                    -webkit-hyphens: auto;
                    hyphens: auto;
                    min-height: 100%;
                    width: 100%;
                }
                
                /* ========== PAGE BY PAGE VERTICAL MODE (swipe up/down) ========== */
                body.page-vertical {
                    overflow: hidden !important;
                    height: 100vh;
                    width: 100vw;
                }
                
                body.page-vertical #page-container {
                    position: absolute;
                    top: ${topPadding}px;
                    left: ${sidePadding}px;
                    right: ${sidePadding}px;
                    bottom: ${bottomPadding}px;
                    overflow: hidden;
                    /* CSS Columns for pagination - columns flow horizontally */
                    column-width: $pageWidthCalc;
                    column-gap: ${sidePadding * 2}px;
                    column-fill: auto;
                    height: $pageHeightCalc;
                    /* Hide initially to prevent flash - JavaScript will show after pagination */
                    visibility: hidden;
                }
                
                /* ========== PAGE BY PAGE HORIZONTAL MODE (swipe left/right) ========== */
                body.page-horizontal {
                    overflow: hidden !important;
                    height: 100vh;
                    width: 100vw;
                }
                
                body.page-horizontal #page-container {
                    position: absolute;
                    top: ${topPadding}px;
                    left: ${sidePadding}px;
                    right: ${sidePadding}px;
                    bottom: ${bottomPadding}px;
                    overflow: hidden;
                    /* CSS Columns for pagination - columns flow horizontally */
                    column-width: $pageWidthCalc;
                    column-gap: ${sidePadding * 2}px;
                    column-fill: auto;
                    height: $pageHeightCalc;
                    /* Hide initially to prevent flash - JavaScript will show after pagination */
                    visibility: hidden;
                }
                
                /* ========== CHAPTER SCROLL MODE ========== */
                body.chapter-scroll {
                    overflow-x: hidden !important;
                    overflow-y: scroll !important;
                    height: auto !important;
                    min-height: 100vh;
                    max-height: none !important;
                    padding: ${topPadding}px ${sidePadding}px ${bottomPadding}px ${sidePadding}px !important;
                    -webkit-overflow-scrolling: touch;
                    touch-action: pan-y;
                }
                
                /* ========== CONTINUOUS VERTICAL MODE ========== */
                body.continuous-vertical {
                    overflow-x: hidden !important;
                    overflow-y: scroll !important;
                    height: auto !important;
                    min-height: 100vh;
                    max-height: none !important;
                    padding: ${topPadding}px ${sidePadding}px ${bottomPadding}px ${sidePadding}px !important;
                    -webkit-overflow-scrolling: touch;
                    touch-action: pan-y;
                }
                
                /* Chapter content wrapper for lazy loading */
                body.continuous-vertical .chapter-content {
                    width: 100%;
                    margin-bottom: 1em;
                    padding-bottom: 1em;
                    border-bottom: 1px solid rgba(128,128,128,0.15);
                }
                
                body.continuous-vertical .chapter-content:last-child {
                    border-bottom: none;
                    margin-bottom: 0;
                }
                
                /* ========== MASK OVERLAYS FOR PAGINATED MODES ========== */
                body.page-vertical::before,
                body.page-horizontal::before {
                    content: '';
                    position: fixed;
                    top: 0;
                    left: 0;
                    right: 0;
                    height: ${topPadding}px;
                    background-color: $bgColor;
                    z-index: 1000;
                    pointer-events: none;
                }
                
                body.page-vertical::after,
                body.page-horizontal::after {
                    content: '';
                    position: fixed;
                    bottom: 0;
                    left: 0;
                    right: 0;
                    height: ${bottomPadding}px;
                    background-color: $bgColor;
                    z-index: 1000;
                    pointer-events: none;
                }
                
                /* Side masks for paginated modes */
                body.page-vertical .side-mask-left,
                body.page-horizontal .side-mask-left {
                    position: fixed;
                    top: 0;
                    left: 0;
                    width: ${sidePadding}px;
                    height: 100%;
                    background-color: $bgColor;
                    z-index: 1000;
                    pointer-events: none;
                }
                
                body.page-vertical .side-mask-right,
                body.page-horizontal .side-mask-right {
                    position: fixed;
                    top: 0;
                    right: 0;
                    width: ${sidePadding}px;
                    height: 100%;
                    background-color: $bgColor;
                    z-index: 1000;
                    pointer-events: none;
                }
                
                /* ========== COMMON TEXT STYLES ========== */
                p, div:not(#page-container):not(.side-mask-left):not(.side-mask-right), span, li, td, th, blockquote, figcaption {
                    text-align: $textAlign !important;
                    color: $textColor !important;
                }
                
                body * {
                    color: inherit;
                }
                
                p {
                    margin: 0 0 1em 0 !important;
                    text-indent: ${if (textAlign == "justify") "1.5em" else "0"} !important;
                    orphans: 3;
                    widows: 3;
                }
                
                p:first-of-type, p.first {
                    text-indent: 0 !important;
                }
                
                h1, h2, h3, h4, h5, h6 {
                    color: $textColor !important;
                    text-align: center !important;
                    margin: 1.5em 0 0.5em 0 !important;
                    font-weight: bold !important;
                    font-family: $fontFamily !important;
                    page-break-after: avoid;
                    break-after: avoid;
                    -webkit-column-break-after: avoid;
                }
                
                h1 { font-size: 1.6em !important; }
                h2 { font-size: 1.4em !important; }
                h3 { font-size: 1.2em !important; }
                h4 { font-size: 1.1em !important; }
                
                a {
                    color: #8B5CF6 !important;
                    text-decoration: none !important;
                }
                
                img {
                    max-width: 100% !important;
                    max-height: 50vh !important;
                    height: auto !important;
                    display: block !important;
                    margin: 1em auto !important;
                    object-fit: contain !important;
                    page-break-inside: avoid;
                    break-inside: avoid;
                    -webkit-column-break-inside: avoid;
                }
                
                img[src=""], img:not([src]), img.broken-image {
                    display: none !important;
                    height: 0 !important;
                    width: 0 !important;
                    margin: 0 !important;
                }
                
                svg {
                    max-width: 100% !important;
                    max-height: 50vh !important;
                    height: auto !important;
                    display: block !important;
                    margin: 1em auto !important;
                }
                
                svg:empty, svg.broken-image {
                    display: none !important;
                }
                
                svg image, image {
                    max-width: 100% !important;
                    height: auto !important;
                }
                
                figure {
                    margin: 1em 0 !important;
                    text-align: center !important;
                    display: block !important;
                    page-break-inside: avoid;
                    break-inside: avoid;
                    -webkit-column-break-inside: avoid;
                }
                
                figcaption {
                    font-size: 0.85em !important;
                    color: $textColor !important;
                    opacity: 0.8;
                    margin-top: 0.5em !important;
                    text-align: center !important;
                }
                
                blockquote {
                    margin: 1em 1.5em !important;
                    padding-left: 1em !important;
                    border-left: 3px solid $textColor !important;
                    opacity: 0.9;
                    font-style: italic !important;
                }
                
                pre, code {
                    background-color: rgba(128, 128, 128, 0.1) !important;
                    padding: 0.5em !important;
                    border-radius: 4px !important;
                    font-size: 0.9em !important;
                    overflow-x: auto !important;
                    font-family: monospace !important;
                }
                
                hr {
                    border: none !important;
                    border-top: 1px solid $textColor !important;
                    opacity: 0.3;
                    margin: 2em 0 !important;
                }
                
                ul, ol {
                    margin: 0.5em 0 1em 0 !important;
                    padding-left: 2em !important;
                }
                
                li {
                    margin-bottom: 0.3em !important;
                }
                
                table {
                    width: 100% !important;
                    border-collapse: collapse !important;
                    margin: 1em 0 !important;
                }
                
                td, th {
                    padding: 0.5em !important;
                    border: 1px solid rgba(128,128,128,0.3) !important;
                }
                
                .title-page, .cover, section[epub\\:type="titlepage"], 
                section[epub\\:type="cover"], div.cover, div.titlepage {
                    display: flex !important;
                    flex-direction: column !important;
                    justify-content: center !important;
                    align-items: center !important;
                    min-height: 60vh !important;
                    text-align: center !important;
                }
                
                .calibre, .calibre1, .calibre2, .calibre3 {
                    display: block !important;
                }
            </style>
        """.trimIndent()
    }

    private fun buildScriptTag(
        isPageByPageVertical: Boolean,
        isPageByPageHorizontal: Boolean,
        isContinuousVertical: Boolean,
        isChapterScroll: Boolean,
        readingModeName: String,
        topPadding: Int,
        bottomPadding: Int,
        sidePadding: Int,
        currentPage: Int
    ): String {
        return """
            <script>
                var currentPage = 0;
                var totalPages = 1;
                var isPageByPageVertical = $isPageByPageVertical;
                var isPageByPageHorizontal = $isPageByPageHorizontal;
                var isContinuousVertical = $isContinuousVertical;
                var isChapterScroll = $isChapterScroll;
                var readingMode = '$readingModeName';
                var pageWidth = 0;
                var pageHeight = 0;
                var columnWidth = 0;
                var columnGap = ${sidePadding * 2};
                var topPadding = $topPadding;
                var bottomPadding = $bottomPadding;
                var sidePadding = $sidePadding;
                var startPage = $currentPage;
                var initialized = false;
                var pagesCalculated = false;
                
                function log(msg) {
                    if (typeof AndroidBridge !== 'undefined') {
                        AndroidBridge.log(msg);
                    }
                    console.log(msg);
                }
                
                function hideBrokenImages() {
                    var images = document.querySelectorAll('img');
                    images.forEach(function(img) {
                        img.onerror = function() {
                            this.classList.add('broken-image');
                            this.style.display = 'none';
                        };
                        if (img.complete && img.naturalHeight === 0) {
                            img.classList.add('broken-image');
                            img.style.display = 'none';
                        }
                    });
                    
                    var svgs = document.querySelectorAll('svg');
                    svgs.forEach(function(svg) {
                        if (svg.children.length === 0 || 
                            (svg.children.length === 1 && svg.children[0].tagName === 'image' && 
                             !svg.children[0].getAttribute('xlink:href') && !svg.children[0].getAttribute('href'))) {
                            svg.classList.add('broken-image');
                            svg.style.display = 'none';
                        }
                    });
                }
                
                function calculatePageCount() {
                    hideBrokenImages();
                    
                    if (isContinuousVertical || isChapterScroll) {
                        // No pagination for scroll modes
                        totalPages = 1;
                        pagesCalculated = true;
                        reportPageCount();
                        return;
                    }
                    
                    var container = document.getElementById('page-container');
                    if (!container) {
                        log('No page container found');
                        totalPages = 1;
                        pagesCalculated = true;
                        reportPageCount();
                        return;
                    }
                    
                    // For column-based pagination
                    // The scrollWidth tells us how wide all columns are
                    pageWidth = window.innerWidth - (sidePadding * 2);
                    pageHeight = window.innerHeight - topPadding - bottomPadding;
                    columnWidth = pageWidth;
                    
                    var scrollWidth = container.scrollWidth;
                    var singlePageWidth = columnWidth + columnGap;
                    
                    if (singlePageWidth > 0 && scrollWidth > 0) {
                        totalPages = Math.max(1, Math.round(scrollWidth / singlePageWidth));
                    } else {
                        totalPages = 1;
                    }
                    
                    pagesCalculated = true;
                    log('Column pagination: ' + totalPages + ' pages (scrollWidth: ' + scrollWidth + ', pageWidth: ' + singlePageWidth + ')');
                    reportPageCount();
                }
                
                function reportPageCount() {
                    if (typeof AndroidBridge !== 'undefined') {
                        AndroidBridge.onPageCountCalculated(totalPages);
                    }
                }
                
                function goToPage(pageNum, skipReport) {
                    if (isContinuousVertical || isChapterScroll) return;
                    
                    pageNum = Math.max(0, Math.min(pageNum, totalPages - 1));
                    currentPage = pageNum;
                    
                    var container = document.getElementById('page-container');
                    if (!container) return;
                    
                    var singlePageWidth = columnWidth + columnGap;
                    var offset = pageNum * singlePageWidth;
                    
                    // Scroll the container horizontally to show the correct column
                    container.scrollLeft = offset;
                    
                    log('Navigated to page ' + (pageNum + 1) + '/' + totalPages + ', scrollLeft: ' + offset + 'px');
                    
                    if (!skipReport && typeof AndroidBridge !== 'undefined') {
                        AndroidBridge.onPageChanged(currentPage);
                        
                        // Notify Android when nearing last page (last 2 pages) for preloading
                        if (totalPages > 0 && (totalPages - currentPage) <= 2) {
                            AndroidBridge.onNearLastPage(currentPage, totalPages);
                        }
                    }
                }
                
                function goToLastPage() {
                    if (!pagesCalculated) {
                        calculatePageCount();
                    }
                    var lastPage = totalPages - 1;
                    log('Going to last page: ' + (lastPage + 1) + ' of ' + totalPages);
                    goToPage(lastPage);
                }
                
                function nextPage() {
                    if (isPageByPageVertical || isPageByPageHorizontal) {
                        if (currentPage < totalPages - 1) {
                            goToPage(currentPage + 1);
                        } else {
                            log('At last page - requesting next chapter');
                            if (typeof AndroidBridge !== 'undefined') {
                                AndroidBridge.requestNextChapter();
                            }
                        }
                    }
                }
                
                function previousPage() {
                    if (isPageByPageVertical || isPageByPageHorizontal) {
                        if (currentPage > 0) {
                            goToPage(currentPage - 1);
                        } else {
                            log('At first page - requesting previous chapter');
                            if (typeof AndroidBridge !== 'undefined') {
                                AndroidBridge.requestPreviousChapter();
                            }
                        }
                    }
                }
                
                // Aliases for JavaScript calls from Android
                function scrollNext() { nextPage(); }
                function scrollPrevious() { previousPage(); }
                
                // Variables for continuous vertical lazy loading
                var loadedChapters = [0]; // Track which chapters are loaded
                var currentChapter = 0;
                var preloadTriggered = false;
                var lastScrollTop = 0;
                
                function initContinuousVerticalScroll() {
                    if (!isContinuousVertical) return;
                    
                    log('Initializing continuous vertical scroll with lazy loading');
                    
                    // Add scroll listener for lazy loading
                    window.addEventListener('scroll', function() {
                        var scrollTop = window.scrollY || document.documentElement.scrollTop || 0;
                        var scrollHeight = document.body.scrollHeight - window.innerHeight;
                        var scrollPercent = scrollHeight > 0 ? (scrollTop / scrollHeight) * 100 : 0;
                        
                        // Determine current chapter based on scroll position
                        var chapters = document.querySelectorAll('.chapter-content');
                        var newChapter = currentChapter;
                        
                        chapters.forEach(function(chapter, index) {
                            var chapterTop = chapter.offsetTop;
                            if (scrollTop >= chapterTop - 100) {
                                newChapter = parseInt(chapter.getAttribute('data-chapter')) || index;
                            }
                        });
                        
                        // Update chapter if changed
                        if (newChapter !== currentChapter) {
                            currentChapter = newChapter;
                            log('Current chapter: ' + currentChapter);
                            if (typeof AndroidBridge !== 'undefined') {
                                AndroidBridge.onChapterChanged(currentChapter);
                            }
                        }
                        
                        // Request preload when 75% through the content
                        if (scrollPercent >= 75 && !preloadTriggered) {
                            preloadTriggered = true;
                            log('Requesting preload - scroll: ' + scrollPercent.toFixed(1) + '%');
                            if (typeof AndroidBridge !== 'undefined') {
                                AndroidBridge.requestPreloadNextChapter();
                            }
                        }
                        
                        // Reset preload trigger when scrolling back up
                        if (scrollPercent < 70) {
                            preloadTriggered = false;
                        }
                        
                        lastScrollTop = scrollTop;
                    }, { passive: true });
                }
                
                function scrollToChapter(chapterIndex) {
                    var chapters = document.querySelectorAll('.chapter-content');
                    for (var i = 0; i < chapters.length; i++) {
                        var chapterNum = parseInt(chapters[i].getAttribute('data-chapter'));
                        if (chapterNum === chapterIndex) {
                            chapters[i].scrollIntoView({ behavior: 'smooth' });
                            currentChapter = chapterIndex;
                            return;
                        }
                    }
                    log('Chapter ' + chapterIndex + ' not found in DOM');
                }
                
                function initPagination() {
                    // Prevent multiple initializations
                    if (initialized) return;
                    initialized = true;
                    
                    log('Initializing pagination, mode: ' + readingMode + ', startPage: ' + startPage);
                    
                    hideBrokenImages();
                    
                    // Initialize continuous vertical scroll handling
                    if (isContinuousVertical) {
                        initContinuousVerticalScroll();
                        return;
                    }
                    
                    // For paginated modes, hide content initially to prevent flash
                    var container = document.getElementById('page-container');
                    if (container && (isPageByPageVertical || isPageByPageHorizontal)) {
                        container.style.visibility = 'hidden';
                    }
                    
                    // Wait for content to fully render before calculating pages
                    setTimeout(function() {
                        hideBrokenImages();
                        calculatePageCount();
                        
                        // Navigate to the correct page
                        if (startPage === -1 && (isPageByPageVertical || isPageByPageHorizontal)) {
                            // Go to last page (when coming from next chapter going backwards)
                            goToLastPage();
                        } else if (startPage > 0 && (isPageByPageVertical || isPageByPageHorizontal)) {
                            goToPage(startPage);
                        } else if (isPageByPageVertical || isPageByPageHorizontal) {
                            goToPage(0);
                        }
                        
                        // Make content visible after positioning
                        if (container) {
                            container.style.visibility = 'visible';
                        }
                        
                        if (typeof AndroidBridge !== 'undefined') {
                            AndroidBridge.onPageChanged(currentPage);
                        }
                    }, 350);
                    
                    // Handle resize
                    window.addEventListener('resize', function() {
                        var oldPage = currentPage;
                        setTimeout(function() {
                            pagesCalculated = false;
                            calculatePageCount();
                            if (isPageByPageVertical || isPageByPageHorizontal) {
                                goToPage(Math.min(oldPage, totalPages - 1));
                            }
                        }, 100);
                    });
                }
                
                // Only use one initialization method to prevent multiple calls
                if (document.readyState === 'complete' || document.readyState === 'interactive') {
                    setTimeout(initPagination, 50);
                } else {
                    document.addEventListener('DOMContentLoaded', function() {
                        setTimeout(initPagination, 50);
                    });
                }
            </script>
        """.trimIndent()
    }

    private fun wrapContent(
        content: String,
        styleTag: String,
        bodyClass: String
    ): String {
        val hasHtmlTag = content.contains("<html", ignoreCase = true)
        val hasHeadTag = content.contains("<head", ignoreCase = true)
        val hasBodyTag = content.contains("<body", ignoreCase = true)

        // For paginated modes, we need to wrap content in a page container
        val needsPageContainer = bodyClass == "page-vertical" || bodyClass == "page-horizontal"

        // For continuous vertical mode, wrap content with chapter-content div for tracking
        val isContinuousVertical = bodyClass == "continuous-vertical"

        // Extract body content for wrapping
        fun extractAndWrapBody(html: String): String {
            val bodyStart = html.indexOf("<body", ignoreCase = true)
            val bodyEnd = html.indexOf("</body>", ignoreCase = true)

            if (bodyStart < 0 || bodyEnd < 0) return html

            val bodyTagEnd = html.indexOf(">", bodyStart) + 1
            val bodyContent = html.substring(bodyTagEnd, bodyEnd)

            return when {
                needsPageContainer -> {
                    html.substring(0, bodyStart) +
                    "<body class=\"$bodyClass\">" +
                    "<div class=\"side-mask-left\"></div>" +
                    "<div class=\"side-mask-right\"></div>" +
                    "<div id=\"page-container\">$bodyContent</div>" +
                    html.substring(bodyEnd)
                }
                isContinuousVertical -> {
                    html.substring(0, bodyStart) +
                    "<body class=\"$bodyClass\">" +
                    "<div class=\"chapter-content\" data-chapter=\"0\">$bodyContent</div>" +
                    html.substring(bodyEnd)
                }
                else -> {
                    html.substring(0, bodyStart) +
                    "<body class=\"$bodyClass\">$bodyContent" +
                    html.substring(bodyEnd)
                }
            }
        }

        return when {
            hasHtmlTag && hasHeadTag && hasBodyTag -> {
                val withStyle = content.replace("</head>", "$styleTag</head>", ignoreCase = true)
                extractAndWrapBody(withStyle)
            }
            hasHtmlTag && hasHeadTag -> {
                content.replace("</head>", "$styleTag</head>", ignoreCase = true)
            }
            hasHtmlTag -> {
                content.replace("<html>", "<html><head>$styleTag</head>", ignoreCase = true)
                    .replace("<HTML>", "<HTML><head>$styleTag</head>")
            }
            else -> {
                val wrappedBody = when {
                    needsPageContainer -> {
                        """<body class="$bodyClass">
                            <div class="side-mask-left"></div>
                            <div class="side-mask-right"></div>
                            <div id="page-container">$content</div>
                        </body>"""
                    }
                    isContinuousVertical -> {
                        """<body class="$bodyClass">
                            <div class="chapter-content" data-chapter="0">$content</div>
                        </body>"""
                    }
                    else -> {
                        """<body class="$bodyClass">$content</body>"""
                    }
                }

                """
                <!DOCTYPE html>
                <html>
                <head>
                    <meta charset="UTF-8">
                    <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=3.0, user-scalable=yes">
                    $styleTag
                </head>
                $wrappedBody
                </html>
                """.trimIndent()
            }
        }
    }
}
