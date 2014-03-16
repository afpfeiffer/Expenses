package com.pfeiffer.expenses;

import android.content.ContentValues;
import android.database.Cursor;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

class RepositoryPurchase extends RepositoryBase {
    private final String[] allPurchaseColumns_ = {ExpensesSQLiteHelper.PURCHASE_ID,
            ExpensesSQLiteHelper.PURCHASE_PRODUCT_ID, ExpensesSQLiteHelper.PURCHASE_AMOUNT,
            ExpensesSQLiteHelper.PURCHASE_DATE, ExpensesSQLiteHelper.PURCHASE_LOCATION,
            ExpensesSQLiteHelper.PURCHASE_PRICE};

    public RepositoryPurchase(ExpensesSQLiteHelper dbHelper) {
        super(dbHelper);
    }

    boolean updatePurchase(int purchaseId, String price, int amount, LOCATION location) {
        Log.d(this.getClass().getName(), "Enter method updatePurchase with arguments purchaseId=" + purchaseId
                + ", price=" + price + ", amount=" + amount + ", location=" + location + ".");

        if (purchaseId <= 0)
            throw new IllegalArgumentException("Value productId is infalid (<=0).");
        if (amount <= 0)
            throw new IllegalArgumentException("Amount must be greater than 0.");
        if (location == null || location.toString().equals(""))
            throw new IllegalArgumentException("Location must be defined.");

        ContentValues values = new ContentValues();
        values.put(ExpensesSQLiteHelper.PURCHASE_AMOUNT, amount);
        values.put(ExpensesSQLiteHelper.PURCHASE_LOCATION, location.name());
        values.put(ExpensesSQLiteHelper.PURCHASE_PRICE, String.valueOf(price));

        int rowsAffected = database_.update(
                ExpensesSQLiteHelper.TABLE_PURCHASE,
                values,
                ExpensesSQLiteHelper.PURCHASE_ID + "=" + purchaseId,
                null);

        return (rowsAffected == 1);
    }

    Purchase createPurchase(
            int productId,
            String name,
            CATEGORY category,
            String price,
            String barcode,
            int amount,
            String date,
            LOCATION location) {
        Log.d(this.getClass().getName(), "Enter method createPurchase with arguments productId=" + productId
                + ", name=" + name + ", category=" + category + ", price=" + price + ", barcode=" + barcode
                + ", amount=" + amount + ", date=" + date + ", location=" + location + ".");

        if (productId <= 0)
            throw new IllegalArgumentException("Value productId is infalid (<=0).");
        if (amount <= 0)
            throw new IllegalArgumentException("Amount must be greater than 0.");
        if (date == null || date.equals(""))
            throw new IllegalArgumentException("Date must be defined.");
        if (location == null || location.toString().equals(""))
            throw new IllegalArgumentException("Location must be defined.");

        ContentValues values = new ContentValues();
        values.put(ExpensesSQLiteHelper.PURCHASE_PRODUCT_ID, productId);
        values.put(ExpensesSQLiteHelper.PURCHASE_AMOUNT, amount);
        values.put(ExpensesSQLiteHelper.PURCHASE_DATE, date);
        values.put(ExpensesSQLiteHelper.PURCHASE_LOCATION, location.name());
        values.put(ExpensesSQLiteHelper.PURCHASE_PRICE, String.valueOf(price));

        Log.d(this.getClass().getName(), "Create row in databse table " + ExpensesSQLiteHelper.TABLE_PURCHASE + ".");

        long insertId = database_.insert(ExpensesSQLiteHelper.TABLE_PURCHASE, null, values);

        Log.d(this.getClass().getName(), "Database returns id '" + insertId + "'.");

        Cursor cursor = database_.query(
                ExpensesSQLiteHelper.TABLE_PURCHASE,
                allPurchaseColumns_,
                ExpensesSQLiteHelper.PURCHASE_ID + " = " + insertId,
                null,
                null,
                null,
                null);

        cursor.moveToFirst();
        Purchase newPurchase = cursorToPurchase(cursor);
        cursor.close();

        Log.d(this.getClass().getName(), "Method createPurchase() returns value '" + newPurchase + "'.");
        return newPurchase;
    }

    private Purchase cursorToPurchase(Cursor cursor) {
        Log.d(this.getClass().getName(), "Enter method cursorToPurchase().");

        if (cursor.isAfterLast())
            throw new IllegalStateException("Database cursor out of bounds.");

        return new Purchase(cursor.getInt(0), cursor.getInt(1), cursor.getInt(2), cursor.getString(3),
                LOCATION.valueOf(cursor.getString(4)), cursor.getString(5));
    }

    List<Purchase> getAllPurchases() {
        // TODO log entry
        List<Purchase> purchases = new ArrayList<Purchase>();
        String orderBy = ExpensesSQLiteHelper.PURCHASE_ID + " DESC";

        Cursor cursor = database_.query(
                ExpensesSQLiteHelper.TABLE_PURCHASE,
                allPurchaseColumns_,
                null,
                null,
                null,
                null,
                orderBy);

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

    Purchase findPurchase(int purcahseId) {
        return findPurchase(ExpensesSQLiteHelper.PURCHASE_ID, String.valueOf(purcahseId));
    }

    private Purchase findPurchase(String searchField, String searchValue) {
        Log.d(this.getClass().getName(), "Enter method findPurchase() with argument field=" + searchField + ", value="
                + searchValue + ".");
        String orderBy = ExpensesSQLiteHelper.PURCHASE_DATE + " DESC";
        Cursor cursor = database_.query(ExpensesSQLiteHelper.TABLE_PURCHASE, allPurchaseColumns_, " " + searchField
                + " = ?", new String[]{searchValue}, null, // e. group by
                null, // f. having
                orderBy, // g. order by
                null); // h. limit
        // 3. if we got results get the first one
        if (cursor != null && cursor.getCount() > 0) {

            cursor.moveToFirst();
            Purchase ret = cursorToPurchase(cursor);
            Log.d(this.getClass().getName(), "Method findPurchase() returns value '" + ret + "'.");
            cursor.close();
            return ret;
        }
        assert cursor != null;
        cursor.close();
        Log.d(
                this.getClass().getName(),
                "Method findPurchase() could not retrieve the according Purchase and returns 'null'.");

        return null;
    }

    int deletePurchase(int purchaseId) {
        // TODO Auto-generated method stub
        Log.d(this.getClass().getName(), "Enter method deletePurchase() with argument id=" + purchaseId + ".");

        return database_.delete(
                ExpensesSQLiteHelper.TABLE_PURCHASE,
                ExpensesSQLiteHelper.PURCHASE_ID + " = ?",
                new String[]{String.valueOf(purchaseId)});
    }
}
