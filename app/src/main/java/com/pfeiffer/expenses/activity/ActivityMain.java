package com.pfeiffer.expenses.activity;

import android.app.Activity;
import android.app.DatePickerDialog;
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
    private ArrayList<HashMap<String, String>> mylist_title;
    private ArrayList<ArrayList<HashMap<String, String>>> mylist;
    private SimpleExpandableListAdapter expListAdapter;
    private HashMap<String, String> map1, map2;
    Calendar myCalendar_;


    private TextView currentMonth_;
    private TextView totalExpenses_;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        expandableListView_ = (ExpandableListView) findViewById(R.id.expandableListViewMain);
        currentMonth_ = (TextView) findViewById(R.id.tvCurrentMonth);
        totalExpenses_ = (TextView) findViewById(R.id.tvTotalExpenses);

        myCalendar_ = Calendar.getInstance();
        myCalendar_.setTime(new Date(System.currentTimeMillis()));
        updateCalendarTextview();


        final DatePickerDialog.OnDateSetListener date = new DatePickerDialog.OnDateSetListener() {

            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear,
                                  int dayOfMonth) {
                // TODO Auto-generated method stub
                myCalendar_.set(Calendar.YEAR, year);
                myCalendar_.set(Calendar.MONTH, monthOfYear);
                myCalendar_.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                updateCalendarTextview();
            }

        };

        currentMonth_.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                DatePickerDialog pickerDialog=new DatePickerDialog(ActivityMain.this, date, myCalendar_
                        .get(Calendar.YEAR), myCalendar_.get(Calendar.MONTH),
                        myCalendar_.get(Calendar.DAY_OF_MONTH)
                );
                DatePicker picker= pickerDialog.getDatePicker();

                try {
                    Field f[] = picker.getClass().getDeclaredFields();
                    for (Field field : f) {
                        if (field.getName().equals("mDayPicker")||field.getName().equals("mDaySpinner")) {
                            field.setAccessible(true);
                            Object dayPicker = new Object();
                            dayPicker = field.get(picker);
                            ((View) dayPicker).setVisibility(View.GONE);
                        }
                    }
                }
                catch (SecurityException e) {
                    Log.d("ERROR", e.getMessage());
                }
                catch (IllegalArgumentException e) {
                    Log.d("ERROR", e.getMessage());
                }
                catch (IllegalAccessException e) {
                    Log.d("ERROR", e.getMessage());
                }

                pickerDialog.show();
            }
        });




        registerForContextMenu(expandableListView_);
        showActivity();
    }


    private void updateCalendarTextview() {
        Log.d(logTag_, myCalendar_.getTime().toString());
        showActivity();

        String myFormat = "MMMM yyyy"; //In which you need put here
        SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.GERMAN);

        currentMonth_.setText(sdf.format(myCalendar_.getTime()));
    }

    @Override
    protected void onPause(){
        super.onPause();
        repositoryManager_.close();
    }



    private void showActivity() {
        mylist = new ArrayList<ArrayList<HashMap<String, String>>>();
        mylist_title = new ArrayList<HashMap<String, String>>();

        // get all Purchases
        repositoryManager_ = new RepositoryManager(this);
        repositoryManager_.open();

        List<Purchase> purchasesCurrentMonth = repositoryManager_.getAllPurchasesForDateRange(
                Translation.getFirstDateOfMonth(myCalendar_.getTime()),
                Translation.getFirstDateOfNextMonth(myCalendar_.getTime()));

        DataAnalysis dataAnalysis = new DataAnalysis(repositoryManager_, purchasesCurrentMonth);

//        currentMonth_.setText(Translation.getMonthString(rightNow));
        Money totalExpenses = dataAnalysis.getExpensesPerMonth(myCalendar_.getTime());
        totalExpenses_.setText(totalExpenses.getHumanReadableRepresentation());

        List<Map.Entry<EnumCategory, Money>> categoryToExpenses = dataAnalysis.getSortedCategoryToExpensesForYearAndMonth
                (myCalendar_.getTime());

        int len = categoryToExpenses.size();
        for (int i = 0; i < len; ++i) {
            Map.Entry<EnumCategory, Money> entry = categoryToExpenses.get(len - 1 - i);
            EnumCategory category = entry.getKey();
            List<Purchase> purchases = dataAnalysis.getPurchasesForYearMonthAndCategory(myCalendar_.getTime(), category);
            if (purchases != null && !purchases.isEmpty()) {
                ArrayList<HashMap<String, String>> secList = new ArrayList<HashMap<String, String>>();

                map1 = new HashMap<String, String>();
                int percentage = entry.getValue().percentage(totalExpenses);
                map1.put("rowCategoryName", category.toString() + " (" + percentage + "%)");
                map1.put("rowCategoryExpenses", entry.getValue().getHumanReadableRepresentation());
                mylist_title.add(map1);

                for (Purchase purchase : purchases) {

                    map2 = new HashMap<String, String>();
                    map2.put("purchaseId", String.valueOf(purchase.getId()));
                    map2.put("rowDate", Translation.shortDate(purchase.getDate()));
                    map2.put("rowName", ((purchase.getAmount() > 1) ? purchase.getAmount() + "x " : "")
                            + purchase.getProductName());

                    map2.put("rowTotalPrice", purchase.getTotalHumanReadablePrice());
                    secList.add(map2);
                }

                mylist.add(secList);
            }
        }


        try {
            expListAdapter = new SimpleExpandableListAdapter(this, mylist_title, R.layout.group_row, new String[]{
                    "rowCategoryName", "rowCategoryExpenses"}, new int[]{R.id.rowCategoryName, R.id.rowCategoryExpenses},
                    mylist, R.layout.row, new String[]{"rowDate", "rowName", "rowTotalPrice"}, new int[]{
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
            menu.setHeaderTitle(mylist.get(group).get(child).get("rowName"));
            menu.add(0, 0, 0, "Bearbeiten"); //TODO
            menu.add(0, 1, 0, "Löschen"); //TODO
            Log.d(logTag_, "ContextMenu created for item with id "
                    + mylist.get(group).get(child).get("purchaseId"));
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
        int purchaseId = Integer.parseInt(mylist.get(groupPos).get(childPos).get("purchaseId"));
        Intent intent = new Intent(this, ActivityRecordPurchase.class);
        intent.putExtra(EXTRA_PURCHASE_ID, purchaseId);
        startActivity(intent);
        finish();

        return true; //TODO
    }

    private boolean deletePurchase(int groupPos, int childPos) {

        repositoryManager_.deletePurchase(Integer
                .parseInt(mylist.get(groupPos).get(childPos).get("purchaseId")));

        startActivity(new Intent(this, ActivityMain.class));
        finish();

        return true;
    }
}
