package com.pfeiffer.expensesassistant;

public class Barcode {
	private String barcode_;

	Barcode( String barcode ) {
		barcode_ = barcode;
	}

	public boolean equals( Barcode b ) {
		return barcode_.equals( b.toString() );
	}

	public boolean equals( String b ) {
		return barcode_.equals( b );
	}

	public String toString() {
		return barcode_;
	}
}
