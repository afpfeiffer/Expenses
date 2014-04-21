package com.pfeiffer.expenses.repository;

import android.content.ContentValues;
import android.database.Cursor;
import android.util.Log;

import com.pfeiffer.expenses.model.LOCATION;
import com.pfeiffer.expenses.model.Money;
import com.pfeiffer.expenses.model.PurchaseTemplate;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by axelpfeiffer on 26.03.14.
 */
public class RepositoryPurchaseTemplate extends RepositoryBase {

    private final String logTag_=this.getClass().getName();

    private final String[] allPurchaseTemplateColumns_ = {ExpensesSQLiteHelper.PURCHASE_TEMPLATE_ID,
            ExpensesSQLiteHelper.PURCHASE_TEMPLATE_PRODUCT_ID, ExpensesSQLiteHelper.PURCHASE_TEMPLATE_AMOUNT,
            ExpensesSQLiteHelper.PURCHASE_TEMPLATE_LOCATION, ExpensesSQLiteHelper.PURCHASE_TEMPLATE_PRICE,
            ExpensesSQLiteHelper.PURCHASE_TEMPLATE_PRODUCT_NAME,
            ExpensesSQLiteHelper.PURCHASE_TEMPLATE_NUMBER_OF_PURCHASES, ExpensesSQLiteHelper.PURCHASE_TEMPLATE_LAST_PURCHASE_DATE};

    public RepositoryPurchaseTemplate(ExpensesSQLiteHelper dbHelper) {
        super(dbHelper);
    }

    void createPurchaseTemplate(PurchaseTemplate purchaseTemplate) {

        int productId=purchaseTemplate.getProductId();
        Money price=purchaseTemplate.getPrice();
        int amount=purchaseTemplate.getAmount();
        LOCATION location=purchaseTemplate.getLocation();
        String productName=purchaseTemplate.getProductName();
        int numberOfPurchases=purchaseTemplate.getNumberOfPurchases();
        Date lastPurchaseDate=purchaseTemplate.getLastPurchaseDate();

        Log.d(logTag_, "Enter method createPurchaseTemplate with arguments productId=" + productId
                + ", price=" + price + ", amount=" + amount + ", location=" + location + ", " +
                "productName="+productName+", numberOfPurchases="+numberOfPurchases+", " +
                "lastPurchaseDate="+lastPurchaseDate+".");

        if (productId <= 0)
            throw new IllegalArgumentException("Value productId is infalid (<=0).");
        if (amount <= 0)
            throw new IllegalArgumentException("Amount must be greater than 0.");
        if (location == null || location.toString().equals(""))
            throw new IllegalArgumentException("Location must be defined.");

        ContentValues values = new ContentValues();
        values.put(ExpensesSQLiteHelper.PURCHASE_TEMPLATE_PRODUCT_ID, productId);
        values.put(ExpensesSQLiteHelper.PURCHASE_TEMPLATE_AMOUNT, amount);
        values.put(ExpensesSQLiteHelper.PURCHASE_TEMPLATE_LOCATION, location.name());
        values.put(ExpensesSQLiteHelper.PURCHASE_TEMPLATE_PRICE, String.valueOf(price));
        values.put(ExpensesSQLiteHelper.PURCHASE_TEMPLATE_PRODUCT_NAME, productName);
        values.put(ExpensesSQLiteHelper.PURCHASE_TEMPLATE_NUMBER_OF_PURCHASES, numberOfPurchases);
        values.put(ExpensesSQLiteHelper.PURCHASE_TEMPLATE_LAST_PURCHASE_DATE, lastPurchaseDate.getTime());

        database_.insert(ExpensesSQLiteHelper.TABLE_PURCHASE_TEMPLATE, null, values);
    }

    private PurchaseTemplate cursorToPurchaseTemplate(Cursor cursor) {
        if (cursor.isAfterLast())
            throw new IllegalStateException("Database cursor out of bounds.");

        return new PurchaseTemplate(cursor.getInt(0), cursor.getInt(1), cursor.getInt(2),
                LOCATION.valueOf(cursor.getString(3)), new Money(cursor.getString(4)), cursor.getString(5),
                cursor.getInt(6),
                new Date( Long.parseLong(cursor.getString(7)) ));
    }

    List<PurchaseTemplate> getAllPurchaseTemplates() {
        // TODO log entry
        List<PurchaseTemplate> purchaseTemplates = new ArrayList<PurchaseTemplate>();

        Cursor cursor = database_.query(
                ExpensesSQLiteHelper.TABLE_PURCHASE_TEMPLATE,
                allPurchaseTemplateColumns_,
                null,
                null,
                null,
                null,
                null);

        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            PurchaseTemplate purchaseTemplate = cursorToPurchaseTemplate(cursor);
            purchaseTemplates.add(purchaseTemplate);
            cursor.moveToNext();
        }
        cursor.close();
        // TODO log exit
        return purchaseTemplates;
    }

    void deleteAllPurchaseTemplates() {

        database_.execSQL("DELETE FROM " + ExpensesSQLiteHelper.TABLE_PURCHASE_TEMPLATE);

    }
}
