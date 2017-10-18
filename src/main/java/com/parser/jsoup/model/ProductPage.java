package com.parser.jsoup.model;


import com.parser.jsoup.utils.Utils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static com.parser.jsoup.utils.Utils.MAIN_URL;

public class ProductPage implements Runnable {

    private String url;
    private static List<Product> allProducts;

    private static final String CSS_PRODUCT_NAME = ".productName_192josg";
    private static final String CSS_DETAILS = ".container_iv4rb4";
    private static final String CSS_DESCRIPTION = ".attributeWrapper_1r89zxg";
    private static final String ARTIKLE_NR = "Artikel-Nr: ";

    public ProductPage(String url, List<Product> allProducts) {
        this.url = url;
        this.allProducts = allProducts;
    }

    @Override
    public void run() {

        try {
            allProducts.addAll(getProducts());
            System.out.print(".");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Elements parsePage(String url) throws IOException {
        Document document = Utils.getDocument(url);
        Elements content = document.select("body");
        return content;
    }


    private List<Product> getProducts() throws IOException {
        List<Product> products = new ArrayList<>();
        List<String> urls = getUrlsColors();
        for (String urlPage : urls) {
            Elements contentPage = parsePage(urlPage);
            Product productFromHTML = getProductFromHTML(contentPage);
            products.add(productFromHTML);
        }

        return products;
    }

    public List<String> getUrlsColors() throws IOException {
        Elements content = parsePage(url);

        String jsonFromJS = getJSONFromJS(content, "script");

        Product firstPageProduct = getFirstPageProduct(content);
        allProducts.add(firstPageProduct);

        return getUrlsProductColors(jsonFromJS);
    }

    private Product getFirstPageProduct(Elements content) throws IOException {
        Product product = getProductFromHTML(content);
        return product;
    }


    public Product getProductFromHTML(String url) throws IOException {
        Elements content = parsePage(url);
        return getProductFromHTML(content);
    }

    public Product getProductFromHTML() throws IOException {
        Elements content = parsePage(url);
        return getProductFromHTML(content);
    }

    public Product getProductFromHTML(Elements content) throws IOException {

        Product product = new Product();

        String productName = content.select(CSS_PRODUCT_NAME).text();

        String name = productName.substring(productName.indexOf("|") + 2, productName.length());
        String brande = productName.substring(0, productName.indexOf("|") - 1);

        Elements descriptions = content.select(CSS_DESCRIPTION);

        List<String> listDescriptions = getDescriptions(descriptions);

        String jsonFromJS = getJSONFromJS(content, "script");

        List<String> sizesProduct = getSizesProduct(jsonFromJS);

        JSONArray objectProducts = getStylesProduct(jsonFromJS);

        String color = new JSONObject(objectProducts.get(0).toString()).get("color").toString();
        String price = getPrice(objectProducts);
        String initialPrice = getInitialPrice(objectProducts);

        String article = getArticle(jsonFromJS);

        product.setName(name);
        product.setBrande(brande);
        product.setInitialPrice(initialPrice);
        product.setPrice(price);
        product.setColor(color);
        product.setArticle(article);
        product.setDescriptions(listDescriptions);
        product.setSizes(sizesProduct);

        return product;
    }

    private String getPrice(JSONArray objectProducts) {
        Object prices = new JSONObject(objectProducts.get(0).toString()).get("price");
        double minPrice = ((Integer) new JSONObject(prices.toString()).get("min")) / 100.0;

        return String.valueOf(minPrice);
    }

    private String getInitialPrice(JSONArray objectProducts) {
        String oldPrice = new JSONObject(objectProducts.get(0).toString()).get("oldPrice").toString();
        String price = null;
        if (oldPrice != "null") {
            price = String.valueOf(Integer.valueOf(oldPrice) / 100.0);
        }
        return price;
    }

    private List<String> getDescriptions(Elements descriptions) {

        List<String> listDescription = new ArrayList<>();
        for (Element description : descriptions) {
            listDescription.add(description.text());
        }
        return listDescription;
    }

    private List<String> getUrlsProductColors(String text) {
        List<String> urlsList = new ArrayList<>();

        JSONArray styles = getStylesProduct(text);

        for (Object style : styles) {
            Object url = new JSONObject(style.toString()).get("url");
            urlsList.add(MAIN_URL + url.toString());
        }
        return urlsList;
    }

    private JSONArray getStylesProduct(String text) {
        Object product = getObjectProduct(text);
        return (JSONArray) new JSONObject(product.toString()).get("styles");
    }

    private String getArticle(String text) {
        Object product = getObjectProduct(text);
        String productInfo = new JSONObject(product.toString()).get("productInfo").toString();
        String articleNumber = new JSONObject(productInfo.toString()).get("articleNumber").toString();

        return articleNumber;
    }

    private List<String> getSizesProduct(String text) {
        List<String> sizesList = new ArrayList<>();
        Object product = getObjectProduct(text);
        JSONArray sizes = (JSONArray) new JSONObject(product.toString()).get("sizes");
        for (Object size : sizes) {
            Boolean isDisable = Boolean.valueOf(new JSONObject(size.toString()).get("isDisabled").toString());
            if (!isDisable) {
                String shopSize = new JSONObject(size.toString()).get("shopSize").toString();
                sizesList.add(shopSize);
            }
        }

        return sizesList;
    }

    private Object getObjectProduct(String content) {
        JSONObject obj = new JSONObject(content);
        Object adpPage = obj.get("adpPage");
        return new JSONObject(adpPage.toString()).get("product");
    }

    private String getJSONFromJS(Elements elements, String tag) {
        Elements scripts = elements.select(tag);
        String json = null;
        for (Element script : scripts) {
            String data = script.data();
            if (data.startsWith("window.__INITIAL_STATE__")) {
                json = convertJSToJSON(data);
                break;
            }
        }
        return json;
    }

    private String convertJSToJSON(String wholeData) {
        String json;
        int indexOf = wholeData.indexOf("=");
        String subNoScriptName = wholeData.substring(indexOf + 1);
        json = subNoScriptName.substring(0, subNoScriptName.length() - 1);
        return json;
    }


}
