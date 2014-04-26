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
    final private int amount_;
    final private EnumLocation location_;
    final private Money price_;
    final private String productName_;
    final private int numberOfPurchases_;
    final private Date lastPurchaseDate_;
    final private EnumCategory category_;

    public PurchaseTemplate(List<Purchase> purchaseList) {
        // TODO: refine algorithms!
        amount_ = purchaseList.get(0).getAmount();
        location_ = purchaseList.get(0).getLocation();
        price_ = purchaseList.get(0).getPrice();
        productName_ = purchaseList.get(0).getProductName();
        numberOfPurchases_ = purchaseList.size();
        lastPurchaseDate_ = purchaseList.get(numberOfPurchases_ - 1).getDate();
        category_=purchaseList.get(0).getCategory();
    }

    public PurchaseTemplate(int purchaseTemplateId, int amount, EnumLocation location,
                            Money price, String productName, int numberOfPurchases, Date lastPurchaseDate,
                            EnumCategory category) {
        Log.d(this.getClass().getName(), "Enter Purchase constructor with arguments: purchaseTemplateId=" + purchaseTemplateId
                + ", amount=" + amount + ", location=" + location
                + ", price=" + price + ", productName=" + productName + ", numberOfPurchases=" + numberOfPurchases +
                ", lastPurchaseDate=" + lastPurchaseDate + ", category="+category+".");

        // TODO check arguments, throw exceptions

        id_ = purchaseTemplateId;
        amount_ = amount;
        location_ = location;
        price_ = price;
        productName_ = productName;
        numberOfPurchases_ = numberOfPurchases;
        lastPurchaseDate_ = lastPurchaseDate;
        category_=category;
    }

    public int getAmount() {
        return amount_;
    }

    public EnumLocation getLocation() {
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

    public EnumCategory getCategory(){ return  category_;}

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