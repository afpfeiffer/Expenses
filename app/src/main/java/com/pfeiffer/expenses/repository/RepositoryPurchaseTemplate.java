package com.pfeiffer.expenses.repository;

import android.content.ContentValues;
import android.database.Cursor;
import android.util.Log;

import com.pfeiffer.expenses.model.EnumCategory;
import com.pfeiffer.expenses.model.EnumLocation;
import com.pfeiffer.expenses.model.Money;
import com.pfeiffer.expenses.model.PurchaseTemplate;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by axelpfeiffer on 26.03.14.
 */
public class RepositoryPurchaseTemplate extends RepositoryBase {

    private final String logTag_ = this.getClass().getName();

    private final String[] allPurchaseTemplateColumns_ = {ExpensesSQLiteHelper.PURCHASE_TEMPLATE_ID,
            ExpensesSQLiteHelper.PURCHASE_TEMPLATE_AMOUNT, ExpensesSQLiteHelper.PURCHASE_TEMPLATE_LOCATION,
            ExpensesSQLiteHelper.PURCHASE_TEMPLATE_PRICE, ExpensesSQLiteHelper.PURCHASE_TEMPLATE_PRODUCT_NAME,
            ExpensesSQLiteHelper.PURCHASE_TEMPLATE_NUMBER_OF_PURCHASES,
            ExpensesSQLiteHelper.PURCHASE_TEMPLATE_LAST_PURCHASE_DATE, ExpensesSQLiteHelper.PURCHASE_TEMPLATE_CATEGORY};

    public RepositoryPurchaseTemplate(ExpensesSQLiteHelper dbHelper) {
        super(dbHelper);
    }

    long savePurchaseTemplate(PurchaseTemplate purchaseTemplate) {
       Log.d(logTag_, "savePurchaseTemplate("+purchaseTemplate+")");

        Money price = purchaseTemplate.getPrice();
        int amount = purchaseTemplate.getAmount();
        EnumLocation location = purchaseTemplate.getLocation();
        String productName = purchaseTemplate.getProductName();
        int numberOfPurchases = purchaseTemplate.getNumberOfPurchases();
        Date lastPurchaseDate = purchaseTemplate.getLastPurchaseDate();
        EnumCategory category = purchaseTemplate.getCategory();

        if (amount <= 0)
            throw new IllegalArgumentException("Amount must be greater than 0.");
        if (location == null || location.toString().equals(""))
            throw new IllegalArgumentException("Location must be defined.");

        ContentValues values = new ContentValues();
        values.put(ExpensesSQLiteHelper.PURCHASE_TEMPLATE_AMOUNT, amount);
        values.put(ExpensesSQLiteHelper.PURCHASE_TEMPLATE_LOCATION, location.name());
        values.put(ExpensesSQLiteHelper.PURCHASE_TEMPLATE_PRICE, price.getDataBaseRepresentation());
        values.put(ExpensesSQLiteHelper.PURCHASE_TEMPLATE_PRODUCT_NAME, productName);
        values.put(ExpensesSQLiteHelper.PURCHASE_TEMPLATE_NUMBER_OF_PURCHASES, numberOfPurchases);
        values.put(ExpensesSQLiteHelper.PURCHASE_TEMPLATE_LAST_PURCHASE_DATE, lastPurchaseDate.getTime());
        values.put(ExpensesSQLiteHelper.PURCHASE_TEMPLATE_CATEGORY, category.name());

        Log.d(logTag_, values.toString());

        return database_.insert(ExpensesSQLiteHelper.TABLE_PURCHASE_TEMPLATE, null, values);
    }

    private PurchaseTemplate cursorToPurchaseTemplate(Cursor cursor) {
        if (cursor.isAfterLast())
            throw new IllegalStateException("Database cursor out of bounds.");

        return new PurchaseTemplate(cursor.getInt(0), cursor.getInt(1),
                EnumLocation.valueOf(cursor.getString(2)), new Money(cursor.getString(3)), cursor.getString(4),
                cursor.getInt(5), new Date(Long.parseLong(cursor.getString(6))), EnumCategory.valueOf(cursor.getString(7)));
    }

    List<PurchaseTemplate> getAllPurchaseTemplates() {
        Log.d(logTag_, "getAllPurchaseTemplates()");
        List<PurchaseTemplate> purchaseTemplates = new ArrayList<PurchaseTemplate>();

        String orderBy = ExpensesSQLiteHelper.PURCHASE_TEMPLATE_NUMBER_OF_PURCHASES + " DESC";


        Cursor cursor = database_.query(
                ExpensesSQLiteHelper.TABLE_PURCHASE_TEMPLATE,
                allPurchaseTemplateColumns_,
                null,
                null,
                null,
                null,
                orderBy);

        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            PurchaseTemplate purchaseTemplate = cursorToPurchaseTemplate(cursor);
            purchaseTemplates.add(purchaseTemplate);
            cursor.moveToNext();
        }
        cursor.close();

        Log.d(logTag_, "getAllPurchaseTemplates returns "+purchaseTemplates);
        return purchaseTemplates;
    }

    void deleteAllPurchaseTemplates() {

        database_.execSQL("DELETE FROM " + ExpensesSQLiteHelper.TABLE_PURCHASE_TEMPLATE);

    }
}
