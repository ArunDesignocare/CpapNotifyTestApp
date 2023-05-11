package com.yantrammedtech.cpap_notifytest.room.repo;

import android.content.Context;

import com.yantrammedtech.cpap_notifytest.room.dao.DaoBattery;
import com.yantrammedtech.cpap_notifytest.room.dao.DaoEepromStatus;
import com.yantrammedtech.cpap_notifytest.room.db.CpapDB;
import com.yantrammedtech.cpap_notifytest.room.model.BatteryData;
import com.yantrammedtech.cpap_notifytest.room.model.EepromStatus;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class RepoEepromStatus {
    private DaoEepromStatus daoEepromStatus;

    public RepoEepromStatus(Context context) {
        CpapDB cpapDB = CpapDB.getInstance(context);
        daoEepromStatus = cpapDB.daoEepromStatus();
    }

    public void insert(EepromStatus eepromStatus) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                daoEepromStatus.insert(eepromStatus);
            }
        }).start();
    }

    // get all static data
    public List<EepromStatus> getStaticData() throws ExecutionException, InterruptedException {
        Callable<List<EepromStatus>> callable = new Callable<List<EepromStatus>>() {
            @Override
            public List<EepromStatus> call() throws Exception {
                return daoEepromStatus.getAllEepromStatusData();
            }
        };
        Future<List<EepromStatus>> future = Executors.newSingleThreadExecutor().submit(callable);
        return future.get();
    }

    public void delete() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                daoEepromStatus.delete();
            }
        }).start();
    }
}
