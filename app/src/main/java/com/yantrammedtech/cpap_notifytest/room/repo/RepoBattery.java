package com.yantrammedtech.cpap_notifytest.room.repo;

import android.content.Context;

import com.yantrammedtech.cpap_notifytest.room.dao.DaoBattery;
import com.yantrammedtech.cpap_notifytest.room.db.CpapDB;
import com.yantrammedtech.cpap_notifytest.room.model.BatteryData;
import com.yantrammedtech.cpap_notifytest.room.model.NotifyData;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class RepoBattery {
    private DaoBattery daoBattery;

    public RepoBattery(Context context) {
        CpapDB cpapDB = CpapDB.getInstance(context);
        daoBattery = cpapDB.daoBattery();
    }

    public void insert(BatteryData batteryData) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                daoBattery.insert(batteryData);
            }
        }).start();
    }

    // get all static data
    public List<BatteryData> getStaticData() throws ExecutionException, InterruptedException {
        Callable<List<BatteryData>> callable = new Callable<List<BatteryData>>() {
            @Override
            public List<BatteryData> call() throws Exception {
                return daoBattery.getAllBatteryData();
            }
        };
        Future<List<BatteryData>> future = Executors.newSingleThreadExecutor().submit(callable);
        return future.get();
    }

    public void delete() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                daoBattery.delete();
            }
        }).start();
    }
}
