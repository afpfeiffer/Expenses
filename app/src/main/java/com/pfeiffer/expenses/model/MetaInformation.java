package com.pfeiffer.expenses.model;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by axelpfeiffer on 29.04.14.
 */
public class MetaInformation implements Serializable {
    // request Purchases (after some date)
    // Header (ObjectStream) (int numberOfPurchases)
    // Trailer (ObjectStream)
    public static final int REQUEST = 1;
    public static final int REPLY_HEADER = 2;
    public static final int REPLY_TRAILER = 3;

    private int messageFunction_;
    private Date date_;
    private int numberOfObjects_;


    public int getMessageFunction() {
        return messageFunction_;
    }

    public Date getDate() {
        return date_;
    }

    public int getNumberOfObjects() {
        return numberOfObjects_;
    }

    public void setRequest(Date date) {
        messageFunction_ = REQUEST;
        date_ = date;
    }

    public void setHeader(int numberOfObjects) {
        messageFunction_ = REPLY_HEADER;
        numberOfObjects_ = numberOfObjects;
    }

    public void setTrailer(int numberOfObjects) {
        messageFunction_ = REPLY_TRAILER;
        numberOfObjects_ = numberOfObjects;
    }
}

