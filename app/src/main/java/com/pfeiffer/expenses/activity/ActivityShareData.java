package com.pfeiffer.expenses.activity;

import android.app.Activity;
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
import android.view.inputmethod.EditorInfo;
import android.widget.TextView;
import android.widget.Toast;

import com.pfeiffer.expenses.R;
import com.pfeiffer.expenses.model.Purchase;
import com.pfeiffer.expenses.repository.RepositoryManager;
import com.pfeiffer.expenses.utility.BluetoothService;

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


    TextView tvSyncInfo_;

    private BluetoothAdapter mBluetoothAdapter = null;
    private BluetoothService mBluetoothService = null;
    private RepositoryManager repositoryManager_ = null;

    private String mConnectedDeviceName = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.e(logTag_, "+++ ON CREATE +++");

        repositoryManager_ = new RepositoryManager(this);
        repositoryManager_.open();

        setContentView(R.layout.activity_share_data);

        // Set up the custom title
        setTitle(R.string.titleActivityShareData);

        // Get local Bluetooth adapter
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        // If the adapter is null, then Bluetooth is not supported
        if (mBluetoothAdapter == null) {
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
        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
            // Otherwise, setup the chat session
        } else {
            if (mBluetoothService == null) setupChat();
        }
    }

    @Override
    public synchronized void onResume() {
        super.onResume();
        Log.e(logTag_, "+ ON RESUME +");

        // Performing this check in onResume() covers the case in which BT was
        // not enabled during onStart(), so we were paused to enable it...
        // onResume() will be called when ACTION_REQUEST_ENABLE activity returns.
        if (mBluetoothService != null) {
            // Only if the state is STATE_NONE, do we know that we haven't started already
            if (mBluetoothService.getState() == BluetoothService.STATE_NONE) {
                // Start the Bluetooth chat services
                mBluetoothService.start();
            }
        }
    }

    private void setupChat() {
        Log.d(logTag_, "setupChat()");

        // Initialize the send button with a listener that for click events
//        mSendButton.setOnClickListener(new View.OnClickListener() {
//            public void onClick(View v) {
//                // Send a message using content of the edit text widget
//                TextView view = (TextView) findViewById(R.id.edit_text_out);
//                String message = view.getText().toString();
//                sendMessage(message);
//            }
//        });

        // Initialize the BluetoothChatService to perform bluetooth connections
        mBluetoothService = new BluetoothService(this, mHandler);

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
        // Stop the Bluetooth chat services
        if (mBluetoothService != null) mBluetoothService.stop();
        mBluetoothAdapter.disable();
        Log.e(logTag_, "--- ON DESTROY ---");
    }

    private void ensureDiscoverable() {
        Log.d(logTag_, "ensure discoverable");
        if (mBluetoothAdapter.getScanMode() !=
                BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
            Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
            discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
            startActivity(discoverableIntent);
        }
    }

    /**
     * Sends a message.
     *
     * @param message A string of text to send.
     */
    private void sendMessage(String message) {
        // Check that we're actually connected before trying anything
        if (mBluetoothService.getState() != BluetoothService.STATE_CONNECTED) {
            Toast.makeText(this, R.string.not_connected, Toast.LENGTH_SHORT).show();
            return;
        }

        // Check that there's actually something to send
        if (message.length() > 0) {
            // Get the message bytes and tell the BluetoothChatService to write
            byte[] send = message.getBytes();
            mBluetoothService.write(send);

        }
    }

//    public static byte[] serializeObject(Object o) {
//        ByteArrayOutputStream bos = new ByteArrayOutputStream();
//
//        try {
//            ObjectOutput out = new ObjectOutputStream(bos);
//            out.writeObject(o);
//            out.close();
//
//            // Get the bytes of the serialized object
//            byte[] buf = bos.toByteArray();
//
//            return buf;
//        } catch(IOException ioe) {
//            Log.e("serializeObject", "error", ioe);
//
//            return null;
//        }
//    }
//
//    public static Object deserializeObject(byte[] b) {
//        try {
//            ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(b));
//            Object object = in.readObject();
//            in.close();
//
//            return object;
//        } catch(ClassNotFoundException cnfe) {
//            Log.e("deserializeObject", "class not found error", cnfe);
//
//            return null;
//        } catch(IOException ioe) {
//            Log.e("deserializeObject", "io error", ioe);
//
//            return null;
//        }
//    }

    private void sendAllPurchases() {
        List<Purchase> allPurchases = repositoryManager_.getAllPurchases();
        for (Purchase purchase : allPurchases) {
            mBluetoothService.write(purchase);
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
                    BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
                    // Attempt to connect to the device
                    mBluetoothService.connect(device);
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

    // The action listener for the EditText widget, to listen for the return key
    private TextView.OnEditorActionListener mWriteListener =
            new TextView.OnEditorActionListener() {
                public boolean onEditorAction(TextView view, int actionId, KeyEvent event) {
                    // If the action is a key-up event on the return key, send the message
                    if (actionId == EditorInfo.IME_NULL && event.getAction() == KeyEvent.ACTION_UP) {
                        String message = view.getText().toString();
                        sendMessage(message);
                    }
                    Log.i(logTag_, "END onEditorAction");
                    return true;
                }
            };

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
                            titleString += ": " + mConnectedDeviceName;
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
//                    byte[] readBuf = (byte[]) msg.obj;
                    Object object = msg.obj;
                    if (object instanceof Purchase) {
                        Log.d(logTag_, "Purchase received via bluetooth: " + (Purchase) object);
                        //...
                    } else if (object instanceof String) {
                        Log.d(logTag_, "Purchase received via bluetooth: " + (String) object);
                        //...
                    } else {
                        System.out.println("Unexpected object type:  " + object.getClass().getName());
                    }

                    break;
                case MESSAGE_DEVICE_NAME:
                    // save the connected device's name
                    mConnectedDeviceName = msg.getData().getString(DEVICE_NAME);
                    Toast.makeText(getApplicationContext(), "Connected to "
                            + mConnectedDeviceName, Toast.LENGTH_SHORT).show();
                    break;
                case MESSAGE_TOAST:
                    Toast.makeText(getApplicationContext(), msg.getData().getString(TOAST),
                            Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    };


}
