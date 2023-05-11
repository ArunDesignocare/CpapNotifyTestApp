package com.yantrammedtech.cpap_notifytest.room.model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "eeprom_status")
public class EepromStatus {
    @PrimaryKey(autoGenerate = true)
    private int id;
    private long timeStamp;

    public EepromStatus(long timeStamp) {
        this.timeStamp = timeStamp;
    }

    public long getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(long timeStamp) {
        this.timeStamp = timeStamp;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return "EepromStatus{" +
                "id=" + id +
                ", timeStamp=" + timeStamp +
                '}';
    }
}
