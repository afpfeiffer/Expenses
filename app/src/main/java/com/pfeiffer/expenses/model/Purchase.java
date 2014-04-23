package com.pfeiffer.expenses.model;

import android.util.Log;

import java.util.Date;


public class Purchase {
    final boolean cash_;
    private int id_ = -1;
    private Barcode barcode_;
    final private int amount_;
    final private Location location_;
    final private Money price_;
    final private Date purchaseDate_;
    final private String productName_;
    final private Category category_;
    final private String owner_;

    public Purchase(int purchaseId, Barcode barcode, int amount, Date date, Location location, Money price,
                    boolean cash, String productName, Category category, String owner) {
        Log.d(this.getClass().getName(), "Enter Purchase constructor with arguments: purchaseId=" + purchaseId
                + ", barcode=" + barcode + ", amount=" + amount + ", date=" + date + ", location=" + location
                + ", price=" + price + ", cash=" + cash + ", productName=" + productName + ", " +
                "category=" + category + ", owner=" + owner + ".");

        // TODO check arguments, throw exceptions

        id_ = purchaseId;
        barcode_ = barcode;
        amount_ = amount;
        purchaseDate_ = date;
        location_ = location;
        price_ = price;
        cash_ = cash;
        productName_ = productName;
        category_ = category;
        owner_ = owner;
    }

    public Date getDate() {
        return purchaseDate_;
    }

    public Barcode getBarcode() {
        return barcode_;
    }

    public int getAmount() {
        return amount_;
    }

    public Location getLocation() {
        return location_;
    }

    public Money getPrice() {
        return price_;
    }

    public int getId() {
        return id_;
    }

    public boolean isCash() {
        return cash_;
    }

    public String getProductName() {
        return productName_;
    }

    public Category getCategory() {
        return category_;
    }

    public String getOwner() {
        return owner_;
    }

    public Money getTotalPrice() {
        return price_.getScaled(amount_);
    }

    public String getTotalHumanReadablePrice() {
        return price_.getHumanReadableRepresentation(amount_);
    }

    public boolean hasValidState() {

        if (!price_.isValid()) return false;
        if (productName_ == null || productName_.equals("")) return false;
        if (category_.equals(Category.NONE)) return false;
        if (location_.equals(Location.NONE)) return false;

        return true;
    }
}
