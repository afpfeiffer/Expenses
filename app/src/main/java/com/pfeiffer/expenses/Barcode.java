package com.pfeiffer.expenses;

public class Barcode {
    private final String barcode_;

    Barcode(String barcode) {
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
