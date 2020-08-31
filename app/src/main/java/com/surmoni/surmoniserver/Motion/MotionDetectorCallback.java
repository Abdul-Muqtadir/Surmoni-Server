package com.surmoni.surmoniserver.Motion;

public interface MotionDetectorCallback {
    void onMotionDetected();
    void onTooDark();
}
