package com.pfeiffer.expenses.activity;

import android.annotation.SuppressLint;
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
import com.pfeiffer.expenses.utility.Translation;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class ActivityRecordPurchase extends Activity {
    public final String logTag_ = this.getClass().getName();

    private final Button.OnClickListener mScan = new Button.OnClickListener() {
        public void onClick(View v) {
            Intent intent = new Intent("com.google.zxing.client.android.SCAN");
            intent.putExtra("com.google.zxing.client.android.SCAN.SCAN_MODE", "QR_CODE_MODE");
            startActivityForResult(intent, 0);
        }
    };

    private RepositoryManager repositoryManager_;
    private String barcodeString_;
    private Product productFromDatabase_;
    private Button scanButton_;
    private EditText name_;
    private Spinner category_;
    private Spinner location_;
    private NumberPicker amount_;
    private EditText price_;
    private CheckBox cash_;
    private TextView barcodeStatus_;
    private String purchaseId_ = null;
    private boolean editMode_ = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record_purchase);

        repositoryManager_ = new RepositoryManager(this);
        repositoryManager_.open();

        purchaseId_ = getIntent().getStringExtra(ActivityMain.EXTRA_PURCHASE_ID);
        editMode_ = (purchaseId_ != null && !purchaseId_.equals(""));


        initUi();
        configureUi();
        fillUi();

    }

    private void initUi() {
        name_ = (EditText) findViewById(R.id.editText1);
        category_ = (Spinner) findViewById(R.id.spinner1);
        location_ = (Spinner) findViewById(R.id.spinner2);
        amount_ = (NumberPicker) findViewById(R.id.editText2);
        price_ = (EditText) findViewById(R.id.editText3);
        cash_ = (CheckBox) findViewById(R.id.checkBoxCash);
        barcodeStatus_ = (TextView) findViewById(R.id.textViewBarcodeScanned);
        scanButton_ = (Button) findViewById(R.id.button_scan_barcode);
    }

    private void configureUi() {

        amount_.setMinValue(1);
        amount_.setMaxValue(20);
        amount_.setWrapSelectorWheel(false);
        amount_.setValue(1);

        cash_.setChecked(false);

        category_.setAdapter(new ArrayAdapter<CATEGORY>(this, android.R.layout.simple_spinner_dropdown_item, CATEGORY
                .values()));

        location_.setAdapter(new ArrayAdapter<LOCATION>(this, android.R.layout.simple_spinner_dropdown_item, LOCATION
                .values()));


        if (editMode_) {
            scanButton_.setVisibility(View.GONE);
            barcodeStatus_.setVisibility(View.GONE);
        } else {
            barcodeStatus_.setVisibility(View.GONE);
            scanButton_.setOnClickListener(mScan);
        }

    }

    private void fillUi() {

        if (editMode_) {
            Purchase purchase = repositoryManager_.findPurchase(Integer.parseInt(purchaseId_));
            if (purchase == null)
                throw new IllegalStateException();
            productFromDatabase_ = repositoryManager_.findProduct(purchase.getProductId());
            if (productFromDatabase_ == null)
                throw new IllegalStateException();
            setFields(
                    productFromDatabase_.getName(),
                    productFromDatabase_.getCategory(),
                    purchase.getLocation(),
                    purchase.getPrice(),
                    Integer.parseInt(purchase.getAmount()), purchase.isCash());
            barcodeString_ = productFromDatabase_.getBarcode().toString();
            if (barcodeString_ != null && !barcodeString_.equals(""))
                barcodeStatus_.setVisibility(View.VISIBLE);
            return;
        }

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

    @SuppressLint("SimpleDateFormat")
    public void onClick(View view) {
        String name;
        name = name_.getText().toString().trim();
        String strCategory;
        strCategory = category_.getSelectedItem().toString();
        String strLocation;
        strLocation = location_.getSelectedItem().toString();
        int amount = amount_.getValue();
        String price = price_.getText().toString().trim();

        if (price.length() - price.replaceAll("\\.", "").length() > 1) {
            Toast.makeText(this, R.string.price_not_valid, Toast.LENGTH_SHORT).show();
            return;
        }


        Log.d(logTag_, "Method onClick() records name=" + name + ", category="
                + strCategory + ", location=" + strLocation + ", amount=" + amount + ", price=" + price);

        // make sure that all field values are set.
        if (name != null && !name.isEmpty() && strCategory != null && !strCategory.equals(CATEGORY.NONE.toString()) &&
                strLocation != null && !strLocation.equals(LOCATION.NONE.toString()) && price != null && !price
                .replaceAll("\\.", "").isEmpty()) {

            CATEGORY category = CATEGORY.fromString(strCategory);
            LOCATION location = LOCATION.fromString(strLocation);
            price = Translation.getValidPrice(price);
            boolean cash = cash_.isChecked();

            if (editMode_) {
                repositoryManager_.updatePurchase(Integer.parseInt(purchaseId_), price, amount, location, cash);
                repositoryManager_.updateProduct(productFromDatabase_.getId(), null, category, null, null);

                Toast.makeText(this, R.string.purchase_updated, Toast.LENGTH_SHORT).show();
            } else {

                // get date
                Calendar c = Calendar.getInstance();
                SimpleDateFormat sdf = new SimpleDateFormat(Translation.getDatbaseDateFormat());
                String date = sdf.format(c.getTime());

                repositoryManager_.createPurchase(name, category, price, barcodeString_, amount, date, location, cash);
                Toast.makeText(this, R.string.purchase_created, Toast.LENGTH_SHORT).show();
            }

            // proceed to DisplayPurchasesActivity
            Intent intent = new Intent(this, ActivityMain.class);
            startActivity(intent);
        } else {
            Toast.makeText(this, R.string.values_not_valid, Toast.LENGTH_SHORT).show();
        }

    }

    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (requestCode == 0) {
            if (resultCode == RESULT_OK) {
                String contents = intent.getStringExtra("SCAN_RESULT");

                Log.d(logTag_, "Scan result: " + contents);

                if (contents != null && !contents.equals("")) {
                    barcodeString_ = contents;
                    scanButton_.setVisibility(View.GONE);
                    barcodeStatus_.setVisibility(View.VISIBLE);

                    repositoryManager_.open();
                    productFromDatabase_ = repositoryManager_.findProduct(new Barcode(barcodeString_));
                    // only continue if a product could be obtained
                    if (productFromDatabase_ != null) {
                        Purchase purchaseFromDatabase = repositoryManager_.findPurchase(productFromDatabase_.getId());
                        setFields(
                                productFromDatabase_.getName(),
                                productFromDatabase_.getCategory(),
                                (purchaseFromDatabase != null) ? purchaseFromDatabase.getLocation() : null,
                                productFromDatabase_.getPrice(),
                                1, (purchaseFromDatabase != null) ? purchaseFromDatabase.isCash() : false);
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
