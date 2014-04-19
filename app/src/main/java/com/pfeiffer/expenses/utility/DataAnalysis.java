package com.pfeiffer.expenses.utility;

import android.util.Log;

import com.pfeiffer.expenses.model.CATEGORY;
import com.pfeiffer.expenses.model.Product;
import com.pfeiffer.expenses.model.Purchase;
import com.pfeiffer.expenses.repository.RepositoryManager;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by axelpfeiffer on 16.03.14.
 */
public class DataAnalysis {
    private final String logTag_ = this.getClass().getName();
    private RepositoryManager repositoryManager_;
    private List<Purchase> purchaseList_;
    private HashMap<String, HashMap<CATEGORY, Double>> expensesPerMonthAndCategory_;
    private HashMap<String, Double> expensesPerMonth_;
    private HashMap<String, HashMap<CATEGORY, List<Purchase>>> purchasesPerMonthAndCategory_;


    public DataAnalysis(RepositoryManager repositoryManager) {
        repositoryManager_ = repositoryManager;
        purchaseList_ = repositoryManager_.getAllPurchases();
        expensesPerMonthAndCategory_ = new HashMap<String, HashMap<CATEGORY, Double>>();
        expensesPerMonth_ = new HashMap<String, Double>();
        purchasesPerMonthAndCategory_ = new HashMap<String, HashMap<CATEGORY, List<Purchase>>>();

        analyzePurchaseList();
    }

    public DataAnalysis(RepositoryManager repositoryManager, List<Purchase> purchaseList) {
        repositoryManager_ = repositoryManager;
        purchaseList_ = purchaseList;
        expensesPerMonthAndCategory_ = new HashMap<String, HashMap<CATEGORY, Double>>();
        expensesPerMonth_ = new HashMap<String, Double>();
        purchasesPerMonthAndCategory_ = new HashMap<String, HashMap<CATEGORY, List<Purchase>>>();
        analyzePurchaseList();
    }


    private void analyzePurchaseList() {
        // return if purchaseList_ is empty
        if (purchaseList_ == null || purchaseList_.isEmpty())
            return;

        Date lastDate = purchaseList_.get(0).getDate();

        HashMap<CATEGORY, Double> categoryToExpenses = new HashMap<CATEGORY, Double>();
        double expensesSum=0.0;
        HashMap<CATEGORY, List<Purchase>> categoryToPurchases = new HashMap<CATEGORY, List<Purchase>>();



        for (Purchase purchase : purchaseList_) {

            // obtain product
            Product product = repositoryManager_.findProductById(purchase.getProductId());

            if ( !Translation.sameMonthAndYear(lastDate, purchase.getDate()) ) {
                expensesPerMonthAndCategory_.put(Translation.yearAndMonth(lastDate), categoryToExpenses);
                expensesPerMonth_.put(Translation.yearAndMonth(lastDate), expensesSum);
                purchasesPerMonthAndCategory_.put(Translation.yearAndMonth(lastDate), categoryToPurchases);

                categoryToExpenses = new HashMap<CATEGORY, Double>();
                expensesSum=0.0;
                categoryToPurchases = new HashMap<CATEGORY, List<Purchase>>();

                lastDate = purchase.getDate();
            }

            CATEGORY category = purchase.getCategory();
            double purchasePrice = Double.parseDouble(purchase.getTotalPrice());
            expensesSum+=purchasePrice;

            if (categoryToExpenses.containsKey(category)) {
                double sumCategory = categoryToExpenses.get(category);
                categoryToExpenses.put(category, sumCategory + purchasePrice);
                List<Purchase> purchases = categoryToPurchases.get(category);
                purchases.add(purchase);
                categoryToPurchases.put(category, purchases);
            } else {
                categoryToExpenses.put(category, purchasePrice);
                List<Purchase> purchases = new ArrayList<Purchase>();
                purchases.add(purchase);
                categoryToPurchases.put(category, purchases);
            }
        }

        expensesPerMonthAndCategory_.put(Translation.yearAndMonth(lastDate), categoryToExpenses);
        expensesPerMonth_.put(Translation.yearAndMonth(lastDate), expensesSum);
        purchasesPerMonthAndCategory_.put(Translation.yearAndMonth(lastDate), categoryToPurchases);

        Log.d(logTag_, expensesPerMonthAndCategory_.toString());
        Log.d(logTag_, purchasesPerMonthAndCategory_.toString());
    }


    public double getExpensesForYearMonthAndCategory(Date date, CATEGORY category) {
        // TODO check arguments
        String yearMonth = Translation.yearAndMonth(date);
        if (expensesPerMonthAndCategory_.containsKey(yearMonth) && expensesPerMonthAndCategory_.get(yearMonth)
                .containsKey(category)) {
            return expensesPerMonthAndCategory_.get(yearMonth).get(category);
        } else return 0.0;
    }

    public List<Map.Entry<CATEGORY, Double>> getSortedCategoryToExpensesForYearAndMonth(Date date){
        List<Map.Entry<CATEGORY, Double>> ret = new ArrayList<Map.Entry<CATEGORY, Double>>();

        for(CATEGORY category : CATEGORY.values()){
            ret.add(new AbstractMap.SimpleEntry<CATEGORY, Double>(category, getExpensesForYearMonthAndCategory(date,
                    category)));
        }

        Collections.sort(ret, new CategoryComparator());

        return ret;
    }

    public List<Purchase> getPurchasesForYearMonthAndCategory(Date date, CATEGORY category){
        // TODO check arguments
        String yearMonth = Translation.yearAndMonth(date);
        if (purchasesPerMonthAndCategory_.containsKey(yearMonth) && purchasesPerMonthAndCategory_.get(yearMonth)
                .containsKey(category)) {
            return purchasesPerMonthAndCategory_.get(yearMonth).get(category);
        } else return null;
    }

    public double getExpensesPerMonth(Date date){
        // TODO check arguments
        String yearMonth = Translation.yearAndMonth(date);
        if(expensesPerMonth_.containsKey(yearMonth)) {
            return expensesPerMonth_.get(yearMonth);
        } else return 0.0;
    }

    public class CategoryComparator implements Comparator<Map.Entry<CATEGORY, Double>> {
        @Override
        public int compare(Map.Entry<CATEGORY, Double> o1, Map.Entry<CATEGORY, Double> o2) {
            return o1.getValue().compareTo(o2.getValue());
        }
    }
}
