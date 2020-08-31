package com.surmoni.surmoniserver;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import io.firekast.Firekast;

public class MainActivity extends AppCompatActivity {
    private EditText user_id;
    private TextView user_exist;
    private Button register;
    private FirebaseDatabase mDatabase;
    private DatabaseReference mRefrence;
    private AdView mAdView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ActivateAdds();
        user_id=findViewById(R.id.user);
        user_exist=findViewById(R.id.tv_compatibility);
        register=findViewById(R.id.bt_register);
        if(checkTemperatureSensorCompatibility(getApplicationContext())==false || checkHumiditySensorCompatibility(getApplicationContext())==false)
        {
            user_id.setEnabled(false);
            register.setEnabled(false);
            user_exist.setText("Your device does not have required features or sensors.");
        }
        else
            {
                if (Build.VERSION.SDK_INT >= 23) {

                    if (checkSelfPermission(Manifest.permission.CAMERA)
                            != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, 1);
                    }

                    if (checkSelfPermission(Manifest.permission.RECORD_AUDIO)
                            != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, 1);
                    }
                    if (checkSelfPermission(Manifest.permission.INTERNET)
                            != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.INTERNET}, 1);
                    }
                }
            }

    }

    public void onClickStartServer(View view) {
        mDatabase = FirebaseDatabase.getInstance();
        mRefrence = mDatabase.getReference();

        user_id=findViewById(R.id.user);
        final String User_ID=user_id.getText().toString().trim();
        user_exist=findViewById(R.id.user_status);
        if(!TextUtils.isEmpty(User_ID)) {
            mRefrence.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.hasChild(User_ID)) {
                        String User_ID = user_id.getText().toString();
                        Intent intent = new Intent(MainActivity.this, SensorActivity.class);
                        intent.putExtra("User_ID", User_ID);
                        mDatabase = FirebaseDatabase.getInstance();
                        mRefrence = mDatabase.getReference(User_ID);

                        mRefrence.child("Stream").child("stream_enable_id").addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                String data = dataSnapshot.getValue(String.class);

                                Firekast.initialize(MainActivity.this, data);

                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });
                        startActivity(intent);
                    } else {
                        user_exist.setText("User does not Exists");
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }else
            {
                Toast.makeText(this, "Field(s) are empty.",
                        Toast.LENGTH_SHORT).show();
            }
    }
    private boolean checkTemperatureSensorCompatibility(Context context) {
        if (context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_SENSOR_AMBIENT_TEMPERATURE)){
            return true;
        }
        else {
            return false;
        }
    }

    private boolean checkHumiditySensorCompatibility(Context context) {
        if (context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_SENSOR_RELATIVE_HUMIDITY)){
            return true;
        }
        else {
            return false;
        }
    }
     public void ActivateAdds(){
        MobileAds.initialize(this, "ca-app-pub-4686298843634340~8442095718");
        AdView adView = new AdView(this);
        adView.setAdSize(AdSize.BANNER);
        adView.setAdUnitId("ca-app-pub-4686298843634340/4502850706");
        mAdView = findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);
    }
}
