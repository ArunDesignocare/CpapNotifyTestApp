package com.yantrammedtech.cpap_notifytest.room.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.yantrammedtech.cpap_notifytest.room.model.EepromData;

import java.util.List;

@Dao
public interface DaoEepromData {
    @Insert
    void insert(EepromData eepromData);

    @Query("SELECT * FROM eeprom_data")
    List<EepromData> getAllEepromData();

    @Query("DELETE FROM eeprom_data")
    void delete();
}
