package com.pfeiffer.expenses.model;

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

    public Product(int id, String name, CATEGORY category, String price, Barcode barcode) {
        id_ = id;
        barcode_ = barcode;
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

    /**
     * This method is needed when the price of a product changes.
     *
     * @param price new price
     */
    public void setPrice(String price) {
        price_ = price;
    }

    public String getName() {
        return name_;
    }

    public void setName(String name) {
        name_ = name;
    }

    public CATEGORY getCategory() {
        return category_;
    }

    public void setCategory(CATEGORY category) {
        category_ = category;
    }

    public boolean hasBarcode(){
        return barcode_!=null;
    }
}
