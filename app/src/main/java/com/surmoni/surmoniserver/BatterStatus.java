package com.surmoni.surmoniserver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.BatteryManager;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class BatterStatus extends BroadcastReceiver {
    private FirebaseDatabase mDatabase;
    private DatabaseReference mRefrence;

    @Override
    public void onReceive(Context context, Intent intent)
    {
        mDatabase = FirebaseDatabase.getInstance();
        mRefrence = mDatabase.getReference(SensorActivity.user_id);

        int status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
        boolean isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                status == BatteryManager.BATTERY_STATUS_FULL;

        int isLow = intent.getIntExtra(BatteryManager.EXTRA_BATTERY_LOW, -1);
        mRefrence.child("Charge").child("battery_status").setValue(isLow);

        int chargePlug = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1);
        boolean usbCharge = chargePlug == BatteryManager.BATTERY_PLUGGED_USB;
        boolean acCharge = chargePlug == BatteryManager.BATTERY_PLUGGED_AC;

        int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
        int scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);

        float batteryPct = level / (float)scale;


    }
}
