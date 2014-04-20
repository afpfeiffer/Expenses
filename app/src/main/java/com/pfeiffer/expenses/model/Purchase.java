package com.pfeiffer.expenses.model;

import android.util.Log;

import com.pfeiffer.expenses.utility.Translation;

import java.util.Date;


public class Purchase {
    final boolean cash_;
    private int id_ = -1;
    private int productId_;
    final private int amount_;
    final private LOCATION location_;
    final private String price_;
    final private Date purchaseDate_;
    final private String productName_;
    final private CATEGORY category_;

    public Purchase(int purchaseId, int productId, int amount, Date date, LOCATION location, String price,
                    boolean cash, String productName, CATEGORY category) {
        Log.d(this.getClass().getName(), "Enter Purchase constructor with arguments: purchaseId=" + purchaseId
                + ", productId=" + productId + ", amount=" + amount + ", date=" + date + ", location=" + location
                + ", price=" + price + ", cash=" + cash + ", productName=" + productName + ", category=" + category + ".");

        // TODO check arguments, throw exceptions

        id_ = purchaseId;
        productId_ = productId;
        amount_ = amount;
        purchaseDate_ = date;
        location_ = location;
        price_ = price;
        cash_ = cash;
        productName_ = productName;
        category_ = category;
    }

    public Date getDate() {
        return purchaseDate_;
    }

    public int getProductId() {
        return productId_;
    }

    public int getAmount() {
        return amount_;
    }

    public LOCATION getLocation() {
        return location_;
    }

    public String getPrice() {
        return Translation.getValidPrice(price_);
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

    public CATEGORY getCategory() {
        return category_;
    }

    public boolean hasProductAttached() {
        return productId_ > 0;
    }

    public String getTotalPrice() {
        return Translation.getValidPrice(String.valueOf(Double.parseDouble(price_) * amount_));
    }

    public boolean hasValidState() {

        if (price_ == null || price_.equals("") || price_.equals(".") || price_.length() - price_.replaceAll("\\.",
                "").length() > 1) return false;
        if (productName_ == null || productName_.equals("")) return false;
        if (category_.equals(CATEGORY.NONE)) return false;
        if (location_.equals(LOCATION.NONE)) return false;

        return true;
    }

    public void setProductId(int productId) {
        productId_ = productId;
    }
}
