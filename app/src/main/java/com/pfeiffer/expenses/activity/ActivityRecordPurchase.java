package com.pfeiffer.expenses.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.NumberPicker;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.pfeiffer.expenses.R;
import com.pfeiffer.expenses.model.Barcode;
import com.pfeiffer.expenses.model.CATEGORY;
import com.pfeiffer.expenses.model.LOCATION;
import com.pfeiffer.expenses.model.Product;
import com.pfeiffer.expenses.model.Purchase;
import com.pfeiffer.expenses.repository.RepositoryManager;
import com.pfeiffer.expenses.repository.UpdatePurchaseTemplates;

import java.util.Date;

public class ActivityRecordPurchase extends Activity {
    public final String logTag_ = this.getClass().getName();

    private final Button.OnClickListener mScan = new Button.OnClickListener() {
        public void onClick(View v) {
            Intent intent = new Intent("com.google.zxing.client.android.SCAN");
            intent.putExtra("com.google.zxing.client.android.SCAN.SCAN_MODE", "QR_CODE_MODE");
            startActivityForResult(intent, 0);
        }
    };

    // TODO: http://developer.android.com/guide/topics/resources/runtime-changes.html
    // Retain Objects through configuration change

    private RepositoryManager repositoryManager_;

    private Button scanButton_;
    private EditText name_;
    private EditText price_;
    private Spinner category_;
    private Spinner location_;
    private NumberPicker amount_;
    private CheckBox cash_;
    private TextView barcodeStatus_;

    private String barcodeString_;

    private int purchaseId_ = -1;
    private int productId_ = -1;

    private boolean editMode_ = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record_purchase);

        repositoryManager_ = new RepositoryManager(this);
        repositoryManager_.open();

        initializeUiElements();
        configureUiElements();


        purchaseId_ = getIntent().getIntExtra(ActivityMain.EXTRA_PURCHASE_ID, -1);
        editMode_ = (purchaseId_ > 0);
        if (editMode_)
            fillUiElementsWithRequestedPurchase();

    }

    private void initializeUiElements() {
        name_ = (EditText) findViewById(R.id.editText1);
        category_ = (Spinner) findViewById(R.id.spinner1);
        location_ = (Spinner) findViewById(R.id.spinner2);
        amount_ = (NumberPicker) findViewById(R.id.editText2);
        price_ = (EditText) findViewById(R.id.editText3);
        cash_ = (CheckBox) findViewById(R.id.checkBoxCash);
        barcodeStatus_ = (TextView) findViewById(R.id.textViewBarcodeScanned);
        scanButton_ = (Button) findViewById(R.id.button_scan_barcode);
    }

    private void configureUiElements() {

        amount_.setMinValue(1);
        amount_.setMaxValue(20);
        amount_.setWrapSelectorWheel(false);
        amount_.setValue(1);

        cash_.setChecked(false);

        category_.setAdapter(new ArrayAdapter<CATEGORY>(this, android.R.layout.simple_spinner_dropdown_item, CATEGORY
                .values()));

        location_.setAdapter(new ArrayAdapter<LOCATION>(this, android.R.layout.simple_spinner_dropdown_item, LOCATION
                .values()));

        scanButton_.setOnClickListener(mScan);

        configureBarcodeUiElements(false);
    }

    private void configureBarcodeUiElements(boolean barcodeAvailable) {
        if (barcodeAvailable) {
            scanButton_.setVisibility(View.GONE);
            barcodeStatus_.setVisibility(View.VISIBLE);
        } else {
            scanButton_.setVisibility(View.VISIBLE);
            barcodeStatus_.setVisibility(View.GONE);
        }
    }

    private void fillUiElementsWithRequestedPurchase() {
        Purchase purchase = repositoryManager_.findPurchaseById(purchaseId_);
        if (purchase == null)
            throw new IllegalStateException();
        setFields(
                purchase.getProductName(),
                purchase.getCategory(),
                purchase.getLocation(),
                purchase.getPrice(),
                purchase.getAmount(), purchase.isCash());

        if (purchase.hasProductAttached()) {
            Product product = repositoryManager_.findProductById(purchase.getProductId());

            if (product == null) throw new IllegalStateException();

            barcodeString_ = product.getBarcode().toString();

            if (barcodeString_ == null || barcodeString_.equals("")) throw new IllegalStateException();

            configureBarcodeUiElements(true);

            productId_ = product.getId();
        }
        return;

    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private void setFields(String name, CATEGORY category, LOCATION location, String price, int amount, boolean cash) {
        if (name != null && !name.equals("")) {
            name_.setText(name);
        }
        if (category != null) {
            ArrayAdapter arrayAdapterCategory = (ArrayAdapter) category_.getAdapter();
            category_.setSelection(arrayAdapterCategory.getPosition(category));
        }
        if (location != null) {
            ArrayAdapter arrayAdapterCategory = (ArrayAdapter) location_.getAdapter();
            location_.setSelection(arrayAdapterCategory.getPosition(location));
        }
        if (price != null && !price.equals("")) {
            price_.setText(price);
        }
        if (amount > 0) {
            amount_.setValue(amount);
        }
        cash_.setChecked(cash);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.record_purchase, menu);
        return true;
    }


    public void onClick(View view) {

        // create a temporary purchase from the ui entries
        Purchase purchase = new Purchase(purchaseId_, productId_, amount_.getValue(),
                new Date(System.currentTimeMillis()), LOCATION.fromString(location_.getSelectedItem().toString()),
                price_.getText().toString().trim(), cash_.isChecked(),
                name_.getText().toString().trim(), CATEGORY.fromString(category_.getSelectedItem().toString()) );


        // make sure that all field values are set correctly.
        if (purchase.hasValidState()) {


            if (editMode_) {
                // if in edit mode: update purchase and product (if applies)
                repositoryManager_.updatePurchase(purchase);
                Toast.makeText(this, R.string.purchase_updated, Toast.LENGTH_SHORT).show();
            } else {
                // else: create purchase and product (if applies)
                repositoryManager_.createPurchaseAndProduct(purchase, new Barcode(barcodeString_));
                Toast.makeText(this, R.string.purchase_created, Toast.LENGTH_SHORT).show();
            }

            // start background service to update the PurchaseTemplate Database
            startService(new Intent(this, UpdatePurchaseTemplates.class));

            // return to Main Acitivty
            startActivity(new Intent(this, ActivityMain.class));

        } else {
            Toast.makeText(this, R.string.values_not_valid, Toast.LENGTH_SHORT).show();
        }

    }

    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (requestCode == 0) {
            if (resultCode == RESULT_OK) {
                barcodeString_ = intent.getStringExtra("SCAN_RESULT");

                if (barcodeString_ != null && !barcodeString_.equals("")) {
                    Log.d(logTag_, "Scan result: " + barcodeString_);

                    configureBarcodeUiElements(true);

                    repositoryManager_.open();
                    Product product = repositoryManager_.findProductByBarcode(new Barcode(barcodeString_));
                    // only continue if a product could be obtained
                    if (product != null) {
                        productId_ = product.getId();
                        Purchase purchaseFromDatabase = repositoryManager_.findLatestPurchase(product);
                        if (purchaseFromDatabase != null) {
                            setFields(purchaseFromDatabase.getProductName(), purchaseFromDatabase.getCategory(),
                                    purchaseFromDatabase.getLocation(), purchaseFromDatabase.getPrice(), 1, false);
                        } else {
                            setFields(product.getName(), null , null, null, 1, false);
                        }
                    }
                }
            } else if (resultCode == RESULT_CANCELED) {
                // TODO Handle cancel
            }
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.
                INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
        return true;
    }


    @Override
    protected void onResume() {
        repositoryManager_.open();
        super.onResume();
    }

    @Override
    protected void onPause() {
        repositoryManager_.close();
        super.onPause();
    }
}
