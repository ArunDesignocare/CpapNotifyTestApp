package com.yantrammedtech.cpap_notifytest.room.repo;

import android.content.Context;

import com.yantrammedtech.cpap_notifytest.room.dao.DaoEepromData;
import com.yantrammedtech.cpap_notifytest.room.db.CpapDB;
import com.yantrammedtech.cpap_notifytest.room.model.EepromData;
import com.yantrammedtech.cpap_notifytest.room.model.NotifyData;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class RepoEepromData {
    private DaoEepromData daoEepromData;

    public RepoEepromData(Context context) {
        CpapDB cpapDB = CpapDB.getInstance(context);
        daoEepromData = cpapDB.daoEepromData();
    }

    public void insert(EepromData eepromData) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                daoEepromData.insert(eepromData);
            }
        }).start();
    }

    // get all static data
    public List<EepromData> getStaticData() throws ExecutionException, InterruptedException {
        Callable<List<EepromData>> callable = new Callable<List<EepromData>>() {
            @Override
            public List<EepromData> call() throws Exception {
                return daoEepromData.getAllEepromData();
            }
        };
        Future<List<EepromData>> future = Executors.newSingleThreadExecutor().submit(callable);
        return future.get();
    }

    public void delete() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                daoEepromData.delete();
            }
        }).start();
    }

}
