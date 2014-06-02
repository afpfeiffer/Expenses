package com.pfeiffer.expenses.activity;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.DatePicker;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.ExpandableListContextMenuInfo;
import android.widget.SimpleExpandableListAdapter;
import android.widget.TextView;

import com.pfeiffer.expenses.R;
import com.pfeiffer.expenses.model.EnumCategory;
import com.pfeiffer.expenses.model.Money;
import com.pfeiffer.expenses.model.Purchase;
import com.pfeiffer.expenses.repository.RepositoryManager;
import com.pfeiffer.expenses.utility.DataAnalysis;
import com.pfeiffer.expenses.utility.Translation;

import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * @author axelpfeiffer
 */
public class ActivityMain extends Activity {

    // TODO: http://developer.android.com/guide/topics/resources/runtime-changes.html
    // Retain Objects through configuration change


    public static final String EXTRA_PURCHASE_ID = "purchaseId";
    private final String logTag_ = this.getClass().getName();

    private RepositoryManager repositoryManager_;
    private ExpandableListView expandableListView_;
    private SimpleExpandableListAdapter expListAdapter;
    private TextView currentMonth_;
    private TextView totalExpenses_;

    DataFragment dataFragment_;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        expandableListView_ = (ExpandableListView) findViewById(R.id.expandableListViewMain);
        currentMonth_ = (TextView) findViewById(R.id.tvCurrentMonth);
        totalExpenses_ = (TextView) findViewById(R.id.tvTotalExpenses);

        FragmentManager fragmentManager = getFragmentManager();
        dataFragment_ = (DataFragment) fragmentManager.findFragmentByTag("ActivityMain");

        if (dataFragment_ == null) {
            // add the fragment
            dataFragment_ = new DataFragment();
            fragmentManager.beginTransaction().add(dataFragment_, "ActivityMain").commit();

            dataFragment_.resetCalendar(); 
        }
        loadData();

        final DatePickerDialog.OnDateSetListener date = new DatePickerDialog.OnDateSetListener() {

            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear,
                                  int dayOfMonth) {
                // TODO Auto-generated method stub
                dataFragment_.getCalendar().set(Calendar.YEAR, year);
                dataFragment_.getCalendar().set(Calendar.MONTH, monthOfYear);
                dataFragment_.getCalendar().set(Calendar.DAY_OF_MONTH, dayOfMonth);
                loadData();
            }

        };

        currentMonth_.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                DatePickerDialog pickerDialog = new DatePickerDialog(ActivityMain.this, date,
                        dataFragment_.getCalendar().get(Calendar.YEAR),
                        dataFragment_.getCalendar().get(Calendar.MONTH),
                        dataFragment_.getCalendar().get(Calendar.DAY_OF_MONTH)
                );
                DatePicker picker = pickerDialog.getDatePicker();

                try {
                    Field f[] = picker.getClass().getDeclaredFields();
                    for (Field field : f) {
                        if (field.getName().equals("mDayPicker") || field.getName().equals("mDaySpinner")) {
                            field.setAccessible(true);
                            Object dayPicker = new Object();
                            dayPicker = field.get(picker);
                            ((View) dayPicker).setVisibility(View.GONE);
                        }
                    }
                } catch (SecurityException e) {
                    Log.d("ERROR", e.getMessage());
                } catch (IllegalArgumentException e) {
                    Log.d("ERROR", e.getMessage());
                } catch (IllegalAccessException e) {
                    Log.d("ERROR", e.getMessage());
                }

                pickerDialog.show();
            }
        });


        registerForContextMenu(expandableListView_);
    }

    @Override
    protected void onPause() {
        super.onPause();
        repositoryManager_.close();
    }

    private void loadData() {
        dataFragment_.setMylist(new ArrayList<ArrayList<HashMap<String, String>>>());
        dataFragment_.setMylist_title(new ArrayList<HashMap<String, String>>());

        // get all Purchases
        repositoryManager_ = new RepositoryManager(this);
        repositoryManager_.open();

        List<Purchase> purchasesCurrentMonth = repositoryManager_.getAllPurchasesForDateRange(
                Translation.getFirstDateOfMonth(dataFragment_.getCalendar().getTime()),
                Translation.getFirstDateOfNextMonth(dataFragment_.getCalendar().getTime()));

        DataAnalysis dataAnalysis = new DataAnalysis(repositoryManager_, purchasesCurrentMonth);

        String myFormat = "MMMM yyyy"; //In which you need put here
        SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.GERMAN);

        currentMonth_.setText(sdf.format(dataFragment_.getCalendar().getTime()));

        Money totalExpenses = dataAnalysis.getExpensesPerMonth(dataFragment_.getCalendar().getTime());
        totalExpenses_.setText(totalExpenses.getHumanReadableRepresentation());

        List<Map.Entry<EnumCategory, Money>> categoryToExpenses = dataAnalysis.getSortedCategoryToExpensesForYearAndMonth
                (dataFragment_.getCalendar().getTime());

        int len = categoryToExpenses.size();
        for (int i = 0; i < len; ++i) {
            Map.Entry<EnumCategory, Money> entry = categoryToExpenses.get(len - 1 - i);
            EnumCategory category = entry.getKey();
            List<Purchase> purchases = dataAnalysis.getPurchasesForYearMonthAndCategory(
                    dataFragment_.getCalendar().getTime(), category);
            if (purchases != null && !purchases.isEmpty()) {
                ArrayList<HashMap<String, String>> secList = new ArrayList<HashMap<String, String>>();

                dataFragment_.setMap1(new HashMap<String, String>());
                int percentage = entry.getValue().percentage(totalExpenses);
                dataFragment_.getMap1().put("rowCategoryName", category.toString() + " (" + percentage + "%)");
                dataFragment_.getMap1().put("rowCategoryExpenses", entry.getValue().getHumanReadableRepresentation());
                dataFragment_.getMylist_title().add(dataFragment_.getMap1());

                for (Purchase purchase : purchases) {

                    dataFragment_.setMap2(new HashMap<String, String>());
                    dataFragment_.getMap2().put("purchaseId", String.valueOf(purchase.getId()));
                    dataFragment_.getMap2().put("rowDate", Translation.shortDate(purchase.getDate()));
                    dataFragment_.getMap2().put("rowName", ((purchase.getAmount() > 1) ? purchase.getAmount() + "x " : "")
                            + purchase.getProductName());

                    dataFragment_.getMap2().put("rowTotalPrice", purchase.getTotalHumanReadablePrice());
                    secList.add(dataFragment_.getMap2());
                }

                dataFragment_.getMylist().add(secList);
            }
        }


        try {
            expListAdapter = new SimpleExpandableListAdapter(this, dataFragment_.getMylist_title(), R.layout.group_row, new String[]{
                    "rowCategoryName", "rowCategoryExpenses"}, new int[]{R.id.rowCategoryName, R.id.rowCategoryExpenses},
                    dataFragment_.getMylist(), R.layout.row, new String[]{"rowDate", "rowName", "rowTotalPrice"}, new int[]{
                    R.id.rowDate, R.id.rowName, R.id.rowTotalPrice}
            );
            expandableListView_.setAdapter(expListAdapter);
        } catch (Exception e) {
// TODO
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
                // About option clicked.
//                Log.d(logTag_, repositoryManager_.getAllPurchaseTemplates().toString());
                return true;
            case R.id.action_sync_data:
//                startActivity(new Intent(this, ActivitySyncData.class));
                startActivity(new Intent(this, ActivityShareData.class));
                finish();
                return true;

            case R.id.action_record_purchase:
                Intent intent = new Intent(this, ActivityRecordPurchase.class);
                startActivity(intent);
                finish();
                return true;

            case R.id.action_record_shopping:
                Intent shoppingIntent = new Intent(this, ActivityShopping.class);
                startActivity(shoppingIntent);
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);

        ExpandableListView.ExpandableListContextMenuInfo info = (ExpandableListView.ExpandableListContextMenuInfo) menuInfo;

        int type = ExpandableListView.getPackedPositionType(info.packedPosition);

        int group = ExpandableListView.getPackedPositionGroup(info.packedPosition);

        int child = ExpandableListView.getPackedPositionChild(info.packedPosition);

        // Only create a context menu for child items
        if (type == ExpandableListView.PACKED_POSITION_TYPE_CHILD) {
            // Array created earlier when we built the expandable list
            menu.setHeaderTitle(dataFragment_.getMylist().get(group).get(child).get("rowName"));
            menu.add(0, 0, 0, "Bearbeiten"); //TODO
            menu.add(0, 1, 0, "Löschen"); //TODO
            Log.d(logTag_, "ContextMenu created for item with id "
                    + dataFragment_.getMylist().get(group).get(child).get("purchaseId"));
        }
    }

    public boolean onContextItemSelected(MenuItem menuItem) {
        ExpandableListContextMenuInfo info = (ExpandableListContextMenuInfo) menuItem.getMenuInfo();

        int groupPos = 0, childPos = 0;

        assert info != null;
        int type = ExpandableListView.getPackedPositionType(info.packedPosition);
        if (type == ExpandableListView.PACKED_POSITION_TYPE_CHILD) {
            groupPos = ExpandableListView.getPackedPositionGroup(info.packedPosition);
            childPos = ExpandableListView.getPackedPositionChild(info.packedPosition);
        }

        // Pull values from the array we built when we created the list

        switch (menuItem.getItemId()) {
            case 0:

                return editPurchase(groupPos, childPos);

            case 1:
                return deletePurchase(groupPos, childPos);

            default:
                return super.onContextItemSelected(menuItem);
        }
    }

    private boolean editPurchase(int groupPos, int childPos) {
        int purchaseId = Integer.parseInt(dataFragment_.getMylist().get(groupPos).get(childPos).get("purchaseId"));
        Intent intent = new Intent(this, ActivityRecordPurchase.class);
        intent.putExtra(EXTRA_PURCHASE_ID, purchaseId);
        startActivity(intent);
        finish();

        return true; //TODO
    }

    private boolean deletePurchase(int groupPos, int childPos) {

        repositoryManager_.deletePurchase(Integer
                .parseInt(dataFragment_.getMylist().get(groupPos).get(childPos).get("purchaseId")));

        startActivity(new Intent(this, ActivityMain.class));
        finish();

        return true;
    }

    private class DataFragment extends Fragment {
        Calendar calendar_;
        private HashMap<String, String> map1, map2;
        private ArrayList<HashMap<String, String>> mylist_title;
        private ArrayList<ArrayList<HashMap<String, String>>> mylist;


        // this method is only called once for this fragment
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            // retain this fragment
            setRetainInstance(true);


        }

        public void resetCalendar() {
            calendar_ = Calendar.getInstance();
            calendar_.setTime(new Date(System.currentTimeMillis()));
        }

        public Calendar getCalendar() {
            return calendar_;
        }

        public void setCalendar(Calendar calendar) {
            this.calendar_ = calendar;
        }

        public HashMap<String, String> getMap1() {
            return map1;
        }

        public void setMap1(HashMap<String, String> map1) {
            this.map1 = map1;
        }

        public HashMap<String, String> getMap2() {
            return map2;
        }

        public void setMap2(HashMap<String, String> map2) {
            this.map2 = map2;
        }

        public ArrayList<HashMap<String, String>> getMylist_title() {
            return mylist_title;
        }

        public void setMylist_title(ArrayList<HashMap<String, String>> mylist_title) {
            this.mylist_title = mylist_title;
        }

        public ArrayList<ArrayList<HashMap<String, String>>> getMylist() {
            return mylist;
        }

        public void setMylist(ArrayList<ArrayList<HashMap<String, String>>> mylist) {
            this.mylist = mylist;
        }
    }
}
