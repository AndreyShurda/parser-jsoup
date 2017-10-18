package com.parser.jsoup;

import com.parser.jsoup.model.Category;
import com.parser.jsoup.model.Offers;
import com.parser.jsoup.model.Product;
import com.parser.jsoup.model.ProductPage;
import com.parser.jsoup.utils.Utils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import javax.xml.bind.JAXBException;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.parser.jsoup.utils.Utils.getDocument;
import static com.parser.jsoup.utils.Utils.marshalingOffers;

public class Main {
    public static void main(String[] args) throws IOException {
        if (args.length == 1) {
            long beforeUsedMem = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
            long timeBegin = System.currentTimeMillis();

            List<Product> allProducts = Collections.synchronizedList(new ArrayList<Product>());

//            String keyword = "tay";
            String keyword = args[0];
            String urlSearch = "https://www.aboutyou.de/suche?term=" + keyword;

            Document doc = getDocument(urlSearch);

            ExecutorService executor = Executors.newFixedThreadPool(20);

            Set<String> linksPagesAll = Collections.synchronizedSet(new HashSet<String>());
            Set<String> linksFromCategories = getLinksFromCategories(doc, linksPagesAll);

            for (String linksFromCategory : linksFromCategories) {
                ProductPage productPage = new ProductPage(linksFromCategory, allProducts);
                executor.execute(productPage);
            }

            executor.shutdown();
            while (!executor.isTerminated()) {
            }
            System.out.println("\nFinished all threads pages");

            writeResultToXML(allProducts);

            long timeEnd = System.currentTimeMillis();
            long periodRunProgram = timeEnd - timeBegin;

            System.out.println("period run program: " + periodRunProgram + " msec");
            long afterUsedMem = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();

            long actualMemUsed = afterUsedMem - beforeUsedMem;
            System.out.println("Amount of triggered HTTP request: " + Utils.totalRequests);
            System.out.println("Memory used: " + actualMemUsed);
            System.out.println("Amount of extracted products: " + allProducts.size());
        } else {
            System.out.println("Enter a keyword");
        }

    }

    private static void writeResultToXML(List<Product> allProducts) {
        Offers offers = new Offers();
        offers.setProducts(allProducts);
        try {
            marshalingOffers(offers);
        } catch (JAXBException e) {
            e.printStackTrace();
        }
    }

    private static Set<String> getLinksFromCategories(Document doc, Set<String> linksPagesAll) throws IOException {
        List<String> linkCategories = getLinksCategories(doc);
        for (String linkCategory : linkCategories) {
            Category category = new Category(linkCategory, linksPagesAll);
            Thread thread = new Thread(category);
            thread.start();
            try {
                thread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        System.out.println("\nFinished all threads Categories");


        return linksPagesAll;
    }

    private static List<String> getLinksCategories(Document document) {
        return getLinks(document, ".category-item-label.category-item-label-2");
    }


    private static List<String> getLinks(Document document, String cssQuery) {
        List<String> links = new ArrayList<>();
        Elements elements = document.select(cssQuery);
        for (Element element : elements) {
            links.add(getLink(element));
        }
        return links;
    }

    private static String getLink(Element element) {
        Element link = element.select("a").first();
        String href = link.attr("abs:href");
        return href;
    }

}
