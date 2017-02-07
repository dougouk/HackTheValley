package me.mathusan.parkthevalley;

import android.os.Parcel;

import java.io.Serializable;

/**
 * Created by Mathu on 2017-01-07.
 */

public class Spot implements Serializable {


    double lat;
    double lng;
    boolean open;
    long time;
    double price;



    public Spot() {
      /*Blank default constructor essential for Firebase*/
    }

    protected Spot(Parcel in) {
        lat = in.readDouble();
        lng = in.readDouble();
        open = in.readByte() != 0;
        time = in.readLong();
    }

//    @Override
//    public void writeToParcel(Parcel dest, int flags) {
//        dest.writeDouble(lat);
//        dest.writeDouble(lng);
//        dest.writeByte((byte) (open ? 1 : 0));
//        dest.writeLong(time);
//    }

//    public static final Creator<Spot> CREATOR = new Creator<Spot>() {
//        @Override
//        public Spot createFromParcel(Parcel in) {
//            return new Spot(in);
//        }
//
//        @Override
//        public Spot[] newArray(int size) {
//            return new Spot[size];
//        }
//    };

    //Getters and setters
    public boolean getOpen() {
        return open;
    }

    public void setOpen(boolean open) {
        this.open = open;
    }

    public double getLat() {
        return lat;
    }

    public double getLng() {
        return lng;
    }

    public long getTime() {
        return time;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public void setLng(double lng) {
        this.lng = lng;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }
}

//    @Override
//    public int describeContents() {
//        return 0;
//    }
//
//
//    }