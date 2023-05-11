package com.yantrammedtech.cpap_notifytest.room.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.yantrammedtech.cpap_notifytest.room.model.EepromStatus;

import java.util.List;

@Dao
public interface DaoEepromStatus {
    @Insert
    void insert(EepromStatus eepromStatus);

    @Query("SELECT * FROM eeprom_status")
    List<EepromStatus> getAllEepromStatusData();

    @Query("DELETE FROM eeprom_status")
    void delete();
}
