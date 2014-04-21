package com.pfeiffer.expenses.model;

import android.util.Log;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.Currency;

/**
 * Created by axelpfeiffer on 21.04.14.
 */
public class Money {
    private BigDecimal amount_;
    private static final Currency currency_ = Currency.getInstance("EUR");
    private static final RoundingMode roundingMode_ = RoundingMode.HALF_UP;
    private String logTag_=this.getClass().getName();


    public Money(String money) {
        money=money.replaceAll("[^\\d.]", "");
        Log.d(logTag_, "string: " + money);


        if(money.equals("")) throw new IllegalArgumentException();

        amount_ = new BigDecimal(money).setScale(2, roundingMode_);
    }

    public String getDataBaseRepresentation() {
        return amount_.toPlainString();
    }

    static public Currency getCurrency() {
        return currency_;
    }

    public Money getScaled(int factor) {
        Money ret = new Money(amount_.toString());
        ret.scale(factor);
        return ret;
    }

    public int percentage(Money money){
        return amount_.divide(money.getAmount(), MathContext.DECIMAL128).multiply(new BigDecimal(100)).intValue();
    }

    public void scale(int factor) {
        amount_=amount_.multiply(new BigDecimal(factor)).setScale(2, roundingMode_);
    }

    public void add(Money money) {
        amount_=amount_.add(money.getAmount().setScale(2, roundingMode_));
    }

    public void subtract(Money money) {
        amount_=amount_.subtract(money.getAmount().setScale(2, roundingMode_));
    }

    private BigDecimal getAmount() {
        return amount_;
    }

    public String getHumanReadableRepresentation() {
        return getHumanReadableRepresentation(1);
    }

    public String getHumanReadableRepresentation(int amount) {
        return getScaled(amount) + " " + getCurrency().getSymbol();
    }

    public boolean isValid() {
        return amount_.compareTo(new BigDecimal(0)) == 1;
    }

    public String toString() {
        return amount_.toString();
    }

    public int compareTo(Money money){
        return amount_.compareTo(money.getAmount());
    }


}

