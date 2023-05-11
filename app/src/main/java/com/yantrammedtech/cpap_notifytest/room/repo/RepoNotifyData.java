package com.yantrammedtech.cpap_notifytest.room.repo;

import android.content.Context;
import android.util.Log;

import com.yantrammedtech.cpap_notifytest.room.dao.DaoNotifyData;
import com.yantrammedtech.cpap_notifytest.room.db.CpapDB;
import com.yantrammedtech.cpap_notifytest.room.model.NotifyData;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class RepoNotifyData {
    private DaoNotifyData daoNotfiyData;

    public RepoNotifyData(Context context) {
        CpapDB cpapDB = CpapDB.getInstance(context);
        daoNotfiyData = cpapDB.daoNotifyData();
    }

    public void insert(NotifyData notifyData) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                daoNotfiyData.insert(notifyData);
            }
        }).start();
    }

    // get all static data
    public List<NotifyData> getStaticData() throws ExecutionException, InterruptedException {
        Callable<List<NotifyData>> callable = new Callable<List<NotifyData>>() {
            @Override
            public List<NotifyData> call() throws Exception {
                return daoNotfiyData.getAllData();
            }
        };
        Future<List<NotifyData>> future = Executors.newSingleThreadExecutor().submit(callable);
        return future.get();
    }

    public void delete() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                daoNotfiyData.delete();
            }
        }).start();
    }
}
