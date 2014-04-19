package com.pfeiffer.expenses.model;

import android.util.Log;

import com.pfeiffer.expenses.utility.Translation;

import java.util.Date;


public class Purchase {
    final boolean cash_;
    private int id_;
    final private int productId_;
    final private int amount_;
    final private LOCATION location_;
    final private String price_;
    final private Date purchaseDate_;

    public Purchase(int purchaseId, int productId, int amount, Date date, LOCATION location, String price,
                    boolean cash) {
        Log.d(this.getClass().getName(), "Enter Purchase constructor with arguments: purchaseId=" + purchaseId
                + ", productId=" + productId + ", amount=" + amount + ", date=" + date + ", location=" + location
                + ", price=" + price + ", cash=" + cash + ".");

        // TODO check arguments, throw exceptions

        id_ = purchaseId;
        productId_ = productId;
        amount_ = amount;
        purchaseDate_ = date;
        location_ = location;
        price_ = price;
        cash_ = cash;
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

    public Purchase getReadOnlyCopy() {
        return new Purchase(-1, productId_,amount_, purchaseDate_, location_, price_, cash_);
    }

    public String getTotalPrice() {
        return Translation.getValidPrice(String.valueOf(Double.parseDouble(price_) * amount_));
    }
}
