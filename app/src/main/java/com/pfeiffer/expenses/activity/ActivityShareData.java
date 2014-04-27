package com.pfeiffer.expenses.activity;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.pfeiffer.expenses.R;
import com.pfeiffer.expenses.model.Purchase;
import com.pfeiffer.expenses.repository.RepositoryManager;
import com.pfeiffer.expenses.utility.BluetoothService;

import java.util.Date;
import java.util.List;


/*
 * Concept for sharing Data with several devices
 *
 * Modifications to Database Tables:
 * - New table PurchaseHistory(id, timestamp, purchaseId, operation(New, Delete, Modify)
 * - New table PartnerDevice(id, Andoid_ID, lasConnectionTimeStamp)
 * - add field "owner(Andoid_IDs)" and "owner id" to Purchase table
 *
 * Upon Connection:
 *  - The Devices exchange their Andoid_IDs
  * - Both Devies know from lasConnectionTimeStamp and the Table PurchaseTableHistory
  *   which data to share.
  *     - new and modified purchases are send as such
  *     - for deleted purchases, an empty Purchase, containing only the purchase Id is sent
  * - Each Device loops over the Purchases that it received from the other device and
  *     - Adds new Purchases to the db
  *     - Updates existing Purchases
  *     - Deletes Purchases (no matter who created the Purchase)
  *   according to the received data.
  *
  *
  *   Communication
 *
 */


public class ActivityShareData extends Activity {
    private final String logTag_ = this.getClass().getName();

    // Message types sent from the BluetoothChatService Handler
    public static final int MESSAGE_STATE_CHANGE = 1;
    public static final int MESSAGE_READ = 2;
    public static final int MESSAGE_WRITE = 3;
    public static final int MESSAGE_DEVICE_NAME = 4;
    public static final int MESSAGE_TOAST = 5;

    // Key names received from the BluetoothChatService Handler
    public static final String DEVICE_NAME = "device_name";
    public static final String TOAST = "toast";

    // Intent request codes
    private static final int REQUEST_CONNECT_DEVICE = 1;
    private static final int REQUEST_ENABLE_BT = 2;

    DataFragment dataFragment_;

    TextView tvSyncInfo_;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.e(logTag_, "+++ ON CREATE +++");

        FragmentManager fragmentManager = getFragmentManager();
        dataFragment_ = (DataFragment) fragmentManager.findFragmentByTag("ActivityShareData");

        if (dataFragment_ == null) {
            // add the fragment
            dataFragment_ = new DataFragment();
            fragmentManager.beginTransaction().add(dataFragment_, "ActivityRecordPurchase").commit();

            dataFragment_.setRepositoryManager(new RepositoryManager(this));

        }

        dataFragment_.getRepositoryManager().open();

        setContentView(R.layout.activity_share_data);

        // Set up the custom title
        setTitle(R.string.titleActivityShareData);

        // Get local Bluetooth adapter
        dataFragment_.setBluetoothAdapter(BluetoothAdapter.getDefaultAdapter());

        // If the adapter is null, then Bluetooth is not supported
        if (dataFragment_.getBluetoothAdapter() == null) {
            Toast.makeText(this, R.string.blu_not_available, Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        tvSyncInfo_ = (TextView) findViewById(R.id.tvSyncInfo);
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.e(logTag_, "++ ON START ++");

        // If BT is not on, request that it be enabled.
        // setupChat() will then be called during onActivityResult
        if (!dataFragment_.getBluetoothAdapter().isEnabled()) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
            // Otherwise, setup the chat session
        } else {
            if (dataFragment_.getBluetoothService() == null) setupChat();
        }
    }

    @Override
    public synchronized void onResume() {
        super.onResume();
        Log.e(logTag_, "+ ON RESUME +");

        // Performing this check in onResume() covers the case in which BT was
        // not enabled during onStart(), so we were paused to enable it...
        // onResume() will be called when ACTION_REQUEST_ENABLE activity returns.
        if (dataFragment_.getBluetoothService() != null) {
            // Only if the state is STATE_NONE, do we know that we haven't started already
            if (dataFragment_.getBluetoothService().getState() == BluetoothService.STATE_NONE) {
                // Start the Bluetooth chat services
                dataFragment_.getBluetoothService().start();
            }
        }
    }

    private void setupChat() {
        Log.d(logTag_, "setupChat()");

        // Initialize the BluetoothChatService to perform bluetooth connections
        dataFragment_.setBluetoothService(new BluetoothService(this, mHandler));

    }

    @Override
    public synchronized void onPause() {
        super.onPause();
        Log.e(logTag_, "- ON PAUSE -");
    }

    @Override
    public void onStop() {
        super.onStop();
        Log.e(logTag_, "-- ON STOP --");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        Log.e(logTag_, "--- ON DESTROY ---");
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
            // Stop the Bluetooth chat services
            if (dataFragment_.getBluetoothService() != null)
                dataFragment_.getBluetoothService().stop();
            dataFragment_.getBluetoothAdapter().disable();

            startActivity(new Intent(this, ActivityMain.class));
            finish();
        }

        return super.onKeyDown(keyCode, event);
    }

    private void ensureDiscoverable() {
        Log.d(logTag_, "ensure discoverable");
        if (dataFragment_.getBluetoothAdapter().getScanMode() !=
                BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
            Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
            discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
            startActivity(discoverableIntent);
        }
    }

    private void sendAllPurchases() {
        List<Purchase> allPurchases = dataFragment_.getRepositoryManager().getAllPurchases();
        for (Purchase purchase : allPurchases) {
            dataFragment_.getBluetoothService().write(purchase);
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(logTag_, "onActivityResult " + resultCode);
        switch (requestCode) {
            case REQUEST_CONNECT_DEVICE:
                // When DeviceListActivity returns with a device to connect
                if (resultCode == Activity.RESULT_OK) {
                    // Get the device MAC address
                    String address = data.getExtras()
                            .getString(ActivityBluetoothDevices.EXTRA_DEVICE_ADDRESS);
                    // Get the BLuetoothDevice object
                    BluetoothDevice device = dataFragment_.getBluetoothAdapter().getRemoteDevice(address);
                    // Attempt to connect to the device
                    dataFragment_.getBluetoothService().connect(device);
                }
                break;
            case REQUEST_ENABLE_BT:
                // When the request to enable Bluetooth returns
                if (resultCode == Activity.RESULT_OK) {
                    // Bluetooth is now enabled, so set up a chat session
                    setupChat();
                } else {
                    // User did not enable Bluetooth or an error occured
                    Log.d(logTag_, "BT not enabled");
                    Toast.makeText(this, R.string.bt_not_enabled_leaving, Toast.LENGTH_SHORT).show();
                    finish();
                }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.activity_share_data, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.scan:
                // Launch the DeviceListActivity to see devices and do scan
                Intent serverIntent = new Intent(this, ActivityBluetoothDevices.class);
                startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE);
                return true;
            case R.id.discoverable:
                // Ensure this device is discoverable by others
                ensureDiscoverable();
                return true;
        }
        return false;
    }


    // The Handler that gets information back from the BluetoothChatService
    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MESSAGE_STATE_CHANGE:
                    Log.i(logTag_, "MESSAGE_STATE_CHANGE: " + msg.arg1);
                    switch (msg.arg1) {
                        case BluetoothService.STATE_CONNECTED:
                            String titleString = getString(R.string.title_connected_to);
                            titleString += ": " + dataFragment_.getConnectedDeviceName();
                            setTitle(titleString);
                            sendAllPurchases();
                            break;
                        case BluetoothService.STATE_CONNECTING:
                            setTitle(R.string.title_connecting);
                            break;
                        case BluetoothService.STATE_LISTEN:
                        case BluetoothService.STATE_NONE:
                            setTitle(R.string.title_not_connected);
                            break;
                    }
                    break;
                case MESSAGE_WRITE:
                    byte[] writeBuf = (byte[]) msg.obj;
                    // construct a string from the buffer
                    String writeMessage = new String(writeBuf);
                    break;
                case MESSAGE_READ:
                    Object object = msg.obj;
                    if (object instanceof Purchase) {

                        Purchase purchase = (Purchase) object;
                        Log.d(logTag_, "Purchase received via bluetooth: " + purchase);
                        dataFragment_.getRepositoryManager().savePurchase(purchase);
                        //...
                    } else if (object instanceof MetaInformation) {
                        Log.d(logTag_, "MetaInformation received via bluetooth: " + (MetaInformation) object);
                        //...
                    } else {
                        System.out.println("Unexpected object type:  " + object.getClass().getName());
                    }

                    break;
                case MESSAGE_DEVICE_NAME:
                    // save the connected device's name
                    dataFragment_.setConnectedDeviceName(msg.getData().getString(DEVICE_NAME));
                    Toast.makeText(getApplicationContext(), "Connected to "
                            + dataFragment_.getConnectedDeviceName(), Toast.LENGTH_SHORT).show();
                    break;
                case MESSAGE_TOAST:
                    Toast.makeText(getApplicationContext(), msg.getData().getString(TOAST),
                            Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    };

    class MetaInformation {
        // request Purchases (after some date)
        // Header (ObjectStream) (int numberOfPurchases)
        // Trailer (ObjectStream)
        public static final int REQUEST = 1;
        public static final int REPLY_HEADER = 2;
        public static final int REPLY_TRAILER = 3;

        private int messageFunction_;
        private Date date_;
        private int numberOfObjects_;


        public int getMessageFunction() {
            return messageFunction_;
        }

        public Date getDate() {
            return date_;
        }

        public int getNumberOfObjects() {
            return numberOfObjects_;
        }

        public void setRequest(Date date) {
            messageFunction_ = REQUEST;
            date_ = date;
        }
        public void setHeader(int numberOfObjects){
            messageFunction_= REPLY_HEADER;
            numberOfObjects_=numberOfObjects;
        }
        public void setTrailer(int numberOfObjects)
        {
            messageFunction_= REPLY_TRAILER;
            numberOfObjects_=numberOfObjects;
        }
    }

    class DataFragment extends Fragment {

        private BluetoothAdapter bluetoothAdapter_ = null;
        private BluetoothService bluetoothService_ = null;
        private RepositoryManager repositoryManager_ = null;
        private String connectedDeviceName_ = null;

        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setRetainInstance(true); // retain this fragment
        }

        public BluetoothService getBluetoothService() {
            return bluetoothService_;
        }

        public RepositoryManager getRepositoryManager() {
            return repositoryManager_;
        }

        public String getConnectedDeviceName() {
            return connectedDeviceName_;
        }

        public BluetoothAdapter getBluetoothAdapter() {
            return bluetoothAdapter_;
        }

        public void setBluetoothAdapter(BluetoothAdapter bluetoothAdapter) {
            this.bluetoothAdapter_ = bluetoothAdapter;
        }

        public void setBluetoothService(BluetoothService bluetoothService) {
            this.bluetoothService_ = bluetoothService;
        }

        public void setRepositoryManager(RepositoryManager repositoryManager) {
            this.repositoryManager_ = repositoryManager;
        }

        public void setConnectedDeviceName(String connectedDeviceName) {
            this.connectedDeviceName_ = connectedDeviceName;
        }
    }
}
