package com.surmoni.surmoniserver.Motion;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.SurfaceView;
import android.widget.TextView;

import com.surmoni.surmoniserver.R;
import com.surmoni.surmoniserver.SensorActivity;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Calendar;

public class MotionActivity extends AppCompatActivity {
    private FirebaseDatabase mDatabase;
    private DatabaseReference mRefrence;
    private TextView txtStatus;
    private MotionDetector motionDetector;
    private Intent intent;
    private String user_id;
    public static Context context;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_motion_detetction);
        context=MotionActivity.this;
        txtStatus = (TextView) findViewById(R.id.txtStatus);
        mDatabase = FirebaseDatabase.getInstance();
        user_id= SensorActivity.getData();
        mRefrence = mDatabase.getReference(user_id);
        motionDetector = new MotionDetector(this, (SurfaceView) findViewById(R.id.surfaceView));
        motionDetector.setMotionDetectorCallback(new MotionDetectorCallback() {
            @Override
            public void onMotionDetected() {
                String DateTime = getDateTime();
                mRefrence.child("Motion").child("motion_detected").setValue("yes");
                mRefrence.child("Motion").child("motion_detected_at").setValue(DateTime);
                txtStatus.setText("detected");
            }

            @Override
            public void onTooDark() {
                mRefrence.child("Motion").child("motion_detected").setValue("Too Dark Here");
            }
        });

        ////// Config Options
        motionDetector.setCheckInterval(500);
        motionDetector.setLeniency(20);
        motionDetector.setMinLuma(1000);

        mRefrence.child("Motion").child("motion_request").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String motion_request=dataSnapshot.getValue(String.class);
                if(motion_request.equals("stop"))
                {
                    finish();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    public void onPause()
    {
        super.onPause();
        motionDetector.onPause();
        user_id= SensorActivity.getData();
        mDatabase = FirebaseDatabase.getInstance();
        mRefrence = mDatabase.getReference(user_id);
        mRefrence.child("Motion").child("motion_request").setValue("stop");
        finish();
    }
    private String getDateTime() {
        Calendar cal = Calendar.getInstance();
        String DateTime=cal.getTime().toString();
        return DateTime;
    }
    @Override
    protected void onResume() {
        super.onResume();
        motionDetector.onResume();

        if (motionDetector.checkCameraHardware()) {
            txtStatus.setText("Camera found");
        } else {
            txtStatus.setText("No camera available");
        }
    }

//    @Override
//    protected void onPause() {
//        super.onPause();
//        motionDetector.onPause();
//    }

}
