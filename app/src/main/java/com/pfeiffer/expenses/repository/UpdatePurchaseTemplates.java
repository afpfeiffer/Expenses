package com.pfeiffer.expenses.repository;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import com.pfeiffer.expenses.model.Purchase;
import com.pfeiffer.expenses.model.PurchaseTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

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


        Map<String, List<Purchase>> productNameToPurchases = new HashMap<String, List<Purchase>>();

        // get all Products, that don't have a barcode
        List<Purchase> purchaseList = repositoryManager.getAllPurchases();

        // get a list map product -> purchases of this product
        for (Purchase purchase : purchaseList) {
            if (purchase.getBarcode()==null) {
                // identifier: productName
                String productName=purchase.getProductName();
                if(productNameToPurchases.containsKey(productName)){
                    List<Purchase> pList=productNameToPurchases.get(productName);
                    pList.add(purchase);
                    productNameToPurchases.put(productName, pList);
                }
                else{
                    List<Purchase> pList=new ArrayList<Purchase>();
                    pList.add(purchase);
                    productNameToPurchases.put(productName, pList);
                }
            }
        }

        repositoryManager.deleteAllPurchaseTemplates();

        Iterator it=productNameToPurchases.entrySet().iterator();
        while(it.hasNext()){
            Map.Entry pairs = (Map.Entry)it.next();
            ArrayList<Purchase> pList= (ArrayList<Purchase>) pairs.getValue();
            PurchaseTemplate purchaseTemplate = new PurchaseTemplate(pList);
            repositoryManager.savePurchaseTemplate(purchaseTemplate);
            it.remove();
        }


        return Service.START_NOT_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }



}
