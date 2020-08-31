package com.surmoni.surmoniserver;

import android.app.Activity;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class SensorCRUD extends Activity {
    private FirebaseDatabase mDatabase;
    private DatabaseReference mRefrence;
    public void CreateSensor() {
        mDatabase= FirebaseDatabase.getInstance();
        mRefrence=mDatabase.getReference();
        mRefrence.child("Sensor").child("Temperature").child("sensor_id").setValue("1");
        mRefrence.child("Sensor").child("Temperature").child("sensor_name").setValue("temperature");
        mRefrence.child("Sensor").child("Temperature").child("sensor_value").setValue("0");
        mRefrence.child("Sensor").child("Temperature").child("sensor_status").setValue("changed");
        mRefrence.child("Sensor").child("Humidity").child("sensor_id").setValue("1");
        mRefrence.child("Sensor").child("Humidity").child("sensor_name").setValue("humidity");
        mRefrence.child("Sensor").child("Humidity").child("sensor_value").setValue("0");
        mRefrence.child("Sensor").child("Humidity").child("sensor_status").setValue("changed");
    }


    public void UpdateSensor(String user_id,String sensor_name,float sensor_value) {
        mDatabase= FirebaseDatabase.getInstance();
        mRefrence=mDatabase.getReference(user_id);
        mRefrence.child("Sensor").child(sensor_name).child("sensor_value").setValue(Float.toString(sensor_value));
    }
}