package com.pfeiffer.expenses.repository;

import android.content.ContentValues;
import android.database.Cursor;
import android.util.Log;

import com.pfeiffer.expenses.model.Barcode;
import com.pfeiffer.expenses.model.CATEGORY;
import com.pfeiffer.expenses.model.Product;
import com.pfeiffer.expenses.repository.ExpensesSQLiteHelper.QueryBuilder;

import java.util.Arrays;

class RepositoryProduct extends RepositoryBase {

    private final String[] allProductColumns_ = {ExpensesSQLiteHelper.PRODUCT_ID, ExpensesSQLiteHelper.PRODUCT_NAME,
            ExpensesSQLiteHelper.PRODUCT_CATEGORY, ExpensesSQLiteHelper.PRODUCT_PRICE,
            ExpensesSQLiteHelper.PRODUCT_BARCODE};

    public RepositoryProduct(ExpensesSQLiteHelper dbHelper) {
        super(dbHelper);
    }

    private Product cursorToProduct(Cursor cursor) {
        // Auto-generated method stub
        Log.d(this.getClass().getName(), "Enter method cursorToProduct().");
        Product ret = new Product(cursor.getInt(0), cursor.getString(1),
                CATEGORY.valueOf(cursor.getString(2)), cursor.getString(3), cursor.getString(4));

        Log.d(this.getClass().getName(), "Method cursorToProduct() returns value '" + ret + "'.");
        return ret;
    }

    Product findProduct(int id) {
        if (id <= 0)
            throw new IllegalArgumentException();

        return findProduct(id, null, null, null);
    }

    Product findProduct(Barcode barcode) {
        if (barcode == null || barcode.equals(""))
            throw new IllegalArgumentException();

        return findProduct(-1, null, null, barcode.toString());
    }

    Product findProduct(String name) {
        if (name == null || name.equals(""))
            throw new IllegalArgumentException();

        return findProduct(-1, name, null, null);
    }

    Product findProduct(int id, String name, String price, String barcode) {
        Log.d(this.getClass().getName(), "Enter method findProduct() with arguments id=" + id + ", name=" + name
                + ", price=" + price + ", barcode=" + barcode + ".");

        QueryBuilder qb = dbHelper_.new QueryBuilder();
        if (id > -1)
            qb.addWhere(ExpensesSQLiteHelper.PRODUCT_ID, Integer.toString(id));
        if (name != null && !name.equals(""))
            qb.addWhere(ExpensesSQLiteHelper.PRODUCT_NAME, name);
        if (price != null && !price.equals(""))
            qb.addWhere(ExpensesSQLiteHelper.PRODUCT_PRICE, price);
        if (barcode != null && !barcode.equals(""))
            qb.addWhere(ExpensesSQLiteHelper.PRODUCT_BARCODE, barcode);

        String whereString = qb.getWhere();
        String[] whereArgs = qb.getWhereArgs();
        Log.d(this.getClass().getName(), "Where clause: " + whereString + " with arguments: " + Arrays.toString(whereArgs));
        if (whereString == null || whereString.equals("") || whereString.equals(" "))
            throw new IllegalArgumentException("Methdod findProduct was called with illigeal arguments.");

        Cursor cursor = database_.query(
                ExpensesSQLiteHelper.TABLE_PRODUCT,
                allProductColumns_,
                whereString,
                whereArgs,
                null, // e. group by
                null, // f. having
                null, // g. order by
                null); // h. limit
        // 3. if we got results get the first one
        if (cursor != null && cursor.getCount() > 0) {

            cursor.moveToFirst();
            Product ret = cursorToProduct(cursor);
            Log.d(this.getClass().getName(), "Method findProduct() returns value '" + ret + "'.");
            return ret;
        }

        Log.d(this.getClass().getName(), "Method findProduct() could not retrieve any Product and returns 'null'.");
        return null;
    }

    private Product createProduct(String name, CATEGORY category, String price, String barcode) {
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
        values.put(ExpensesSQLiteHelper.PRODUCT_BARCODE, barcode);

        Log.d(this.getClass().getName(), "Create row in databse table " + ExpensesSQLiteHelper.TABLE_PRODUCT + ".");

        long insertId = database_.insert(ExpensesSQLiteHelper.TABLE_PRODUCT, null, values);

        Log.d(this.getClass().getName(), "Database returns id '" + insertId + "'.");

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

        Log.d(this.getClass().getName(), "Method createProduct() returns value '" + newProduct + "'.");
        return newProduct;
    }

    boolean updateProduct(int id, String name, CATEGORY category, String price, String barcode) {
        Log.d(this.getClass().getName(), "Enter method updateProduct with arguments id=" + id + ", name=" + name
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
            values.put(ExpensesSQLiteHelper.PRODUCT_BARCODE, barcode);

        int rowsAffected = database_.update(
                ExpensesSQLiteHelper.TABLE_PRODUCT,
                values,
                ExpensesSQLiteHelper.PRODUCT_ID + "=" + id,
                null);

        return (rowsAffected == 1);
    }

    Product findOrCreateProduct(String name, CATEGORY category, String price, String barcode) {
        Log.d(this.getClass().getName(), "Enter method findOrCreateProduct with arguments name=" + name
                + ", category=" + category + ", price=" + price + ", barcode=" + barcode + ".");

        if (name == null || name.equals(""))
            throw new IllegalArgumentException("Name must be defined.");
        if (category == null || category.toString().equals(""))
            throw new IllegalArgumentException("Category must be defined.");
        if (price == null || price.equals(""))
            throw new IllegalArgumentException("Price must be defined.");

        // if barcode is known, return the associated Product.
        if (barcode != null && !barcode.equals("")) {
            Product temporaryProduct = findProduct(-1, null, null, barcode);
            // If the barcode is defined, we either have an existing product
            // with this barcode, or we need to create one.
            if (temporaryProduct != null) {
                Log.d(this.getClass().getName(), "Method findOrCreateProduct() returns found Product "
                        + temporaryProduct + ".");
                updateProduct(temporaryProduct.getId(), name, category, price, barcode);
                return temporaryProduct;
            }
        } else {
            Product temporaryProduct = findProduct(-1, name, price, null);
            // if we don't have a barcode for our new product, we only want to
            // reuse an old one, if it has no barcode either (e.g. coffee 2 go).
            if (temporaryProduct != null && temporaryProduct.getBarcode() == null) {
                Log.d(this.getClass().getName(), "Method findOrCreateProduct() returns found Product "
                        + temporaryProduct + ".");
                return temporaryProduct;
            }
        }
        Product ret = createProduct(name, category, price, barcode);
        Log.d(this.getClass().getName(), "Method findOrCreateProduct() returns created Product " + ret + ".");
        return ret;
    }

}
