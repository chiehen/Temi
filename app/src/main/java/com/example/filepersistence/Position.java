package com.example.filepersistence;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Position implements Parcelable {
    String name;
    HashMap<String, Integer> adjacency = new HashMap<String, Integer>();  // key is position name; value is the edge's weight
    List<String> stores = new ArrayList<String>();;
    String zone;
    String floor;
    boolean lift;
    int num;
    Position(String name, String store, String adjacency,String loc, String lift) {
        this.name = name;
        String[] adjs = adjacency.split("\\.");
        for(String adj:adjs) {
            String[] pair = adj.split(":");
            try {
                this.adjacency.put(pair[0], Integer.valueOf(pair[1]));
            } catch (NumberFormatException e) {
                Log.e("PosTAG", "NumberFormatException");
            }
        }
        String[] stores = store.split("\\.");
        for(String st:stores){

            this.stores.add(st);
        }
        String[] location = loc.split(":");
        this.zone = location[0];
        this.floor = location[1];
        if(lift.equals(1)){
            this.lift = true;
        }else {
            this.lift = false;
        }

    }

    Position(String name, String store, String zone, String floor, boolean lift){
        this.name = name;
        if(store!=null){ this.stores.add(store); }
        this.zone = zone;
        this.floor = floor;
        this.lift = lift;
    }

    public  String getName(){
        return  this.name;
    }

    public String getLoc(){
        return this.zone + ": " + this.floor;
    }

    public String getStores(){
        String rtval = "";
        for(String st:this.stores){
            rtval += (st + ",");
        }
        return rtval;
    }
    public String getAdjacency(){
        String rtval = "";
        for (String pos : this.adjacency.keySet()) {
            rtval += (pos + ",");
        }
        return rtval;
    }

    // implement Parcelable
    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel out, int i) {
        // The sequence gotta correspond to the Constructor which parameter is parcel
        out.writeString(name);
        out.writeString(zone);
        out.writeString(floor);
        out.writeStringList(stores);
        out.writeByte((byte) (lift ? 1 : 0));
    }

    public static final Parcelable.Creator<Position> CREATOR = new Creator<Position>()
    {
        @Override
        public Position[] newArray(int size)
        {
            return new Position[size];
        }
        @Override
        public Position createFromParcel(Parcel in)
        {
            return new Position(in);
        }
    };
    public Position(Parcel in)
    {
        name = in.readString();
        zone = in.readString();
        floor = in.readString();
        stores = in.createStringArrayList();
        lift = in.readByte() != 0;
    }
}
