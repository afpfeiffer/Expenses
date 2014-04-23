package com.pfeiffer.expenses.utility;

import android.util.Log;

import com.pfeiffer.expenses.model.Category;
import com.pfeiffer.expenses.model.Money;
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
    private HashMap<String, HashMap<Category, Money>> expensesPerMonthAndCategory_;
    private HashMap<String, Money> expensesPerMonth_;
    private HashMap<String, HashMap<Category, List<Purchase>>> purchasesPerMonthAndCategory_;


    public DataAnalysis(RepositoryManager repositoryManager) {
        repositoryManager_ = repositoryManager;
        purchaseList_ = repositoryManager_.getAllPurchases();
        expensesPerMonthAndCategory_ = new HashMap<String, HashMap<Category, Money>>();
        expensesPerMonth_ = new HashMap<String, Money>();
        purchasesPerMonthAndCategory_ = new HashMap<String, HashMap<Category, List<Purchase>>>();

        analyzePurchaseList();
    }

    public DataAnalysis(RepositoryManager repositoryManager, List<Purchase> purchaseList) {
        repositoryManager_ = repositoryManager;
        purchaseList_ = purchaseList;
        expensesPerMonthAndCategory_ = new HashMap<String, HashMap<Category, Money>>();
        expensesPerMonth_ = new HashMap<String, Money>();
        purchasesPerMonthAndCategory_ = new HashMap<String, HashMap<Category, List<Purchase>>>();
        analyzePurchaseList();
    }


    private void analyzePurchaseList() {
        // return if purchaseList_ is empty
        if (purchaseList_ == null || purchaseList_.isEmpty())
            return;

        Date lastDate = purchaseList_.get(0).getDate();

        HashMap<Category, Money> categoryToExpenses = new HashMap<Category, Money>();
        Money expensesSum=new Money("0.0");
        HashMap<Category, List<Purchase>> categoryToPurchases = new HashMap<Category, List<Purchase>>();



        for (Purchase purchase : purchaseList_) {

            if ( !Translation.sameMonthAndYear(lastDate, purchase.getDate()) ) {
                expensesPerMonthAndCategory_.put(Translation.yearAndMonth(lastDate), categoryToExpenses);
                expensesPerMonth_.put(Translation.yearAndMonth(lastDate), expensesSum);
                purchasesPerMonthAndCategory_.put(Translation.yearAndMonth(lastDate), categoryToPurchases);

                categoryToExpenses = new HashMap<Category, Money>();
                expensesSum=new Money("0.0");
                categoryToPurchases = new HashMap<Category, List<Purchase>>();

                lastDate = purchase.getDate();
            }

            Category category = purchase.getCategory();
            Money purchasePrice = purchase.getTotalPrice();
            expensesSum.add(purchasePrice);

            if (categoryToExpenses.containsKey(category)) {
                Money sumCategory = categoryToExpenses.get(category);
                sumCategory.add(purchasePrice);
                categoryToExpenses.put(category, sumCategory );
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


    public Money getExpensesForYearMonthAndCategory(Date date, Category category) {
        // TODO check arguments
        String yearMonth = Translation.yearAndMonth(date);
        if (expensesPerMonthAndCategory_.containsKey(yearMonth) && expensesPerMonthAndCategory_.get(yearMonth)
                .containsKey(category)) {
            return expensesPerMonthAndCategory_.get(yearMonth).get(category);
        } else return new Money("0.0");
    }

    public List<Map.Entry<Category, Money>> getSortedCategoryToExpensesForYearAndMonth(Date date){
        List<Map.Entry<Category, Money>> ret = new ArrayList<Map.Entry<Category, Money>>();

        for(Category category : Category.values()){
            ret.add(new AbstractMap.SimpleEntry<Category, Money>(category, getExpensesForYearMonthAndCategory(date,
                    category)));
        }

        Collections.sort(ret, new CategoryComparator());

        return ret;
    }

    public List<Purchase> getPurchasesForYearMonthAndCategory(Date date, Category category){
        // TODO check arguments
        String yearMonth = Translation.yearAndMonth(date);
        if (purchasesPerMonthAndCategory_.containsKey(yearMonth) && purchasesPerMonthAndCategory_.get(yearMonth)
                .containsKey(category)) {
            return purchasesPerMonthAndCategory_.get(yearMonth).get(category);
        } else return null;
    }

    public Money getExpensesPerMonth(Date date){
        // TODO check arguments
        String yearMonth = Translation.yearAndMonth(date);
        if(expensesPerMonth_.containsKey(yearMonth)) {
            return expensesPerMonth_.get(yearMonth);
        } else return new Money("0.0");
    }

    public class CategoryComparator implements Comparator<Map.Entry<Category, Money>> {
        @Override
        public int compare(Map.Entry<Category, Money> o1, Map.Entry<Category, Money> o2) {
            return o1.getValue().compareTo(o2.getValue());
        }
    }
}
