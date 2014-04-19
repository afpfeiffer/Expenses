package com.pfeiffer.expenses.repository;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import com.pfeiffer.expenses.model.Product;
import com.pfeiffer.expenses.model.Purchase;
import com.pfeiffer.expenses.model.PurchaseTemplate;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by axelpfeiffer on 26.03.14.
 */
public class UpdatePurchaseTemplates extends Service {
    private final String logTag_ = this.getClass().getName();


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(logTag_, "starting service...");

        RepositoryManager repositoryManager = new RepositoryManager(this);
        repositoryManager.open();



        List<PurchaseTemplate> templateCandidates = new ArrayList<PurchaseTemplate>();

        // get all Products, that don't have a barcode
        List<Product> allProducts = repositoryManager.getAllProducts();

        // read DB and cache all purchases associated with products that don't have a barcode
        for (Product product : allProducts) {
            if (!product.hasBarcode()) {
                List<Purchase> productPurchases = repositoryManager.getRepositoryPurchase().findPurchases(ExpensesSQLiteHelper
                        .PURCHASE_PRODUCT_ID, String.valueOf(product.getId()));

                if (productPurchases != null && productPurchases.size() > 0) {
                    templateCandidates.add(new PurchaseTemplate(product, productPurchases));
                }
            }
        }

        if (templateCandidates.isEmpty()) return Service.START_NOT_STICKY;



        for(int i=0; i <Math.min(10, templateCandidates.size()); ++i) {
            Log.d(logTag_, "product " + templateCandidates.get(i).getProductName()+
                    " was purchased "+templateCandidates.get(i).getNumberOfPurchases()+" times.");

        }

        Log.d(logTag_, templateCandidates.toString());


        return Service.START_NOT_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }



}
