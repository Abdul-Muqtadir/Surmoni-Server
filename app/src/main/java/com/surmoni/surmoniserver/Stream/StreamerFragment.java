package com.surmoni.surmoniserver.Stream;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.surmoni.surmoniserver.Motion.MotionActivity;
import com.surmoni.surmoniserver.R;
import com.surmoni.surmoniserver.SensorActivity;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import io.firekast.FKCamera;
import io.firekast.FKCameraFragment;
import io.firekast.FKError;
import io.firekast.FKStream;
import io.firekast.FKStreamer;

import static android.content.Context.MODE_PRIVATE;

public class StreamerFragment extends Fragment implements FKStreamer.StreamingCallback, FKCameraFragment.OnCameraReadyCallback {
    private FirebaseDatabase mDatabase;
    private DatabaseReference mRefrence;
    private Button mButton;
    private TextView mTextViewLive;
    private ProgressBar mProgressBarCreateStream;
    private ProgressBar mProgressBarLive;
    private LinearLayout mLayoutLive;
    public static String user_id;
    FKCameraFragment cameraFragment;

    @Nullable
    public static String latestStreamId = null;
    @NonNull
    private FKCamera mCamera;
    @NonNull
    private FKStreamer mStreamer;

    /**
     * The current stream
     */
    @Nullable
    private FKStream mStream;
    private boolean mIsCreatingStream = false;

    @Override
    public void onResume()
    {
        super.onResume();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        user_id= SensorActivity.getData();
        ////////database work///////////
        mDatabase = FirebaseDatabase.getInstance();
        mRefrence = mDatabase.getReference(user_id);
        ////////database work///////////
        stream();

        return inflater.inflate(R.layout.fragment_streamer, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        cameraFragment = new FKCameraFragment();
        cameraFragment.getCameraAsync(this);
        getChildFragmentManager().beginTransaction()
                .replace(R.id.camera_container, cameraFragment)
                .commit();

        mButton = view.findViewById(R.id.button);
        mButton.setEnabled(false);

        mTextViewLive = view.findViewById(R.id.textViewLive);
        mLayoutLive = view.findViewById(R.id.layoutLive);
        mProgressBarLive = view.findViewById(R.id.progressBarLive);
        mProgressBarLive.setVisibility(View.GONE);
        mProgressBarCreateStream = view.findViewById(R.id.progressBarCreateStream);
        mProgressBarCreateStream.setVisibility(View.GONE);
    }

    private void updateUI() {
        mButton.setText(mStreamer.isStreaming() ? R.string.streaming_stop : R.string.streaming_start);
        mButton.setEnabled(!mIsCreatingStream);
        mProgressBarCreateStream.setVisibility(mIsCreatingStream ? View.VISIBLE : View.GONE);
        mLayoutLive.setVisibility(mStreamer.isStreaming() ? View.VISIBLE : View.GONE);
        if (mStream != null) {
            mLayoutLive.setBackgroundColor(mStream.getState() == FKStream.State.LIVE ? Color.RED : Color.GRAY);
            mProgressBarLive.setVisibility(mStream.getState() == FKStream.State.LIVE ? View.GONE : View.VISIBLE);
            mTextViewLive.setText(mStream.getState().toString());
        }
    }
    ////////database work///////////
    public void stream() {
        mDatabase = FirebaseDatabase.getInstance();
        mRefrence = mDatabase.getReference(user_id);


        mRefrence.child("Stream").child("stream_status").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String video_status = dataSnapshot.getValue(String.class);
                if (video_status.equals("stop")) {
                    if (mStreamer.isStreaming()) {
                        handleStop();
                        getFragmentManager().beginTransaction()
                        .remove(cameraFragment).remove(StreamerFragment.this).commit();
                    }
                }else
                {
                    if (video_status.equals("start")) {
                        handleStart();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    ////////database work///////////

    private void handleStart() {
        mIsCreatingStream = true;
        updateUI();
        // First request a stream
        mStreamer.createStream(new FKStreamer.CreateStreamCallback() {
            @Override
            public void done(@Nullable final FKStream stream, @Nullable FKError error) {
                mIsCreatingStream = false;
                if (error != null) {
                    new AlertDialog.Builder(StreamerFragment.this.getContext())
                            .setTitle("Create stream error")
                            .setMessage(error.toString())
                            .setNeutralButton(android.R.string.ok, null)
                            .show();
                } else {

                                mButton.setText(R.string.streaming_stop);
                                mStream = stream;
                                latestStreamId = stream.getId();
                                String stream_id=latestStreamId;
                                 ////////database work///////////
                                mDatabase = FirebaseDatabase.getInstance();
                                mRefrence = mDatabase.getReference( SensorActivity.getData());
                                mRefrence.child("Stream").child("stream_id").setValue(stream_id);
                                ////////database work///////////
                                mStreamer.startStreaming(stream, StreamerFragment.this);
                }
                updateUI();
            }
        });
    }

    private void handleStop() {
        mStream = null;
        mStreamer.stopStreaming();
        updateUI();
    }

    @Override
    public void onCameraReady(@Nullable FKCamera camera, @Nullable FKStreamer streamer, @Nullable FKError error) {
        if (error != null) {
            new AlertDialog.Builder(StreamerFragment.this.getContext())
                    .setTitle("Camera error")
                    .setMessage(error.toString())
                    .show();
            return;
        }
        SharedPreferences camera_prefs = getContext().getSharedPreferences("com.surmoni.server.stream_camera",MODE_PRIVATE);
        if(camera_prefs.contains("stream_camera_position"))
        {
            String data=camera_prefs.getString("stream_camera_position",null);
            if(data.equals("CAMERA_FACING_FRONT"))
            {
                mCamera = camera;
                mStreamer = streamer;
                updateUI();
            }
            else
            {
                mCamera = camera;
                mCamera.switchToNextPosition();
                mStreamer = streamer;
                updateUI();
            }
        }
        else
        {
            mCamera = camera;
            mCamera.switchToNextPosition();
            mStreamer = streamer;
            updateUI();
        }
//        mCamera = camera;
//        mCamera.switchToNextPosition();
//        mStreamer = streamer;
//        updateUI();

    }

    @Override
    public void onSteamWillStartUnless(@Nullable FKStream stream, @Nullable FKError error) {
        updateUI();
        if (error != null) {
            new AlertDialog.Builder(StreamerFragment.this.getContext())
                    .setTitle("Streaming not started")
                    .setMessage(error.toString())
                    .setNeutralButton(android.R.string.ok, null)
                    .show();
            return;
        }
    }

    @Override
    public void onStreamDidStop(@Nullable FKStream stream, FKError error) {
        updateUI();
    }

    @Override
    public void onStreamHealthDidUpdate(boolean freezing, float health) {

    }

    @Override
    public void onStreamDidBecomeLive(@NonNull FKStream stream) {
        updateUI();
    }


}
