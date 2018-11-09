package com.example.anas.mp3player;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.CursorLoader;
import android.util.Log;

import java.io.File;
import java.net.URI;

public class PlayerService extends Service {
    MP3Player mp3Player = new MP3Player();
    private final IBinder playerBinder = new PlayerBinder();
    Intent notificationIntent;
    NotificationManager notificationManager;
    NotificationCompat.Builder notificationBuilder;

    public PlayerService() { }

    @Override
    public void onCreate() {
        Log.d("MYAPP", "PlayerService onCreate()");

        notificationIntent = new Intent(this, MainActivity.class);
        notificationManager = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);

        notificationBuilder = new NotificationCompat.Builder(this)
                .setContentIntent(PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT))
                .setSmallIcon(R.mipmap.ic_launcher_round)
                .setContentTitle("My Music App")
                .setContentText("Running...");


        // Build the notification.
        Notification notification = notificationBuilder.build();


        notificationManager.notify(1, notification);

        // Start foreground service.
        startForeground(1, notification);
        Log.d("MYAPP", "Notification Created");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d("MYAPP", "PlayerService onStartCommand()");
        Log.d("MYAPP", "State: " + getState().name());
        return START_NOT_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Log.d("MYAPP", "PlayerService onBind()");
        return playerBinder;
    }

    public class PlayerBinder extends Binder {
        PlayerService getService() {
            Log.d("MYAPP", "PlayerBinder getService()");
            return PlayerService.this;
        }
    }

    @Override
    public boolean onUnbind(Intent intent){
        Log.d("MYAPP", "PlayerService onUnbind()");
        Log.d("MYAPP", "State: " + getState().name());
        return true;
    }

    public void loadMp3File(Uri fileUri) { mp3Player.load(this, fileUri.toString()); }

    public void playMp3File() { mp3Player.play(); }

    public void pauseMp3File() {
        mp3Player.pause();
    }

    public void stopMp3File() {
        mp3Player.stop();
    }

    public String getFileName(Uri fileUri) {
        File myFile = new File(fileUri.toString());
        String path = myFile.getAbsolutePath();
        Log.d("MYAPP", path);
        return path.substring(path.lastIndexOf("/")+1);
    }

    public MP3Player.MP3PlayerState getState() { return mp3Player.getState(); }

    @Override
    public void onDestroy() {
        Log.d("MYAPP", "PlayerService onDestroy()");
    }
}
