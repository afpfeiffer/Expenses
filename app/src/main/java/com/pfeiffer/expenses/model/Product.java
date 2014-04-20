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
    private String name_;

    public Product(int id, String name, Barcode barcode) {
        id_ = id;
        barcode_ = barcode;
        name_ = name;
    }

    public int getId() {
        return id_;
    }

    public Barcode getBarcode() {
        return barcode_;
    }

    public String getName() {
        return name_;
    }

    public void setName(String name) {
        name_ = name;
    }

    public boolean hasBarcode(){
        return !barcode_.isEmpty();
    }
}
