package com.pfeiffer.expenses.model;

import android.util.Log;

import java.util.Comparator;
import java.util.Date;
import java.util.List;

/**
 * Created by axelpfeiffer on 19.04.14.
 */
public class PurchaseTemplate {
    private int id_;
    final private int productId_;
    final private int amount_;
    final private LOCATION location_;
    final private Money price_;

    final private String productName_;
    final private int numberOfPurchases_;
    final private Date lastPurchaseDate_;

    public PurchaseTemplate(Product product, List<Purchase> purchaseList) {
        productId_ = product.getId();

        // TODO: refine algorithms!
        amount_ = purchaseList.get(0).getAmount();
        location_ = purchaseList.get(0).getLocation();
        price_ = purchaseList.get(0).getPrice();
        productName_ = product.getName();
        numberOfPurchases_ = purchaseList.size();
        lastPurchaseDate_ = purchaseList.get(numberOfPurchases_ - 1).getDate();

    }

    public PurchaseTemplate(int purchaseTemplateId, int productId, int amount, LOCATION location,
                            Money price, String productName, int numberOfPurchases, Date lastPurchaseDate) {
        Log.d(this.getClass().getName(), "Enter Purchase constructor with arguments: purchaseTemplateId=" + purchaseTemplateId
                + ", productId=" + productId + ", amount=" + amount + ", location=" + location
                + ", price=" + price + ", productName=" + productName + ", numberOfPurchses=" + numberOfPurchases + ", " +
                "lastPurchaseDate=" + lastPurchaseDate + ".");

        // TODO check arguments, throw exceptions

        id_ = purchaseTemplateId;
        productId_ = productId;
        amount_ = amount;
        location_ = location;
        price_ = price;
        productName_ = productName;
        numberOfPurchases_ = numberOfPurchases;
        lastPurchaseDate_ = lastPurchaseDate;
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

    public Money getPrice() {
        return price_;
    }

    public int getId() {
        return id_;
    }

    public String getProductName() {
        return productName_;
    }

    public int getNumberOfPurchases() {
        return numberOfPurchases_;
    }

    public Date getLastPurchaseDate() {
        return lastPurchaseDate_;
    }

    public Money getTotalPrice() {
        return price_.getScaled(amount_);
    }

    public String getTotalHumanReadablePrice(){
        return price_.getHumanReadableRepresentation(amount_);
    }

    public class PurchaseTemplateComparator implements Comparator<PurchaseTemplate> {
        @Override
        public int compare(PurchaseTemplate o1, PurchaseTemplate o2) {
            return new Integer(o1.getNumberOfPurchases()).compareTo(new Integer(o2.getNumberOfPurchases()));
        }
    }
}