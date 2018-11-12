package com.example.anas.mp3player;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.util.Log;
import java.io.IOException;
import java.io.Serializable;

/**
 * Created by pszmdf on 06/11/16.
 */

// LINES 31-37 WERE ADDED TO THIS CLASS BY STUDENT 'ANAS ALMASRI'
public class MP3Player implements Serializable {

    protected MediaPlayer mediaPlayer;
    protected MP3PlayerState state;
    protected String filePath;
    private MediaPlayer latestMediaPlayer;
    private int loadCount = 0;

    public enum MP3PlayerState {
        ERROR,
        PLAYING,
        PAUSED,
        STOPPED
    }

    public MP3Player() { this.state = MP3PlayerState.STOPPED; }

    public void jumpToTime(int msec) { mediaPlayer.seekTo(msec); }

    public void setLooping() { mediaPlayer.setLooping(true); }

    public MP3PlayerState getState() {
        return this.state;
    }

    public void load(Context cont, String filePath) {
        this.filePath = filePath;
        mediaPlayer = new MediaPlayer();
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        loadCount += 1;
        try {
            if (loadCount > 1) {
                latestMediaPlayer.stop();
            } else {
                latestMediaPlayer = mediaPlayer;
            }
            mediaPlayer.setDataSource(cont, Uri.parse(filePath));
            mediaPlayer.prepare();
        } catch (IOException e) {
            Log.e("MP3Player", e.toString());
            e.printStackTrace();
            this.state = MP3PlayerState.ERROR;
            return;
        } catch (IllegalArgumentException e) {
            Log.e("MP3Player", e.toString());
            e.printStackTrace();
            this.state = MP3PlayerState.ERROR;
            return;
        }

        this.state = MP3PlayerState.PLAYING;

        mediaPlayer.start();
        mediaPlayer.setLooping(false);
    }

    public int getProgress() {
        if(mediaPlayer!=null) {
            if(this.state == MP3PlayerState.PAUSED || this.state == MP3PlayerState.PLAYING)
                return mediaPlayer.getCurrentPosition();
        }
        return 0;
    }

    public int getDuration() {
        if(mediaPlayer!=null)
            if(this.state == MP3PlayerState.PAUSED || this.state == MP3PlayerState.PLAYING)
                return mediaPlayer.getDuration();
        return 0;
    }

    public void play() {
        if(this.state == MP3PlayerState.PAUSED) {
            mediaPlayer.start();
            this.state = MP3PlayerState.PLAYING;
            Log.i("MYAPP", "CLASS: MP3Player play()");
        }
    }

    public void pause() {
        if(this.state == MP3PlayerState.PLAYING) {
            mediaPlayer.pause();
            state = MP3PlayerState.PAUSED;
            Log.i("MYAPP", "CLASS: MP3Player pause()");
        }
    }

    public void stop() {
        if(mediaPlayer!=null) {
            if(mediaPlayer.isPlaying())
                mediaPlayer.stop();
            state = MP3PlayerState.STOPPED;
            mediaPlayer.reset();
            mediaPlayer.release();
            mediaPlayer = null;
            Log.i("MYAPP", "CLASS: MP3Player stop()");
        }
    }
}
