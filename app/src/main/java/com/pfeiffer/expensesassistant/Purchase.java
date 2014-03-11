package com.pfeiffer.expensesassistant;

import android.util.Log;

enum LOCATION {
	NONE( "" ), EDEKA( "Edeka" ), ALDI( "Aldi" ), REWE( "Rewe" ), PENNY( "Penny" ), KANTINE( "Kantine" ), BAKEREI(
			"BŠckerei" ), KIOSK( "Kiosk" ), TJARDENS( "Tjardens" ), BASIC( "Basic" ), TANKSTELLE( "Tankstelle" ), BUDNI(
			"Budni" ), SONSTIGES( "Sonstiges" );
	private String friendlyName;

	private LOCATION( String friendlyName ) {
		this.friendlyName = friendlyName;
	}

	public static LOCATION fromString( String description ) {
		for( LOCATION l : values() ) {
			if( l.friendlyName.equals( description ) )
				return l;
		}
		return null;
	}

	@Override
	public String toString() {
		return friendlyName;
	}
};

public class Purchase {
	final int purchaseId_;
	final int productId_;
	final int amount_;
	final String date_;
	final LOCATION location_;
	final String price_;

	public Purchase( int purchaseId, int productId, int amount, String date, LOCATION location, String price ) {
		Log.d( this.getClass().getName(), "Enter Purchase constructor with arguments: purchaseId=" + purchaseId
				+ ", productId=" + productId + ", amount=" + amount + ", date=" + date + ", location=" + location
				+ ", price=" + price + "." );

		// TODO check arguments, throw exceptions

		purchaseId_ = purchaseId;
		productId_ = productId;
		amount_ = amount;
		date_ = date;
		location_ = location;
		price_ = price;
	}

	public String getDate() {
		return date_;
	}

	public int getProductId() {
		return productId_;
	}

	public String getAmount() {
		return String.valueOf( amount_ );
	}

	public LOCATION getLocation() {
		return location_;
	}

	public String getPrice() {
		return Translation.getValidPrice( price_ );
	}

	public int getId() {
		return purchaseId_;
	}

	public String getTotalPrice() {
		return Translation.getValidPrice( String.valueOf( Double.parseDouble( price_ ) * amount_ ) );
	}
}
