package com.yantrammedtech.cpap_notifytest;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.material.appbar.MaterialToolbar;
import com.yantrammedtech.cpap_notifytest.bluetooth.BleUUIDS;
import com.yantrammedtech.cpap_notifytest.databinding.ActivityConnectDeviceBinding;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class ConnectDevice extends AppCompatActivity {
    private static final String TAG = "swaas_ConnectDevice";
    private ActivityConnectDeviceBinding binding;
    private String deviceName = "", deviceAddress = "";
    //
    private BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
    private BluetoothGatt bluetoothGatt;
    private Handler handler = new Handler(Looper.getMainLooper());
    private boolean isConnected = false;
    private int connectionStatus = 0; // 0 - started , 1 - connected , -1 disconnected
    private int CONNECT_TIMEOUT = 30000;
    private boolean isConnTimeout = false;
    private boolean isNotfiyEnabled = false;

    private BluetoothGattCharacteristic char_battery;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityConnectDeviceBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);
        //
        initToolBar();
        initDefaults();
        onClickListeners();
    }

    private void initToolBar() {
        MaterialToolbar materialToolbar = findViewById(R.id.toolBar_appBar);
        materialToolbar.setTitle("Connect To Device");
        materialToolbar.setTitleTextColor(getResources().getColor(R.color.white));
        materialToolbar.setNavigationIcon(ContextCompat.getDrawable(this, R.drawable.ic_baseline_arrow_back_24));
        materialToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
    }

    private void initDefaults() {
        Intent intent = getIntent();
        deviceName = intent.getStringExtra("deviceName");
        deviceAddress = intent.getStringExtra("deviceAddress");
        if (deviceName.isEmpty() || deviceAddress.isEmpty()) {
            Log.d(TAG, "initDefaults: DEVICE NAME AND ADDRESS EMPTY");
            Toast.makeText(this, "FAILED DEVICE NAME", Toast.LENGTH_SHORT).show();
            return;
        }
        binding.connectDeviceName.setText(deviceName);
        binding.connectDeviceAddress.setText(deviceAddress);
        resetConnect();
    }

    private void onClickListeners() {
        binding.connectReconnectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                resetConnect();
            }
        });
    }

    private void resetConnect() {
        connect();
    }

    @SuppressLint("MissingPermission")
    private void connect() {
        Log.d(TAG, " * connect: Start");
        BluetoothDevice bluetoothDevice = adapter.getRemoteDevice(deviceAddress);
        setConnecting();
        isConnTimeout = true;
        handler.postDelayed(RunnableConnectTimeout, CONNECT_TIMEOUT);
        bluetoothGatt = bluetoothDevice.connectGatt(this, false, bluetoothGattCallback, BluetoothDevice.TRANSPORT_LE);
    }


    @SuppressLint("MissingPermission")
    private void disconnect() {
        Log.d(TAG, "disconnect: called");
        if (bluetoothGatt != null) {
            bluetoothGatt.disconnect();
        }
    }

    @SuppressLint("MissingPermission")
    private void close() {
        Log.d(TAG, "close: called");
        if (isConnTimeout) {
            isConnTimeout = false;
            handler.removeCallbacks(RunnableConnectTimeout);
        }
        if (bluetoothGatt != null) {
            isConnected = false;
            bluetoothGatt.close();
            bluetoothGatt = null;
            handler.post(new Runnable() {
                @Override
                public void run() {
                    connectionStatus = -1;
                    setConnecting();
                }
            });
        }
    }

    private Runnable RunnableConnectTimeout = new Runnable() {
        @Override
        public void run() {
            isConnTimeout = false;
            disconnect();
        }
    };

    private BluetoothGattCallback bluetoothGattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            super.onConnectionStateChange(gatt, status, newState);
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                connectionStatus = 1;

                bluetoothGatt = gatt;
                handler.removeCallbacks(RunnableConnectTimeout); // remove timeout
                handler.post(new Runnable() {
                    @SuppressLint("MissingPermission")
                    @Override
                    public void run() {
                        setConnecting();
                        bluetoothGatt.discoverServices();
                    }
                });
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {

                if (status == 133) {
                    Log.d(TAG, "onConnectionStateChange: 133 Connection status");
                    Log.d(TAG, "onConnectionStateChange: retry connection");
                }
                close();
            }
        }


        @Override
        public void onDescriptorRead(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            super.onDescriptorRead(gatt, descriptor, status);
            Log.d(TAG, "onDescriptorRead: ");
        }

        @Override
        public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            super.onDescriptorWrite(gatt, descriptor, status);
            if (status != BluetoothGatt.GATT_SUCCESS) {
                Log.d(TAG, "onDescriptorWrite: write descriptor failed");
                return;
            }
            if (descriptor.getUuid().equals(UUID.fromString(BleUUIDS.CPAP_DESCRIPTOR_UUID))) {
                Log.d(TAG, "onDescriptorWrite: SUCCESS WRITING DESCRIPTOR");
                byte[] value = descriptor.getValue();
                if (value != null) {
                    if (value[0] != 0) {
                        Log.d(TAG, "onDescriptorWrite: NOTIFY ENABLED");
                    } else {
                        Log.d(TAG, "onDescriptorWrite: NOTIFY DISABLED");
                    }
                }
                isNotfiyEnabled = true;
            }
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            super.onCharacteristicChanged(gatt, characteristic);
            Log.d(TAG, "onCharacteristicChanged:* ");

            new Thread(new Runnable() {
                @Override
                public void run() {
                    Log.d(TAG, "run: Thread running and closing");
                }
            });
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            super.onServicesDiscovered(gatt, status);
            if (status == BluetoothGatt.GATT_SUCCESS) {
                List<BluetoothGattService> serviceList = gatt.getServices();
                boolean serviceFound = false;
                BluetoothGattService s = null;
                for (BluetoothGattService service : serviceList) {
                    if (service.getUuid().toString().equals(BleUUIDS.CPAP_SERVICE_UUID)) {
                        serviceFound = true;
                        s = service;
                        break;
                    }
                }
                if (serviceFound) {
                    discoverCharacteristics(s);
                } else {
                    Log.d(TAG, "onServicesDiscovered: NO SERVICE FOUND");
                    showToast("No Service Found");
                    disconnect();
                }
            }
        }
    };


//    private BluetoothGattCallback bluetoothGattCallback = new BluetoothGattCallback() {
//        @Override
//        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
//            super.onConnectionStateChange(gatt, status, newState);
//        }
//
//        @Override
//        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
//            super.onServicesDiscovered(gatt, status);
//        }
//
//        @Override
//        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
//            super.onCharacteristicChanged(gatt, characteristic);
//        }
//
//        @Override
//        public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
//            super.onDescriptorWrite(gatt, descriptor, status);
//        }
//    };

    private void discoverCharacteristics(BluetoothGattService service) {
        List<BluetoothGattCharacteristic> characteristicList = service.getCharacteristics();
        for (BluetoothGattCharacteristic characteristic : characteristicList) {
            switch (characteristic.getUuid().toString()) {
                case BleUUIDS.CPAP_BATTERY:
                    char_battery = characteristic;
                    Log.d(TAG, "discoverCharacteristics: Found Battery Characteristic");
                    break;
            }
        }

        if (char_battery != null) {
            setNotify(char_battery, true);
        } else {
            Log.d(TAG, "discoverCharacteristics: CHAR BATTERY IS NULL");
            disconnect();
        }

    }


    private void startNotifyBattery() {
        setNotify(char_battery, true);
    }

    private void stopNotifyBattery() {
        setNotify(char_battery, false);
    }

    @SuppressLint("MissingPermission")
    private void setNotify(BluetoothGattCharacteristic gattCharacteristic, boolean enable) {
        Log.d(TAG, "setNotify: " + enable);
        if (gattCharacteristic != null) {
            int properties = gattCharacteristic.getProperties();
            byte[] value;
//            if ((properties & BluetoothGattCharacteristic.PROPERTY_NOTIFY) > 0) {
//                Log.d(TAG, "setNotify: notification found");
//                value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE;
//            }
////            else if ((properties & BluetoothGattCharacteristic.PROPERTY_INDICATE) > 0) {
////                Log.d(TAG, "setNotify: indicator found");
////                value = BluetoothGattDescriptor.ENABLE_INDICATION_VALUE;
////            }
//            else {
//                Log.e(TAG, "setNotify: FAILED , NO NOTIFY OR INDICATE ON GATT CHARACTERISTIC");
//                return;
//            }

            BluetoothGattDescriptor descriptor = gattCharacteristic.
                    getDescriptor(UUID.fromString(BleUUIDS.CPAP_DESCRIPTOR_UUID));
            if (descriptor == null) {
                Log.e(TAG, "setNotify: DESCRIPTOR IS NULL ");
                return;
            }
//            byte[] finalValue = enable ? value : BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE;
//            Log.d(TAG, "setNotify: final value " + Arrays.toString(finalValue));
//            gattCharacteristic.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT);
            value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE;
            descriptor.setValue(value);
            bluetoothGatt.writeDescriptor(descriptor);
        }
    }


    /**
     * ****************** CONNECTION CHANGES ************************
     */
    private void setConnecting() {
        if (connectionStatus == 0) {
            binding.connectConnectStatus.setText("CONNECTING...");
            binding.connectConnectStatus.setBackground(ContextCompat.getDrawable(this, R.drawable.text_pending));
            binding.connectReconnectButton.setVisibility(View.GONE);
        } else if (connectionStatus == 1) {
            binding.connectConnectStatus.setText("CONNECTED");
            binding.connectConnectStatus.setBackground(ContextCompat.getDrawable(this, R.drawable.text_accepted));
            binding.connectReconnectButton.setVisibility(View.GONE);
        } else if (connectionStatus == -1) {
            binding.connectConnectStatus.setText("DISCONNECTED");
            binding.connectConnectStatus.setBackground(ContextCompat.getDrawable(this, R.drawable.text_rejected));
            binding.connectReconnectButton.setVisibility(View.VISIBLE);
        }
    }

    /**
     * ***************** SHOW TOAST ********************************
     */
    private void showToast(String message) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(ConnectDevice.this, "message", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * *************** LIFECYCLE ***********************************
     */
    @Override
    protected void onStop() {
        super.onStop();
        if (isNotfiyEnabled) {
            setNotify(char_battery, false);
        }
        if (connectionStatus > 0) {
            disconnect();
        }
    }
}