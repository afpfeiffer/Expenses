package com.pfeiffer.expenses.utility;

import android.util.Log;

import com.pfeiffer.expenses.model.EnumCategory;
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
    private HashMap<String, HashMap<EnumCategory, Money>> expensesPerMonthAndCategory_;
    private HashMap<String, Money> expensesPerMonth_;
    private HashMap<String, HashMap<EnumCategory, List<Purchase>>> purchasesPerMonthAndCategory_;


    public DataAnalysis(RepositoryManager repositoryManager) {
        repositoryManager_ = repositoryManager;
        purchaseList_ = repositoryManager_.getAllPurchases();
        expensesPerMonthAndCategory_ = new HashMap<String, HashMap<EnumCategory, Money>>();
        expensesPerMonth_ = new HashMap<String, Money>();
        purchasesPerMonthAndCategory_ = new HashMap<String, HashMap<EnumCategory, List<Purchase>>>();

        analyzePurchaseList();
    }

    public DataAnalysis(RepositoryManager repositoryManager, List<Purchase> purchaseList) {
        repositoryManager_ = repositoryManager;
        purchaseList_ = purchaseList;
        expensesPerMonthAndCategory_ = new HashMap<String, HashMap<EnumCategory, Money>>();
        expensesPerMonth_ = new HashMap<String, Money>();
        purchasesPerMonthAndCategory_ = new HashMap<String, HashMap<EnumCategory, List<Purchase>>>();
        analyzePurchaseList();
    }


    private void analyzePurchaseList() {
        // return if purchaseList_ is empty
        if (purchaseList_ == null || purchaseList_.isEmpty())
            return;

        Date lastDate = purchaseList_.get(0).getDate();

        HashMap<EnumCategory, Money> categoryToExpenses = new HashMap<EnumCategory, Money>();
        Money expensesSum=new Money("0.0");
        HashMap<EnumCategory, List<Purchase>> categoryToPurchases = new HashMap<EnumCategory, List<Purchase>>();



        for (Purchase purchase : purchaseList_) {

            if ( !Translation.sameMonthAndYear(lastDate, purchase.getDate()) ) {
                expensesPerMonthAndCategory_.put(Translation.yearAndMonth(lastDate), categoryToExpenses);
                expensesPerMonth_.put(Translation.yearAndMonth(lastDate), expensesSum);
                purchasesPerMonthAndCategory_.put(Translation.yearAndMonth(lastDate), categoryToPurchases);

                categoryToExpenses = new HashMap<EnumCategory, Money>();
                expensesSum=new Money("0.0");
                categoryToPurchases = new HashMap<EnumCategory, List<Purchase>>();

                lastDate = purchase.getDate();
            }

            EnumCategory category = purchase.getCategory();
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


    public Money getExpensesForYearMonthAndCategory(Date date, EnumCategory category) {
        // TODO check arguments
        String yearMonth = Translation.yearAndMonth(date);
        if (expensesPerMonthAndCategory_.containsKey(yearMonth) && expensesPerMonthAndCategory_.get(yearMonth)
                .containsKey(category)) {
            return expensesPerMonthAndCategory_.get(yearMonth).get(category);
        } else return new Money("0.0");
    }

    public List<Map.Entry<EnumCategory, Money>> getSortedCategoryToExpensesForYearAndMonth(Date date){
        List<Map.Entry<EnumCategory, Money>> ret = new ArrayList<Map.Entry<EnumCategory, Money>>();

        for(EnumCategory category : EnumCategory.values()){
            ret.add(new AbstractMap.SimpleEntry<EnumCategory, Money>(category, getExpensesForYearMonthAndCategory(date,
                    category)));
        }

        Collections.sort(ret, new CategoryComparator());

        return ret;
    }

    public List<Purchase> getPurchasesForYearMonthAndCategory(Date date, EnumCategory category){
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

    public class CategoryComparator implements Comparator<Map.Entry<EnumCategory, Money>> {
        @Override
        public int compare(Map.Entry<EnumCategory, Money> o1, Map.Entry<EnumCategory, Money> o2) {
            return o1.getValue().compareTo(o2.getValue());
        }
    }
}
