package com.yantrammedtech.cpap_notifytest.room.db;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.yantrammedtech.cpap_notifytest.room.dao.DaoBattery;
import com.yantrammedtech.cpap_notifytest.room.dao.DaoEepromData;
import com.yantrammedtech.cpap_notifytest.room.dao.DaoEepromStatus;
import com.yantrammedtech.cpap_notifytest.room.dao.DaoNotifyData;
import com.yantrammedtech.cpap_notifytest.room.model.BatteryData;
import com.yantrammedtech.cpap_notifytest.room.model.EepromData;
import com.yantrammedtech.cpap_notifytest.room.model.EepromStatus;
import com.yantrammedtech.cpap_notifytest.room.model.NotifyData;

@Database(entities = {NotifyData.class, BatteryData.class,
        EepromData.class, EepromStatus.class}, version = 1)
public abstract class CpapDB extends RoomDatabase {
    private static CpapDB instance;

    public abstract DaoNotifyData daoNotifyData();

    public abstract DaoBattery daoBattery();

    public abstract DaoEepromData daoEepromData();

    public abstract DaoEepromStatus daoEepromStatus();

    public static synchronized CpapDB getInstance(Context context) {
        if (instance == null) {
            instance = Room.databaseBuilder(context.getApplicationContext(),
                            CpapDB.class, "cpap")
                    .fallbackToDestructiveMigration()
                    .build();
        }
        return instance;
    }
}
