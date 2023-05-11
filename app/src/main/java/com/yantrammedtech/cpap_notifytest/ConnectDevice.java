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
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.android.material.appbar.MaterialToolbar;
import com.yantrammedtech.cpap_notifytest.Share.ExcelFileCreator2;
import com.yantrammedtech.cpap_notifytest.bluetooth.BleUUIDS;
import com.yantrammedtech.cpap_notifytest.databinding.ActivityConnectDeviceBinding;
import com.yantrammedtech.cpap_notifytest.dialogs.Dialog_Processing;
import com.yantrammedtech.cpap_notifytest.room.model.BatteryData;
import com.yantrammedtech.cpap_notifytest.room.model.EepromData;
import com.yantrammedtech.cpap_notifytest.room.model.EepromStatus;
import com.yantrammedtech.cpap_notifytest.room.model.NotifyData;
import com.yantrammedtech.cpap_notifytest.room.repo.RepoBattery;
import com.yantrammedtech.cpap_notifytest.room.repo.RepoEepromData;
import com.yantrammedtech.cpap_notifytest.room.repo.RepoEepromStatus;
import com.yantrammedtech.cpap_notifytest.room.repo.RepoNotifyData;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class ConnectDevice extends AppCompatActivity implements ExcelFileCreator.FileCreationListener {
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
    private boolean isNotifyEnabled = false;

    private BluetoothGattCharacteristic char_battery, char_eeprom_status, char_eeprom_data, char_temp,
            char_version, char_on_time, char_off_time, char_mask_detection;
    private BluetoothGattCharacteristic[] characteristicArray = new BluetoothGattCharacteristic[3];
    private int index = 0;

    // dialog
    private Dialog_Processing dialog_processing;

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
        initFlowGraph();
        initPressureGraph();
        initIOGraph();
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

        binding.connectNotifyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (connectionStatus == 1) {
                    if (isNotifyEnabled) {
                        isNotifyEnabled = false;
                        binding.connectNotifyButton.setText("Start Notify");
                        stopNotifyProcess();
                    } else {
                        isNotifyEnabled = true;
                        binding.connectNotifyButton.setText("Stop Notify");
                        startNotifyProcess();
                    }
                }
            }
        });

        binding.connectShareButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createShareFile();
            }
        });

        binding.connectDeleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                RepoNotifyData repoNotifyData = new RepoNotifyData(ConnectDevice.this);
                repoNotifyData.delete();
                showToast("Delete Success");
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
        public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            super.onDescriptorWrite(gatt, descriptor, status);
            if (status != BluetoothGatt.GATT_SUCCESS) {
//                Timber.tag(TAG).d("onDescriptorWrite: write descriptor failed");
                return;
            }
            if (descriptor.getUuid().equals(UUID.fromString(BleUUIDS.CPAP_DESCRIPTOR_UUID))) {
//                Timber.tag(TAG).d("onDescriptorWrite: SUCCESS WRITING DESCRIPTOR");
                byte[] value = descriptor.getValue();
                if (value != null) {
                    if (value[0] != 0) {
                        Log.d(TAG, "onDescriptorWrite: NOTIFY ENABLED");
//                        Timber.tag(TAG).d("onDescriptorWrite: Notify Enabled");
//                        if (start < 8) {
//                            start++;
//                            setNotifyArray();
//                        }
//                        jobStatus = BleConstants.JOB_STATUS_SUCCESS;
//                        endJob();
                    } else {
                        Log.d(TAG, "onDescriptorWrite: NOTIFY DISABLED");
//                        Timber.tag(TAG).d("onDescriptorWrite: Notify Disabled");
                    }
//                    jobStatus = BleConstants.JOB_STATUS_SUCCESS;
//                    endJob();
                    if (isNotifyEnabled) {
                        index--;
                        startNotifyProcess();
                    } else {
                        index++;
                        stopNotifyProcess();
                    }

                }
            }
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            super.onCharacteristicChanged(gatt, characteristic);
//            Timber.tag(TAG).d("onCharacteristicChanged: ");
            byte[] data = characteristic.getValue();
//            Timber.tag(TAG).d("onCharacteristicChanged: %s", Arrays.toString(data));
            if (data != null) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        storeNotifyDataFull(data, characteristic);
                    }
                }).start();
            }
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

    @SuppressLint("MissingPermission")
    private void setNotify(BluetoothGattCharacteristic gattCharacteristic, boolean enable) {
        if (gattCharacteristic != null) {
            Log.d(TAG, "setNotify: ");
            int properties = gattCharacteristic.getProperties();
            byte[] value;
//            Timber.tag(TAG).d("setNotify: %s", properties);
            if ((properties & BluetoothGattCharacteristic.PROPERTY_NOTIFY) > 0) {
                value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE;
//                Timber.tag(TAG).d("setNotify: %s", Arrays.toString(value));
            } else if ((properties & BluetoothGattCharacteristic.PROPERTY_INDICATE) > 0) {
                value = BluetoothGattDescriptor.ENABLE_INDICATION_VALUE;
//                Timber.tag(TAG).d("setNotify: %s", Arrays.toString(value));
            } else {
//                Timber.tag(TAG).e("setNotify: NOT NOTIFY OR INDICATE");
                return;
            }
            BluetoothGattDescriptor descriptor = gattCharacteristic.
                    getDescriptor(UUID.fromString(BleUUIDS.CPAP_DESCRIPTOR_UUID));
            if (descriptor == null) {
//                Timber.tag(TAG).d("setNotify: Descriptor is null");
                return;
            }
            byte[] finalValue = enable ? value : BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE;
            gattCharacteristic.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT);
            bluetoothGatt.setCharacteristicNotification(gattCharacteristic, enable);
            descriptor.setValue(finalValue);
            bluetoothGatt.writeDescriptor(descriptor);
        }
    }

    private void discoverCharacteristics(BluetoothGattService service) {
        List<BluetoothGattCharacteristic> characteristicList = service.getCharacteristics();
        for (BluetoothGattCharacteristic characteristic : characteristicList) {
            switch (characteristic.getUuid().toString()) {
                case BleUUIDS.CPAP_BATTERY:
                    char_battery = characteristic;
                    Log.d(TAG, "discoverCharacteristics: Found Battery Characteristic");
                    characteristicArray[index] = char_battery;
                    index++;
                    break;
                case BleUUIDS.CPAP_EEPROM_STATUS:
                    char_eeprom_status = characteristic;
                    Log.d(TAG, "discoverCharacteristics: Found EEPROM STATUS Characteristic");
                    characteristicArray[index] = char_eeprom_status;
                    index++;
                    break;
                case BleUUIDS.CPAP_EEPROM_DATA:
                    char_eeprom_data = characteristic;
                    Log.d(TAG, "discoverCharacteristics: Found EEPROM DATA Characteristic");
//                    characteristicArray[index] = char_eeprom_data;
//                    index++;
                    break;
                case BleUUIDS.CPAP_TEMP:
                    char_temp = characteristic;
                    Log.d(TAG, "discoverCharacteristics: Found CPAP TEMP Characteristic");
//                    characteristicArray[index] = char_temp;
//                    index++;
                    break;
                case BleUUIDS.CPAP_VERSION:
                    char_version = characteristic;
                    Log.d(TAG, "discoverCharacteristics: Found CPAP VERSION Characteristic");
//                    characteristicArray[index] = char_version;
//                    index++;
                    break;
                case BleUUIDS.CPAP_ON_TIME:
                    char_on_time = characteristic;
                    Log.d(TAG, "discoverCharacteristics: Found CPAP ON TIME Characteristic");
//                    characteristicArray[index] = char_on_time;
//                    index++;
                    break;
                case BleUUIDS.CPAP_OFF_TIME:
                    char_off_time = characteristic;
                    Log.d(TAG, "discoverCharacteristics: Found CPAP OFF TIME Characteristic");
//                    characteristicArray[index] = char_off_time;
//                    index++;
                    break;
                case BleUUIDS.CPAP_MASK_DETECTION:
                    char_mask_detection = characteristic;
                    Log.d(TAG, "discoverCharacteristics: Found MASK DETECTION Characteristic");
//                    characteristicArray[index] = char_mask_detection;
//                    index++;
                    break;
            }
        }
    }

    /**
     * *********************** START NOTIFY PROCESS **************************
     */
    private void startNotifyProcess() {
        if (index - 1 < 0) {
            Log.d(TAG, "startNotifyProcess: NOTIFY FINISHED");
        } else {
            setNotify(characteristicArray[index - 1], true);
        }
    }

    private void stopNotifyProcess() {
        if (index == 2) {
            Log.d(TAG, "stopNotifyProcess: STOP NOTIFY FINISHED");
        } else {
            setNotify(characteristicArray[index], false);
        }
    }

    /**
     * ***************** PROCESS NOTIFICATIONS ***************************
     */
//    private void storeNotifyData(byte[] value, BluetoothGattCharacteristic characteristic) {
//        String data = new String(value, StandardCharsets.US_ASCII);
//        String char_string = "";
//        String[] s = data.split(",");
//        switch (characteristic.getUuid().toString()) {
//            case BleUUIDS.CPAP_BATTERY:
//                char_string = "battery";
//                break;
//            case BleUUIDS.CPAP_EEPROM_STATUS:
//                char_string = "eeprom_status";
//                break;
//
//            case BleUUIDS.CPAP_EEPROM_DATA:
//                char_string = "eeprom_data";
//                break;
//        }
//        try {
//            NotifyData notifyData = new NotifyData(char_string, s[0], s[1], s[2], s[3], s[4], s[5]);
//            RepoNotifyData repoNotify = new RepoNotifyData(this);
//            repoNotify.insert(notifyData);
//            if (notifyData.getCharacteristic().equals("battery")) {
//                handler.post(new Runnable() {
//                    @Override
//                    public void run() {
//                        try {
//                            addEntry(Float.parseFloat(s[0]), Float.parseFloat(s[0]));
//                        } catch (Exception e) {
//                            Log.d(TAG, "run: " + e.getMessage());
//                        }
//                    }
//                });
//            }
//        } catch (Exception e) {
//            Log.d(TAG, "storeNotifyData: " + e.getMessage());
//            NotifyData notifyData = new NotifyData(char_string, "*", "*", "*", "*", "*", "*");
//            RepoNotifyData repoNotify = new RepoNotifyData(this);
//            repoNotify.insert(notifyData);
//        }
//    }
    private void storeNotifyDataFull(byte[] value, BluetoothGattCharacteristic characteristic) {
        String data = new String(value, StandardCharsets.US_ASCII);
        String[] s = data.split(",");
        switch (characteristic.getUuid().toString()) {
            case BleUUIDS.CPAP_BATTERY:
                insertBatteryData(value);
                break;
            case BleUUIDS.CPAP_EEPROM_STATUS:
                insertEepromStatusData(value);
//                Log.d(TAG, "storeNotifyDataFull: eeprom status ignore");
                break;
//            case BleUUIDS.CPAP_EEPROM_DATA:
//                insertEepromData(value);
//                break;
            default:
                Log.d(TAG, "storeNotifyDataFull: Unknown Characteristic");
                break;
        }

    }

    /**
     * *********************** DATABASE INSERT*****************************
     */
    private void insertBatteryData(byte[] value) {
        try {
            String s = new String(value, StandardCharsets.US_ASCII);
            String[] arr = s.split(",");
            BatteryData batteryData = new BatteryData(Float.parseFloat(arr[0]),
                    Float.parseFloat(arr[1]),
                    Integer.parseInt(arr[2]),
                    Integer.parseInt(arr[3]),
                    Integer.parseInt(arr[4]),
                    Integer.parseInt(arr[5]),
                    Integer.parseInt(arr[6]),
                    Integer.parseInt(arr[7]));
            RepoBattery repoBattery = new RepoBattery(ConnectDevice.this);
            repoBattery.insert(batteryData);

            handler.post(new Runnable() {
                @Override
                public void run() {
                    String flowRate = batteryData.getFlow_rate() + "";
                    binding.connecFlowRateValue.setText(flowRate);
                    String pressure = batteryData.getPressure() + "";
                    binding.connectPressureValue.setText(pressure);
                    addFlowEntry(flowX, batteryData.getFlow_rate());
                    flowX++;
                    addPressureEntry(pressureX, batteryData.getPressure());
                    pressureX++;
                    addIOEntry(ioX, batteryData.getIo());
                    ioX++;
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "insertBatteryData: " + e.getMessage());
        }


    }

    private void insertEepromStatusData(byte[] value) {
        try {
            String s = new String(value, StandardCharsets.US_ASCII);
            String[] arr = s.split(",");
            EepromStatus eepromStatus = new EepromStatus(Long.parseLong(arr[0]));
            RepoEepromStatus repoEepromStatus = new RepoEepromStatus(ConnectDevice.this);
            repoEepromStatus.insert(eepromStatus);

            handler.post(new Runnable() {
                @Override
                public void run() {
                    // TODO add entry in graph
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "insertEepromStatusData: " + e.getMessage());
        }
    }

    private void insertEepromData(byte[] value) {
        try {
            String s = new String(value, StandardCharsets.US_ASCII);
            String[] arr = s.split(",");
            EepromData eepromData = new EepromData(Integer.parseInt(arr[0]), Integer.parseInt(arr[1]));
            RepoEepromData repoEepromData = new RepoEepromData(ConnectDevice.this);
            repoEepromData.insert(eepromData);
            handler.post(new Runnable() {
                @Override
                public void run() {
                    // TODO add entry in graph
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "insertEepromData: " + e.getMessage());
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
            binding.connectNotifyButton.setVisibility(View.GONE);
        } else if (connectionStatus == 1) {
            binding.connectConnectStatus.setText("CONNECTED");
            binding.connectConnectStatus.setBackground(ContextCompat.getDrawable(this, R.drawable.text_accepted));
            binding.connectReconnectButton.setVisibility(View.GONE);
            binding.connectNotifyButton.setVisibility(View.VISIBLE);
        } else if (connectionStatus == -1) {
            binding.connectConnectStatus.setText("DISCONNECTED");
            binding.connectConnectStatus.setBackground(ContextCompat.getDrawable(this, R.drawable.text_rejected));
            binding.connectReconnectButton.setVisibility(View.VISIBLE);
            binding.connectNotifyButton.setVisibility(View.GONE);
        }
    }

    /**
     * ***************** SHOW TOAST ********************************
     */
    private void showToast(String message) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(ConnectDevice.this, message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * *************** LIFECYCLE ***********************************
     */
    @Override
    protected void onStop() {
        super.onStop();
        if (isNotifyEnabled) {
            stopNotifyProcess();
        }
        if (connectionStatus > 0) {
            disconnect();
        }
    }

    /**
     * **************** CREATE SHARE FILE *************************
     */
    private void createShareFile() {
        showProcessingDialog();
        String name = String.valueOf(System.currentTimeMillis());
        ExcelFileCreator2 excelFileCreator = new ExcelFileCreator2(this, name, this);
        new Thread(new Runnable() {
            @Override
            public void run() {
                excelFileCreator.createExcelFile();
            }
        }).start();
    }

    @Override
    public void onFileCreated(boolean result, String fileName) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                hideProcessingDialog();
                if (result) {
                    shareDocument(fileName);
                } else {
                    Log.d(TAG, "run: FILE CREATION FAILED");
                    showToast("Failed to Share file");
                }
            }
        });
    }

    private void shareDocument(String fileName) {
        File file = new File(this.getExternalFilesDir(Environment.DIRECTORY_DCIM), fileName);
        Uri path = FileProvider.getUriForFile(this, "com.yantrammedtech.cpap_notifytest.fileprovider", file);
        Log.d(TAG, "shareDocument: " + path.getPath());
        Intent i = new Intent(Intent.ACTION_SEND);
        i.putExtra(Intent.EXTRA_STREAM, path);
//        i.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        i.setType("application/x-excel");
        startActivity(i);
    }

    /**
     * *************** PROCESSING DIALOG **************************
     */
    private void showProcessingDialog() {
        dialog_processing = new Dialog_Processing(this);
        dialog_processing.createDialog();
    }

    private void hideProcessingDialog() {
        if (dialog_processing != null) {
            dialog_processing.dismissDialog();
        }
    }

    /**
     * ****************************** GRAPH ******************************************
     */
    private LineData lineData;
    private List<Entry> flowEntries;
    private List<Entry> pressureEntries;
    private List<Entry> ioEntries;
    private int flowX;
    private int pressureX;
    private int ioX;

    /**
     * ************************* FLOW GRAPH ****************************************************
     */
    private void initFlowGraph() {
        Description description = new Description();
        description.setText("Flow Rate vs Time");
        binding.connectGraphFlowRate.setDescription(description);
        binding.connectGraphFlowRate.setNoDataText("No Data Available to Plot");
        binding.connectGraphFlowRate.setDrawGridBackground(false);
        binding.connectGraphFlowRate.setDragEnabled(true);
        binding.connectGraphFlowRate.setScaleEnabled(true);
        binding.connectGraphFlowRate.setPinchZoom(true);
        binding.connectGraphFlowRate.setBackgroundColor(Color.WHITE);

        Legend legend = binding.connectGraphFlowRate.getLegend();
        legend.setEnabled(false);

        XAxis xAxis = binding.connectGraphFlowRate.getXAxis();
        xAxis.setEnabled(true);
        xAxis.setDrawGridLines(false);

        YAxis leftAxis = binding.connectGraphFlowRate.getAxisLeft();

        YAxis rightAxis = binding.connectGraphFlowRate.getAxisRight();
        rightAxis.setEnabled(false);

        LineData lineData = new LineData();
        binding.connectGraphFlowRate.setData(lineData);

        flowEntries = new ArrayList<>();
    }

    private void addFlowEntry(float x, float y) {
        LineData data = binding.connectGraphFlowRate.getData();
        if (data != null) {
            ILineDataSet set = data.getDataSetByIndex(0);
            if (set == null) {
                set = createSet();
                data.addDataSet(set);
            }

            data.addEntry(new Entry(x, y), 0);
            data.notifyDataChanged();

            binding.connectGraphFlowRate.notifyDataSetChanged();

            binding.connectGraphFlowRate.setVisibleXRangeMaximum(120);

            binding.connectGraphFlowRate.moveViewToX(data.getEntryCount());
        }
    }

    private LineDataSet createSet() {
        LineDataSet set = new LineDataSet(null, "");
        set.setAxisDependency(YAxis.AxisDependency.LEFT);
        set.setColor(ColorTemplate.getHoloBlue());
        set.setCircleColor(getResources().getColor(R.color.accepted, null));
        set.setLineWidth(1f);
        set.setCircleRadius(1f);
        set.setCircleHoleColor(getResources().getColor(R.color.accepted, null));
        set.setFillAlpha(65);
        set.setFillColor(ColorTemplate.getHoloBlue());
        set.setHighLightColor(Color.rgb(244, 117, 117));
        set.setValueTextColor(Color.WHITE);
        set.setValueTextSize(9f);
        set.setDrawValues(false);
        return set;
    }

    /**
     * *********************** PRESSURE GRAPH *****************************************************
     */
    private void initPressureGraph() {
        Description description = new Description();
        description.setText("Pressure vs Time");
        binding.connectGraphPressure.setDescription(description);
        binding.connectGraphPressure.setNoDataText("No Data Available to Plot");
        binding.connectGraphPressure.setDrawGridBackground(false);
        binding.connectGraphPressure.setDragEnabled(true);
        binding.connectGraphPressure.setScaleEnabled(true);
        binding.connectGraphPressure.setPinchZoom(true);
        binding.connectGraphPressure.setBackgroundColor(Color.WHITE);

        Legend legend = binding.connectGraphPressure.getLegend();
        legend.setEnabled(false);

        XAxis xAxis = binding.connectGraphPressure.getXAxis();
        xAxis.setEnabled(true);
        xAxis.setDrawGridLines(false);

        YAxis leftAxis = binding.connectGraphPressure.getAxisLeft();

        YAxis rightAxis = binding.connectGraphPressure.getAxisRight();
        rightAxis.setEnabled(false);

        LineData lineData = new LineData();
        binding.connectGraphPressure.setData(lineData);

        pressureEntries = new ArrayList<>();
    }

    private void addPressureEntry(float x, float y) {
        LineData data = binding.connectGraphPressure.getData();
        if (data != null) {
            ILineDataSet set = data.getDataSetByIndex(0);
            if (set == null) {
                set = createSet();
                data.addDataSet(set);
            }

            data.addEntry(new Entry(x, y), 0);
            data.notifyDataChanged();

            binding.connectGraphPressure.notifyDataSetChanged();

            binding.connectGraphPressure.setVisibleXRangeMaximum(120);

            binding.connectGraphPressure.moveViewToX(data.getEntryCount());
        }
    }

    /**
     * *********************** INHALATION AND EXHALATION GRAPH *****************************************************
     */
    private void initIOGraph() {
        Description description = new Description();
        description.setText("Inhalation/Exhalation vs Time");
        binding.connectGraphIo.setDescription(description);
        binding.connectGraphIo.setNoDataText("No Data Available to Plot");
        binding.connectGraphIo.setDrawGridBackground(false);
        binding.connectGraphIo.setDragEnabled(true);
        binding.connectGraphIo.setScaleEnabled(true);
        binding.connectGraphIo.setPinchZoom(true);
        binding.connectGraphPressure.setBackgroundColor(Color.WHITE);

        Legend legend = binding.connectGraphIo.getLegend();
        legend.setEnabled(false);

        XAxis xAxis = binding.connectGraphIo.getXAxis();
        xAxis.setEnabled(true);
        xAxis.setDrawGridLines(false);

        YAxis leftAxis = binding.connectGraphIo.getAxisLeft();

        YAxis rightAxis = binding.connectGraphIo.getAxisRight();
        rightAxis.setEnabled(false);

        LineData lineData = new LineData();
        binding.connectGraphIo.setData(lineData);

        ioEntries = new ArrayList<>();
    }

    private void addIOEntry(float x, float y) {
        LineData data = binding.connectGraphIo.getData();
        if (data != null) {
            ILineDataSet set = data.getDataSetByIndex(0);
            if (set == null) {
                set = createSet();
                data.addDataSet(set);
            }

            data.addEntry(new Entry(x, y), 0);
            data.notifyDataChanged();

            binding.connectGraphIo.notifyDataSetChanged();

            binding.connectGraphIo.setVisibleXRangeMaximum(120);

            binding.connectGraphIo.moveViewToX(data.getEntryCount());
        }
    }

}