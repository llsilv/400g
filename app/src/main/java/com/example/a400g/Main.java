package com.example.a400g;


import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.Set;

public class Main extends AppCompatActivity {
    private BluetoothAdapter BA;
    private Set<BluetoothDevice> pairedDevices;
    ListView lv;
    int x_peak_data;
    int y_peak_data;
    int z_peak_data;
    int x_current_data;
    int y_current_data;
    int z_current_data;
    TextView x_current;
    TextView x_peak;
    TextView y_current;
    TextView y_peak;
    TextView z_current;
    TextView z_peak;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        x_current = findViewById(R.id.x_current);
        x_peak = findViewById(R.id.x_peak);
        y_current = findViewById(R.id.y_current);
        y_peak = findViewById(R.id.y_peak);
        z_current = findViewById(R.id.z_current);
        z_peak = findViewById(R.id.z_peak);
        peak(4,6,7);
    }
    public void peak(int x, int y, int z){
        if (x > x_peak_data) x_peak_data = x;
        if (y > y_peak_data) y_peak_data = y;
        if (z > z_peak_data) z_peak_data = z;

        x_current.setText(Integer.toString(x_current_data));
        x_peak.setText(Integer.toString(x_peak_data));
        y_current.setText(Integer.toString(y_current_data));
        y_peak.setText(Integer.toString(y_peak_data));
        z_current.setText(Integer.toString(z_current_data));
        z_peak.setText(Integer.toString(z_peak_data));
    }

    public void on(View v){
        if (!BA.isEnabled()) {
            Intent turnOn = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(turnOn, 0);
            Toast.makeText(getApplicationContext(), "Turned on", Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(getApplicationContext(), "Already on", Toast.LENGTH_LONG).show();
        }
    }

    public void off(View v){
        BA.disable();
        Toast.makeText(getApplicationContext(), "Turned off" ,Toast.LENGTH_LONG).show();
    }


    public  void visible(View v){
        Intent getVisible = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        startActivityForResult(getVisible, 0);
    }


    public void list(View v){
        pairedDevices = BA.getBondedDevices();

        ArrayList list = new ArrayList();

        for(BluetoothDevice bt : pairedDevices) list.add(bt.getName());
        Toast.makeText(getApplicationContext(), "Showing Paired Devices",Toast.LENGTH_SHORT).show();

        final ArrayAdapter adapter = new  ArrayAdapter(this,android.R.layout.simple_list_item_1, list);

        lv.setAdapter(adapter);
    }
}

