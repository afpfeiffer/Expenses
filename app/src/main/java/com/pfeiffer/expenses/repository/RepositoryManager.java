package com.pfeiffer.expenses.repository;

import android.content.Context;
import android.database.SQLException;

import com.pfeiffer.expenses.model.Barcode;
import com.pfeiffer.expenses.model.CATEGORY;
import com.pfeiffer.expenses.model.LOCATION;
import com.pfeiffer.expenses.model.Product;
import com.pfeiffer.expenses.model.Purchase;

import java.util.Date;
import java.util.List;

public class RepositoryManager {
    private final RepositoryProduct repoProduct_;
    private final RepositoryPurchase repoPurchase_;

    public RepositoryManager(Context context) {
        ExpensesSQLiteHelper dbHelper = new ExpensesSQLiteHelper(context);

        repoProduct_ = new RepositoryProduct(dbHelper);
        repoPurchase_ = new RepositoryPurchase(dbHelper);
    }

    public void open() throws SQLException {
        repoProduct_.open();
        repoPurchase_.open();

        // WARNING: Activate this for testing only. This will dump the Database.
        // dbHelper_.onUpgrade( database_, 1, 1 );
    }

    public void close() {
        repoProduct_.close();
        repoPurchase_.close();
    }

    public Product findProductByBarcode(Barcode barcode) {
        return repoProduct_.findProduct(ExpensesSQLiteHelper.PRODUCT_BARCODE, barcode.toString());
    }

    public Product findProductById(int productId) {
        return repoProduct_.findProduct(ExpensesSQLiteHelper.PRODUCT_ID, String.valueOf(productId));
    }

    public List<Product> getAllProducts() {
        return repoProduct_.getAllProducts();
    }


    public boolean updateProduct(int id, String name, CATEGORY category, String price, Barcode barcode) {
        return repoProduct_.updateProduct(id, name, category, price, barcode);
    }

    public boolean updatePurchase(int purchaseId, String price, int amount, LOCATION location, boolean cash) {

        return repoPurchase_.updatePurchase(purchaseId, price, amount, location, cash);
    }

    public Purchase createPurchase(
            String name,
            CATEGORY category,
            String price,
            Barcode barcode,
            int amount,
            LOCATION location, boolean cash) {

        Product product = repoProduct_.updateOrCreateProduct(name, category, price, barcode);
        return repoPurchase_.createPurchase(product.getId(), price, amount, location, cash);
    }

    public List<Purchase> getAllPurchases() {
        return repoPurchase_.getAllPurchases();
    }

    public List<Purchase> getAllPurchasesForDateRange(Date minDate, Date maxDate) {
        return repoPurchase_.getAllPurchasesForDateRange(minDate, maxDate);
    }

    public Purchase findPurchaseByProductId(int productId) {
        return repoPurchase_.findLatestPurchase(ExpensesSQLiteHelper.PURCHASE_PRODUCT_ID, String.valueOf(productId));
    }

    public Purchase findPurchaseById( int purchaseId ){
        return repoPurchase_.findLatestPurchase(ExpensesSQLiteHelper.PURCHASE_ID, String.valueOf(purchaseId));
    }

    public int deletePurchase(int purchaseId) {
        return repoPurchase_.deletePurchase(purchaseId);
    }


    // only for low level use
    RepositoryProduct getRepositoryProduct(){
        return repoProduct_;
    }

    // only for low level use
    RepositoryPurchase getRepositoryPurchase(){
        return repoPurchase_;
    }

}
