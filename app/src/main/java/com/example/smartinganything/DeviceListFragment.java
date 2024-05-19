package com.example.smartinganything;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;

import androidx.core.app.ActivityCompat;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Toast;

import com.example.smartinganything.RecyclerView.DeviceListRecyclerAdapter;
import com.example.smartinganything.databinding.FragmentDeviceListBinding;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;


public class DeviceListFragment extends DialogFragment {

    public static final String TAG = "DeviceListFragment";
    private final Context context;
    private final Activity activity;
    private final BluetoothAdapter bluetoothAdapter;
    public List<Device> mPairedDevices;
    public List<Device> mDiscoveredDevices = new ArrayList<>();
    public Set<BluetoothDevice> sDevices;
    FragmentDeviceListBinding binding;
    RecyclerView rvPairedDevices;
    RecyclerView rvDiscoveredDevices;
    DeviceListRecyclerAdapter mPairedDeviceListAdapter;
    DeviceListRecyclerAdapter mDiscoveredDeviceListAdapter;

    public DeviceListFragment(Activity activity) {
        this.activity = activity;
        this.context = activity.getApplicationContext();
        this.bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        mPairedDevices =new ArrayList<>();

    }

    @Override
    public void onStart() {
        super.onStart();

        if (getDialog() != null) {
            Window window = getDialog().getWindow();
            if (window != null) {
                // get the size of the screen
                DisplayMetrics displayMetrics = new DisplayMetrics();
                window.getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
                int screenWidth = displayMetrics.widthPixels;

                // set the width of the dialog to 90% of the screen width
                int dialogWidth = (int) (screenWidth * 0.9);
                window.setLayout(dialogWidth, ViewGroup.LayoutParams.WRAP_CONTENT);
            }
        }
    }

    private void bluetoothSetup() {

        //Supporting Bluetooth checker
        if (bluetoothAdapter == null) {
            dismiss();
        }


        //Get paired devices
        sDevices = bluetoothAdapter.getBondedDevices();
        if (sDevices != null) {
            for (BluetoothDevice d : sDevices) {
                mPairedDevices.add(new Device(d.getName(), d.getAddress()));
            }
        }


        //Discover bluetooth device
        bluetoothAdapter.startDiscovery();
        Toast.makeText(getContext(),"Discovering is Begin . . .",Toast.LENGTH_LONG).show();
        // Register for broadcasts when a device is discovered
        IntentFilter intentFilter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        context.registerReceiver(mBroadcastReceiver, intentFilter);

        // Register for broadcasts when discovery has finished
        intentFilter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        context.registerReceiver(mBroadcastReceiver, intentFilter);

    }

    private final BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            // When discovery finds a device
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                // Get the BluetoothDevice object from the Intent
                BluetoothDevice d = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                // If it's already paired, skip it, because it's been listed already
                if (d != null && d.getBondState() != BluetoothDevice.BOND_BONDED) {
                    Device device = new Device(d.getName(), d.getAddress());

                    if(!mDiscoveredDevices.contains(device)){

                        //Add device to device list in adapter
                        mDiscoveredDevices.add(device);
                        mDiscoveredDeviceListAdapter.notifyDataSetChanged();

                    }
                }
                // When discovery is finished, change the Activity title
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                Log.i("BLService","Discover Finished");
                binding.nts2.setVisibility(View.GONE);
            }
        }
    };

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (bluetoothAdapter != null)
            bluetoothAdapter.cancelDiscovery();
        mPairedDevices.clear();

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        bluetoothSetup();

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Define binding
        binding = FragmentDeviceListBinding.inflate(inflater,container,false);

        //Define recycler views
        rvPairedDevices =binding.listPairedContainer;
        rvDiscoveredDevices =binding.listDiscoveredContainer;

        //Define Adapters
        mPairedDeviceListAdapter = new DeviceListRecyclerAdapter(this,mPairedDevices);
        mDiscoveredDeviceListAdapter = new DeviceListRecyclerAdapter(this,mDiscoveredDevices);

        //define layout manager and item animator and adapter for recycler view discovered devices
        rvDiscoveredDevices.setLayoutManager(new LinearLayoutManager(getContext(),LinearLayoutManager.VERTICAL,false));
        rvDiscoveredDevices.setItemAnimator(new DefaultItemAnimator());
        rvDiscoveredDevices.setAdapter(mDiscoveredDeviceListAdapter);

        //define layout manager and item animator and adapter for recycler view paired devices
        rvPairedDevices.setLayoutManager(new LinearLayoutManager(getContext(),LinearLayoutManager.VERTICAL,false));
        rvPairedDevices.setItemAnimator(new DefaultItemAnimator());
        rvPairedDevices.setAdapter(mPairedDeviceListAdapter);

        //set 'Nothing to show' gone
        binding.nts1.setVisibility(View.GONE);
        if(mPairedDevices.size()<1){
            binding.nts1.setVisibility(View.VISIBLE);
        }

        return binding.getRoot();
    }

    public void onClickedDevice(String deviceName,String deviceAddress){
        bluetoothAdapter.cancelDiscovery();
        BluetoothDevice device = bluetoothAdapter.getRemoteDevice(deviceAddress);
        MainActivity mainActivity = (MainActivity) getActivity();
        assert mainActivity != null;
        Toast.makeText(getContext(),"Connecting to "+ deviceName,Toast.LENGTH_LONG).show();
        mainActivity.bluetoothService.connectToDevice(device);

    }

    @Override
    public void onResume() {
        super.onResume();
        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothDevice.ACTION_FOUND);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        context.registerReceiver(mBroadcastReceiver, filter);
    }

    @Override
    public void onPause() {
        super.onPause();
        context.unregisterReceiver(mBroadcastReceiver);
    }

    public static class Device {
        public final String DeviceName;
        public final String MACAddress;

        public Device(String deviceName, String mACAddress) {
            DeviceName = deviceName;
            MACAddress = mACAddress;

        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Device)) return false;
            Device device = (Device) o;
            return Objects.equals(DeviceName, device.DeviceName) &&
                    Objects.equals(MACAddress, device.MACAddress);
        }

        @Override
        public int hashCode() {
            return Objects.hash(DeviceName, MACAddress);
        }
    }
}