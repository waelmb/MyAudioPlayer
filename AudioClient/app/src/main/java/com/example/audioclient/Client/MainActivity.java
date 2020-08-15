package com.example.audioclient.Client;

import androidx.appcompat.app.AppCompatActivity;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.Toast;

import com.example.audioserver.KeyCommon.MyInterface;

public class MainActivity extends AppCompatActivity {

    boolean isServiceStarted;
    Button startServiceBtn;
    Button startClipBtn;
    Button pauseClipBtn;
    Button resumeClipBtn;
    Button stopClipBtn;
    Button stopServiceBtn;
    SeekBar seekBar;
    final static String TAG = "MainActivityTAG";
    Intent musicServiceIntent;
    private MyInterface myInterface;
    private boolean mIsBound = false;
    private static final String BROADCAST_FILTER =
            "mySpecialFilter";
    private MyReceiver mReceiver;
    private IntentFilter mFilter;
    private boolean isStartClipRequest = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //init views
        isServiceStarted = false;
        startServiceBtn = findViewById(R.id.startService);
        startClipBtn = findViewById(R.id.startClip);
        startClipBtn.setEnabled(false);
        pauseClipBtn = findViewById(R.id.pauseClip);
        pauseClipBtn.setEnabled(false);
        resumeClipBtn = findViewById(R.id.resumeClip);
        resumeClipBtn.setEnabled(false);
        stopClipBtn = findViewById(R.id.stopClip);
        stopClipBtn.setEnabled(false);
        stopServiceBtn = findViewById(R.id.stopService);
        stopServiceBtn.setEnabled(false);
        seekBar = findViewById(R.id.seekBar1);
        seekBar.setProgress(0);

        //init service intent
        musicServiceIntent =  new Intent(MyInterface.class.getName());
        ResolveInfo info = getPackageManager().resolveService(musicServiceIntent, 0);
        musicServiceIntent.setComponent(new ComponentName(info.serviceInfo.packageName, info.serviceInfo.name));

        Log.i(TAG, "onCreate: info.serviceInfo.packageName: " + info.serviceInfo.packageName);
        Log.i(TAG, "onCreate: info.serviceInfo.name" + info.serviceInfo.name);

        //register broadcast receiver
        mReceiver = new MyReceiver() ;
        mFilter = new IntentFilter(BROADCAST_FILTER) ;
        mFilter.setPriority(1);
        registerReceiver(mReceiver, mFilter) ;
    }

    @Override
    protected void onDestroy() {
        /*if(mIsBound) {
            try {
                myInterface.stopAudioClip();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
            myUnbindService();
        }*/

        boolean stopped = stopService(musicServiceIntent);
        Log.i(TAG, "onDestroy: stopped " + stopped);

        super.onDestroy();
        unregisterReceiver(mReceiver) ;
    }

    public void startServiceBtnHandler(View v) throws RemoteException {
        startServiceBtn.setEnabled(false);
        startClipBtn.setEnabled(true);
        stopServiceBtn.setEnabled(true);

        getApplicationContext().startForegroundService(musicServiceIntent);
    }

    public void startClipBtnHandler(View v) throws RemoteException {
        //bind service
        myBindService();
        isStartClipRequest = true;

        /*if(mIsBound) {
            startClipBtn.setEnabled(false);
            pauseClipBtn.setEnabled(true);
            stopClipBtn.setEnabled(true);
            int clipId = seekBar.getProgress();

            //start audio
            Log.i(TAG, "startClipBtnHandler: " + clipId);
            myInterface.startAudioClip(clipId);
        }
        else {
            Log.i(TAG, "startClipBtnHandler: didn't bind");
        }*/
    }

    public void pauseClipBtnHandler(View v) throws RemoteException {
        pauseClipBtn.setEnabled(false);
        resumeClipBtn.setEnabled(true);

        if(mIsBound) {
            myInterface.pauseAudioClip();
        }
        else {
            Log.i(TAG, "pauseClipBtnHandler: didn't bind");
        }
    }

    public void resumeClipBtnHandler(View v) throws RemoteException {
        resumeClipBtn.setEnabled(false);
        pauseClipBtn.setEnabled(true);

        if(mIsBound) {
            myInterface.resumeAudioClip();
        }
        else {
            Log.i(TAG, "resumeClipBtnHandler: didn't bind");
        }
    }

    public void stopClipBtnHandler(View v) throws RemoteException {
        startClipBtn.setEnabled(true);
        stopClipBtn.setEnabled(false);
        pauseClipBtn.setEnabled(false);
        resumeClipBtn.setEnabled(false);

        if(mIsBound) {
            myInterface.stopAudioClip();
            myUnbindService();
        }
        else {
            Log.i(TAG, "stopClipBtnHandler: didn't bind");
        }
    }

    public void stopServiceBtnHandler(View v) throws RemoteException {
        startServiceBtn.setEnabled(true);
        startClipBtn.setEnabled(false);
        pauseClipBtn.setEnabled(false);
        resumeClipBtn.setEnabled(false);
        stopClipBtn.setEnabled(false);
        stopServiceBtn.setEnabled(false);

        if(mIsBound) {
            myInterface.stopService();
            myUnbindService();
        }

        boolean stopped = stopService(musicServiceIntent);
        Log.i(TAG, "stopServiceBtnHandler: stopped " + stopped);
    }

    private final ServiceConnection mConnection = new ServiceConnection() {

        public void onServiceConnected(ComponentName className, IBinder iservice) {

            myInterface = MyInterface.Stub.asInterface(iservice);

            mIsBound = true;

            Log.i(TAG, "onServiceConnected: connection successful");

            if(isStartClipRequest && mIsBound) {
                startClipBtn.setEnabled(false);
                pauseClipBtn.setEnabled(true);
                stopClipBtn.setEnabled(true);
                int clipId = seekBar.getProgress();

                //start audio
                Log.i(TAG, "onServiceConnected: " + clipId);
                try {
                    myInterface.startAudioClip(clipId);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }

                isStartClipRequest = false;
            }
            else {
                Log.i(TAG, "onServiceConnected: didn't bind");
            }
        }

        public void onServiceDisconnected(ComponentName className) {

            myInterface = null;

            mIsBound = false;

            Log.i(TAG, "onServiceDisconnected: disconnection successful");
        }
    };

    private void myBindService() {
        if (!mIsBound) {

            boolean b = false;

            b = bindService(musicServiceIntent, this.mConnection, Context.BIND_AUTO_CREATE);
            if (b) {
                Log.i(TAG, "bindService() succeeded!");
            } else {
                Log.i(TAG, "bindService() failed!");
            }
        }
    }

    private void myUnbindService() {
        if (mIsBound) {
            unbindService(this.mConnection);
            mIsBound = false;
        }
    }

    public class MyReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent i) {
            Log.i(TAG, "receiver in action") ;

            //get intent extra
            String show = i.getStringExtra("CMD");

            if(show != null) {
                if (show.equalsIgnoreCase("clipEnded")) {
                    //inform user
                    Toast.makeText(getApplicationContext(),"Audio Clip Has Ended", Toast.LENGTH_SHORT).show();

                    //adjust buttons
                    startClipBtn.setEnabled(true);
                    stopClipBtn.setEnabled(false);
                    pauseClipBtn.setEnabled(false);
                    resumeClipBtn.setEnabled(false);

                    //unbind
                    if(mIsBound) {
                        myUnbindService();
                    }
                    else {
                        Log.i(TAG, "onReceive: didn't unbind");
                    }
                }
                else if (show.equalsIgnoreCase("serviceStopped")) {
                    //inform user
                    Toast.makeText(getApplicationContext(),"Service has stopped", Toast.LENGTH_SHORT).show();

                    //adjust buttons
                    startServiceBtn.setEnabled(true);
                    startClipBtn.setEnabled(false);
                    pauseClipBtn.setEnabled(false);
                    resumeClipBtn.setEnabled(false);
                    stopClipBtn.setEnabled(false);
                    stopServiceBtn.setEnabled(false);

                    //unbind
                    if(mIsBound) {
                        myUnbindService();
                    }
                    else {
                        Log.i(TAG, "onReceive: didn't unbind");
                    }
                }
            }

        }

    }
}