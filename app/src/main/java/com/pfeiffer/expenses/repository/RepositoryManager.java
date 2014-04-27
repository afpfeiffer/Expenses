package com.pfeiffer.expenses.repository;

import android.content.Context;
import android.database.SQLException;

import com.pfeiffer.expenses.model.Barcode;
import com.pfeiffer.expenses.model.PartnerDevice;
import com.pfeiffer.expenses.model.Purchase;
import com.pfeiffer.expenses.model.PurchaseHistory;
import com.pfeiffer.expenses.model.PurchaseTemplate;

import java.util.Date;
import java.util.List;

public class RepositoryManager {
    private final RepositoryPurchase repositoryPurchase_;
    private final RepositoryPurchaseTemplate repositoryPurchaseTemplate_;
    private final RepositoryPartnerDevice repositoryPartnerDevice_;
    private final RepositoryPurchaseHistory repositoryPurchaseHistory_;

    public RepositoryManager(Context context) {
        ExpensesSQLiteHelper dbHelper = new ExpensesSQLiteHelper(context);

        repositoryPurchase_ = new RepositoryPurchase(context, dbHelper);
        repositoryPurchaseTemplate_ = new RepositoryPurchaseTemplate(context, dbHelper);
        repositoryPartnerDevice_ = new RepositoryPartnerDevice(context, dbHelper);
        repositoryPurchaseHistory_=new RepositoryPurchaseHistory(context, dbHelper);
    }

    public void open() throws SQLException {
        repositoryPurchase_.open();
        repositoryPurchaseTemplate_.open();
        repositoryPartnerDevice_.open();
        repositoryPurchaseHistory_.open();
    }

    public void close() {
        repositoryPurchase_.close();
        repositoryPurchaseTemplate_.close();
        repositoryPartnerDevice_.close();
        repositoryPurchaseHistory_.close();
    }

    public boolean updatePurchase(Purchase purchase) {
        return repositoryPurchase_.updatePurchase(purchase);
    }

    public long savePurchase(Purchase purchase) {

        if (!purchase.hasValidState()) throw new IllegalStateException();

        return repositoryPurchase_.savePurchase(purchase);
    }

    public List<Purchase> getAllPurchases() {
        return repositoryPurchase_.getAllPurchases();
    }

    public List<Purchase> getAllPurchasesForDateRange(Date minDate, Date maxDate) {
        return repositoryPurchase_.getAllPurchasesForDateRange(minDate, maxDate);
    }

    public Purchase findLatestPurchase(Barcode barcode) {
        return repositoryPurchase_.findLatestPurchase(ExpensesSQLiteHelper.PURCHASE_BARCODE, barcode.toString());
    }

    public Purchase findPurchaseById(long purchaseId) {
        return repositoryPurchase_.findLatestPurchase(ExpensesSQLiteHelper.PURCHASE_ID, String.valueOf(purchaseId));
    }

    public int deletePurchase(long purchaseId) {
        return repositoryPurchase_.deletePurchase(purchaseId);
    }

    public void deleteAllPurchaseTemplates(){
        repositoryPurchaseTemplate_.deleteAllPurchaseTemplates();
    }

    public List<PurchaseTemplate> getAllPurchaseTemplates(){
        return repositoryPurchaseTemplate_.getAllPurchaseTemplates();
    }

    public long savePurchaseTemplate(PurchaseTemplate purchaseTemplate){
        return repositoryPurchaseTemplate_.savePurchaseTemplate(purchaseTemplate);
    }

    public long savePurchaseHistory(PurchaseHistory purchaseHistory){
        return repositoryPurchaseHistory_.savePurchaseHistory(purchaseHistory);
    }

    public List<PurchaseHistory> getPurchaseHistoryAfter( Date date ){
        return repositoryPurchaseHistory_.getPurchaseHistoryAfter(date);
    }

    public long savePartnerDevice( PartnerDevice partnerDevice){
        return repositoryPartnerDevice_.savePartnerDevice(partnerDevice);
    }

    public boolean updatePartnerDevice( PartnerDevice partnerDevice){
        return repositoryPartnerDevice_.updatePartnerDevice(partnerDevice);
    }

    public PartnerDevice findPartnerDeviceByAndroidId(String androidId){
        return repositoryPartnerDevice_.findPartnerDeviceByAndroidId(androidId);
    }


}
