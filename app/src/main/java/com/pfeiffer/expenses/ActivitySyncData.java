package com.pfeiffer.expenses;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class ActivitySyncData extends Activity {

    private static final int REQUEST_ENABLE_BT = 1;
    private static final int DISCOVER_DURATION = 300;
    private Button buttonOn_;
    private Button buttonOff_;
    private Button buttonList_;
    private Button buttonFind_;
    private TextView textViewStatus_;
    private BluetoothAdapter bluetoothAdapter_;
    private Set<BluetoothDevice> foundDevices_;
    private List<BluetoothDevice> displayedDevices_;
    private final UUID UUID_ = UUID.fromString("294FD210-A894-11E3-A5E2-0800200C9A66");

    private ListView devicesListView_;
    private ArrayAdapter<String> BTArrayAdapter_;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_activity_sync_data);

        foundDevices_ = new HashSet<BluetoothDevice>();
        displayedDevices_ = new ArrayList<BluetoothDevice>();

        // take an instance of BluetoothAdapter - Bluetooth radio
        bluetoothAdapter_ = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter_ == null) {
            buttonOn_.setEnabled(false);
            buttonOff_.setEnabled(false);
            buttonList_.setEnabled(false);
            buttonFind_.setEnabled(false);
            textViewStatus_.setText(R.string.blu_status_not_supported);

            Toast.makeText(getApplicationContext(), R.string.blu_not_available, Toast.LENGTH_LONG).show();
        } else {
            textViewStatus_ = (TextView) findViewById(R.id.bluetooth_status_text);
            buttonOn_ = (Button) findViewById(R.id.turnOn);
            buttonOn_.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    // TODO Auto-generated method stub
                    on(v);
                }
            });

            buttonOff_ = (Button) findViewById(R.id.turnOff);
            buttonOff_.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    // TODO Auto-generated method stub
                    off(v);
                }
            });

            buttonList_ = (Button) findViewById(R.id.paired);
            buttonList_.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    // TODO Auto-generated method stub
                    list();
                }
            });

            buttonFind_ = (Button) findViewById(R.id.search);
            buttonFind_.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    // TODO Auto-generated method stub
                    find(v);
                }
            });

            devicesListView_ = (ListView) findViewById(R.id.listView1);

            devicesListView_.setOnItemClickListener(new OnItemClickListener() {
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    BluetoothDevice device = displayedDevices_.get(position);

                    // Toast.makeText( getApplicationContext(),
                    // device.getName(), Toast.LENGTH_SHORT ).show();

                }
            });

            BTArrayAdapter_ = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1);
            devicesListView_.setAdapter(BTArrayAdapter_);
        }
    }

    void on(View view) {
        if (!bluetoothAdapter_.isEnabled()) {
            Intent turnOnIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
            turnOnIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, DISCOVER_DURATION);

            startActivityForResult(turnOnIntent, REQUEST_ENABLE_BT);

        } else {
            Toast.makeText(getApplicationContext(), R.string.blu_already_turned_on, Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_ENABLE_BT) {
            if (bluetoothAdapter_.isEnabled()) {
                Toast.makeText(getApplicationContext(), R.string.blu_turned_on, Toast.LENGTH_LONG).show();

                textViewStatus_.setText(R.string.blu_status_turned_on);
            } else {
                textViewStatus_.setText(R.string.blu_status_turned_off);
            }
        }
    }

    void list() {
        // get paired devices
        BTArrayAdapter_.clear();
        foundDevices_.addAll(bluetoothAdapter_.getBondedDevices());

        for (BluetoothDevice device : foundDevices_) {
            BTArrayAdapter_.add(device.getName() + "\n" + device.getAddress());
            displayedDevices_.add(device);
        }
        Log.d(this.getClass().getName(), "Stored devices: " + displayedDevices_.size());
        BTArrayAdapter_.notifyDataSetChanged();
    }

    private final BroadcastReceiver bReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            // When discovery finds a device
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                // Get the BluetoothDevice object from the Intent
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                foundDevices_.add(device);
                list();
            }
        }
    };

    void find(View view) {
        if (bluetoothAdapter_.isDiscovering()) {
            // the button is pressed when it discovers, so cancel the discovery
            bluetoothAdapter_.cancelDiscovery();
        } else {
            list();
            bluetoothAdapter_.startDiscovery();
            registerReceiver(bReceiver, new IntentFilter(BluetoothDevice.ACTION_FOUND));
        }
    }

    void off(View view) {
        bluetoothAdapter_.disable();
        textViewStatus_.setText(R.string.blu_status_turned_off);

        Toast.makeText(getApplicationContext(), R.string.blu_turned_off, Toast.LENGTH_LONG).show();
    }

    private void manageConnectedSocket(BluetoothSocket socket) {
        Toast.makeText(getApplicationContext(), "Connection established", Toast.LENGTH_LONG).show();

    }

    @Override
    protected void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();
        try {
            unregisterReceiver(bReceiver);
        } catch (Exception e) {
            // TODO
        }
    }

    private class ConnectThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final BluetoothDevice mmDevice;

        public ConnectThread(BluetoothDevice device) {
            // Use a temporary object that is later assigned to mmSocket,
            // because mmSocket is final
            BluetoothSocket tmp = null;
            mmDevice = device;

            // Get a BluetoothSocket to connect with the given BluetoothDevice
            try {
                // MY_UUID is the app's UUID string, also used by the server
                // code
                tmp = device.createRfcommSocketToServiceRecord(UUID_);
            } catch (IOException e) {
                //TODO
            }
            mmSocket = tmp;
        }

        public void run() {
            // Cancel discovery because it will slow down the connection
            bluetoothAdapter_.cancelDiscovery();

            try {
                // Connect the device through the socket. This will block
                // until it succeeds or throws an exception
                mmSocket.connect();
            } catch (IOException connectException) {
                // Unable to connect; close the socket and get out
                try {
                    mmSocket.close();
                } catch (IOException closeException) {
                    //TODO
                }
                return;
            }

            // Do work to manage the connection (in a separate thread)
            manageConnectedSocket(mmSocket);
        }

        /**
         * Will cancel an in-progress connection, and close the socket
         */
        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
                //TODO
            }
        }
    }

    private class AcceptThread extends Thread {
        private final BluetoothServerSocket mmServerSocket;

        public AcceptThread() {
            // Use a temporary object that is later assigned to mmServerSocket,
            // because mmServerSocket is final
            BluetoothServerSocket tmp = null;
            try {
                // MY_UUID is the app's UUID string, also used by the client
                // code

                tmp = bluetoothAdapter_.listenUsingRfcommWithServiceRecord("ExpensesAssistant", UUID_);
            } catch (IOException e) {
                //TODO
            }
            mmServerSocket = tmp;
        }

        public void run() {
            BluetoothSocket socket = null;
            // Keep listening until exception occurs or a socket is returned
            while (true) {
                try {
                    socket = mmServerSocket.accept();
                } catch (IOException e) {
                    break;
                }
                // If a connection was accepted
                if (socket != null) {
                    // Do work to manage the connection (in a separate thread)
                    manageConnectedSocket(socket);
                    try {
                        mmServerSocket.close();
                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                    break;
                }
            }
        }

        /**
         * Will cancel the listening socket, and cause the thread to finish
         */
        public void cancel() {
            try {
                mmServerSocket.close();
            } catch (IOException e) {
                //TODO
            }
        }
    }

}
