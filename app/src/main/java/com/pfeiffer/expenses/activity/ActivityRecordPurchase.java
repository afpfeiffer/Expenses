package com.pfeiffer.expenses.activity;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.NumberPicker;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.pfeiffer.expenses.R;
import com.pfeiffer.expenses.model.Barcode;
import com.pfeiffer.expenses.model.Category;
import com.pfeiffer.expenses.model.Location;
import com.pfeiffer.expenses.model.Money;
import com.pfeiffer.expenses.model.Product;
import com.pfeiffer.expenses.model.Purchase;
import com.pfeiffer.expenses.model.PurchaseTemplate;
import com.pfeiffer.expenses.repository.RepositoryManager;
import com.pfeiffer.expenses.repository.UpdatePurchaseTemplates;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ActivityRecordPurchase extends Activity {
    public final String logTag_ = this.getClass().getName();

    // TODO: http://developer.android.com/guide/topics/resources/runtime-changes.html
    // Retain Objects through configuration change

    private DataFragment dataFragment_;
    private RepositoryManager repositoryManager_;

    private Button bScan_;
    private Button bDone_;
    private AutoCompleteTextView actvName_;
    private EditText etPrice_;
    private Spinner sCategory_;
    private Spinner sLocation_;
    private NumberPicker npAmount_;
    private CheckBox cbCash_;
    private TextView tvBarcodeStatus_;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record_purchase);


        repositoryManager_ = new RepositoryManager(this);
        repositoryManager_.open();

        FragmentManager fragmentManager = getFragmentManager();
        dataFragment_ = (DataFragment) fragmentManager.findFragmentByTag("recordPurchaseData");

        // create the fragment and data the first time
        if (dataFragment_ == null) {
            // add the fragment
            dataFragment_ = new DataFragment();
            fragmentManager.beginTransaction().add(dataFragment_, "recordPurchaseData").commit();

            dataFragment_.setPurchaseId(getIntent().getIntExtra(ActivityMain.EXTRA_PURCHASE_ID, -1));
            dataFragment_.setEditMode(dataFragment_.getPurchaseId() > 0);

            initializeUiElements();
            preparePurchaseTemplates();
            configureUiElements();

            if (dataFragment_.isEditMode_())
                fillUiElementsWithRequestedPurchase();

        } else {
            initializeUiElements();
            configureUiElements();
        }
    }

    private void initializeUiElements() {
        actvName_ = (AutoCompleteTextView) findViewById(R.id.actvName);
        sCategory_ = (Spinner) findViewById(R.id.sCategory);
        sLocation_ = (Spinner) findViewById(R.id.sLocation);
        npAmount_ = (NumberPicker) findViewById(R.id.npAmount);
        etPrice_ = (EditText) findViewById(R.id.etPrice);
        cbCash_ = (CheckBox) findViewById(R.id.cbCash);
        tvBarcodeStatus_ = (TextView) findViewById(R.id.tvBarcodeStatus);
        bScan_ = (Button) findViewById(R.id.bScanBarcode);
        bDone_=(Button) findViewById(R.id.bDone);
    }

    private void preparePurchaseTemplates() {
        // get a list of all purchase templates (# should be limited)
        List<PurchaseTemplate> purchaseTemplatesList = repositoryManager_.getAllPurchaseTemplates();
        int len = purchaseTemplatesList.size();
        // create an array that will be connected to the AutoCompleteTextView
        String tempArray[] = new String[len];
        Map<String, PurchaseTemplate> tempMap = new HashMap<String, PurchaseTemplate>();
        for (int i = 0; i < len; ++i) {
            PurchaseTemplate pt = purchaseTemplatesList.get(i);
            // populate the array
            tempArray[i] = pt.getProductName();
            // populate the the map that will help to automatically fill the fields after name is entered
            tempMap.put(pt.getProductName(), pt);
        }
        dataFragment_.setTemplateProductName(tempArray);
        dataFragment_.setProductNameToPurchaseTemplate(tempMap);
    }

    private void configureUiElements() {
        npAmount_.setMinValue(1);
        npAmount_.setMaxValue(20);
        npAmount_.setWrapSelectorWheel(false);

        sCategory_.setAdapter(new ArrayAdapter<Category>(this, android.R.layout.simple_spinner_dropdown_item, Category
                .values()));

        sLocation_.setAdapter(new ArrayAdapter<Location>(this, android.R.layout.simple_spinner_dropdown_item, Location
                .values()));

        bScan_.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent("com.google.zxing.client.android.SCAN");
                intent.putExtra("com.google.zxing.client.android.SCAN.SCAN_MODE", "QR_CODE_MODE");
                startActivityForResult(intent, 0);
            }
        });

        // connect the array of product names to the AutoCompleteTextView
        actvName_.setAdapter(new ArrayAdapter(this, android.R.layout.simple_list_item_1,
                dataFragment_.getTemplateProductName()));

        // configure the OnItemClickListener to populate the remaining fields when the product name is selected from
        // drop-down.
        actvName_.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {

                String productName = actvName_.getText().toString();
                PurchaseTemplate pt = dataFragment_.getProductNameToPurchaseTemplate().get(productName);
                if (pt != null) {
                    setFields(null, pt.getCategory(), pt.getLocation(), pt.getPrice(), pt.getAmount(), false);
                }
            }
        });

        bDone_.setOnClickListener(new Button.OnClickListener(){

            @Override
            public void onClick(View view) {

                // create a temporary purchase from the ui entries
                Purchase purchase;
                try {
                    purchase = new Purchase(dataFragment_.getPurchaseId(), dataFragment_.getProductId(), npAmount_.getValue(),
                            new Date(System.currentTimeMillis()), Location.fromString(sLocation_.getSelectedItem().toString()),
                            new Money(etPrice_.getText().toString().trim()), cbCash_.isChecked(),
                            actvName_.getText().toString().trim(), Category.fromString(sCategory_.getSelectedItem().toString()));
                } catch (IllegalArgumentException e) {
                    Toast.makeText(view.getContext(), R.string.values_not_valid, Toast.LENGTH_SHORT).show();
                    return;
                }

                // make sure that all field values are set correctly.
                if (purchase.hasValidState()) {


                    if (dataFragment_.isEditMode_()) {
                        // if in edit mode: update purchase and product (if applies)
                        repositoryManager_.updatePurchase(purchase);
                        Toast.makeText(view.getContext(), R.string.purchase_updated, Toast.LENGTH_SHORT).show();
                    } else {
                        // else: create purchase and product (if applies)
                        repositoryManager_.createPurchaseAndProduct(purchase, new Barcode(dataFragment_.getBarcodeString()));
                        Toast.makeText(view.getContext(), R.string.purchase_created, Toast.LENGTH_SHORT).show();
                    }

                    // start background service to update the PurchaseTemplate Database
                    startService(new Intent(view.getContext(), UpdatePurchaseTemplates.class));

                    // return to Main Acitivty
                    startActivity(new Intent(view.getContext(), ActivityMain.class));

                } else {
                    Toast.makeText(view.getContext(), R.string.values_not_valid, Toast.LENGTH_SHORT).show();
                }

            }

        });

        configureBarcodeUiElements();
    }

    private void configureBarcodeUiElements() {
        if (dataFragment_.getBarcodeString() != null && !dataFragment_.getBarcodeString().equals("")) {
            bScan_.setVisibility(View.GONE);
            tvBarcodeStatus_.setVisibility(View.VISIBLE);
        } else {
            bScan_.setVisibility(View.VISIBLE);
            tvBarcodeStatus_.setVisibility(View.GONE);
        }
    }

    private void fillUiElementsWithRequestedPurchase() {
        Purchase purchase = repositoryManager_.findPurchaseById(dataFragment_.getPurchaseId());
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

            dataFragment_.setBarcodeString(product.getBarcode().toString());

            if (dataFragment_.getBarcodeString() == null || dataFragment_.getBarcodeString().equals(""))
                throw new IllegalStateException();

            dataFragment_.setProductId(product.getId());
        }
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private void setFields(String name, Category category, Location location, Money price, int amount, boolean cash) {
        if (name != null && !name.equals("")) {
            actvName_.setText(name);
        }
        if (category != null) {
            ArrayAdapter arrayAdapterCategory = (ArrayAdapter) sCategory_.getAdapter();
            sCategory_.setSelection(arrayAdapterCategory.getPosition(category));
        }
        if (location != null) {
            ArrayAdapter arrayAdapterCategory = (ArrayAdapter) sLocation_.getAdapter();
            sLocation_.setSelection(arrayAdapterCategory.getPosition(location));
        }
        if (price != null && price.isValid()) {
            etPrice_.setText(price.toString());
        }
        if (amount > 0) {
            npAmount_.setValue(amount);
        }
        cbCash_.setChecked(cash);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.record_purchase, menu);
        return true;
    }


    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (requestCode == 0) {
            if (resultCode == RESULT_OK) {
                dataFragment_.setBarcodeString(intent.getStringExtra("SCAN_RESULT"));

                if (dataFragment_.getBarcodeString() != null && !dataFragment_.getBarcodeString().equals("")) {
                    Log.d(logTag_, "Scan result: " + dataFragment_.getBarcodeString());

                    configureBarcodeUiElements();

                    repositoryManager_.open();
                    Product product = repositoryManager_.findProductByBarcode(new Barcode(dataFragment_.getBarcodeString()));
                    // only continue if a product could be obtained
                    if (product != null) {
                        dataFragment_.setProductId(product.getId());
                        Purchase purchaseFromDatabase = repositoryManager_.findLatestPurchase(product);
                        if (purchaseFromDatabase != null) {
                            setFields(purchaseFromDatabase.getProductName(), purchaseFromDatabase.getCategory(),
                                    purchaseFromDatabase.getLocation(), purchaseFromDatabase.getPrice(), 1, false);
                        } else {
                            setFields(product.getName(), null, null, null, 1, false);
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

    private class DataFragment extends Fragment {
        private String barcodeString_;
        private int purchaseId_ = -1;
        private int productId_ = -1;
        private boolean editMode_ = false;
        private Map<String, PurchaseTemplate> productNameToPurchaseTemplate_ = new HashMap<String, PurchaseTemplate>();
        private String templateProductName_[];

        // this method is only called once for this fragment
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            // retain this fragment
            setRetainInstance(true);
        }

        public void setBarcodeString(String barcodeString) {
            barcodeString_ = barcodeString;
        }

        public void setPurchaseId(int purchaseId) {
            purchaseId_ = purchaseId;
        }

        public void setProductId(int productId) {
            productId_ = productId;
        }

        public void setEditMode(boolean editMode) {
            editMode_ = editMode;
        }

        public void setProductNameToPurchaseTemplate(Map<String, PurchaseTemplate> productNameToPurchaseTemplate) {
            productNameToPurchaseTemplate_ = productNameToPurchaseTemplate;
        }

        public void setTemplateProductName(String templateProductName[]) {
            templateProductName_ = templateProductName;
        }

        public String getBarcodeString() {
            return barcodeString_;
        }

        public int getPurchaseId() {
            return purchaseId_;
        }

        public int getProductId() {
            return productId_;
        }

        public boolean isEditMode_() {
            return editMode_;
        }

        public Map<String, PurchaseTemplate> getProductNameToPurchaseTemplate() {
            return productNameToPurchaseTemplate_;
        }

        public String[] getTemplateProductName() {
            return templateProductName_;
        }

    }
}
