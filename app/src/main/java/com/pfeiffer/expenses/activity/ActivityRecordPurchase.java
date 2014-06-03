package com.pfeiffer.expenses.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.NumberPicker;
import android.widget.Spinner;
import android.widget.Toast;

import com.pfeiffer.expenses.R;
import com.pfeiffer.expenses.model.Barcode;
import com.pfeiffer.expenses.model.EnumCategory;
import com.pfeiffer.expenses.model.EnumLocation;
import com.pfeiffer.expenses.model.Money;
import com.pfeiffer.expenses.model.Purchase;
import com.pfeiffer.expenses.model.PurchaseTemplate;
import com.pfeiffer.expenses.repository.RepositoryManager;
import com.pfeiffer.expenses.utility.UpdatePurchaseTemplates;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ActivityRecordPurchase extends Activity {
    public final String logTag_ = this.getClass().getName();

    private DataFragment dataFragment_;
    private RepositoryManager repositoryManager_;

    private EditText etDate_;
    private Button bScan_;
    private Button bDone_;
    private Spinner sCategory_;
    private Spinner sLocation_;
    private AutoCompleteTextView actvName_;
    private EditText etPrice_;
    private NumberPicker npAmount_;
    private CheckBox cbCash_;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record_purchase);


        repositoryManager_ = new RepositoryManager(this);
        repositoryManager_.open();

        FragmentManager fragmentManager = getFragmentManager();
        dataFragment_ = (DataFragment) fragmentManager.findFragmentByTag("ActivityRecordPurchase");

        // create the fragment and data the first time
        if (dataFragment_ == null) {
            // add the fragment
            dataFragment_ = new DataFragment();
            fragmentManager.beginTransaction().add(dataFragment_, "ActivityRecordPurchase").commit();

            dataFragment_.setPurchaseId(getIntent().getIntExtra(ActivityMain.EXTRA_PURCHASE_ID, -1));
            dataFragment_.setEditMode(dataFragment_.getPurchaseId() > 0);
            dataFragment_.resetCalendar();

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
        bScan_ = (Button) findViewById(R.id.bScanBarcode);
        bDone_ = (Button) findViewById(R.id.bDone);
        etDate_ = (EditText) findViewById(R.id.etPurchaseDate);

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

        updateCalendarTextView();

        npAmount_.setMinValue(1);
        npAmount_.setMaxValue(20);
        npAmount_.setWrapSelectorWheel(false);

        sCategory_.setAdapter(new ArrayAdapter<EnumCategory>(this, android.R.layout.simple_spinner_dropdown_item, EnumCategory
                .values()));

        sLocation_.setAdapter(new ArrayAdapter<EnumLocation>(this, android.R.layout.simple_spinner_dropdown_item, EnumLocation
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
                    setFields(null, pt.getCategory(), pt.getLocation(), pt.getPrice(), pt.getAmount(), pt.isCash(), new Date(System.currentTimeMillis()));
                }
            }
        });

        bDone_.setOnClickListener(new Button.OnClickListener() {

            @Override
            public void onClick(View view) {

                // create a temporary purchase from the ui entries
                Purchase purchase;
                try {

                    purchase = new Purchase(
                            dataFragment_.getPurchaseId(),
                            dataFragment_.getBarcode(),
                            npAmount_.getValue(),
                            dataFragment_.getCalendar().getTime(),
                            EnumLocation.fromString(sLocation_.getSelectedItem().toString()),
                            new Money(etPrice_.getText().toString().trim()),
                            cbCash_.isChecked(),
                            actvName_.getText().toString().trim(),
                            EnumCategory.fromString(sCategory_.getSelectedItem().toString()),
                            Settings.Secure.getString(view.getContext().getContentResolver(), Settings.Secure.ANDROID_ID),
                            -1);

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
                        repositoryManager_.savePurchase(purchase);
                        Toast.makeText(view.getContext(), R.string.purchase_created, Toast.LENGTH_SHORT).show();
                    }

                    // start background service to update the PurchaseTemplate Database
                    startService(new Intent(view.getContext(), UpdatePurchaseTemplates.class));

                    // return to Main Acitivty
                    startActivity(new Intent(view.getContext(), ActivityMain.class));
                    finish();

                } else {
                    Toast.makeText(view.getContext(), R.string.values_not_valid, Toast.LENGTH_SHORT).show();
                }
            }
        });

        final DatePickerDialog.OnDateSetListener date = new DatePickerDialog.OnDateSetListener() {

            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear,
                                  int dayOfMonth) {
                // TODO Auto-generated method stub
                dataFragment_.getCalendar().set(Calendar.YEAR, year);
                dataFragment_.getCalendar().set(Calendar.MONTH, monthOfYear);
                dataFragment_.getCalendar().set(Calendar.DAY_OF_MONTH, dayOfMonth);
                updateCalendarTextView();
            }

        };

        etDate_.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                new DatePickerDialog(v.getContext(), date,
                        dataFragment_.getCalendar().get(Calendar.YEAR),
                        dataFragment_.getCalendar().get(Calendar.MONTH),
                        dataFragment_.getCalendar().get(Calendar.DAY_OF_MONTH)).show();
            }
        });


        configureBarcodeUiElements();
    }

    private void configureBarcodeUiElements() {
        if (dataFragment_.getBarcode() != null) {
            bScan_.setEnabled(false);
        } else {
            bScan_.setEnabled(true);
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
                purchase.getAmount(), purchase.isCash(), purchase.getDate());

        if (purchase.getBarcode() != null) {
            dataFragment_.setBarcode(purchase.getBarcode());
        }
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private void setFields(String name, EnumCategory category, EnumLocation location, Money price, int amount, boolean cash, Date date) {
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
        if (date != null) {
            dataFragment_.getCalendar().setTime(date);
            updateCalendarTextView();
        }
        cbCash_.setChecked(cash);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.record_purchase, menu);
        return true;
    }

    private void updateCalendarTextView() {

        String myFormat = "dd.MM.yyyy"; //In which you need put here
        SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.GERMAN);

        etDate_.setText(sdf.format(dataFragment_.getCalendar().getTime()));
    }

    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (requestCode == 0) {
            if (resultCode == RESULT_OK) {
                dataFragment_.setBarcode(new Barcode(intent.getStringExtra("SCAN_RESULT")));

                if (dataFragment_.getBarcode() != null) {
                    Log.d(logTag_, "Scan result: " + dataFragment_.getBarcode().toString());

                    configureBarcodeUiElements();

                    repositoryManager_.open();
                    // only continue if a product could be obtained
                    Purchase purchase = repositoryManager_.findLatestPurchase(dataFragment_
                            .getBarcode());

                    if (purchase != null) {

                        setFields(purchase.getProductName(), purchase.getCategory(),
                                purchase.getLocation(), purchase.getPrice(), 1, purchase.isCash(), new Date(System.currentTimeMillis()));

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

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
            final Intent activityMainIntent = new Intent(this, ActivityMain.class);
            if (!actvName_.getText().toString().trim().equals("")
                    || dataFragment_.getBarcode() != null) {
                new AlertDialog.Builder(this)
                        .setTitle(R.string.button_cancel)
                        .setMessage(R.string.question_cancel)
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                // return to Main Acitivty
                                startActivity(activityMainIntent);
                                finish();
                            }
                        })
                        .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                // do nothing
                            }
                        })
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .show();
            } else {
                // return to Main Acitivty
                startActivity(activityMainIntent);
                finish();
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    public static class DataFragment extends Fragment {
        Calendar calendar_;
        private Barcode barcode_;
        private long purchaseId_ = -1;
        private boolean editMode_ = false;
        private Map<String, PurchaseTemplate> productNameToPurchaseTemplate_ = new HashMap<String, PurchaseTemplate>();
        private String templateProductName_[];

        public DataFragment() {

        }

        // this method is only called once for this fragment
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            // retain this fragment
            setRetainInstance(true);


        }

        public Calendar getCalendar() {
            return calendar_;
        }


        public void resetCalendar() {
            calendar_ = Calendar.getInstance();
            calendar_.setTime(new Date(System.currentTimeMillis()));
        }


        public void setBarcode(Barcode barcode) {
            barcode_ = barcode;
        }

        public void setPurchaseId(long purchaseId) {
            purchaseId_ = purchaseId;
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

        public Barcode getBarcode() {
            return barcode_;
        }

        public long getPurchaseId() {
            return purchaseId_;
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
