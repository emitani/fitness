package com.example.eiko.alphafitness;


import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.res.Configuration;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Bundle;

import android.os.IBinder;
import android.support.v4.app.FragmentActivity;
import android.widget.Toast;

import java.util.List;


public class MainActivity extends FragmentActivity  {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SensorManager smgr = (SensorManager) getSystemService(SENSOR_SERVICE);
        List<Sensor> list = smgr.getSensorList(Sensor.TYPE_ALL);
        for(Sensor sensor : list) {
            System.out.println(sensor.getName());
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

}
