package com.yantrammedtech.cpap_notifytest.room.model;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "notify_data")
public class NotifyData implements Parcelable {
    @PrimaryKey(autoGenerate = true)
    private int id;
    private String characteristic;
    private String x;
    private String y;
    private String x1;
    private String y1;
    private String x2;
    private String y2;


    public NotifyData(String characteristic, String x, String y, String x1, String y1, String x2, String y2) {
        this.characteristic = characteristic;
        this.x = x;
        this.y = y;
        this.x1 = x1;
        this.y1 = y1;
        this.x2 = x2;
        this.y2 = y2;
    }

    protected NotifyData(Parcel in) {
        id = in.readInt();
        characteristic = in.readString();
        x = in.readString();
        y = in.readString();
        x1 = in.readString();
        y1 = in.readString();
        x2 = in.readString();
        y2 = in.readString();
    }

    public static final Creator<NotifyData> CREATOR = new Creator<NotifyData>() {
        @Override
        public NotifyData createFromParcel(Parcel in) {
            return new NotifyData(in);
        }

        @Override
        public NotifyData[] newArray(int size) {
            return new NotifyData[size];
        }
    };

    public String getCharacteristic() {
        return characteristic;
    }

    public void setCharacteristic(String characteristic) {
        this.characteristic = characteristic;
    }

    public String getX() {
        return x;
    }

    public void setX(String x) {
        this.x = x;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public String getY() {
        return y;
    }

    public void setY(String y) {
        this.y = y;
    }

    public String getX1() {
        return x1;
    }

    public void setX1(String x1) {
        this.x1 = x1;
    }

    public String getY1() {
        return y1;
    }

    public void setY1(String y1) {
        this.y1 = y1;
    }

    public String getX2() {
        return x2;
    }

    public void setX2(String x2) {
        this.x2 = x2;
    }

    public String getY2() {
        return y2;
    }

    public void setY2(String y2) {
        this.y2 = y2;
    }

    @Override
    public String toString() {
        return "NotifyData{" +
                "id=" + id +
                ", characteristic='" + characteristic + '\'' +
                ", x='" + x + '\'' +
                ", y='" + y + '\'' +
                ", x1='" + x1 + '\'' +
                ", y1='" + y1 + '\'' +
                ", x2='" + x2 + '\'' +
                ", y2='" + y2 + '\'' +
                '}';
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel parcel, int i) {
        parcel.writeInt(id);
        parcel.writeString(characteristic);
        parcel.writeString(x);
        parcel.writeString(y);
        parcel.writeString(x1);
        parcel.writeString(y1);
        parcel.writeString(x2);
        parcel.writeString(y2);
    }


}
