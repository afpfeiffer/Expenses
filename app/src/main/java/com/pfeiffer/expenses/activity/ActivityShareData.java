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
import android.provider.Settings;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.pfeiffer.expenses.R;
import com.pfeiffer.expenses.model.MetaInformation;
import com.pfeiffer.expenses.model.Purchase;
import com.pfeiffer.expenses.repository.RepositoryManager;
import com.pfeiffer.expenses.utility.BluetoothService;

import java.util.ArrayList;
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

    private static final int MAX_RETRY = 3;
    DataFragment dataFragment_;

    TextView tvDatatransfer_;
    ProgressBar pbDataTransfer_;
    private Handler progressBarHandler_ = new Handler();
    int progressBarStatus_ = 0;
    int progressBarMax_ = 0;
    boolean transferCompleted_ = false;

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

        pbDataTransfer_ = (ProgressBar) findViewById(R.id.pbDatatransfer);
        pbDataTransfer_.setVisibility(ProgressBar.GONE);

        tvDatatransfer_ = (TextView) findViewById(R.id.tvDatatransfer);
        tvDatatransfer_.setVisibility(TextView.GONE);
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
            if (dataFragment_.getBluetoothService() == null) {
                dataFragment_.setBluetoothService(new BluetoothService(this, mHandler));
            }
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
            leaveForMainActivity();
        }

        return super.onKeyDown(keyCode, event);
    }

    void leaveForMainActivity() {
        // Stop the Bluetooth chat services
        transferCompleted_ = true;
        if (dataFragment_.getBluetoothService() != null)
            dataFragment_.getBluetoothService().stop();
        dataFragment_.getBluetoothAdapter().disable();

        startActivity(new Intent(this, ActivityMain.class));
        finish();
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
                    dataFragment_.setBluetoothService(new BluetoothService(this, mHandler));
                } else {
                    // User did not enable Bluetooth or an error occured
                    Log.d(logTag_, "BT not enabled");
                    Toast.makeText(this, R.string.bt_not_enabled_leaving, Toast.LENGTH_SHORT).show();
                    finish();
                }
        }
    }

    private void sendPurchases(MetaInformation metaInformation) {
        if (metaInformation.getMessageFunction() != MetaInformation.REQUEST) {
            throw new IllegalArgumentException();
        }

        String deviceOwner = Settings.Secure.getString(this.getContentResolver(), Settings.Secure.ANDROID_ID);


        List<Purchase> allPurchases = dataFragment_.getRepositoryManager().getAllPurchases();
        List<Purchase> filteredPurchases=new ArrayList<Purchase>();
        for (Purchase purchase : allPurchases) {
            if (purchase.getOwner().equals(deviceOwner)) {
                filteredPurchases.add(purchase);
            }
        }
        int numberOfPurchases = filteredPurchases.size();

        MetaInformation messageHeader = new MetaInformation();
        messageHeader.setHeader(numberOfPurchases);
        dataFragment_.getBluetoothService().write(messageHeader);

        for (Purchase purchase : filteredPurchases) {
            // only send own purchases, do not update partner device with its own purchases (and possibly override changes)
            dataFragment_.getBluetoothService().write(purchase);

        }

        MetaInformation messageTrailer = new MetaInformation();
        messageTrailer.setTrailer(numberOfPurchases);
        dataFragment_.getBluetoothService().write(messageTrailer);
    }

    private void savePurchases(MetaInformation metaInformation) {
        if (metaInformation.getMessageFunction() != MetaInformation.REPLY_TRAILER) {
            throw new IllegalArgumentException();
        }

        tvDatatransfer_.setText("Speichern...");


        List<Purchase> receivedPurchases = dataFragment_.getReceivedPurchases();

        // check if all purchases were received
        if (receivedPurchases.size() != metaInformation.getNumberOfObjects()) {
            // if not: retry
            if (dataFragment_.getNumberOfRequests() >= MAX_RETRY) {
                // cancel
                leaveForMainActivity();
            } else {
                // retry
                throw new IllegalStateException();

//                MetaInformation request = new MetaInformation();
//                request.setRequest(new Date(0));
//                dataFragment_.getBluetoothService().write(request);
//                dataFragment_.setNumberOfRequests(dataFragment_.getNumberOfRequests() + 1);
            }
        } else {
            // process received purchases
            RepositoryManager repositoryManager = dataFragment_.getRepositoryManager();
            for (Purchase purchase : receivedPurchases) {
                Purchase storedPurchase = repositoryManager.findPurchaseByOwnerAndId(
                        purchase.getOwner(),
                        purchase.getPurchaseIdOwner());

                // delete db entry if it exists
                if (storedPurchase != null) {
                    repositoryManager.deletePurchase(storedPurchase.getId());
                }

                // save received purchase if it has a valid state
                if (purchase.hasValidState()) {
                    repositoryManager.savePurchase(purchase);
                }
            }

            MetaInformation disconnect = new MetaInformation();
            disconnect.setDisconnect();
            dataFragment_.getBluetoothService().write(disconnect);
            if (dataFragment_.isDisconnectReceived()) {
                leaveForMainActivity();
            } else {
                dataFragment_.setDisconnectSend(true);
                tvDatatransfer_.setText("Auf Partnergerät warten...");
            }
        }

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
                            MetaInformation metaInformation = new MetaInformation();
                            metaInformation.setRequest(new Date(0));
                            dataFragment_.getBluetoothService().write(metaInformation);
//                            pbDataTransfer_.setVisibility(ProgressBar.VISIBLE);
//                            pbDataTransfer_.setProgress(progressBarStatus_);
//                            pbThread_.start();
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
                        dataFragment_.getReceivedPurchases().add(purchase);
                        progressBarStatus_++;
                        tvDatatransfer_.setText("Empfangen: " + progressBarStatus_ + "/" + progressBarMax_);
                        //...
                    } else if (object instanceof MetaInformation) {
                        MetaInformation metaInformation = (MetaInformation) object;
                        Log.d(logTag_, "MetaInformation received via bluetooth: " + metaInformation);
                        switch (metaInformation.getMessageFunction()) {

                            case MetaInformation.REQUEST:
                                sendPurchases(metaInformation);
                                break;
                            case MetaInformation.REPLY_HEADER:
                                pbDataTransfer_.setMax(metaInformation.getNumberOfObjects());
                                progressBarMax_ = metaInformation.getNumberOfObjects();
                                break;
                            case MetaInformation.REPLY_TRAILER:
                                savePurchases(metaInformation);
                                break;
                            case MetaInformation.DISCONNECT:
                                if (dataFragment_.isDisconnectSend()) {
                                    leaveForMainActivity();
                                } else {
                                    dataFragment_.setDisconnectReceived(true);
                                }
                                break;

                        }
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

    Thread pbThread_ = new Thread(new Runnable() {
        public void run() {
            while (!transferCompleted_) {
                // your computer is too fast, sleep 1 second
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                // Update the progress bar
                progressBarHandler_.post(new Runnable() {
                    public void run() {
                        pbDataTransfer_.setProgress(progressBarStatus_);
                    }
                });
            }

            // ok, file is downloaded,
            if (transferCompleted_) {

                // sleep 2 seconds, so that you can see the 100%
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                // close the progress bar dialog
                pbDataTransfer_.setVisibility(ProgressBar.INVISIBLE);
            }
        }
    });


    class DataFragment extends Fragment {

        private BluetoothAdapter bluetoothAdapter_ = null;
        private BluetoothService bluetoothService_ = null;
        private RepositoryManager repositoryManager_ = null;
        private String connectedDeviceName_ = null;
        private List<Purchase> receivedPurchases_ = null;
        private int numberOfRequests_ = 0;
        private boolean disconnectSend_ = false;
        private boolean disconnectReceived_ = false;

        public boolean isDisconnectSend() {
            return disconnectSend_;
        }

        public void setDisconnectSend(boolean disconnectSend) {
            this.disconnectSend_ = disconnectSend;
        }

        public boolean isDisconnectReceived() {
            return disconnectReceived_;
        }

        public void setDisconnectReceived(boolean disconnectReceived) {
            this.disconnectReceived_ = disconnectReceived;
        }

        public void onCreate(Bundle savedInstanceState) {
            receivedPurchases_ = new ArrayList<Purchase>();
            super.onCreate(savedInstanceState);
            setRetainInstance(true); // retain this fragment
        }


        public int getNumberOfRequests() {
            return numberOfRequests_;
        }

        public void setNumberOfRequests(int numberOfRequests) {
            this.numberOfRequests_ = numberOfRequests;
        }

        public List<Purchase> getReceivedPurchases() {
            return receivedPurchases_;
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
