package com.pfeiffer.expenses.model;

import java.util.Date;

/**
 * Created by axelpfeiffer on 26.04.14.
 */
public class PurchaseHistory {
    private int id_;
    private int purchaseId_;
    private int operation_;
    private Date date_;

    private static int OPERATION_NEW = 1;
    private static int OPERATION_MODIFY = 2;
    private static int OPERATION_DELETE = 3;

    public PurchaseHistory(int id, Date date, int purchaseId, int operation) {
        if (operation != OPERATION_NEW && operation != OPERATION_MODIFY &&
                operation != OPERATION_DELETE) {
            throw new IllegalArgumentException();
        }

        id_ = id;
        purchaseId_ = purchaseId;
        operation_ = operation;
        date_ = date;
    }

    public int getId() {
        return id_;
    }

    public int getPurchaseId() {
        return purchaseId_;
    }

    public int getOperation() {
        return operation_;
    }

    public Date getDate() {
        return date_;
    }

    public String toString() {
        return "PurchaseHistory(id=" + id_ + ", purchaseId=" + purchaseId_ + ", date=" +
                date_.toString() + ", operation=" + operation_ + ")";
    }

}
