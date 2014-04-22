package com.pfeiffer.expenses.repository;

import android.content.Context;
import android.database.SQLException;

import com.pfeiffer.expenses.model.Barcode;
import com.pfeiffer.expenses.model.Product;
import com.pfeiffer.expenses.model.Purchase;
import com.pfeiffer.expenses.model.PurchaseTemplate;

import java.util.Date;
import java.util.List;

public class RepositoryManager {
    private final RepositoryProduct repoProduct_;
    private final RepositoryPurchase repoPurchase_;
    private final RepositoryPurchaseTemplate repoPurchaseTemplate_;

    public RepositoryManager(Context context) {
        ExpensesSQLiteHelper dbHelper = new ExpensesSQLiteHelper(context);

        repoProduct_ = new RepositoryProduct(dbHelper);
        repoPurchase_ = new RepositoryPurchase(dbHelper);
        repoPurchaseTemplate_ = new RepositoryPurchaseTemplate(dbHelper);
    }

    public void open() throws SQLException {
        repoProduct_.open();
        repoPurchase_.open();
        repoPurchaseTemplate_.open();
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

    public boolean updatePurchase(Purchase purchase) {
        return repoPurchase_.updatePurchase(purchase);
    }

    public Purchase createPurchaseAndProduct(Purchase purchase, Barcode barcode) {

        if (!purchase.hasValidState()) throw new IllegalStateException();

        if (!barcode.isEmpty()) {
            Product product = repoProduct_.findProduct(ExpensesSQLiteHelper.PRODUCT_BARCODE, barcode.toString());
            if (product == null) {
                product = repoProduct_.createProduct(new Product(-1, purchase.getProductName(), barcode));
            }
            purchase.setProductId(product.getId());
        }

        return repoPurchase_.createPurchase(purchase);
    }

    public List<Purchase> getAllPurchases() {
        return repoPurchase_.getAllPurchases();
    }

    public List<Purchase> getAllPurchasesForDateRange(Date minDate, Date maxDate) {
        return repoPurchase_.getAllPurchasesForDateRange(minDate, maxDate);
    }

    public Purchase findLatestPurchase(Product product) {
        return repoPurchase_.findLatestPurchase(ExpensesSQLiteHelper.PURCHASE_PRODUCT_ID, String.valueOf(product.getId()));
    }

    public Purchase findPurchaseById(int purchaseId) {
        return repoPurchase_.findLatestPurchase(ExpensesSQLiteHelper.PURCHASE_ID, String.valueOf(purchaseId));
    }

    public int deletePurchase(int purchaseId) {
        return repoPurchase_.deletePurchase(purchaseId);
    }

    public void deleteAllPurchaseTemplates(){
        repoPurchaseTemplate_.deleteAllPurchaseTemplates();
    }

    public List<PurchaseTemplate> getAllPurchaseTemplates(){
        return repoPurchaseTemplate_.getAllPurchaseTemplates();
    }

    public void savePurchaseTemplate(PurchaseTemplate purchaseTemplate){
        repoPurchaseTemplate_.savePurchaseTemplate(purchaseTemplate);
    }

}
