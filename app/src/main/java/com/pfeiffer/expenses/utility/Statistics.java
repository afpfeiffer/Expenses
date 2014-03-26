package com.pfeiffer.expenses.utility;

import android.util.Log;

import com.pfeiffer.expenses.model.CATEGORY;
import com.pfeiffer.expenses.model.Product;
import com.pfeiffer.expenses.model.Purchase;
import com.pfeiffer.expenses.repository.RepositoryManager;

import java.util.HashMap;
import java.util.List;

/**
 * Created by axelpfeiffer on 16.03.14.
 */
public class Statistics {
    private final String logTag_ = this.getClass().getName();
    private RepositoryManager repositoryManager_;
    private List<Purchase> purchaseList_;
    private HashMap<String, HashMap<CATEGORY, Double>> expensesPerMonthAndCategory_;
    private String currentYearMonthKey_;


    public Statistics(RepositoryManager repositoryManager) {
        repositoryManager_ = repositoryManager;
        purchaseList_ = repositoryManager_.getAllPurchases();
        expensesPerMonthAndCategory_ = new HashMap<String, HashMap<CATEGORY, Double>>();

        buildExpensesHashMap();

    }


    private void buildExpensesHashMap() {
        // return if purchaseList_ is empty
        if (purchaseList_ == null || purchaseList_.isEmpty())
            return;


        currentYearMonthKey_ = purchaseList_.get(0).getDate().substring(0, 7);
        String currentMonthYear = currentYearMonthKey_;

        HashMap<CATEGORY, Double> categoryToExpenses = new HashMap<CATEGORY, Double>();


        for (Purchase purchase : purchaseList_) {

            // obtain product
            Product product = repositoryManager_.findProductById(purchase.getProductId());

            String monthYear = purchase.getDate().substring(0, 7);

            if (!monthYear.equals(currentMonthYear)) {
                expensesPerMonthAndCategory_.put(currentMonthYear, categoryToExpenses);

                categoryToExpenses = new HashMap<CATEGORY, Double>();

                currentMonthYear = monthYear;
            }

            CATEGORY category = product.getCategory();


            double purchasePrice = Double.parseDouble(purchase.getTotalPrice());

            if (categoryToExpenses.containsKey(category)) {
                double sumCategory = categoryToExpenses.get(category);
                categoryToExpenses.put(category, sumCategory + purchasePrice);
            } else {
                categoryToExpenses.put(category, purchasePrice);
            }
        }

        expensesPerMonthAndCategory_.put(currentMonthYear, categoryToExpenses);

        Log.d(logTag_, expensesPerMonthAndCategory_.toString());
    }


    public String getCurrentYearMonthKey() {
        if (currentYearMonthKey_ == null)
            throw new IllegalStateException();
        return currentYearMonthKey_;
    }

    public double getExpenses(String yearMonth, CATEGORY category) {
        // TODO check arguments
        if (expensesPerMonthAndCategory_.containsKey(yearMonth) && expensesPerMonthAndCategory_.get(yearMonth)
                .containsKey(category)) {
            return expensesPerMonthAndCategory_.get(yearMonth).get(category);
        } else return 0.0;
    }
}
