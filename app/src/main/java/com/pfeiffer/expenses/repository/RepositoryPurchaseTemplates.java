package com.pfeiffer.expenses.repository;

import android.content.ContentValues;
import android.database.Cursor;
import android.util.Log;

import com.pfeiffer.expenses.model.LOCATION;
import com.pfeiffer.expenses.model.Purchase;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by axelpfeiffer on 26.03.14.
 */
public class RepositoryPurchaseTemplates extends RepositoryBase {

    private final String logTag_=this.getClass().getName();

    private final String[] allPurchaseTemplateColumns_ = {ExpensesSQLiteHelper.PURCHASE_ID,
            ExpensesSQLiteHelper.PURCHASE_PRODUCT_ID, ExpensesSQLiteHelper.PURCHASE_AMOUNT,
            ExpensesSQLiteHelper.PURCHASE_LOCATION, ExpensesSQLiteHelper.PURCHASE_PRICE};

    public RepositoryPurchaseTemplates(ExpensesSQLiteHelper dbHelper) {
        super(dbHelper);
    }

    void createPurchaseTemplate(
            int productId,
            String price,
            int amount,
            LOCATION location) {
        Log.d(logTag_, "Enter method createPurchaseTemplate with arguments productId=" + productId
                + ", price=" + price + ", amount=" + amount + ", location=" + location + ".");

        if (productId <= 0)
            throw new IllegalArgumentException("Value productId is infalid (<=0).");
        if (amount <= 0)
            throw new IllegalArgumentException("Amount must be greater than 0.");
        if (location == null || location.toString().equals(""))
            throw new IllegalArgumentException("Location must be defined.");

        ContentValues values = new ContentValues();
        values.put(ExpensesSQLiteHelper.PURCHASE_TEMPLATES_PRODUCT_ID, productId);
        values.put(ExpensesSQLiteHelper.PURCHASE_TEMPLATES_AMOUNT, amount);
        values.put(ExpensesSQLiteHelper.PURCHASE_TEMPLATES_LOCATION, location.name());
        values.put(ExpensesSQLiteHelper.PURCHASE_TEMPLATES_PRICE, String.valueOf(price));

        database_.insert(ExpensesSQLiteHelper.TABLE_PURCHASE_TEMPLATES, null, values);
    }

    private Purchase cursorToPurchase(Cursor cursor) {
        if (cursor.isAfterLast())
            throw new IllegalStateException("Database cursor out of bounds.");

        return new Purchase(-1, cursor.getInt(1), cursor.getInt(2), null,
                LOCATION.valueOf(cursor.getString(3)), cursor.getString(4), false);
    }

    List<Purchase> getAllPurchaseTemplates() {
        // TODO log entry
        List<Purchase> purchases = new ArrayList<Purchase>();

        Cursor cursor = database_.query(
                ExpensesSQLiteHelper.TABLE_PURCHASE,
                allPurchaseTemplateColumns_,
                null,
                null,
                null,
                null,
                null);

        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            Purchase purchase = cursorToPurchase(cursor);
            purchases.add(purchase);
            cursor.moveToNext();
        }
        cursor.close();
        // TODO log exit
        return purchases;
    }

    void deleteAllPurchaseTemplates() {

        database_.execSQL("DELETE FROM " + ExpensesSQLiteHelper.TABLE_PURCHASE_TEMPLATES);

    }
}
