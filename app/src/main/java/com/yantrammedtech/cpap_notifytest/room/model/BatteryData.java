package com.yantrammedtech.cpap_notifytest.room.model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "battery")
public class BatteryData {
    @PrimaryKey(autoGenerate = true)
    private int id;
    private float flow_rate;
    private float pressure;
    private int io; // inhalation/exhalation time  (1,0)
    private int respRate; //(0-60)
    private int pMax; // (0, 250)
    private int pMin; // (-250,250)
    private int airPressure; // -250 , 250
    private int totalFlow; // +60 , -60

    public BatteryData(float flow_rate, float pressure, int io,
                       int respRate, int pMax, int pMin,
                       int airPressure, int totalFlow) {
        this.flow_rate = flow_rate;
        this.pressure = pressure;
        this.io = io;
        this.respRate = respRate;
        this.pMax = pMax;
        this.pMin = pMin;
        this.airPressure = airPressure;
        this.totalFlow = totalFlow;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public float getFlow_rate() {
        return flow_rate;
    }

    public void setFlow_rate(float flow_rate) {
        this.flow_rate = flow_rate;
    }

    public float getPressure() {
        return pressure;
    }

    public void setPressure(float pressure) {
        this.pressure = pressure;
    }

    public int getIo() {
        return io;
    }

    public void setIo(int io) {
        this.io = io;
    }

    public int getRespRate() {
        return respRate;
    }

    public void setRespRate(int respRate) {
        this.respRate = respRate;
    }

    public int getPMax() {
        return pMax;
    }

    public void setPMax(int pMax) {
        this.pMax = pMax;
    }

    public int getPMin() {
        return pMin;
    }

    public void setPMin(int pMin) {
        this.pMin = pMin;
    }

    public int getAirPressure() {
        return airPressure;
    }

    public void setAirPressure(int airPressure) {
        this.airPressure = airPressure;
    }

    public int getTotalFlow() {
        return totalFlow;
    }

    public void setTotalFlow(int totalFlow) {
        this.totalFlow = totalFlow;
    }

    @Override
    public String toString() {
        return "BatteryData{" +
                "id=" + id +
                ", flow_rate=" + flow_rate +
                ", pressure=" + pressure +
                ", io=" + io +
                ", respRate=" + respRate +
                ", pMax=" + pMax +
                ", pMin=" + pMin +
                ", airPressure=" + airPressure +
                ", totalFlow=" + totalFlow +
                '}';
    }
}
