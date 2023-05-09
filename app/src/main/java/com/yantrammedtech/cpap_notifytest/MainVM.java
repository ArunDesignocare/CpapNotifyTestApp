package com.yantrammedtech.cpap_notifytest;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.ArrayList;
import java.util.List;

import no.nordicsemi.android.support.v18.scanner.BluetoothLeScannerCompat;
import no.nordicsemi.android.support.v18.scanner.ScanCallback;
import no.nordicsemi.android.support.v18.scanner.ScanResult;

public class MainVM extends ViewModel {
    private BluetoothAdapter bluetoothAdapter;
    private BluetoothLeScannerCompat scanner;
    private MutableLiveData<List<BluetoothDevice>> scannedDevices;

    public MainVM() {
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        scanner = BluetoothLeScannerCompat.getScanner();
        scannedDevices = new MutableLiveData<>();
    }

    public void startScan() {
        scanner.startScan(scanCallback);
    }

    public void stopScan() {
        scanner.stopScan(scanCallback);
    }

    public LiveData<List<BluetoothDevice>> getScannedDevices() {
        return scannedDevices;
    }

    private ScanCallback scanCallback = new ScanCallback() {
        @SuppressLint("MissingPermission")
        @Override
        public void onScanResult(int callbackType, @NonNull ScanResult result) {
            super.onScanResult(callbackType, result);
            if (result.getDevice().getName() != null) {
                updateScannedDevices(result.getDevice());
            }
        }

        @Override
        public void onScanFailed(int errorCode) {
            super.onScanFailed(errorCode);
        }
    };

    private void updateScannedDevices(BluetoothDevice device) {
        List<BluetoothDevice> devices = scannedDevices.getValue();
        if (devices == null) {
            devices = new ArrayList<>();
        }
        if (!devices.contains(device)) {
            devices.add(device);
            scannedDevices.setValue(devices);
        }
    }
}
