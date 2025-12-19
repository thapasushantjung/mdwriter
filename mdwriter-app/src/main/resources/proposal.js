document.addEventListener("DOMContentLoaded", function () {
    if (typeof hljs !== 'undefined') {
        hljs.highlightAll();
    }
    // Delay to ensure DOM is fully rendered in WebView
    setTimeout(function () {
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

                    console.log('Page ' + (i + 1) + ' height: ' + contentHeight.toFixed(0) + 'px');

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
        document.body.addEventListener('click', function (e) {
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
