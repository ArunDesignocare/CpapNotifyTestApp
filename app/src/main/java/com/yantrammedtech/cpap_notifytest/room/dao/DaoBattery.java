package com.yantrammedtech.cpap_notifytest.room.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.yantrammedtech.cpap_notifytest.room.model.BatteryData;

import java.util.List;

@Dao
public interface DaoBattery {
    @Insert
    void insert(BatteryData batteryData);

    @Query("SELECT * FROM battery")
    List<BatteryData> getAllBatteryData();

    @Query("DELETE FROM battery")
    void delete();
}
