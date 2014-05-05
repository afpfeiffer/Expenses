package com.pfeiffer.expenses.repository;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.util.Log;

import com.pfeiffer.expenses.model.Barcode;
import com.pfeiffer.expenses.model.EnumCategory;
import com.pfeiffer.expenses.model.EnumLocation;
import com.pfeiffer.expenses.model.Money;
import com.pfeiffer.expenses.model.Purchase;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

class RepositoryPurchase extends RepositoryBase {
    private final String logTag_ = this.getClass().getName();


    private final static String[] allPurchaseColumns_ = {ExpensesSQLiteHelper.PURCHASE_ID,
            ExpensesSQLiteHelper.PURCHASE_BARCODE, ExpensesSQLiteHelper.PURCHASE_AMOUNT,
            ExpensesSQLiteHelper.PURCHASE_DATE, ExpensesSQLiteHelper.PURCHASE_LOCATION,
            ExpensesSQLiteHelper.PURCHASE_PRICE, ExpensesSQLiteHelper.PURCHASE_CASH,
            ExpensesSQLiteHelper.PURCHASE_PRODUCT_NAME, ExpensesSQLiteHelper.PURCHASE_CATEGORY,
            ExpensesSQLiteHelper.PURCHASE_OWNER, ExpensesSQLiteHelper.PURCHASE_ID_OWNER};

    public RepositoryPurchase(Context context, ExpensesSQLiteHelper dbHelper) {
        super(context, dbHelper);
    }


    private Purchase cursorToPurchase(Cursor cursor) {

        if (cursor.isAfterLast())
            throw new IllegalStateException("Database cursor out of bounds.");

        String barcodeString = cursor.getString(1);
        return new Purchase(cursor.getLong(0), (barcodeString == null) ? null : new Barcode(barcodeString), cursor.getInt(2),
                new Date(Long.parseLong(cursor.getString(3))),
                EnumLocation.valueOf(cursor.getString(4)), new Money(cursor.getString(5)), (cursor.getInt(6)) == 1,
                cursor.getString(7), EnumCategory.valueOf(cursor.getString(8)), cursor.getString(9), cursor.getLong(10));
    }


    long savePurchase(Purchase purchase) {
        Log.d(logTag_, "savePurchase(" + purchase + ")");

        Barcode barcode = purchase.getBarcode();
        Money price = purchase.getPrice();
        int amount = purchase.getAmount();
        EnumLocation location = purchase.getLocation();
        boolean cash = purchase.isCash();
        String productName = purchase.getProductName();
        EnumCategory category = purchase.getCategory();
        String owner = purchase.getOwner();
        Long purchaseIdOwner = purchase.getPurchaseIdOwner();
        Date date = purchase.getDate();

        if (amount <= 0)
            throw new IllegalArgumentException("Amount must be greater than 0.");
        if (location == null || location.toString().equals(""))
            throw new IllegalArgumentException("Location must be defined.");

        ContentValues values = new ContentValues();
        if (barcode != null)
            values.put(ExpensesSQLiteHelper.PURCHASE_BARCODE, barcode.toString());
        else
            values.put(ExpensesSQLiteHelper.PURCHASE_BARCODE, (String) null);

        values.put(ExpensesSQLiteHelper.PURCHASE_AMOUNT, amount);
        values.put(ExpensesSQLiteHelper.PURCHASE_DATE, date.getTime());
        values.put(ExpensesSQLiteHelper.PURCHASE_LOCATION, location.name());
        values.put(ExpensesSQLiteHelper.PURCHASE_PRICE, price.getDataBaseRepresentation());
        values.put(ExpensesSQLiteHelper.PURCHASE_CASH, (cash) ? 1 : 0);
        values.put(ExpensesSQLiteHelper.PURCHASE_PRODUCT_NAME, productName);
        values.put(ExpensesSQLiteHelper.PURCHASE_CATEGORY, category.name());
        values.put(ExpensesSQLiteHelper.PURCHASE_OWNER, owner);
        values.put(ExpensesSQLiteHelper.PURCHASE_ID_OWNER, purchaseIdOwner);

        long purchaseId = database_.insert(ExpensesSQLiteHelper.TABLE_PURCHASE, null, values);

        if (purchaseIdOwner < 0 && owner.equals(deviceOwner_)) {
            values.clear();
            values.put(ExpensesSQLiteHelper.PURCHASE_ID_OWNER, purchaseId);
            database_.update(ExpensesSQLiteHelper.TABLE_PURCHASE, values,
                    ExpensesSQLiteHelper.PURCHASE_ID + "=" + purchaseId,
                    null);
        }

        return purchaseId;
    }


    boolean updatePurchase(Purchase purchase) {
        long purchaseId = purchase.getId();
        Money price = purchase.getPrice();
        int amount = purchase.getAmount();
        EnumLocation location = purchase.getLocation();
        boolean cash = purchase.isCash();
        String productName = purchase.getProductName();
        EnumCategory category = purchase.getCategory();

        Log.d(logTag_, "updatePurchase(" + purchase + ")");

        if (purchaseId <= 0)
            throw new IllegalArgumentException("Value productId is infalid (<=0).");
        if (amount <= 0)
            throw new IllegalArgumentException("Amount must be greater than 0.");
        if (location == null || location.toString().equals(""))
            throw new IllegalArgumentException("Location must be defined.");

        ContentValues values = new ContentValues();
        values.put(ExpensesSQLiteHelper.PURCHASE_AMOUNT, amount);
        values.put(ExpensesSQLiteHelper.PURCHASE_LOCATION, location.name());
        values.put(ExpensesSQLiteHelper.PURCHASE_PRICE, price.getDataBaseRepresentation());
        values.put(ExpensesSQLiteHelper.PURCHASE_CASH, (cash) ? 1 : 0);
        values.put(ExpensesSQLiteHelper.PURCHASE_PRODUCT_NAME, productName);
        values.put(ExpensesSQLiteHelper.PURCHASE_CATEGORY, category.name());

        int rowsAffected = database_.update(
                ExpensesSQLiteHelper.TABLE_PURCHASE,
                values,
                ExpensesSQLiteHelper.PURCHASE_ID + "=" + purchaseId,
                null);

        boolean oneRowUpdated = rowsAffected == 1;
        Log.d(logTag_, "updatePurchase returns " + oneRowUpdated);
        return (rowsAffected == 1);
    }

    Purchase findLatestPurchase(String searchField, String searchValue) {
        List<Purchase> temp = findPurchases(searchField, searchValue);
        return (temp.size() > 0) ? temp.get(0) : null;
    }

    List<Purchase> findPurchases(String searchField, String searchValue) {
        Log.d(logTag_, "findPurchases(searchField=" + searchField + ", searchValue="
                + searchValue + ")");
        String orderBy = ExpensesSQLiteHelper.PURCHASE_DATE + " DESC";
        Cursor cursor;
        if (searchField != null && !searchField.equals(""))
            cursor = database_.query(ExpensesSQLiteHelper.TABLE_PURCHASE, allPurchaseColumns_, " " + searchField
                    + " = ?", new String[]{searchValue}, null, null, orderBy, null);
        else
            cursor = database_.query(ExpensesSQLiteHelper.TABLE_PURCHASE, allPurchaseColumns_, null, null, null, null, orderBy);

        List<Purchase> purchases = new ArrayList<Purchase>();
        cursor.moveToFirst();

        while (!cursor.isAfterLast()) {
            Purchase purchase = cursorToPurchase(cursor);
            purchases.add(purchase);
            cursor.moveToNext();
        }
        cursor.close();

        Log.d(logTag_, "findPurchases returns " + purchases);
        return purchases;
    }


    List<Purchase> getAllPurchases() {
        return findPurchases(null, null);
    }

    List<Purchase> getAllPurchasesForDateRange(Date minDate, Date maxDate) {
        Log.d(logTag_, "getAllPurchasesForDateRange( minDate=" + minDate.toString() + ", maxDate="
                + maxDate.toString());

        String orderBy = ExpensesSQLiteHelper.PURCHASE_DATE + " DESC";

        Cursor cursor = database_.query(ExpensesSQLiteHelper.TABLE_PURCHASE, null,
                ExpensesSQLiteHelper.PURCHASE_DATE + " BETWEEN ? AND ?",
                new String[]{String.valueOf(minDate.getTime()), String.valueOf(maxDate.getTime())},
                null, null, orderBy, null);


        List<Purchase> purchases = new ArrayList<Purchase>();
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            Purchase purchase = cursorToPurchase(cursor);
            purchases.add(purchase);
            cursor.moveToNext();
        }
        cursor.close();

        Log.d(logTag_, "getAllPurchasesForDateRange returns " + purchases);
        return purchases;
    }

    int deletePurchase(long purchaseId) {
        Log.d(logTag_, "deletePurchase(" + purchaseId + ")");

        int numberOfRowsDeleted = database_.delete(
                ExpensesSQLiteHelper.TABLE_PURCHASE,
                ExpensesSQLiteHelper.PURCHASE_ID + " = ?",
                new String[]{String.valueOf(purchaseId)});
        Log.d(logTag_, "deletePurchase returns " + numberOfRowsDeleted);
        return numberOfRowsDeleted;
    }
}
