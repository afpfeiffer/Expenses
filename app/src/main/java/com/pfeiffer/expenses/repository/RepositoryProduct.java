package com.pfeiffer.expenses.repository;

import android.content.ContentValues;
import android.database.Cursor;
import android.util.Log;

import com.pfeiffer.expenses.model.Barcode;
import com.pfeiffer.expenses.model.CATEGORY;
import com.pfeiffer.expenses.model.Product;

import java.util.ArrayList;
import java.util.List;

class RepositoryProduct extends RepositoryBase {

    private final String logTag_=this.getClass().getName();

    private final String[] allProductColumns_ = {ExpensesSQLiteHelper.PRODUCT_ID, ExpensesSQLiteHelper.PRODUCT_NAME,
            ExpensesSQLiteHelper.PRODUCT_CATEGORY, ExpensesSQLiteHelper.PRODUCT_PRICE,
            ExpensesSQLiteHelper.PRODUCT_BARCODE};

    public RepositoryProduct(ExpensesSQLiteHelper dbHelper) {
        super(dbHelper);
    }

    private Product cursorToProduct(Cursor cursor) {
        // Auto-generated method stub
        Log.d(logTag_, "Enter method cursorToProduct().");
        Product ret = new Product(cursor.getInt(0), cursor.getString(1),
                CATEGORY.valueOf(cursor.getString(2)), cursor.getString(3), new Barcode(cursor.getString(4)));

        Log.d(logTag_, "Method cursorToProduct() returns value '" + ret + "'.");
        return ret;
    }

    private Product createProduct(String name, CATEGORY category, String price, Barcode barcode) {
        Log.d(this.getClass().getName(), "Enter method createProduct with arguments name=" + name + ", category="
                + category + ", price=" + price + ", barcode=" + barcode + ".");

        if (name == null || name.equals(""))
            throw new IllegalArgumentException("Name must be defined.");
        if (category == null || category.toString().equals(""))
            throw new IllegalArgumentException("Category must be defined.");
        if (price == null || price.equals(""))
            throw new IllegalArgumentException("Price must be defined.");

        ContentValues values = new ContentValues();
        values.put(ExpensesSQLiteHelper.PRODUCT_NAME, name);
        values.put(ExpensesSQLiteHelper.PRODUCT_CATEGORY, category.name());
        values.put(ExpensesSQLiteHelper.PRODUCT_PRICE, price);
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

    boolean updateProduct(int id, String name, CATEGORY category, String price, Barcode barcode) {
        Log.d(logTag_, "Enter method updateProduct with arguments id=" + id + ", name=" + name
                + ", category=" + category + ", price=" + price + ", barcode=" + barcode + ".");

        if (id < 0)
            throw new IllegalArgumentException();

        ContentValues values = new ContentValues();
        if (name != null && !name.equals(""))
            values.put(ExpensesSQLiteHelper.PRODUCT_NAME, name);
        if (category != null)
            values.put(ExpensesSQLiteHelper.PRODUCT_CATEGORY, category.name());
        if (price != null && !price.equals(""))
            values.put(ExpensesSQLiteHelper.PRODUCT_PRICE, price);
        if (barcode != null && !barcode.equals(""))
            values.put(ExpensesSQLiteHelper.PRODUCT_BARCODE, barcode.toString());

        Log.d(logTag_, "Update values "+values);

        int rowsAffected = database_.update(
                ExpensesSQLiteHelper.TABLE_PRODUCT,
                values,
                ExpensesSQLiteHelper.PRODUCT_ID + "=" + id,
                null);

        Log.d(logTag_, "Update affected "+rowsAffected+" row(s).");

        return (rowsAffected == 1);
    }

    Product updateOrCreateProduct(String name, CATEGORY category, String price, Barcode barcode) {
        Log.d(logTag_, "Enter method updateOrCreateProduct with arguments name=" + name
                + ", category=" + category + ", price=" + price + ", barcode=" + barcode + ".");

        if (name == null || name.equals(""))
            throw new IllegalArgumentException("Name must be defined.");
        if (category == null || category.toString().equals(""))
            throw new IllegalArgumentException("Category must be defined.");
        if (price == null || price.equals(""))
            throw new IllegalArgumentException("Price must be defined.");

        // if barcode is known, return the associated Product.
        if (barcode != null && !barcode.equals("")) {
            Product temporaryProduct = findProduct(ExpensesSQLiteHelper.PRODUCT_BARCODE, barcode.toString());
            // If the barcode is defined, we either have an existing product
            // with this barcode, or we need to create one.
            if (temporaryProduct != null) {
                Log.d(this.getClass().getName(), "Method updateOrCreateProduct() obtained the product with id "
                        + temporaryProduct.getId() + ".");
                updateProduct(temporaryProduct.getId(), name, category, price, barcode);
                // return product after db update
                return findProduct(ExpensesSQLiteHelper.PRODUCT_ID, String.valueOf(temporaryProduct.getId()));
            }
        }
        Product ret = createProduct(name, category, price, barcode);
        Log.d(logTag_, "Method updateOrCreateProduct() created a product with id " + ret.getId() +
                ".");
        return ret;
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
