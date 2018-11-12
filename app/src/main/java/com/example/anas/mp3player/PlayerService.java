package com.example.anas.mp3player;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import static com.example.anas.mp3player.NotificationHandler.CHANNEL_ID;

// LINES 143-157 WERE RETRIEVED FROM STACKOVERFLOW

public class PlayerService extends Service {
    // initialize global variables
    MP3Player mp3Player = new MP3Player();
    Notification notification;
    private MainActivity mainInstance;
    private final IBinder playerBinder = new PlayerBinder();
    private boolean isPaused = false;

    // declare and define binder class
    public class PlayerBinder extends Binder {
        PlayerService getService() {
            Log.d("MYAPP", "CLASS: PlayerBinder getService()");
            if (getMp3Progress() > 1000) {
                mainInstance.progressSeekbar.setProgress(getMp3Progress());
            }
            return PlayerService.this;
        } // end of method getService()
    } // end of class PlayerBinder

    @Override
    public void onCreate() { Log.d("MYAPP", "CLASS: PlayerService onCreate()"); }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d("MYAPP", "CLASS: PlayerService onStartCommand()");
        Log.d("MYAPP", "State: " + getState().name());

        // deal with different intent actions
        if(intent.getAction() != null && intent.getAction().equals("SERVICE_START")) {
            // create notification intent
            Intent notificationIntent = new Intent(this, MainActivity.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0,
                    notificationIntent, 0);

            // create pause intents
            Intent pauseNotBtnIntent = new Intent(this, PlayerService.class);
            pauseNotBtnIntent.setAction("ACTION_PAUSE");
            PendingIntent pausePendIntent = PendingIntent.getService(this, 0,
                    pauseNotBtnIntent, 0);
            NotificationCompat.Action pauseAction = new NotificationCompat.Action(
                    android.R.drawable.ic_media_pause, "Pause", pausePendIntent);

            // create close intents
            Intent closeNotBtnIntent = new Intent(this, PlayerService.class);
            closeNotBtnIntent.setAction("ACTION_CLOSE");
            PendingIntent closePendIntent = PendingIntent.getService(this, 0,
                    closeNotBtnIntent, 0);
            NotificationCompat.Action closeAction = new NotificationCompat.Action(
                    android.R.drawable.ic_menu_close_clear_cancel, "Close", closePendIntent);

            // build notification
            notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                    .setContentTitle("Anas' Music Player")
                    .setContentText(" ")
                    .setSmallIcon(R.drawable.ic_app)
                    .setContentIntent(pendingIntent)
                    .addAction(pauseAction)
                    .addAction(closeAction)
                    .build();
            startForeground(1, notification);

            Log.d("MYAPP", "Notification Created");
        } else if (intent.getAction() != null && intent.getAction().equals("ACTION_PAUSE")) {
            if (isPaused) {
                mp3Player.play();
                isPaused = false;
            } else {
                mp3Player.pause();
                isPaused = true;
            }
        } else {
            mp3Player.stop();
            mainInstance.stopServiceClass(); // stop the service from MainActivity
            stopSelf();
        }
        return START_NOT_STICKY;
    } // end of method onStartCommand()

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Log.d("MYAPP", "CLASS: PlayerService onBind()");
        return playerBinder;
    } // end of method onBind()

    @Override
    public boolean onUnbind(Intent intent){
        Log.d("MYAPP", "CLASS: PlayerService onUnbind()");
        Log.d("MYAPP", "State: " + getState().name());
        return true;
    } // end of method onUnbind()

    @Override
    public void onDestroy() {
        Log.d("MYAPP", "CLASS: PlayerService onDestroy()");
    }

    public void setMainInstance(MainActivity mainActivity) {
        this.mainInstance = mainActivity;
    }

    public void loadMp3File(Uri fileUri) { mp3Player.load(this, fileUri.toString()); }

    public void playMp3File() { mp3Player.play(); }

    public void pauseMp3File() {
        mp3Player.pause();
    }

    public void loopMp3File() { mp3Player.setLooping(); }

    public void stopMp3File() {
        mp3Player.stop();
    }

    public void jumpToTime(int msec) {
        mp3Player.jumpToTime(msec);
    }

    public int getMp3Duration() { return mp3Player.getDuration(); }

    public int getMp3Progress() { return mp3Player.getProgress(); }

    public MP3Player.MP3PlayerState getState() { return mp3Player.getState(); }

    public String getFormattedDuration(long msec) {
        String finalTimerString = "";
        String secondsString = "";
        // Convert total duration into time
        int hours = (int) (msec / (1000 * 60 * 60));
        int minutes = (int) (msec % (1000 * 60 * 60)) / (1000 * 60);
        int seconds = (int) ((msec % (1000 * 60 * 60)) % (1000 * 60) / 1000);
        // Add hours if there
        if (hours > 0) { finalTimerString = hours + ":"; }
        // Prepending 0 to seconds if it is one digit
        if (seconds < 10) { secondsString = "0" + seconds; }
        else { secondsString = "" + seconds; }
        finalTimerString = finalTimerString + minutes + ":" + secondsString;
        return finalTimerString;
    } // end of method getFormattedDuration()
} // end of class PlayerService.java
