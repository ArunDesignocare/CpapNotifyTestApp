package com.yantrammedtech.cpap_notifytest.room.dao;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.yantrammedtech.cpap_notifytest.room.model.NotifyData;

import java.util.List;

@Dao
public interface DaoNotifyData {
    @Insert
    void insert(NotifyData notifyData);

    @Query("SELECT * FROM notify_data ORDER BY characteristic DESC")
    List<NotifyData> getAllData();

    @Query("DELETE FROM notify_data")
    void delete();
}
