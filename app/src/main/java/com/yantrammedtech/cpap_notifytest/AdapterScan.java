package com.yantrammedtech.cpap_notifytest;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class AdapterScan extends RecyclerView.Adapter<AdapterScan.ScanViewModel> {
    private Context context;
    private List<BluetoothDevice> bluetoothDeviceList;
    private BluetoothDeviceListener listener;

    public interface BluetoothDeviceListener {
        void onDeviceSelected(BluetoothDevice bluetoothDevice);
    }

    public AdapterScan(Context context, List<BluetoothDevice> bluetoothDeviceList, BluetoothDeviceListener listener) {
        this.context = context;
        this.bluetoothDeviceList = bluetoothDeviceList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ScanViewModel onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.recycler_scan_items, parent, false);
        return new ScanViewModel(view);
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onBindViewHolder(@NonNull ScanViewModel holder, int position) {
        BluetoothDevice bluetoothDevice = bluetoothDeviceList.get(position);
        holder.deviceName.setText(bluetoothDevice.getName());
        holder.deviceAddress.setText(bluetoothDevice.getAddress());
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listener.onDeviceSelected(bluetoothDevice);
            }
        });
    }

    @Override
    public int getItemCount() {
        return bluetoothDeviceList.size();
    }

    class ScanViewModel extends RecyclerView.ViewHolder {
        private TextView deviceName, deviceAddress;

        public ScanViewModel(@NonNull View itemView) {
            super(itemView);
            deviceName = itemView.findViewById(R.id.recScan_deviceName);
            deviceAddress = itemView.findViewById(R.id.recScan_deviceAddress);
        }
    }
}
