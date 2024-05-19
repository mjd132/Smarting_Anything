package com.example.smartinganything;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.smartinganything.BluetoothHandling.BluetoothService;
import com.example.smartinganything.ViewModels.ButtonViewModel;
import com.example.smartinganything.databinding.ActivityMainBinding;
import com.google.android.material.navigation.NavigationBarView;

import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_ENABLE_BLUETOOTH = 1;
    public BluetoothService bluetoothService;
    DeviceListFragment deviceListFragment;
    BluetoothAdapter bluetoothAdapter;
    BluetoothManager bluetoothManager;

    ActivityResultLauncher<Intent> bluetoothEnableResultActivity = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
        @Override
        public void onActivityResult(ActivityResult activityResult) {
            if (activityResult.getResultCode() == Activity.RESULT_OK) {
                Toast.makeText(getApplicationContext(), "Bluetooth enabled", Toast.LENGTH_SHORT).show();

            } else {
                Toast.makeText(getApplicationContext(), "Bluetooth enabling failed", Toast.LENGTH_LONG).show();

            }
        }
    });
    private ActivityMainBinding binding;
    //Handler for handle bluetooth tx and rx
    private final Handler handler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(@NonNull Message msg) {

            switch (msg.what) {
                case BluetoothService.STATUS_DISCONNECTED:
                    Toast.makeText(getApplicationContext(), "Lost Connection.\nDisconnected!", Toast.LENGTH_LONG).show();
                    binding.btFAButton.setBackgroundTintList(ContextCompat.getColorStateList(getApplicationContext(), R.color.primary_purple));
                    Log.i("BLService", "Disconnected from device");
                    break;
                case BluetoothService.STATUS_CONNECTION_FAILED:
                    Toast.makeText(getApplicationContext(), "Couldn't connect to device.", Toast.LENGTH_LONG).show();
                    Log.i("BLService", "Connection Failed!");
                    break;
                case BluetoothService.STATUS_CONNECTING:
                    Toast.makeText(getApplicationContext(), "Conncting . . .", Toast.LENGTH_LONG).show();
                    Log.i("BLService", "Connecting ...");
                    break;
                case BluetoothService.STATUS_CONNECTED:
                    if (deviceListFragment != null) deviceListFragment.dismiss();
                    Toast.makeText(getApplicationContext(), "Conncted!", Toast.LENGTH_LONG).show();
                    binding.btFAButton.setBackgroundTintList(ContextCompat.getColorStateList(getApplicationContext(), R.color.green_btConnected));
                    Log.i("BLService", "Connected");
                    break;
                case BluetoothService.STATUS_MESSAGE_READ:
                    String receivedMessage = (String) msg.obj;
                    Fragment activeFragment = getSupportFragmentManager().findFragmentById(binding.container.getId());
                    if(activeFragment instanceof ActionFragment) {
                        ActionFragment.messagesReceived.append(receivedMessage);
                        actionFragment.receiveMessage();
                    }
                    else {
                        ButtonViewModel.ReceivedResponseMessageBT(receivedMessage);
                        ActionFragment.messagesReceived.append(receivedMessage);
                    }
                    Log.i("BLService", "RECEIVED MESSAGE  ----  message : " + receivedMessage);
                    break;
                case BluetoothService.STATUS_MESSAGE_WRITE:
                    Toast.makeText(getApplicationContext(), "Message Sent!", Toast.LENGTH_LONG).show();
                    Log.i("BLService", "SENT MESSAGE");
                    break;

            }

            return true;
        }
    });
    private ButtonFragment buttonFragment ;
    private ActionFragment actionFragment = new ActionFragment();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

        //create bluetooth adapter class instance and check bluetooth
        bluetoothAdapter= BluetoothAdapter.getDefaultAdapter();
        checkPermissions();
        checkBluetoothEnabled();

        // navigation bottom logic
        NavigationBarView bottomNav = binding.bottomnav;
        buttonFragment = new ButtonFragment(bluetoothService);
        getSupportFragmentManager().beginTransaction().replace(binding.container.getId(), buttonFragment).commit();

        bottomNav.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {

                int itemId = item.getItemId();
                if (itemId == R.id.actionsNav) {
                    getSupportFragmentManager().beginTransaction().replace(binding.container.getId(), actionFragment).commit();
                    return true;
                } else if (itemId == R.id.buttonsNav) {
                    getSupportFragmentManager().beginTransaction().replace(binding.container.getId(), buttonFragment).commit();
                    return true;
                }

                return false;
            }
        });

        //check bluetooth is supported
        //devices and connect to device fragment
        if (BluetoothAdapter.getDefaultAdapter() != null) {

            deviceListFragment = new DeviceListFragment(this);

            binding.btFAButton.setOnClickListener(v -> {
                deviceListFragment.show(getSupportFragmentManager(), DeviceListFragment.TAG);
            });
            binding.btFAButton.setOnLongClickListener(v->{
                bluetoothService.disconnectDevice();
                return true;
            });
        } else {
            binding.btFAButton.setClickable(false);
            binding.btFAButton.setBackgroundColor(Color.GRAY);
        }

    }

    @Override
    protected void onStart() {
        super.onStart();


    }

    public void checkPermissions(){
        String TAG ="Permission";
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            ActivityResultLauncher<String[]> requestMultiplePermissions = registerForActivityResult(
                    new ActivityResultContracts.RequestMultiplePermissions(),
                    permissions -> {
                        for (Map.Entry<String, Boolean> entry : permissions.entrySet()) {
                            Log.d("Permissions", entry.getKey() + " = " + entry.getValue());
                        }
                    }
            );
            requestMultiplePermissions.launch(new String[]{
                    Manifest.permission.BLUETOOTH_SCAN,
                    Manifest.permission.BLUETOOTH_CONNECT
            });
        } else {
            ActivityResultLauncher<Intent> requestBluetooth = registerForActivityResult(
                    new ActivityResultContracts.StartActivityForResult(),
                    result -> {
                        if (result.getResultCode() == Activity.RESULT_OK) {
                            Log.d(TAG,"granted");
                        } else {
                            Log.d(TAG,"denied");
                        }
                    }
            );
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            requestBluetooth.launch(enableBtIntent);
        }
    }
    void checkBluetoothEnabled() {
        if (bluetoothAdapter != null) if (!bluetoothAdapter.isEnabled())
            turnOnBluetooth();

        else if (bluetoothService == null) setupBluetooth();
    }

    void turnOnBluetooth() {
        Intent enableBluetoothIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        bluetoothEnableResultActivity.launch(enableBluetoothIntent);
        setupBluetooth();
    }

    private void setupBluetooth() {
        bluetoothService = new BluetoothService(this, handler);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.d("test","pg");
            } else
                Toast.makeText(getApplicationContext(), "Please Accept Permission", Toast.LENGTH_LONG).show();
        }
    }


}