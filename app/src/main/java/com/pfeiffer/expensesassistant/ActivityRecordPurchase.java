package com.pfeiffer.expensesassistant;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.NumberPicker;
import android.widget.Spinner;
import android.widget.Toast;

public class ActivityRecordPurchase extends Activity {
	private RepositoryManager repository_;
	private String barcodeString_;
	private Product productFromDatabase_;

	boolean editMode_ = false;
	EditText name_;
	Spinner category_;
	Spinner location_;
	NumberPicker amount_;
	Spinner type_;
	EditText price_;
	private String purchaseId_ = null;
	private String productNameFromPreviousActivity_;

	@Override
	protected void onCreate( Bundle savedInstanceState ) {
		super.onCreate( savedInstanceState );
		setContentView( R.layout.activity_record_purchase );

		repository_ = new RepositoryManager( this );
		repository_.open();

		name_ = ( EditText )findViewById( R.id.editText1 );
		category_ = ( Spinner )findViewById( R.id.spinner1 );
		location_ = ( Spinner )findViewById( R.id.spinner2 );
		amount_ = ( NumberPicker )findViewById( R.id.editText2 );
		price_ = ( EditText )findViewById( R.id.editText3 );

		amount_.setMinValue( 1 );
		amount_.setMaxValue( 20 );
		amount_.setWrapSelectorWheel( false );
		amount_.setValue( 1 );

		category_.setAdapter( new ArrayAdapter<CATEGORY>( this, android.R.layout.simple_spinner_dropdown_item, CATEGORY
				.values() ) );

		location_.setAdapter( new ArrayAdapter<LOCATION>( this, android.R.layout.simple_spinner_dropdown_item, LOCATION
				.values() ) );

		Intent intent = getIntent();

		purchaseId_ = intent.getStringExtra( ActivityMain.EXTRA_PURCHASE_ID );
		if( purchaseId_ != null && ! purchaseId_.equals( "" ) )
			editMode_ = true;
		else
			editMode_ = false;

		productNameFromPreviousActivity_ = intent.getStringExtra( ActivityEnterName.EXTRA_NAME );
		barcodeString_ = intent.getStringExtra( ActivityEnterName.EXTRA_BARCODE );

		fillFieldsFromDatabase();

	}

	private void fillFieldsFromDatabase() {

		if( editMode_ ) {
			Purchase purchase = repository_.findPurchase( Integer.parseInt( purchaseId_ ) );
			if( purchase == null )
				throw new IllegalStateException();
			productFromDatabase_ = repository_.findProduct( purchase.getProductId() );
			if( productFromDatabase_ == null )
				throw new IllegalStateException();
			setFields(
					productFromDatabase_.getName(),
					productFromDatabase_.getCategory(),
					purchase.getLocation(),
					purchase.getPrice(),
					Integer.parseInt( purchase.getAmount() ) );
			return;
		}

		if( productFromDatabase_ == null && barcodeString_ != null && ! barcodeString_.equals( "" ) ) {
			productFromDatabase_ = repository_.findProduct( new Barcode( barcodeString_ ) );
		}
		// check if a Barcode Object was found
		if( productFromDatabase_ == null && productNameFromPreviousActivity_ != null
				&& ! productNameFromPreviousActivity_.equals( "" ) ) {
			// if not: try to get a match using the product name
			// set the name, since it is known
			name_.setText( productNameFromPreviousActivity_ );
			productFromDatabase_ = repository_.findProduct( productNameFromPreviousActivity_ );
		}

		// only continue if a product could be obtained
		if( productFromDatabase_ != null ) {
			Purchase purchaseFromDatabase = repository_.findPurchase( productFromDatabase_.getId() );
			setFields(
					productFromDatabase_.getName(),
					productFromDatabase_.getCategory(),
					( purchaseFromDatabase != null ) ? purchaseFromDatabase.getLocation() : null,
					productFromDatabase_.getPrice(),
					1 );
		}
	}

	@SuppressWarnings( { "rawtypes", "unchecked" } )
	private void setFields( String name, CATEGORY category, LOCATION location, String price, int amount ) {
		if( name != null && ! name.equals( "" ) ) {
			name_.setText( name );
		}
		if( category != null ) {
			ArrayAdapter arrayAdapterCategory = ( ArrayAdapter )category_.getAdapter();
			category_.setSelection( arrayAdapterCategory.getPosition( category ) );
		}
		if( location != null ) {
			ArrayAdapter arrayAdapterCategory = ( ArrayAdapter )location_.getAdapter();
			location_.setSelection( arrayAdapterCategory.getPosition( location ) );
		}
		if( price != null && ! price.equals( "" ) ) {
			price_.setText( price );
		}
		if( amount > 0 ) {
			amount_.setValue( amount );
		}
	}

	@Override
	public boolean onCreateOptionsMenu( Menu menu ) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate( R.menu.record_purchase, menu );
		return true;
	}

	@SuppressLint( "SimpleDateFormat" )
	public void onClick( View view ) {
		String name = name_.getText().toString();
		String strCategory = category_.getSelectedItem().toString();
		String strLocation = location_.getSelectedItem().toString();
		int amount = amount_.getValue();
		String price = Translation.getValidPrice( price_.getText().toString() );

		Log.d( ActivityRecordPurchase.class.getName(), "Method onClick() records name=" + name + ", category="
				+ strCategory + ", location=" + strLocation + ", amount=" + amount + ", price=" + price );

		// make sure that all field values are set.
		if( name != null && ! name.isEmpty() && strCategory != null && ! strCategory.isEmpty() && strLocation != null
				&& ! strLocation.isEmpty() && price != null && ! price.isEmpty() ) {

			CATEGORY category = CATEGORY.fromString( strCategory );
			LOCATION location = LOCATION.fromString( strLocation );

			if( editMode_ ) {
				repository_.updatePurchase( Integer.parseInt( purchaseId_ ), price, amount, location );
				repository_.updateProduct( productFromDatabase_.getId(), null, category, null, null );

				Toast.makeText( this, R.string.purchase_updated, Toast.LENGTH_SHORT ).show();
			} else {

				// get date
				Calendar c = Calendar.getInstance();
				SimpleDateFormat sdf = new SimpleDateFormat( Translation.getDatbaseDateFormat() );
				String date = sdf.format( c.getTime() );

				repository_.createPurchase( name, category, price, barcodeString_, amount, date, location );
				Toast.makeText( this, R.string.purchase_created, Toast.LENGTH_SHORT ).show();
			}

			// proceed to DisplayPurchasesActivity
			Intent intent = new Intent( this, ActivityMain.class );
			startActivity( intent );
		}

	}

	@Override
	protected void onResume() {
		repository_.open();
		super.onResume();
	}

	@Override
	protected void onPause() {
		repository_.close();
		super.onPause();
	}
}
