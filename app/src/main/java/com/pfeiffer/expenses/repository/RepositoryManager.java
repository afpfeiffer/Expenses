package com.pfeiffer.expenses.repository;

import android.content.Context;
import android.database.SQLException;

import com.pfeiffer.expenses.model.Barcode;
import com.pfeiffer.expenses.model.CATEGORY;
import com.pfeiffer.expenses.model.LOCATION;
import com.pfeiffer.expenses.model.Product;
import com.pfeiffer.expenses.model.Purchase;

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

    public Product findProduct(Barcode barcode) {
        return repoProduct_.findProduct(barcode);
    }

    public Product findProduct(String name) {
        return repoProduct_.findProduct(name);
    }

    public Product findProduct(int productId) {
        return repoProduct_.findProduct(productId);
    }

    public boolean updateProduct(int id, String name, CATEGORY category, String price, String barcode) {
        return repoProduct_.updateProduct(id, name, category, price, barcode);
    }

    public boolean updatePurchase(int purchaseId, String price, int amount, LOCATION location, boolean cash) {

        return repoPurchase_.updatePurchase(purchaseId, price, amount, location, cash);
    }

    public Purchase createPurchase(
            String name,
            CATEGORY category,
            String price,
            String barcode,
            int amount,
            String date,
            LOCATION location, boolean cash) {

        Product product = repoProduct_.findOrCreateProduct(name, category, price, barcode);
        return repoPurchase_.createPurchase(product.getId(), name, category, price, barcode, amount, date, location,
                cash);
    }

    public List<Purchase> getAllPurchases() {
        return repoPurchase_.getAllPurchases();
    }

    public Purchase findPurchase(int purchaseId) {
        return repoPurchase_.findPurchase(purchaseId);
    }

    public int deletePurchase(int purchaseId) {
        return repoPurchase_.deletePurchase(purchaseId);
    }

}
