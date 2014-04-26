package com.pfeiffer.expenses.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.ExpandableListContextMenuInfo;
import android.widget.SimpleExpandableListAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.pfeiffer.expenses.R;
import com.pfeiffer.expenses.model.Category;
import com.pfeiffer.expenses.model.Money;
import com.pfeiffer.expenses.model.Purchase;
import com.pfeiffer.expenses.repository.RepositoryManager;
import com.pfeiffer.expenses.utility.DataAnalysis;
import com.pfeiffer.expenses.utility.Translation;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
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

    private TextView currentMonth_;
    private TextView totalExpenses_;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        expandableListView_ = (ExpandableListView) findViewById(R.id.expandableListViewMain);
        currentMonth_ = (TextView) findViewById(R.id.tvCurrentMonth);
        totalExpenses_ = (TextView) findViewById(R.id.tvTotalExpenses);

        registerForContextMenu(expandableListView_);
        showActivity();
    }

    private void showActivity() {
        mylist = new ArrayList<ArrayList<HashMap<String, String>>>();
        mylist_title = new ArrayList<HashMap<String, String>>();

        // get all Purchases
        repositoryManager_ = new RepositoryManager(this);
        repositoryManager_.open();


        Date rightNow = new Date(System.currentTimeMillis());

        List<Purchase> purchasesCurrentMonth = repositoryManager_.getAllPurchasesForDateRange(Translation.getFirstDateOfCurrentMonth
                (), rightNow);

        DataAnalysis dataAnalysis = new DataAnalysis(repositoryManager_, purchasesCurrentMonth);

        currentMonth_.setText(Translation.getMonthString(rightNow));
        Money totalExpenses = dataAnalysis.getExpensesPerMonth(rightNow);
        totalExpenses_.setText(totalExpenses.getHumanReadableRepresentation());

        List<Map.Entry<Category, Money>> categoryToExpenses = dataAnalysis.getSortedCategoryToExpensesForYearAndMonth
                (rightNow);

        int len = categoryToExpenses.size();
        for (int i = 0; i < len; ++i) {
            Map.Entry<Category, Money> entry = categoryToExpenses.get(len - 1 - i);
            Category category = entry.getKey();
            List<Purchase> purchases = dataAnalysis.getPurchasesForYearMonthAndCategory(rightNow, category);
            if (purchases != null && !purchases.isEmpty()) {
                ArrayList<HashMap<String, String>> secList = new ArrayList<HashMap<String, String>>();

                map1 = new HashMap<String, String>();
                int percentage = entry.getValue().percentage(totalExpenses);
                map1.put("rowCategoryName", category.toString() + " (" + percentage + " %)");
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
                startActivity(new Intent(this, ActivityShareData.class ));
                return true;

            case R.id.action_record_purchase:
                Intent intent = new Intent(this, ActivityRecordPurchase.class);
                startActivity(intent);

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
        Money price = new Money(mylist.get(groupPos).get(childPos).get("rowTotalPrice"));

        mylist.get(groupPos).remove(childPos);

        if (mylist.get(groupPos).isEmpty()) {
            mylist_title.remove(groupPos);
        } else {

            Money totalCategoryExpenses = new Money(mylist_title.get(groupPos).get("rowCategoryExpenses"));

            totalCategoryExpenses.subtract(price);

            mylist_title.get(groupPos).put(
                    "rowCategoryExpenses",
                    totalCategoryExpenses.getHumanReadableRepresentation());
        }

        Money totalExpenses = new Money(totalExpenses_.getText().toString());
        totalExpenses.subtract(price);
        totalExpenses_.setText(totalExpenses.getHumanReadableRepresentation());

        expListAdapter.notifyDataSetChanged();
        Toast.makeText(this, R.string.purchase_deleted, Toast.LENGTH_SHORT).show();
        return true; // TODO what do we really need to return?

    }

    public void statistik(View view) {
//        Intent intent = new Intent(this, ActivityPieChart.class);
//        startActivity(intent);
//        finish();


    }
}
