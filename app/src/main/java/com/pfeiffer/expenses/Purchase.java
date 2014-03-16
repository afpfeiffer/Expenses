package com.pfeiffer.expenses;

import android.util.Log;

enum LOCATION {
    NONE(""), EDEKA("Edeka"), ALDI("Aldi"), REWE("Rewe"), PENNY("Penny"), KANTINE("Kantine"), BAKEREI(
            "Bäckerei"), KIOSK("Kiosk"), TJARDENS("Tjardens"), BASIC("Basic"), TANKSTELLE("Tankstelle"), BUDNI(
            "Budni"), SONSTIGES("Sonstiges");
    private final String friendlyName;

    private LOCATION(String friendlyName) {
        this.friendlyName = friendlyName;
    }

    public static LOCATION fromString(String description) {
        for (LOCATION l : values()) {
            if (l.friendlyName.equals(description))
                return l;
        }
        return null;
    }

    @Override
    public String toString() {
        return friendlyName;
    }
}

public class Purchase {
    final private int purchaseId_;
    final private int productId_;
    final private int amount_;
    final private String date_;
    final private LOCATION location_;
    final private String price_;

    public Purchase(int purchaseId, int productId, int amount, String date, LOCATION location, String price) {
        Log.d(this.getClass().getName(), "Enter Purchase constructor with arguments: purchaseId=" + purchaseId
                + ", productId=" + productId + ", amount=" + amount + ", date=" + date + ", location=" + location
                + ", price=" + price + ".");

        // TODO check arguments, throw exceptions

        purchaseId_ = purchaseId;
        productId_ = productId;
        amount_ = amount;
        date_ = date;
        location_ = location;
        price_ = price;
    }

    public String getDate() {
        return date_;
    }

    public int getProductId() {
        return productId_;
    }

    public String getAmount() {
        return String.valueOf(amount_);
    }

    public LOCATION getLocation() {
        return location_;
    }

    public String getPrice() {
        return Translation.getValidPrice(price_);
    }

    public int getId() {
        return purchaseId_;
    }

    public String getTotalPrice() {
        return Translation.getValidPrice(String.valueOf(Double.parseDouble(price_) * amount_));
    }
}
