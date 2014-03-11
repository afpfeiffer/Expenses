package com.pfeiffer.expensesassistant;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class ActivityEnterName extends Activity {
	Button button2;
	public final static String EXTRA_NAME = "name";
	public final static String EXTRA_BARCODE = "barcode";

	@Override
	protected void onCreate( Bundle savedInstanceState ) {
		super.onCreate( savedInstanceState );
		setContentView( R.layout.activity_enter_name );
		button2 = ( Button )findViewById( R.id.button2 );
		button2.setOnClickListener( mScan );
	}

	@Override
	public boolean onCreateOptionsMenu( Menu menu ) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate( R.menu.enter_name, menu );
		return true;
	}

	public void recordPurchase( View view ) {
		Intent intent = new Intent( this, ActivityRecordPurchase.class );
		EditText editText = ( EditText )findViewById( R.id.enterName );
		String message = editText.getText().toString();
		// TODO: check if message is valid
		Log.d( this.getClass().getName(), "recordPurchase() records value " + message + " from field enterName." );
		intent.putExtra( EXTRA_NAME, message );
		startActivity( intent );
	}

	public Button.OnClickListener mScan = new Button.OnClickListener() {
		public void onClick( View v ) {
			Intent intent = new Intent( "com.google.zxing.client.android.SCAN" );
			// intent.putExtra( "SCAN_MODE", "QR_CODE_MODE" );
			intent.putExtra( "com.google.zxing.client.android.SCAN.SCAN_MODE", "QR_CODE_MODE" );
			startActivityForResult( intent, 0 );
		}
	};

	public void onActivityResult( int requestCode, int resultCode, Intent intent ) {
		if( requestCode == 0 ) {
			if( resultCode == RESULT_OK ) {
				String contents = intent.getStringExtra( "SCAN_RESULT" );
				// String format = intent.getStringExtra( "SCAN_RESULT_FORMAT"
				// );
				// Handle successful scan

				Log.d( this.getClass().getName(), "Scan result: " + contents );
				// Log.d( this.getClass().getName(), "Scan format: " + format );

				Intent recordPurchasesIntent = new Intent( this, ActivityRecordPurchase.class );
				recordPurchasesIntent.putExtra( EXTRA_BARCODE, contents );

				startActivity( recordPurchasesIntent );

			} else if( resultCode == RESULT_CANCELED ) {
				// Handle cancel
			}
		}
	}
}
