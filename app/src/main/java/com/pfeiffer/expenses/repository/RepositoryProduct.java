package com.pfeiffer.expenses.repository;

import android.content.ContentValues;
import android.database.Cursor;
import android.util.Log;

import com.pfeiffer.expenses.model.Barcode;
import com.pfeiffer.expenses.model.Product;

import java.util.ArrayList;
import java.util.List;

class RepositoryProduct extends RepositoryBase {

    private final String logTag_ = this.getClass().getName();

    private final String[] allProductColumns_ = {ExpensesSQLiteHelper.PRODUCT_ID, ExpensesSQLiteHelper.PRODUCT_NAME,
            ExpensesSQLiteHelper.PRODUCT_BARCODE};

    public RepositoryProduct(ExpensesSQLiteHelper dbHelper) {
        super(dbHelper);
    }

    private Product cursorToProduct(Cursor cursor) {
        // Auto-generated method stub
        Log.d(logTag_, "Enter method cursorToProduct().");
        Product ret = new Product(cursor.getInt(0), cursor.getString(1), new Barcode(cursor.getString(2)));

        Log.d(logTag_, "Method cursorToProduct() returns value '" + ret + "'.");
        return ret;
    }

    Product createProduct(Product product) {
        String name=product.getName();
        Barcode barcode=product.getBarcode();
        Log.d(this.getClass().getName(), "Enter method createProduct with arguments name=" + name + ", " +
                "barcode=" + barcode + ".");

        if (name == null || name.equals(""))
            throw new IllegalArgumentException("Name must be defined.");

        ContentValues values = new ContentValues();
        values.put(ExpensesSQLiteHelper.PRODUCT_NAME, name);
        values.put(ExpensesSQLiteHelper.PRODUCT_BARCODE, barcode.toString());

        long insertId = database_.insert(ExpensesSQLiteHelper.TABLE_PRODUCT, null, values);

        Log.d(logTag_, "Database returns id '" + insertId + "'.");

        Cursor cursor = database_.query(
                ExpensesSQLiteHelper.TABLE_PRODUCT,
                allProductColumns_,
                ExpensesSQLiteHelper.PRODUCT_ID + " = " + insertId,
                null,
                null,
                null,
                null);
        cursor.moveToFirst();
        Product newProduct = cursorToProduct(cursor);
        cursor.close();

        return newProduct;
    }

    Product findProduct(String searchField, String searchValue) {
        Log.d(logTag_, "Enter method findProduct() with argument field=" + searchField + ", value="
                + searchValue + ".");

        Cursor cursor = database_.query(ExpensesSQLiteHelper.TABLE_PRODUCT, allProductColumns_, " " + searchField
                        + " = ?", new String[]{searchValue}, null, null, null, null
        );

        if (cursor != null && cursor.getCount() > 0) {
            cursor.moveToFirst();
            Product ret = cursorToProduct(cursor);
            Log.d(logTag_, "Method findProduct() returns value '" + ret + "'.");
            return ret;
        }

        Log.d(logTag_, "Method findProduct() could not retrieve any Product and returns 'null'.");
        return null;
    }

    public List<Product> getAllProducts() {
        // TODO log entry
        List<Product> products = new ArrayList<Product>();
        String orderBy = ExpensesSQLiteHelper.PRODUCT_ID + " DESC";

        Cursor cursor = database_.query(
                ExpensesSQLiteHelper.TABLE_PRODUCT,
                allProductColumns_,
                null,
                null,
                null,
                null,
                orderBy);

        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            Product product = cursorToProduct(cursor);
            products.add(product);
            cursor.moveToNext();
        }
        cursor.close();
        // TODO log exit
        return products;
    }

}
