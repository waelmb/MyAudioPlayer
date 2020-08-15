package com.example.audioserver.Server;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import com.example.audioserver.KeyCommon.MyInterface;


public class AudioServer extends Service {
    final static String TAG = "AudioServer";
    private MediaPlayer mPlayer;
    private Integer mStartID;
    private static final int NOTIFICATION_ID = 1;
    private Notification notification;
    private static String CHANNEL_ID = "Music player style" ;
    private int[] rawClipIdsArray;
    private static final String BROADCAST_FILTER =
            "mySpecialFilter";

    @Override
    public void onCreate() {
        super.onCreate();

        rawClipIdsArray = new int[] {
                R.raw.clip1_american_anthem,
                R.raw.clip2_russian_anthem,
                R.raw.clip3_german_anthem,
                R.raw.clip4_fur_elise,
                R.raw.clip5_river_flows_in_you
        };

        createNotificationChannel();

        notification =
                new NotificationCompat.Builder(getApplicationContext(), CHANNEL_ID)
                        .setSmallIcon(android.R.drawable.ic_media_play)
                        .setOngoing(true).setContentTitle("Music Service")
                        .setTicker("Music Service is Running!")
                        .build();

        startForeground(NOTIFICATION_ID, notification);
    }

    @Override
    public void onDestroy() {
        Log.i(TAG, "onDestroy");

        if(mPlayer != null) {
            mPlayer.stop();
            mPlayer.release();
            mPlayer = null;
        }

        if(mStartID != null) {
            stopForeground(true);
            stopSelf(mStartID);
            mStartID = null;
        }

        Log.i(TAG, "onDestroy: sending broadcast");
        Intent aIntent = new Intent(BROADCAST_FILTER) ;
        aIntent.putExtra("CMD", "serviceStopped");
        sendOrderedBroadcast(aIntent, null) ;

        super.onDestroy();
    }

    private final MyInterface.Stub mBinder = new MyInterface.Stub() {

        public void startService() {
            Log.i(TAG, "startService");
        }

        public void startAudioClip(int clipId) {
            Log.i(TAG, "startAudioClip " + clipId);

            int rawClip = rawClipIdsArray[clipId];
            // Set up the Media Player
            mPlayer = MediaPlayer.create(getApplicationContext(), rawClip);

            if (null != mPlayer) {
                if (mPlayer.isPlaying()) {

                    // Rewind to beginning of song
                    mPlayer.seekTo(0);

                } else {

                    //handler
                    mPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                        @Override
                        public void onCompletion(MediaPlayer mediaPlayer) {
                            //send a broadcast to tell client that song is done
                            Log.i(TAG, "onCompletion: sending broadcast");
                            Intent aIntent = new Intent(BROADCAST_FILTER) ;
                            aIntent.putExtra("CMD", "clipEnded");
                            sendOrderedBroadcast(aIntent, null) ;
                        }
                    });

                    // Start playing song
                    mPlayer.start();
                }
            }
        }

        public void pauseAudioClip() {
            Log.i(TAG, "pauseAudioClip");

            if(mPlayer != null) {
                mPlayer.pause();
            }
        }


        public void resumeAudioClip() {
            Log.i(TAG, "resumeAudioClip");

            if(mPlayer != null) {
                mPlayer.start();
            }
        }

        public void stopAudioClip()  {
            Log.i(TAG, "stopAudioClip");

            if(mPlayer != null) {
                mPlayer.stop();
                mPlayer.release();
                mPlayer = null;
            }
        }

        public void stopService() {
            Log.i(TAG, "stopService");

            if(mPlayer != null) {
                mPlayer.stop();
                mPlayer.release();
                mPlayer = null;
            }

            if(mStartID != null) {
                stopForeground(true);
                stopSelf(mStartID);
                mStartID = null;
            }
        }
    };

    @Override
    public int onStartCommand(Intent intent, int flags, int startid) {
        Log.i(TAG, "onStartCommand: startid is " + startid);
        mStartID = startid;
        if (null != mPlayer) {

            // ID for this start command
            //mStartID = startid;
            Log.i(TAG, "onStartCommand: mStartID is " + mStartID);

            if (mPlayer.isPlaying()) {

                // Rewind to beginning of song
                mPlayer.seekTo(0);
            } else {

                // Start playing song
                mPlayer.start();
            }

        }

        // Don't automatically restart this Service if it is killed
        return START_NOT_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    private void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Music player notification";
            String description = "The channel for music player notifications";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }
}
