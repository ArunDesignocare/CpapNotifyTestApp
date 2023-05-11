package com.yantrammedtech.cpap_notifytest.room.model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "eeprom_data")
public class EepromData {
    @PrimaryKey(autoGenerate = true)
    private int id;
    private long timeStamp;

    public EepromData(int id, long timeStamp) {
        this.id = id;
        this.timeStamp = timeStamp;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public long getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(long timeStamp) {
        this.timeStamp = timeStamp;
    }
}
