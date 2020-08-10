package com.example.filepersistence;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import java.util.ArrayList;


public class SecondActivity extends AppCompatActivity {
    private static final String TAG="SecActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_second);
        Intent intent = getIntent();
        ArrayList<Position> route = intent.getParcelableArrayListExtra("route");
        // test
        for(Position pos: route){
            Log.d(TAG, "onCreate: "+ pos.getName());
            Log.d(TAG, "onCreate: "+ pos.getStores());
            Log.d(TAG, "onCreate: "+ pos.getLoc());
        }
    }
}