package com.pfeiffer.expenses;

enum CATEGORY {
    NONE(""), OBST("Obst"), BACKWAREN("Backwaren"), GENUSS("Genuss"), LEBENSMITTEL("Sonstige Lebensmittel"), EXTERN(
            "Auswärts Essen"), SONSTIGES("Sonstiges");

    private final String friendlyName_;

    private CATEGORY(String friendlyName) {
        this.friendlyName_ = friendlyName;
    }

    public static CATEGORY fromString(String description) {
        for (CATEGORY c : values()) {
            if (c.friendlyName_.equals(description))
                return c;
        }
        return null;
    }

    @Override
    public String toString() {
        return friendlyName_;
    }
}

/**
 * class represents a specific product, usually but not necessarily from a
 * specific dealer
 *
 * @author axelpfeiffer
 */
public class Product {
    private final int id_;
    private final Barcode barcode_;
    private String price_;
    private CATEGORY category_;
    private String name_;

    public Product(int id, String name, CATEGORY category, String price, String barcode) {
        id_ = id;
        barcode_ = new Barcode(barcode);
        price_ = price;
        category_ = category;
        name_ = name;
    }

    public int getId() {
        return id_;
    }

    public Barcode getBarcode() {
        return barcode_;
    }

    public String getPrice() {
        return price_;
    }

    public String getName() {
        return name_;
    }

    public CATEGORY getCategory() {
        return category_;
    }

    /**
     * This method is needed when the price of a product changes.
     *
     * @param price new price
     */
    public void setPrice(String price) {
        price_ = price;
    }

    public void setCategory(CATEGORY category) {
        category_ = category;
    }

    public void setName(String name) {
        name_ = name;
    }
}
