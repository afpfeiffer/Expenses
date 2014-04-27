package com.pfeiffer.expenses.repository;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.util.Log;

import com.pfeiffer.expenses.model.PurchaseHistory;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by axelpfeiffer on 26.04.14.
 */
public class RepositoryPurchaseHistory extends RepositoryBase {
    private final String logTag_ = this.getClass().getName();

    private final static String[] allPurchaseHistoryColumns_ = {ExpensesSQLiteHelper.PURCHASE_HISTORY_ID,
            ExpensesSQLiteHelper.PURCHASE_HISTORY_TIMESTAMP,
            ExpensesSQLiteHelper.PURCHASE_HISTORY_PURCHASE_ID,
            ExpensesSQLiteHelper.PURCHASE_HISTORY_OPERATION};

    public RepositoryPurchaseHistory(Context context, ExpensesSQLiteHelper dbHelper) {
        super(context, dbHelper);
    }

    private PurchaseHistory cursorToPurchaseHistory(Cursor cursor) {
        if (cursor.isAfterLast())
            throw new IllegalStateException("Database cursor out of bounds.");
        return new PurchaseHistory(cursor.getInt(0), new Date(Long.parseLong(cursor.getString(1))),
                cursor.getInt(2), cursor.getInt(3));

    }

    long savePurchaseHistory(PurchaseHistory purchaseHistory) {
        Log.d(logTag_, "savePurchaseHistory(" + purchaseHistory + ")");
        if (purchaseHistory == null || purchaseHistory.getDate() == null) {
            throw new IllegalArgumentException();
        }

        Date date = purchaseHistory.getDate();
        int purchaseId = purchaseHistory.getPurchaseId();
        int operation = purchaseHistory.getOperation();

        ContentValues values = new ContentValues();
        values.put(ExpensesSQLiteHelper.PURCHASE_HISTORY_TIMESTAMP, date.getTime());
        values.put(ExpensesSQLiteHelper.PURCHASE_HISTORY_PURCHASE_ID, purchaseId);
        values.put(ExpensesSQLiteHelper.PURCHASE_HISTORY_OPERATION, operation);

        return database_.insert(ExpensesSQLiteHelper.TABLE_PURCHASE_HISTORY, null, values);
    }

    public List<PurchaseHistory> getPurchaseHistoryAfter(Date date) {
        Log.d(logTag_, "getPurchaseHistoryAfter(" + date.toString() + ")");

        if (date == null) {
            throw new IllegalArgumentException();
        }

        String orderBy = ExpensesSQLiteHelper.PURCHASE_HISTORY_TIMESTAMP + " DESC";

        Cursor cursor = database_.query(ExpensesSQLiteHelper.TABLE_PURCHASE_HISTORY, allPurchaseHistoryColumns_,
                ExpensesSQLiteHelper.PURCHASE_HISTORY_TIMESTAMP +
                        " BETWEEN ? AND ?", new String[]{String.valueOf(date.getTime()),
                        String.valueOf(System.currentTimeMillis())},
                null, null, orderBy, null
        );

        List<PurchaseHistory> purchaseHistoryList = new ArrayList<PurchaseHistory>();
        cursor.moveToFirst();

        while (!cursor.isAfterLast()) {
            PurchaseHistory purchaseHistory = cursorToPurchaseHistory(cursor);
            purchaseHistoryList.add(purchaseHistory);
            cursor.moveToNext();
        }
        cursor.close();

        Log.d(logTag_, "getPurchaseHistoryAfter returns "+purchaseHistoryList);
        return purchaseHistoryList;
    }

}
