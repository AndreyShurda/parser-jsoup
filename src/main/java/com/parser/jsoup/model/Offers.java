package com.parser.jsoup.model;

import javax.xml.bind.JAXBException;
import javax.xml.bind.annotation.*;
import java.util.ArrayList;
import java.util.List;

import static com.parser.jsoup.utils.Utils.marshalingOffers;

@XmlRootElement(name = "offers")
@XmlAccessorType(XmlAccessType.FIELD)
public class Offers {

    @XmlElement(name = "offer")
    private List<Product> products = null;

    public List<Product> getProducts() {
        return products;
    }

    public void setProducts(List<Product> products) {
        this.products = products;
    }

}
