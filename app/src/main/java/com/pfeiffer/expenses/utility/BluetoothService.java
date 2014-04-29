package com.pfeiffer.expenses.utility;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.pfeiffer.expenses.activity.ActivityShareData;
import com.pfeiffer.expenses.model.MetaInformation;
import com.pfeiffer.expenses.model.Purchase;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.UUID;

/**
 * Created by axelpfeiffer on 25.04.14.
 */
public class BluetoothService {

    // Debugging
    private final String TAG = this.getClass().getName();

    // Name for the SDP record when creating server socket
    private static final String NAME = "Expenses";

    // Unique UUID for this application
    private static final UUID UUID_ = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    // Member fields
    private final BluetoothAdapter mAdapter_;
    private final Handler mHandler_;
    private AcceptThread mAcceptThread_;
    private ConnectThread mConnectThread_;
    private ConnectedThread mConnectedThread_;
    private int mState_;

    // Constants that indicate the current connection state
    public static final int STATE_NONE = 0;       // we're doing nothing
    public static final int STATE_LISTEN = 1;     // now listening for incoming connections
    public static final int STATE_CONNECTING = 2; // now initiating an outgoing connection
    public static final int STATE_CONNECTED = 3;  // now connected to a remote device

    /**
     * Constructor. Prepares a new BluetoothChat session.
     *
     * @param context The UI Activity Context
     * @param handler A Handler to send messages back to the UI Activity
     */
    public BluetoothService(Context context, Handler handler) {
        mAdapter_ = BluetoothAdapter.getDefaultAdapter();
        mState_ = STATE_NONE;
        mHandler_ = handler;
    }

    /**
     * Set the current state of the chat connection
     *
     * @param state An integer defining the current connection state
     */
    private synchronized void setState(int state) {
        Log.d(TAG, "setState() " + mState_ + " -> " + state);
        mState_ = state;

        // Give the new state to the Handler so the UI Activity can update
        mHandler_.obtainMessage(ActivityShareData.MESSAGE_STATE_CHANGE, state, -1).sendToTarget();
    }

    /**
     * Return the current connection state.
     */
    public synchronized int getState() {
        return mState_;
    }

    /**
     * Start AcceptThread to begin a
     * session in listening (server) mode. Called by the Activity onResume()
     */
    public synchronized void start() {
        Log.d(TAG, "start");

        // Cancel any thread attempting to make a connection
        if (mConnectThread_ != null) {
            mConnectThread_.cancel();
            mConnectThread_ = null;
        }

        // Cancel any thread currently running a connection
        if (mConnectedThread_ != null) {
            mConnectedThread_.cancel();
            mConnectedThread_ = null;
        }

        // Start the thread to listen on a BluetoothServerSocket
        if (mAcceptThread_ == null) {
            mAcceptThread_ = new AcceptThread();
            mAcceptThread_.start();
        }
        setState(STATE_LISTEN);
    }

    /**
     * Start the ConnectThread to initiate a connection to a remote device.
     *
     * @param device The BluetoothDevice to connect
     */
    public synchronized void connect(BluetoothDevice device) {
        Log.d(TAG, "connect to: " + device);

        // Cancel any thread attempting to make a connection
        if (mState_ == STATE_CONNECTING) {
            if (mConnectThread_ != null) {
                mConnectThread_.cancel();
                mConnectThread_ = null;
            }
        }

        // Cancel any thread currently running a connection
        if (mConnectedThread_ != null) {
            mConnectedThread_.cancel();
            mConnectedThread_ = null;
        }

        // Start the thread to connect with the given device
        mConnectThread_ = new ConnectThread(device);
        mConnectThread_.start();
        setState(STATE_CONNECTING);
    }

    /**
     * Start the ConnectedThread to begin managing a Bluetooth connection
     *
     * @param socket The BluetoothSocket on which the connection was made
     * @param device The BluetoothDevice that has been connected
     */
    public synchronized void connected(BluetoothSocket socket, BluetoothDevice device) {
        Log.d(TAG, "connected");

        // Cancel the thread that completed the connection
        if (mConnectThread_ != null) {
            mConnectThread_.cancel();
            mConnectThread_ = null;
        }

        // Cancel any thread currently running a connection
        if (mConnectedThread_ != null) {
            mConnectedThread_.cancel();
            mConnectedThread_ = null;
        }

        // Cancel the accept thread because we only want to connect to one device
        if (mAcceptThread_ != null) {
            mAcceptThread_.cancel();
            mAcceptThread_ = null;
        }

        // Start the thread to manage the connection and perform transmissions
        mConnectedThread_ = new ConnectedThread(socket);
        mConnectedThread_.start();

        // Send the name of the connected device back to the UI Activity
        Message msg = mHandler_.obtainMessage(ActivityShareData.MESSAGE_DEVICE_NAME);
        Bundle bundle = new Bundle();
        bundle.putString(ActivityShareData.DEVICE_NAME, device.getName());
        msg.setData(bundle);
        mHandler_.sendMessage(msg);

        setState(STATE_CONNECTED);
    }

    /**
     * Stop all threads
     */
    public synchronized void stop() {
        Log.d(TAG, "stop");
        if (mConnectThread_ != null) {
            mConnectThread_.cancel();
            mConnectThread_ = null;
        }
        if (mConnectedThread_ != null) {
            mConnectedThread_.cancel();
            mConnectedThread_ = null;
        }
        if (mAcceptThread_ != null) {
            mAcceptThread_.cancel();
            mAcceptThread_ = null;
        }
        setState(STATE_NONE);
    }

    /**
     * Write to the ConnectedThread in an unsynchronized manner
     *
     * @param out The bytes to write
     * @see ConnectedThread#write(byte[])
     */
    public void write(byte[] out) {
        // Create temporary object
        ConnectedThread r;
        // Synchronize a copy of the ConnectedThread
        synchronized (this) {
            if (mState_ != STATE_CONNECTED) return;
            r = mConnectedThread_;
        }
        // Perform the write unsynchronized
        r.write(out);
    }

    public void write(MetaInformation object) {
        // Create temporary object
        ConnectedThread r;
        // Synchronize a copy of the ConnectedThread
        synchronized (this) {
            if (mState_ != STATE_CONNECTED) return;
            r = mConnectedThread_;
        }
        // Perform the write unsynchronized
        r.write(object);
    }

    public void write(Purchase object) {
        // Create temporary object
        ConnectedThread r;
        // Synchronize a copy of the ConnectedThread
        synchronized (this) {
            if (mState_ != STATE_CONNECTED) return;
            r = mConnectedThread_;
        }
        // Perform the write unsynchronized
        r.write(object);
    }

    public ConnectedThread getConnetedThread() {
        return mConnectedThread_;
    }

    /**
     * Indicate that the connection attempt failed and notify the UI Activity.
     */
    private void connectionFailed() {
        setState(STATE_LISTEN);

        // Send a failure message back to the Activity
        Message msg = mHandler_.obtainMessage(ActivityShareData.MESSAGE_TOAST);
        Bundle bundle = new Bundle();
        bundle.putString(ActivityShareData.TOAST, "Unable to connect device");
        msg.setData(bundle);
        mHandler_.sendMessage(msg);
    }

    /**
     * Indicate that the connection was lost and notify the UI Activity.
     */
    private void connectionLost() {
        setState(STATE_LISTEN);

        // Send a failure message back to the Activity
        Message msg = mHandler_.obtainMessage(ActivityShareData.MESSAGE_TOAST);
        Bundle bundle = new Bundle();
        bundle.putString(ActivityShareData.TOAST, "Device connection was lost");
        msg.setData(bundle);
        mHandler_.sendMessage(msg);
    }

    /**
     * This thread runs while listening for incoming connections. It behaves
     * like a server-side client. It runs until a connection is accepted
     * (or until cancelled).
     */
    private class AcceptThread extends Thread {
        // The local server socket
        private final BluetoothServerSocket mmServerSocket;

        public AcceptThread() {
            BluetoothServerSocket socket = null;

            // Create a new listening server socket
            try {
                socket = mAdapter_.listenUsingRfcommWithServiceRecord("Expenses", UUID_);

            } catch (IOException e) {
                Log.e(TAG, "listen() failed", e);
            }
            mmServerSocket = socket;
        }

        public void run() {
            Log.d(TAG, "BEGIN mAcceptThread_" + this);
            setName("AcceptThread");
            BluetoothSocket socket = null;

            // Listen to the server socket if we're not connected
            while (mState_ != STATE_CONNECTED) {
                try {
                    // This is a blocking call and will only return on a
                    // successful connection or an exception
                    socket = mmServerSocket.accept();
                } catch (IOException e) {
                    Log.e(TAG, "accept() failed", e);
                    break;
                }

                // If a connection was accepted
                if (socket != null) {
                    synchronized (BluetoothService.this) {
                        switch (mState_) {
                            case STATE_LISTEN:
                            case STATE_CONNECTING:
                                // Situation normal. Start the connected thread.
                                connected(socket, socket.getRemoteDevice());
                                break;
                            case STATE_NONE:
                            case STATE_CONNECTED:
                                // Either not ready or already connected. Terminate new socket.
                                try {
                                    socket.close();
                                } catch (IOException e) {
                                    Log.e(TAG, "Could not close unwanted socket", e);
                                }
                                break;
                        }
                    }
                }
            }
            Log.i(TAG, "END mAcceptThread_");
        }

        public void cancel() {
            Log.d(TAG, "cancel " + this);
            try {
                mmServerSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "close() of server failed", e);
            }
        }
    }


    /**
     * This thread runs while attempting to make an outgoing connection
     * with a device. It runs straight through; the connection either
     * succeeds or fails.
     */
    private class ConnectThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final BluetoothDevice mmDevice;

        public ConnectThread(BluetoothDevice device) {
            mmDevice = device;

            // Get a BluetoothSocket for a connection with the
            // given BluetoothDevice
            BluetoothSocket socket = null;
            try {
                socket = device.createRfcommSocketToServiceRecord(UUID_);
            } catch (IOException e) {
                Log.e(TAG, "create() failed", e);
            }
            mmSocket = socket;
        }

        public void run() {
            Log.i(TAG, "BEGIN mConnectThread_");
            setName("ConnectThread");

            // Always cancel discovery because it will slow down a connection
            mAdapter_.cancelDiscovery();

            // Make a connection to the BluetoothSocket
            try {
                // This is a blocking call and will only return on a
                // successful connection or an exception
                mmSocket.connect();
            } catch (IOException e) {
                Log.e(TAG, "connection failed", e);
                connectionFailed();
                // Close the socket
                try {
                    mmSocket.close();
                } catch (IOException e2) {
                    Log.e(TAG, "unable to close() socket during connection failure", e2);
                }
                // Start the service over to restart listening mode
                BluetoothService.this.start();
                return;
            }

            // Reset the ConnectThread because we're done
            synchronized (BluetoothService.this) {
                mConnectThread_ = null;
            }

            // Start the connected thread
            connected(mmSocket, mmDevice);
        }

        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "close() of connect socket failed", e);
            }
        }
    }

    /**
     * This thread runs during a connection with a remote device.
     * It handles all incoming and outgoing transmissions.
     */
    private class ConnectedThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;
        private final ObjectOutputStream mmObjectOutStream;


        public ConnectedThread(BluetoothSocket socket) {
            Log.d(TAG, "create ConnectedThread");
            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;
            ObjectOutputStream tmpObjectOut=null;

            // Get the BluetoothSocket input and output streams
            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
                tmpObjectOut=new ObjectOutputStream(tmpOut);
            } catch (IOException e) {
                Log.e(TAG, "temp sockets not created", e);
            }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
            mmObjectOutStream =tmpObjectOut;

        }

        public void run() {
            Log.i(TAG, "BEGIN mConnectedThread_");
            byte[] buffer = new byte[1024];
            int bytes;

            ObjectInputStream ois = null;

            try {
                ois = new ObjectInputStream(mmInStream);
            } catch (Exception e) {
                return;
            }


            // Keep listening to the InputStream while connected
            while (true) {
                try {
                    // Read from the InputStream
//                    bytes = mmInStream.read(buffer);

                    // Send the obtained bytes to the UI Activity
//                    mHandler_.obtainMessage(ActivityShareData.MESSAGE_READ, bytes, -1, buffer)
//                            .sendToTarget();
                    Object object = ois.readObject();
                    mHandler_.obtainMessage(ActivityShareData.MESSAGE_READ, 1, -1, object)
                            .sendToTarget();
                } catch (IOException e) {
                    Log.e(TAG, "disconnected", e);
                    connectionLost();
                    break;
                } catch (ClassNotFoundException e) {
                    break;
                }
            }
        }

        /**
         * Write to the connected OutStream.
         *
         * @param buffer The bytes to write
         */
        public void write(byte[] buffer) {
            try {
                mmOutStream.write(buffer);

                // Share the sent message back to the UI Activity
//                mHandler_.obtainMessage(ActivityShareData.MESSAGE_WRITE, -1, -1, buffer)
//                        .sendToTarget();
            } catch (IOException e) {
                Log.e(TAG, "Exception during write", e);
            }
        }

        public void write(Purchase object) {
            try {
                mmObjectOutStream.writeObject(object);

            } catch (IOException e) {
                Log.e(TAG, "Exception during write", e);
            }
        }

        public void write(MetaInformation object) {
            try {
                mmObjectOutStream.writeObject(object);

            } catch (IOException e) {
                Log.e(TAG, "Exception during write", e);
            }
        }

        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "close() of connect socket failed", e);
            }
        }
    }

}
