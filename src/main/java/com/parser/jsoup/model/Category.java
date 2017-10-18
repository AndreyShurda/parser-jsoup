package com.parser.jsoup.model;


import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import static com.parser.jsoup.utils.Utils.getDocument;

public class Category implements Runnable {
    private static final String CSS_LINKS_PAGE = ".col-xs-4.isLayout3";
    private static final String CSS_LINK_NEXT_PAGE = ".col-xs-4.productlist-item-border";
    private Set<String> linksPages;

    private Document document;

    public Category(Document document) {
        this.document = document;
    }

    public Category(String url, Set<String> linksPages) throws IOException {
        this.document = getDocument(url);
        this.linksPages = linksPages;
    }

    public Set<String> getLinksPages() {
        return linksPages;
    }

    @Override
    public void run() {

        System.out.print(".");
        try {
            synchronized (linksPages) {
                linksPages.addAll(getLinksPage());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Set<String> getLinksPage() throws IOException {
        linksPages = getLinksPage(document);
        String linkToNextPage = getLinkToNextPage(document);

        moveToNextPage(linkToNextPage);

        return linksPages;
    }

    private void moveToNextPage(String linkToNextPage) throws IOException {
        if (linkToNextPage != null) {
            Document document = getDocument(linkToNextPage);
            Set<String> linksPages = getLinksPage(document);
            this.linksPages.addAll(linksPages);

            String linkNextPage = getLinkToNextPage(document);
            moveToNextPage(linkNextPage);
        }
    }

    private static Set<String> getLinksPage(Document document) {
        return getLinks(document, CSS_LINKS_PAGE);
    }

    private static String getLinkToNextPage(Document document) {
        Elements endLink = document.select(CSS_LINK_NEXT_PAGE);
        return getLink(endLink.first());
    }

    private static Set<String> getLinks(Document document, String cssQuery) {
        Set<String> links = new HashSet<>();
        Elements elements = document.select(cssQuery);
        for (Element element : elements) {
            String href = getLink(element);
            links.add(href);
        }
        return links;
    }

    private static String getLink(Element element) {
        String href = null;

        try {
            Element link = element.select("a").first();
            href = link.attr("abs:href");
        } catch (NullPointerException e) {
//            e.printStackTrace();
//            System.out.println("Not found link");
        }
        return href;
    }

}
