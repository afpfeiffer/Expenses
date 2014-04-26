package com.pfeiffer.expenses.model;

import java.io.Serializable;

public class Barcode implements Serializable{
    private final String barcode_;

    public Barcode(String barcode) {
        if( barcode == null || barcode.equals(""))
            throw new IllegalArgumentException();

        barcode_ = barcode;
    }

    public boolean equals(Barcode b) {
        return barcode_.equals(b.toString());
    }

    public String toString() {
        return barcode_;
    }
}
