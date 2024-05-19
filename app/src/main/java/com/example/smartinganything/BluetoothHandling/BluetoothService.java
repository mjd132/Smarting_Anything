package com.example.smartinganything.BluetoothHandling;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;


public class BluetoothService {

    public static final int STATUS_UNKNOWN = 0;
    public static final int STATUS_CONNECTING = 1;
    public static final int STATUS_CONNECTED = 2;
    public static final int STATUS_MESSAGE_READ = 3;
    public static final int STATUS_MESSAGE_WRITE = 4;
    public static final int STATUS_DISCONNECTED = 5;
    public static final int STATUS_CONNECTION_FAILED = 6;
    public static final int STATE_CHANGED = 3;
    private static final String TAG = "BLService";
    private static final UUID MY_BL_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private static final int REQUEST_ENABLE_BLUETOOTH = 1;
    public static int STATE = STATUS_UNKNOWN;
    public final boolean hasBluetoothPermission;
    public final boolean hasBluetoothFeature;
    public final boolean isBluetoothSupported;
    private final BluetoothAdapter bluetoothAdapter;
    private final Handler handler;
    private final Activity activity;
    public Context context;
    private BluetoothDevice targetDevice;
    private ConnectThread connectThread;
    private ConnectedThread connectedThread;

    public BluetoothService(Context context, Handler handler) {
        this.handler = handler;
        this.context = context;
        this.activity = (Activity) context;

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        hasBluetoothPermission = checkBluetoothPermission();
        hasBluetoothFeature = activity.getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH);
        isBluetoothSupported = bluetoothAdapter != null;


    }

    public boolean checkBluetoothPermission() {
        if (context.checkSelfPermission(Manifest.permission.BLUETOOTH) == PackageManager.PERMISSION_GRANTED)
            if (context.checkSelfPermission(Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED)
                return context.checkSelfPermission(Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_GRANTED;

        return false;
    }

    public boolean checkBluetoothSupported() {
        return bluetoothAdapter != null;
    }

    public boolean checkBluetoothEnabled() {
        return bluetoothAdapter.isEnabled();
    }

    public boolean connectToDevice(BluetoothDevice device) {
//        if(STATE == STATUS_CONNECTING)
        if (connectThread != null) {
            connectThread.cancel();
            connectThread = null;
        }
        if (connectedThread != null) {
            connectedThread.cancel();
            connectedThread = null;
        }

        connectThread = new ConnectThread(device);
        connectThread.start();
        return true;

    }

    public void disconnectDevice() {
        if (STATE == STATUS_CONNECTED) {
            if (connectThread != null) {
                connectThread.cancel();
                connectThread = null;
            }
            if (connectedThread != null) {
                connectedThread.cancel();
                connectedThread = null;
            }
            setState(STATUS_DISCONNECTED);
        }
    }

    public void sendData(String message) {

        if (connectedThread != null) {
            if (message.contains("\r\n")) connectedThread.write(message.getBytes());
            else {
                message = message + "\r\n";
                connectedThread.write(message.getBytes());
            }
        } else {
            try {
                connectThread.cancel();

            } catch (Exception e) {
                connectThread = null;
                Log.d(TAG, "bluetooth connect thread cant cancel.", e);
            }
            setState(STATUS_DISCONNECTED);
        }

    }

    public String receiveData() {
        if (connectedThread != null) {
            connectedThread.run();
        }
        return null;
    }

    public void stop() {
        if (connectThread != null) {
            connectThread.cancel();
            connectThread = null;
        }

        if (connectedThread != null) {
            connectedThread.cancel();
            connectedThread = null;
        }
    }

    public void setState(int newState) {
        STATE = newState;

        Log.i(TAG, "STATE CHANGED , NEW STATE : " + STATE);
        if (STATE != STATUS_MESSAGE_READ && STATE != STATUS_MESSAGE_WRITE)
            handler.obtainMessage(STATE).sendToTarget();

    }

    private class ConnectThread extends Thread {

        private final BluetoothSocket socket;

        public ConnectThread(BluetoothDevice device) {
            BluetoothSocket tmp = null;
            targetDevice = device;
            setState(STATUS_CONNECTING);
            try {
                tmp = device.createRfcommSocketToServiceRecord(MY_BL_UUID);
            } catch (IOException e) {

                Log.e(TAG, "Socket create() failed", e);

            }
            socket = tmp;
        }

        public void run() {

            bluetoothAdapter.cancelDiscovery();

            try {
                socket.connect();
                connectedThread = new ConnectedThread(socket);
                connectedThread.start();
                setState(STATUS_CONNECTED);
            } catch (IOException connectException) {
                Log.e(TAG, "Unable to connect socket", connectException);
                setState(STATUS_CONNECTION_FAILED);
                try {
                    socket.close();
                } catch (IOException closeException) {
                    Log.e(TAG, "Socket close() failed", closeException);
                }
            }
        }

        public void cancel() {
            try {
                socket.close();
                setState(STATUS_DISCONNECTED);
            } catch (IOException e) {
                Log.e(TAG, "Socket close() failed", e);
            }
        }
    }

    private class ConnectedThread extends Thread {

        private final BluetoothSocket socket;
        private final InputStream inputStream;
        private final OutputStream outputStream;


        public ConnectedThread(BluetoothSocket socket) {
            this.socket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) {
                Log.e(TAG, "Error creating Input/Output streams", e);
            }

            inputStream = tmpIn;
            outputStream = tmpOut;

        }

        public void run() {
            byte[] buffer = new byte[1024];
            int numBytes;
            StringBuilder readMessage = new StringBuilder();
            while (STATE == STATUS_CONNECTED) {
                try {
                    numBytes = inputStream.read(buffer);
                    String read = new String(buffer, 0, numBytes);
                    readMessage.append(read);
                    if (read.contains("\n")) {
                        handler.obtainMessage(STATUS_MESSAGE_READ, numBytes, -1, readMessage.toString()).sendToTarget();
                        readMessage.setLength(0);
                    }

                } catch (IOException e) {
                    setState(STATUS_DISCONNECTED);
                    Log.e(TAG, "Input stream disconnected", e);
                    break;
                }
            }
            setState(STATUS_DISCONNECTED);
        }

        public void write(byte[] bytes) {
            try {
                outputStream.write(bytes);
                handler.obtainMessage(STATUS_MESSAGE_WRITE, -1, -1, bytes).sendToTarget();
            } catch (IOException e) {
                Log.e(TAG, "Error writing to output stream", e);
            }
        }

        public void cancel() {
            try {
                socket.close();
            } catch (IOException e) {
                Log.e(TAG, "Socket close() failed", e);
            }
        }
    }

}




