package com.surmoni.surmoniserver.Stream;

import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;

import com.surmoni.surmoniserver.R;
import com.surmoni.surmoniserver.SensorActivity;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class StreamActivity extends AppCompatActivity {
    private FirebaseDatabase mDatabase;
    private DatabaseReference mRefrence;
    private BottomNavigationView mBottomNavigationView;
    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener = new BottomNavigationView.OnNavigationItemSelectedListener() {
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            return navigateTo(item.getItemId());
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stream);

        mBottomNavigationView = findViewById(R.id.navigation);
        mBottomNavigationView.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        navigateTo(mBottomNavigationView.getSelectedItemId());
        stopStream();
    }

    private boolean navigateTo(@IdRes int navigationItemId) {
        if (mBottomNavigationView.getSelectedItemId() == navigationItemId
                && !getSupportFragmentManager().getFragments().isEmpty()) {
            return false;
        }
        switch (navigationItemId) {
            case R.id.navigation_streamer:
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.container_main, new StreamerFragment())
                        .commit();
                return true;
        }
        return false;
    }
    public void stopStream() {
        mDatabase = FirebaseDatabase.getInstance();
        mRefrence = mDatabase.getReference(SensorActivity.getData());


        mRefrence.child("Stream").child("stream_status").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String video_status = dataSnapshot.getValue(String.class);
                if (video_status.equals("stop"))
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
        mDatabase = FirebaseDatabase.getInstance();
        mRefrence = mDatabase.getReference(SensorActivity.getData());
        mRefrence.child("Stream").child("stream_status").setValue("stop");
        mRefrence.child("Stream").child("stream_request").setValue("no");
            finish();
    }
}
