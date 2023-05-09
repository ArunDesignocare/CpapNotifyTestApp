package com.yantrammedtech.cpap_notifytest;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.material.appbar.MaterialToolbar;
import com.yantrammedtech.cpap_notifytest.databinding.ActivityMainBinding;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements AdapterScan.BluetoothDeviceListener {
    private static final String TAG = "swa_MainActivity";
    private ActivityMainBinding binding;
    private Handler handler = new Handler(Looper.getMainLooper());
    private static final int LOCATION_PERMISSION_CODE = 121;
    private static final int BLUETOOTH_SCAN_CODE = 122;
    private static final int BLUETOOTH_CONNECT_CODE = 123;
    private MainVM mainVM;
    private AdapterScan adapterScan;
    private List<BluetoothDevice> bluetoothDeviceList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);
        //
        initToolBar();
        checkDarkMode();
        checkPermissions();
        onClickListeners();
        initRecycler();
        initVM();
    }

    private void initToolBar() {
        MaterialToolbar materialToolbar = findViewById(R.id.toolBar_appBar);
        materialToolbar.setTitle("Scan For Device");
        materialToolbar.setTitleTextColor(getResources().getColor(R.color.white));
//        materialToolbar.setNavigationIcon(ContextCompat.getDrawable(this, R.drawable.chevron_left));
//        materialToolbar.setNavigationOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                onBackPressed();
//            }
//        });
    }

    private void onClickListeners() {
        binding.mainStartScanButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startScan();
            }
        });
    }

    private void initRecycler() {
        binding.mainRecycler.setLayoutManager(new LinearLayoutManager(this));
        adapterScan = new AdapterScan(this, bluetoothDeviceList, this);
        binding.mainRecycler.setAdapter(adapterScan);
    }

    @Override
    public void onDeviceSelected(BluetoothDevice bluetoothDevice) {
        Log.d(TAG, "onDeviceSelected: " + bluetoothDevice.toString());
        handler.post(new Runnable() {
            @Override
            public void run() {
                gotoConnect(bluetoothDevice);
            }
        });
    }

    @SuppressLint("MissingPermission")
    private void gotoConnect(BluetoothDevice device) {
        stopScan();
        Intent intent = new Intent(this, ConnectDevice.class);
        intent.putExtra("deviceName", device.getName());
        intent.putExtra("deviceAddress", device.getAddress());
        startActivity(intent);
    }

    private void initVM() {
        mainVM = new ViewModelProvider(this).get(MainVM.class);
        mainVM.getScannedDevices().observe(this, new Observer<List<BluetoothDevice>>() {
            @Override
            public void onChanged(List<BluetoothDevice> bluetoothDevices) {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        setDevices(bluetoothDevices);
                    }
                });
            }
        });
    }

    private void setDevices(List<BluetoothDevice> bluetoothDevices) {
        bluetoothDeviceList.clear();
        bluetoothDeviceList.addAll(bluetoothDevices);
        adapterScan.notifyDataSetChanged();
//        binding.main.setText(bluetoothDeviceList.size());
//        int size = bluetoothDevices.size();
//        if (size > 0) {
//            binding.mainDevicesFound.setText(String.valueOf(size));
//        } else {
//            binding.mainDevicesFound.setText(String.valueOf(0));
//        }
//        binding.mainDevicesFound.setText(bluetoothDevices.size());
    }

    @Override
    protected void onStop() {
        super.onStop();
        stopScan();
    }

    /**
     * \**************** LIFECYCLE ****************************
     */


    //
    private boolean isScanning = false;
    private boolean isScanTimeOut = false;

    private Runnable RunnableScanTimeOut = new Runnable() {
        @Override
        public void run() {
            isScanTimeOut = false;
            stopScan();
        }
    };

    private void startScan() {
        if (!isScanning) {
            isScanning = true;
            isScanTimeOut = true;
            handler.postDelayed(RunnableScanTimeOut, 30000);
            showScanning();
            mainVM.startScan();
        }
    }

    private void stopScan() {
        if (isScanTimeOut) {
            isScanTimeOut = false;
            handler.removeCallbacks(RunnableScanTimeOut);
        }
        if (isScanning) {
            isScanning = false;
            mainVM.stopScan();
            showScanEnd();
        }
    }

    private void checkPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (!checkSPermissions()) {
                requestAllPermissions();
            } else {
                hidePermissionsLayout();
            }
        } else {
            if (!checkLocationPermission()) {
                requestPermission();
            } else {
                hidePermissionsLayout();
            }
        }
    }

    /**
     * ************************ CHECK DARK MODE *****************************
     */
    private void checkDarkMode() {
        if (AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES) {
//            Log.d(TAG, "checkDarkMode: DARK MODE");
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
//            Log.d(TAG, "checkDarkMode: LIGHT MODE");
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
    }

    /**
     * ********************** PERMISSIONS *********************
     */
    @RequiresApi(api = Build.VERSION_CODES.S)
    private boolean checkSPermissions() {
        boolean isLocation = ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
        boolean isScan = ContextCompat.checkSelfPermission(this, android.Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_GRANTED;
        boolean isConnect = ContextCompat.checkSelfPermission(this, android.Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED;
        return isLocation && isScan && isConnect;
    }

    private boolean checkLocationPermission() {
        return ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermission() {
        ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_CODE);
    }

    @RequiresApi(api = Build.VERSION_CODES.S)
    private void requestAllPermissions() {
        ActivityCompat.requestPermissions(this,
                new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION,
                        android.Manifest.permission.BLUETOOTH_SCAN,
                        android.Manifest.permission.BLUETOOTH_CONNECT}, LOCATION_PERMISSION_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_CODE) {
            if (grantResults.length != 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                hidePermissionsLayout();
            } else {
                Toast.makeText(this, "Permission Required", Toast.LENGTH_SHORT).show();
                showPermissionRequest();
            }
        }
    }

    private void showPermissionRequest() {
        binding.mainPermissionsLayout.setVisibility(View.VISIBLE);
        binding.mainScanningLayout.setVisibility(View.GONE);
        binding.mainStartScanLayout.setVisibility(View.GONE);
    }

    private void hidePermissionsLayout() {
        binding.mainPermissionsLayout.setVisibility(View.GONE);
        binding.mainScanningLayout.setVisibility(View.GONE);
        binding.mainStartScanLayout.setVisibility(View.VISIBLE);
    }

    private void showScanning() {
        binding.mainPermissionsLayout.setVisibility(View.GONE);
        binding.mainScanningLayout.setVisibility(View.VISIBLE);
        binding.mainStartScanLayout.setVisibility(View.GONE);
    }

    private void showScanEnd() {
        binding.mainPermissionsLayout.setVisibility(View.GONE);
        binding.mainScanningLayout.setVisibility(View.GONE);
        binding.mainStartScanLayout.setVisibility(View.VISIBLE);
    }

}