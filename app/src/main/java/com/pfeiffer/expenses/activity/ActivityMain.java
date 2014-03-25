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
import android.widget.Toast;

import com.pfeiffer.expenses.R;
import com.pfeiffer.expenses.model.Product;
import com.pfeiffer.expenses.model.Purchase;
import com.pfeiffer.expenses.repository.RepositoryManager;
import com.pfeiffer.expenses.utility.Translation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * @author axelpfeiffer
 */
public class ActivityMain extends Activity {
    public static final String EXTRA_PURCHASE_ID = "purchaseId";
    private final String logTag_ = this.getClass().getName();

    private RepositoryManager repositoryManager_;
    private ExpandableListView expandableListView_;
    private ArrayList<HashMap<String, String>> mylist_title;
    private ArrayList<ArrayList<HashMap<String, String>>> mylist;
    private SimpleExpandableListAdapter expListAdapter;
    private HashMap<String, String> map1, map2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        expandableListView_ = (ExpandableListView) findViewById(R.id.expandableListViewMain);

        registerForContextMenu(expandableListView_);
        showActivity();
    }

    private void showActivity() {
        mylist = new ArrayList<ArrayList<HashMap<String, String>>>();
        mylist_title = new ArrayList<HashMap<String, String>>();

        // get all Purchases
        repositoryManager_ = new RepositoryManager(this);
        repositoryManager_.open();
        List<Purchase> values = repositoryManager_.getAllPurchases();

        // Loop through values by months. The purchases in the values list come
        // in order
        if (!values.isEmpty()) {

            String currentMonthYear = values.get(0).getDate().substring(0, 7);
            double sumTotalMonthExpenses = 0.;

            ArrayList<HashMap<String, String>> secList = new ArrayList<HashMap<String, String>>();

            for (Purchase purchase : values) {

                // obtain product
                Product product = repositoryManager_.findProduct(purchase.getProductId());

                String monthYear = purchase.getDate().substring(0, 7);
                Log.d(logTag_, "Purchase " + purchase + " has monthYear=" + monthYear);

                if (!monthYear.equals(currentMonthYear)) {
                    map1 = new HashMap<String, String>();
                    map1.put("rowMonthName", Translation.getMonthString(currentMonthYear.substring(5, 7)) + " "
                            + currentMonthYear.substring(0, 4));
                    map1.put("rowMonthExpenses", Translation.getValidPrice(String.valueOf(sumTotalMonthExpenses))
                            + " €");
                    mylist_title.add(map1);

                    mylist.add(secList);
                    secList = new ArrayList<HashMap<String, String>>();
                    sumTotalMonthExpenses = 0.;
                    currentMonthYear = monthYear;

                }

                map2 = new HashMap<String, String>();
                map2.put("purchaseId", String.valueOf(purchase.getId()));

                map2.put("rowDate", Translation.humanReadableDate(purchase.getDate()));

                map2.put("rowName", ((!purchase.getAmount().equals("1")) ? purchase.getAmount() + "x " : "")
                        + product.getName());

                map2.put("rowTotalPrice", purchase.getTotalPrice() + " €");
                secList.add(map2);

                sumTotalMonthExpenses += Double.parseDouble(purchase.getTotalPrice());
            }
            map1 = new HashMap<String, String>();
            map1.put("rowMonthName", Translation.getMonthString(currentMonthYear.substring(5, 7)) + " "
                    + currentMonthYear.substring(0, 4));
            map1.put("rowMonthExpenses", Translation.getValidPrice(String.valueOf(sumTotalMonthExpenses)) + " €");
            mylist_title.add(map1);

            mylist.add(secList);

            // TODO set group total expenses in month

            try {
                expListAdapter = new SimpleExpandableListAdapter(this, mylist_title, R.layout.group_row, new String[]{
                        "rowMonthName", "rowMonthExpenses"}, new int[]{R.id.rowMonthName, R.id.rowMonthExpenses},
                        mylist, R.layout.row, new String[]{"rowDate", "rowName", "rowTotalPrice"}, new int[]{
                        R.id.rowDate, R.id.rowName, R.id.rowTotalPrice}
                );
                expandableListView_.setAdapter(expListAdapter);
            } catch (Exception e) {
// TODO
            }
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
                return true;
            case R.id.action_sync_data:
                startActivity(new Intent(this, ActivitySyncData.class));

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
        intent.putExtra(EXTRA_PURCHASE_ID, String.valueOf(purchaseId));
        startActivity(intent);
        return true; //TODO
    }

    private boolean deletePurchase(int groupPos, int childPos) {

        repositoryManager_.deletePurchase(Integer
                .parseInt(mylist.get(groupPos).get(childPos).get("purchaseId")));
        mylist.get(groupPos).remove(childPos);

        if (mylist.get(groupPos).isEmpty())
            mylist_title.remove(groupPos);

        double sumTotalMonthExpenses = 0.;
        int len = mylist.get(groupPos).size();
        if (len == 0)
            mylist.remove(groupPos);
        else {
            for (int i = 0; i < len; ++i) {
                String temp = mylist.get(groupPos).get(i).get("rowTotalPrice");

                sumTotalMonthExpenses += Double.parseDouble(temp.substring(0, temp.length() - 2));
            }

            mylist_title.get(groupPos).put(
                    "rowMonthExpenses",
                    Translation.getValidPrice(String.valueOf(sumTotalMonthExpenses)) + " €");
        }
        expListAdapter.notifyDataSetChanged();
        Toast.makeText(this, R.string.purchase_deleted, Toast.LENGTH_SHORT).show();
        return true; // TODO what do we really need to return?

    }

    /**
     * Called when the user clicks the Send button
     */
    public void recordPurchase(View view) {
        Intent intent = new Intent(this, ActivityRecordPurchase.class);
        startActivity(intent);
    }

    public void statistik(View view) {
        Intent intent = new Intent(this, ActivityPieChart.class);
        startActivity(intent);

    }
}
