package com.pfeiffer.expenses.repository;

import android.content.Context;
import android.database.SQLException;

import com.pfeiffer.expenses.model.Barcode;
import com.pfeiffer.expenses.model.Purchase;
import com.pfeiffer.expenses.model.PurchaseTemplate;

import java.util.Date;
import java.util.List;

public class RepositoryManager {
    private final RepositoryPurchase repoPurchase_;
    private final RepositoryPurchaseTemplate repoPurchaseTemplate_;

    public RepositoryManager(Context context) {
        ExpensesSQLiteHelper dbHelper = new ExpensesSQLiteHelper(context);

        repoPurchase_ = new RepositoryPurchase(dbHelper);
        repoPurchaseTemplate_ = new RepositoryPurchaseTemplate(dbHelper);
    }

    public void open() throws SQLException {
        repoPurchase_.open();
        repoPurchaseTemplate_.open();
    }

    public void close() {
        repoPurchase_.close();
        repoPurchaseTemplate_.close();
    }

    public boolean updatePurchase(Purchase purchase) {
        return repoPurchase_.updatePurchase(purchase);
    }

    public Purchase createPurchase(Purchase purchase) {

        if (!purchase.hasValidState()) throw new IllegalStateException();

        return repoPurchase_.createPurchase(purchase);
    }

    public List<Purchase> getAllPurchases() {
        return repoPurchase_.getAllPurchases();
    }

    public List<Purchase> getAllPurchasesForDateRange(Date minDate, Date maxDate) {
        return repoPurchase_.getAllPurchasesForDateRange(minDate, maxDate);
    }

    public Purchase findLatestPurchase(Barcode barcode) {
        return repoPurchase_.findLatestPurchase(ExpensesSQLiteHelper.PURCHASE_BARCODE, barcode.toString());
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
