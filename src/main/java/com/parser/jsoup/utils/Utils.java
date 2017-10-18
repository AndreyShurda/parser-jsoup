package com.parser.jsoup.utils;


import com.parser.jsoup.model.Offers;
import org.apache.commons.io.FileUtils;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import java.io.File;
import java.io.IOException;

public class Utils {
    public static final String MAIN_URL = "https://www.aboutyou.de";

    public static int totalRequests = 0;

    public static void marshalingOffers(Offers offers) throws JAXBException {
        JAXBContext jaxbContext = JAXBContext.newInstance(Offers.class);
        Marshaller jaxbMarshaller = jaxbContext.createMarshaller();

        jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);

        //Marshal the product list in console
//        jaxbMarshaller.marshal(offers, System.out);

        //Marshal the employees list in file
        jaxbMarshaller.marshal(offers, new File("offers.xml"));
    }

    public static Document getDocument(String url) throws IOException {
        totalRequests++;
//        return Jsoup.parse(new File(url), "UTF-8");
        return Jsoup.connect(url)
                .timeout(10000)
                .get();
    }

    public static void saveHTMLContentToFile(String url, String filename) throws IOException {
        final Connection.Response response = Jsoup.connect(url).execute();
        final Document doc = response.parse();

        final File f = new File(filename);
        FileUtils.writeStringToFile(f, doc.outerHtml(), "UTF-8");
    }
}
