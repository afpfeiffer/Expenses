package com.pfeiffer.expenses.model;

public class Barcode {
    private final String barcode_;

    public Barcode(String barcode) {
        barcode_ = barcode;
    }

    public boolean equals(Barcode b) {
        return barcode_.equals(b.toString());
    }

    public boolean equals(String b) {
        return barcode_.equals(b);
    }

    public String toString() {
        return barcode_;
    }
}
