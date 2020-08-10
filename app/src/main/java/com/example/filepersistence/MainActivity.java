package com.example.filepersistence;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.opencsv.CSVReader;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class MainActivity extends AppCompatActivity {

    private List<Position> map;
    private int weight;
    private ArrayList<String> sequence;
    final String start = "L4";
    private EditText edit;
    private Button planBtn;
    private TextView retText;
    private static final String TAG="MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setLayout();

        findView();

        setView();

        setListener();
    }
    private void setLayout(){
        setContentView(R.layout.activity_main);
    }

    private void findView(){
        edit = (EditText) findViewById(R.id.edit);
        planBtn = (Button) findViewById(R.id.plan_btn);
        retText = (TextView) findViewById(R.id.text_view);
    }

    private void setView(){
        loadMap();
    }

    private void setListener(){
        planBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String editVal = edit.getText().toString();
                String[] store_name = editVal.split(",");
                List<String> stores = Arrays.asList(store_name);
                ArrayList<Position> positions = mapToPos(stores);
                // test
                StringBuilder result = new StringBuilder();
                for(Position pos: positions){
                    result.append(pos.getName());
                    result.append(":");
                    result.append(pos.getStores());
                    result.append(", ");
                }
                retText.setText(result.toString());

                // plan the route
                positions = routePlan(positions);

                // test
                for(Position pos: positions){
                    Log.d(TAG, "onClick: "+ pos.getName());
                    Log.d(TAG, "onClick: "+ pos.getStores());
                    Log.d(TAG, "onClick: "+ pos.getLoc());
                }

                // intent
                Intent intent = new Intent(MainActivity.this, SecondActivity.class);
                intent.putParcelableArrayListExtra("route",positions);
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        String inputText = edit.getText().toString();
        save(inputText);
    }

    public void save(String inputText){
        FileOutputStream out = null;
        BufferedWriter writer = null;
        try{
            out = openFileOutput("data", Context.MODE_PRIVATE);
            writer = new BufferedWriter(new OutputStreamWriter(out));
            writer.write(inputText);
        }catch (IOException e){
            e.printStackTrace();
        }finally {
            try{
                if(writer != null) {
                    writer.close();
                }
                }catch(IOException e){
                    e.printStackTrace();
            }
        }
    }

    private void loadMap(){
        String Tag = "Load";
        if(map == null) {
            map = new ArrayList<Position>();
        }
        try {
            CSVReader reader = new CSVReader(new InputStreamReader(
                    this.getAssets().open("output.csv")
            ));
            String[] next;
            while ((next = reader.readNext()) != null) {
                Log.d(Tag, next[0]);
                Log.d(Tag, next[1]);
                Log.d(Tag, next[2]);
                Log.d(Tag, next[3]);
                Log.d(Tag, next[4]);
                map.add(new Position(next[0], next[1], next[2], next[3], next[4]));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        Tag = "Pos";
        for(Position node: map){
            Log.d(Tag, node.getName());
            Log.d(Tag, node.getAdjacency());
            Log.d(Tag, node.getLoc());
        }
    }

    private ArrayList mapToPos(List<String> stores){
        ArrayList<Position> positions = new ArrayList<Position>();
        for(String store:stores){
            Position found = toPos(store);
            if(found!=null){
                boolean add = false;
                for(Position pos: positions){
                    if(found.name.equals(pos.name)){
                        pos.stores.add(store);
                        add = true;
                        break;
                    }
                }
                if(!add){
                    positions.add(new Position(found.name, store, found.zone, found.floor, found.lift));
                }
            }
        }
        return positions;
    }

    private Position toPos(String store){
        if(map != null){
            for(Position node : map){
                if(node.stores.contains(store)){
                    return node;
                }
            }
            Log.d(TAG, "toPos: position not found ");
            return null;
        }
        Log.d(TAG, "toPos: map is null");
        return null;
    }

    private ArrayList routePlan(ArrayList<Position> positions){
        int num = map.size();
        int inf = 10000;
        weight = inf;
        int[][] edge = new int[num][num];
        String[][] pred = new String[num][num]; // Store predecessor's name
        // Floyd Warshall Algorithm
        for(int i=0; i<num; i++){
            for(int j=0; j<num; j++){
                pred[i][j] = null;
                if(i==j) edge[i][j] = 0;
                else edge[i][j] = inf;
            }
            // convert adjacency list to adjacency matrix
            for(Map.Entry<String, Integer> adj:map.get(i).adjacency.entrySet()){
                try {
                    int j = Integer.parseInt(adj.getKey().substring(1)) - 1;
                    edge[i][j] = adj.getValue();
                    pred[i][j] = map.get(i).name;
                } catch (NumberFormatException e) {
                    Log.e(TAG, "routePlan: ", e);
                }
            }
        }
        for(int k=0; k<num; k++){
            for(int i=0; i<num; i++){
                for(int j=0; j<num; j++){
                    if(edge[i][j] > edge[i][k] + edge[k][j]){
                        edge[i][j] = edge[i][k] + edge[k][j];
                        pred[i][j] = pred[k][j];
                    }
                }
            }
        }

//        // Test Floyd Warshall Algorithm
//        for(int i=0; i<num; i++){
//            String out = "";
//            for(int j=0; j<num; j++){
//                out += String.valueOf(edge[i][j]);
//                out += ", ";
//            }
//            Log.d(TAG, "routePlan: "+ out);
//        }
//        for(int i=0; i<num; i++){
//            String out = "";
//            for(int j=0; j<num; j++){
//                out += pred[i][j];
//                out += ", ";
//            }
//            Log.d(TAG, "routePlan: "+ out);
//        }

        // Permutation: Heap's algorithm
        ArrayList<String> pos_name = new ArrayList<String>();
        for(Position pos: positions) { pos_name.add(pos.name); }
        pos_name.remove(start); // if the start pos in the sequence, remove it when do permutations
        getPermutation(pos_name.size(), pos_name, edge);

        // test
        Log.d(TAG, "routePlan: weig" + String.valueOf(weight));
        String out = "";
        for(String ele: sequence){ out += ele; }
        Log.d(TAG, "routePlan: " + out);

        positions = findRoute(positions, pred);

        out = "";
        for(Position pos: positions){ out += pos.name; }
        Log.d(TAG, "routePlan: " + out);

        return positions;
    }

    private void getPermutation(int n, ArrayList arr, int[][] edge){
        if(n==1){
            // get one permutation
            getShortest(arr, edge);

        }else{
            getPermutation(n-1, arr, edge);
            for(int i=0; i<n-1; i++){
                if(n%2 == 0){
                    Collections.swap(arr, i, n-1);
                }else{
                    Collections.swap(arr, 0, n-1);
                }
                getPermutation(n-1, arr, edge);
            }
        }

    }

    private void getShortest(ArrayList<String> arr, int[][] edge){
        ArrayList<Integer> locs= new ArrayList<Integer>();

        locs.add(Integer.parseInt(start.substring(1))-1);
        for(String ele: arr){
            int loc = Integer.parseInt(ele.substring(1)) -1;
            locs.add(loc);
        }
        int temp_weight = 0;
        for(int i=0; i<locs.size()-1; i++){
            temp_weight += edge[locs.get(i)][locs.get(i+1)];
        }
        if(temp_weight < weight){
            weight = temp_weight;
            sequence = (ArrayList)arr.clone();
            sequence.add(0, start);
        }
    }

    private ArrayList<Position> findRoute(List<Position> positions, String[][] pred){
        ArrayList<Position> route = new ArrayList<Position>();
        for(String ele: sequence){
            boolean added = false;
            for(Position pos: positions){
                if(ele.equals(pos.name)){
                    route.add(pos);
                    added = true;
                    break;
                }
            }
            if(!added){
                for(Position node: map){
                    if(start.equals(node.name)){
                        Position pass_node = new Position(node.name, null, node.zone, node.floor, node.lift);
                        route.add(0, pass_node);
                        break;
                    }
                }
            }
        }
        int index = route.size() -1;
        while(index>0){
            int st = Integer.parseInt(route.get(index-1).name.substring(1)) - 1;
            int end = Integer.parseInt(route.get(index).name.substring(1)) - 1;
            String pass = pred[st][end];
            if(!pass.equals(route.get(index-1).name)){
                for(Position node: map){
                    if(pass.equals(node.name)){
                        Position pass_node = new Position(node.name, null, node.zone, node.floor, node.lift);
                        route.add(index, pass_node);
                        break;
                    }
                }
            }else{
                index--;
            }
        }
        return route;
    }
}