package com.pfeiffer.expenses.model;

import java.io.Serializable;
import java.util.Date;


public class Purchase implements Serializable {
    private final boolean cash_;
    private long id_ = -1;
    private Barcode barcode_;
    private int amount_;
    private EnumLocation location_;
    private Money price_;
    private Date purchaseDate_;
    private String productName_;
    private EnumCategory category_;
    private String owner_;
    private long purchaseIdOwner_;

    public Purchase(long purchaseId, Barcode barcode, int amount, Date date, EnumLocation location, Money price,
                    boolean cash, String productName, EnumCategory category, String owner, long purchaseIdOwner) {
//        Log.d(this.getClass().getName(), "Enter Purchase constructor with arguments: purchaseId=" + purchaseId
//                + ", barcode=" + barcode + ", amount=" + amount + ", date=" + date + ", location=" + location
//                + ", price=" + price + ", cash=" + cash + ", productName=" + productName + ", " +
//                "category=" + category + ", owner=" + owner + ", idOwner=" + purchaseIdOwner + ".");

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
        purchaseIdOwner_ = purchaseIdOwner;
    }

    public void setCategory(EnumCategory category){
        category_=category;
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

    public EnumLocation getLocation() {
        return location_;
    }

    public Money getPrice() {
        return price_;
    }

    public long getId() {
        return id_;
    }

    public boolean isCash() {
        return cash_;
    }

    public String getProductName() {
        return productName_;
    }

    public EnumCategory getCategory() {
        return category_;
    }

    public String getOwner() {
        return owner_;
    }

    public long getPurchaseIdOwner() {
        return purchaseIdOwner_;
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
        if (category_.equals(EnumCategory.NONE)) return false;
        if (location_.equals(EnumLocation.NONE)) return false;

        return true;
    }

    public String toString() {
        return "Purchase(id=" + id_ + ", barcode=" +
                ((barcode_ == null) ? null : barcode_.toString()) + ", amount=" + amount_ +
                ", purchaseDate=" + purchaseDate_.toString() + ", location=" + location_ +
                ", price=" + price_.toString() + ", cash=" + cash_ + ", productName=" + productName_
                + ", category=" + category_ + ", owner=" + owner_ + ", purchaseIdOwner=" +
                purchaseIdOwner_ + ")";
    }
}
