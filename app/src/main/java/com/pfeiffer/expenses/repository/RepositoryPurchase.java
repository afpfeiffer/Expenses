package com.pfeiffer.expenses.repository;

import android.content.ContentValues;
import android.database.Cursor;
import android.util.Log;

import com.pfeiffer.expenses.model.Barcode;
import com.pfeiffer.expenses.model.Category;
import com.pfeiffer.expenses.model.Location;
import com.pfeiffer.expenses.model.Money;
import com.pfeiffer.expenses.model.Purchase;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

class RepositoryPurchase extends RepositoryBase {
    private final String logTag_ = this.getClass().getName();


    private final String[] allPurchaseColumns_ = {ExpensesSQLiteHelper.PURCHASE_ID,
            ExpensesSQLiteHelper.PURCHASE_BARCODE, ExpensesSQLiteHelper.PURCHASE_AMOUNT,
            ExpensesSQLiteHelper.PURCHASE_DATE, ExpensesSQLiteHelper.PURCHASE_LOCATION,
            ExpensesSQLiteHelper.PURCHASE_PRICE, ExpensesSQLiteHelper.PURCHASE_CASH,
            ExpensesSQLiteHelper.PURCHASE_PRODUCT_NAME, ExpensesSQLiteHelper.PURCHASE_CATEGORY,
            ExpensesSQLiteHelper.PURCHASE_OWNER};

    public RepositoryPurchase(ExpensesSQLiteHelper dbHelper) {
        super(dbHelper);
    }


    private Purchase cursorToPurchase(Cursor cursor) {

        if (cursor.isAfterLast())
            throw new IllegalStateException("Database cursor out of bounds.");

        String barcodeString = cursor.getString(1);
        return new Purchase(cursor.getInt(0), (barcodeString==null)? null: new Barcode(barcodeString), cursor.getInt(2),
                new Date(Long.parseLong(cursor.getString(3))),
                Location.valueOf(cursor.getString(4)), new Money(cursor.getString(5)), (cursor.getInt(6)) == 1,
                cursor.getString(7), Category.valueOf(cursor.getString(8)),cursor.getString(9));
    }


    Purchase createPurchase(Purchase purchase) {
        Barcode barcode = purchase.getBarcode();
        Money price=purchase.getPrice();
        int amount=purchase.getAmount();
        Location location=purchase.getLocation();
        boolean cash=purchase.isCash();
        String productName=purchase.getProductName();
        Category category=purchase.getCategory();
        String owner=purchase.getOwner();

        Log.d(logTag_, "Enter method createPurchase with arguments barcode=" + barcode
                + ", price=" + price + ", amount=" + amount + ", location=" + location + ", cash=" + cash + ", " +
                "productName="+productName+", category="+category+", owner="+owner+".");

        if (amount <= 0)
            throw new IllegalArgumentException("Amount must be greater than 0.");
        if (location == null || location.toString().equals(""))
            throw new IllegalArgumentException("Location must be defined.");

        ContentValues values = new ContentValues();
        if( barcode != null )
            values.put(ExpensesSQLiteHelper.PURCHASE_BARCODE, barcode.toString());
        else
            values.put(ExpensesSQLiteHelper.PURCHASE_BARCODE, (String) null );

        values.put(ExpensesSQLiteHelper.PURCHASE_AMOUNT, amount);
        values.put(ExpensesSQLiteHelper.PURCHASE_DATE, System.currentTimeMillis());
        values.put(ExpensesSQLiteHelper.PURCHASE_LOCATION, location.name());
        values.put(ExpensesSQLiteHelper.PURCHASE_PRICE, price.getDataBaseRepresentation());
        values.put(ExpensesSQLiteHelper.PURCHASE_CASH, (cash) ? 1 : 0);
        values.put(ExpensesSQLiteHelper.PURCHASE_PRODUCT_NAME, productName);
        values.put(ExpensesSQLiteHelper.PURCHASE_CATEGORY, category.name());
        values.put(ExpensesSQLiteHelper.PURCHASE_OWNER, owner);

        long insertId = database_.insert(ExpensesSQLiteHelper.TABLE_PURCHASE, null, values);

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

        Log.d(logTag_, "Method createPurchase() returns value '" + newPurchase + "'.");
        return newPurchase;
    }


    boolean updatePurchase(Purchase purchase) {
        int purchaseId=purchase.getId();
        Money price=purchase.getPrice();
        int amount=purchase.getAmount();
        Location location=purchase.getLocation();
        boolean cash=purchase.isCash();
        String productName=purchase.getProductName();
        Category category=purchase.getCategory();

        Log.d(logTag_, "Enter method updatePurchase with arguments purchaseId=" + purchaseId
                + ", price=" + price + ", amount=" + amount + ", location=" + location + ", cash=" + cash + ", " +
                "productName="+productName+", category="+category+".");

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

        Log.d(logTag_, "Update values " + values);

        int rowsAffected = database_.update(
                ExpensesSQLiteHelper.TABLE_PURCHASE,
                values,
                ExpensesSQLiteHelper.PURCHASE_ID + "=" + purchaseId,
                null);

        Log.d(logTag_, "Update affected " + rowsAffected + " row(s).");


        return (rowsAffected == 1);
    }

    Purchase findLatestPurchase(String searchField, String searchValue) {
        List<Purchase> temp = findPurchases(searchField, searchValue);
        return (temp.size() > 0) ? temp.get(0) : null;
    }

    List<Purchase> findPurchases(String searchField, String searchValue) {
        Log.d(logTag_, "Enter method findPurchases() with argument field=" + searchField + ", value="
                + searchValue + ".");
        String orderBy = ExpensesSQLiteHelper.PURCHASE_DATE + " DESC";
        Cursor cursor;
        if (searchField != null && !searchField.equals(""))
            cursor = database_.query(ExpensesSQLiteHelper.TABLE_PURCHASE, allPurchaseColumns_, " " + searchField
                    + " = ?", new String[]{searchValue}, null, null, orderBy, null);
        else
            cursor = database_.query(ExpensesSQLiteHelper.TABLE_PURCHASE, allPurchaseColumns_, null, null, null, null, orderBy);

        List<Purchase> purchases = new ArrayList<Purchase>();
        cursor.moveToFirst();
        Log.d(logTag_, "Enter method findPurchases() log1=" + cursor.isAfterLast() );

        while (!cursor.isAfterLast()) {
            Purchase purchase = cursorToPurchase(cursor);
            purchases.add(purchase);
            cursor.moveToNext();
        }
        cursor.close();
        Log.d(logTag_, "Enter method findPurchases() found=" + purchases );

        return purchases;
    }


    List<Purchase> getAllPurchases() {
        return findPurchases(null, null);
    }

    List<Purchase> getAllPurchasesForDateRange(Date minDate, Date maxDate) {
        String orderBy = ExpensesSQLiteHelper.PURCHASE_DATE + " DESC";

        Cursor cursor = database_.query(ExpensesSQLiteHelper.TABLE_PURCHASE, null, ExpensesSQLiteHelper.PURCHASE_DATE +
                        " BETWEEN ? AND ?", new String[]{String.valueOf(minDate.getTime()),
                        String.valueOf(maxDate.getTime())},
                null, null,
                orderBy, null
        );


        List<Purchase> purchases = new ArrayList<Purchase>();
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            Purchase purchase = cursorToPurchase(cursor);
            purchases.add(purchase);
            cursor.moveToNext();
        }
        cursor.close();
        return purchases;
    }

    int deletePurchase(int purchaseId) {
        // TODO Auto-generated method stub
        Log.d(logTag_, "Enter method deletePurchase() with argument id=" + purchaseId + ".");

        return database_.delete(
                ExpensesSQLiteHelper.TABLE_PURCHASE,
                ExpensesSQLiteHelper.PURCHASE_ID + " = ?",
                new String[]{String.valueOf(purchaseId)});
    }
}
