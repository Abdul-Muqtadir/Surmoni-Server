package com.surmoni.surmoniserver;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.surmoni.surmoniserver.Motion.MotionActivity;
import com.surmoni.surmoniserver.Stream.StreamActivity;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class SensorActivity extends Activity {
    private FirebaseDatabase mDatabase;
    private DatabaseReference mRefrence;
    private Intent intent;
    public static String user_id;
    private AdView mAdView;
    @Override
    public final void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sensor);

        intent=getIntent();
        user_id=intent.getStringExtra("User_ID");

        initializeTemperatureSensor(this,user_id);
        initializeHumiditySensor(this,user_id);
        stream(user_id);
        motion_detection(user_id);
        getCameraSettings(user_id);

        registerReceiver(mBatInfoReceiver, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));

        ActivateAdds();
    }
    public void initializeTemperatureSensor(Context context,final String user_id){
        SensorManager sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        final SensorEventListener mEventListener = new SensorEventListener() {
            public void onAccuracyChanged(android.hardware.Sensor sensor, int accuracy) {}
            public void onSensorChanged(SensorEvent event) {
                switch (event.sensor.getType()) {
                    case Sensor.TYPE_AMBIENT_TEMPERATURE: {
                        float temp = event.values[0];
                        SensorCRUD sensorCRUD = new SensorCRUD();
                        sensorCRUD.UpdateSensor(user_id,"Temperature",temp);
                    }
                    break;
                    default:
                        break;
                }
            }
        };
        setTemperatureListeners(sensorManager, mEventListener);
    }
    public void setTemperatureListeners(SensorManager sensorManager, SensorEventListener mEventListener)
    {
        sensorManager.registerListener(mEventListener,
                sensorManager.getDefaultSensor(android.hardware.Sensor.TYPE_AMBIENT_TEMPERATURE),
                SensorManager.SENSOR_DELAY_NORMAL);
    }

    public void initializeHumiditySensor(Context context,final String user_id){
        SensorManager sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        final SensorEventListener mEventListener = new SensorEventListener() {
            public void onAccuracyChanged(android.hardware.Sensor sensor, int accuracy) {}
            public void onSensorChanged(SensorEvent event) {
                switch (event.sensor.getType()) {
                    case Sensor.TYPE_RELATIVE_HUMIDITY: {
                        float temp = event.values[0];
                        SensorCRUD sensorCRUD = new SensorCRUD();
                        sensorCRUD.UpdateSensor(user_id,"Humidity",temp);
                    }break;
                    default:
                        break;
                }
            }
        };
        setHumidityListeners(sensorManager, mEventListener);
    }
    public void setHumidityListeners(SensorManager sensorManager, SensorEventListener mEventListener)
    {
        sensorManager.registerListener(mEventListener,
                sensorManager.getDefaultSensor(android.hardware.Sensor.TYPE_RELATIVE_HUMIDITY),
                SensorManager.SENSOR_DELAY_NORMAL);
    }

    public void stream(final String user_id)
    {
        final Intent intent=new Intent(SensorActivity.this,StreamActivity.class);
        final boolean checkStatus;

        mDatabase = FirebaseDatabase.getInstance();
        mRefrence = mDatabase.getReference(user_id);
        mRefrence.child("Stream").child("stream_request").addValueEventListener(new ValueEventListener() {
            int count=0 ;
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String stream_request = dataSnapshot.getValue(String.class);
                if (stream_request.equals("yes")) {
                    startActivity(intent);
                    count++;
                    Toast.makeText(SensorActivity.this,"Starting live stream",Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public void motion_detection(final String user_id)
    {
        mDatabase = FirebaseDatabase.getInstance();
        mRefrence = mDatabase.getReference(user_id);
        final Intent intent=new Intent(SensorActivity.this, MotionActivity.class);
        mRefrence.child("Motion").child("motion_request").addValueEventListener(new ValueEventListener() {
            int count=0 ;
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String motion_request = dataSnapshot.getValue(String.class);
                if (motion_request.equals("start")) {
                    startActivity(intent);
                    count++;
                    Toast.makeText(SensorActivity.this,"Starting motion detection",Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public void getCameraSettings(final String user_id)
    {
        mDatabase = FirebaseDatabase.getInstance();
        mRefrence = mDatabase.getReference(user_id);

        mRefrence.child("Motion").child("motion_camera").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String data=dataSnapshot.getValue(String.class);
                //if you want to open front facing camera use this line
                //////////changing position/////////////
                SharedPreferences user_prefs = getSharedPreferences("com.surmoni.server.motion_camera",MODE_PRIVATE);
                SharedPreferences.Editor editor = user_prefs.edit();
                editor.putString("motion_camera_position",data);
                editor.apply();
                //////////changing position/////////////
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        mRefrence.child("Stream").child("stream_camera").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String data=dataSnapshot.getValue(String.class);
                //if you want to open front facing camera use this line
                //////////changing position/////////////
                SharedPreferences user_prefs = getSharedPreferences("com.surmoni.server.stream_camera",MODE_PRIVATE);
                SharedPreferences.Editor editor = user_prefs.edit();
                editor.putString("stream_camera_position",data);
                editor.apply();
                //////////changing position/////////////
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    public static String getData()
    {
        return user_id;
    }

    private BroadcastReceiver mBatInfoReceiver = new BroadcastReceiver() {

            @Override
            public void onReceive(Context c, Intent i) {
                int level = i.getIntExtra("level", 0);
            mRefrence.child("Charge").child("battery_status").setValue(level);
        }
    };

    public void ActivateAdds(){
        MobileAds.initialize(this, "ca-app-pub-4686298843634340~8442095718");
        AdView adView = new AdView(this);
        adView.setAdSize(AdSize.BANNER);
        adView.setAdUnitId("ca-app-pub-4686298843634340/5335805825");
        mAdView = findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);
    }
}

