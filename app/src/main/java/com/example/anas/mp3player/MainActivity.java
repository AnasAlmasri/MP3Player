package com.example.anas.mp3player;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import com.example.anas.mp3player.PlayerService.PlayerBinder;

/* LINES 215-233 WERE RETRIEVED FROM STACKOVERFLOW */

public class MainActivity extends AppCompatActivity {
    // declare global variables
    PlayerService playerService;
    Intent playerIntent;
    TextView progressTextView;
    TextView durationTextView;
    ImageView musicPlayerImageView;
    ImageView playBtn;
    ImageView pauseBtn;
    ImageView loopBtn;
    SeekBar progressSeekbar;
    private Uri filePath;
    private boolean STOP_FREQ_UPDATE = false;
    private boolean loopOn = false;
    private boolean fileLoaded = false;
    private boolean fileStopped = false;

    @Override
    protected void onStart() {
        Log.d("MYAPP", "CLASS: MainActivity onStart()");
        super.onStart();

        // initialize class PlayerService
        startServiceClass();
    } // end of method onStart()

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.d("MYAPP", "CLASS: MainActivity onCreate()");

        //Remove title bar
        getSupportActionBar().hide();

        // handle landscape mode along with the landscape xml file
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            getSupportActionBar().hide();
        }

        // initialize widget controller objects
        progressSeekbar = findViewById(R.id.progressSeekbar);
        progressTextView = findViewById(R.id.progressTextView);
        durationTextView = findViewById(R.id.durationTextView);
        musicPlayerImageView = findViewById(R.id.musicPlayerImageView);
        playBtn = findViewById(R.id.playBtn);
        pauseBtn = findViewById(R.id.pauseBtn);
        loopBtn = findViewById(R.id.loopBtn);

        // set initial widget attributes
        musicPlayerImageView.bringToFront();
        progressSeekbar.setEnabled(false);
        pauseBtn.setVisibility(View.INVISIBLE);

        // progressSeekbar change listener
        progressSeekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) { }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) { }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                if (fileLoaded) {
                    // (seekbar_progress / seekbar_max) = (current_progress / mp3_duration)
                    playerService.jumpToTime(playerService.getMp3Duration() *
                            progressSeekbar.getProgress() / progressSeekbar.getMax());
                }
            }
        }); // end of seekbar change listener
    } // end of method onCreate()

    @Override
    protected void onStop() {
        Log.d("MYAPP", "CLASS: MainActivity onStop()");
        super.onStop();
    } // end of method onStop()

    @Override
    protected void onDestroy() {
        Log.d("MYAPP", "CLASS: MainActivity onDestroy()");
        super.onDestroy();
    } // end of method onDestroy()

    private void startServiceClass() { // method to initialize and start PlayerService
        if(playerIntent==null){ // only initialize an uninitialized intent
            playerIntent = new Intent(this, PlayerService.class);
            playerIntent.setAction("SERVICE_START");
            startService(playerIntent);
            bindService(playerIntent, musicConnection, Context.BIND_AUTO_CREATE);
        }
    } // end of method startServiceClass()

    public void stopServiceClass() { // method to stop service
        playerIntent = new Intent(this, PlayerService.class);
        stopService(playerIntent);
    } // end of method stopServiceClass()

    public void loadBtnOnClick(View v) {
        // open audio file explorer to choose mp3 file
        Intent intent = new Intent();
        intent.setType("audio/mp3/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select MP3 file"), 0);
    }

    public void playBtnOnClick(View v) {
        playerService.playMp3File();
        if (fileLoaded) { // switch Play and Pause buttons
            playBtn.setVisibility(View.INVISIBLE);
            pauseBtn.setVisibility(View.VISIBLE);
        }
        if (fileStopped) { // load and play same file if Play was clicked
            playerService.loadMp3File(filePath);
            fileStopped = false;
        }
    } // end of method playBtnOnClick()

    public void pauseBtnOnClick(View v) {
        playerService.pauseMp3File();
        // switch Play and Pause buttons
        pauseBtn.setVisibility(View.INVISIBLE);
        playBtn.setVisibility(View.VISIBLE);
    } // end of method pauseBtnOnClick()

    public void loopBtnOnClick(View v) {
        if (fileLoaded) {
            playerService.loopMp3File();
            // change icon based on situation
            if (!loopOn) {
                loopBtn.setImageResource(R.drawable.ic_loop_green);
                loopOn = true;
            } else {
                loopBtn.setImageResource(R.drawable.ic_loop_blue);
                loopOn = false;
            }
        }
    } // end of method loopBtnOnClick()

    public void stopBtnOnClick(View v) {
        if (fileLoaded) {
            playerService.stopMp3File();
            fileStopped = true;
            // reset seekbar controls
            progressSeekbar.setProgress(0);
            progressTextView.setText("00:00");
            durationTextView.setText("00:00");
            // retrieve original activity look
            playBtn.setVisibility(View.VISIBLE);
            pauseBtn.setVisibility(View.INVISIBLE);
            loopBtn.setImageResource(R.drawable.ic_loop_blue);
        }
    } // end of method stopBtnOnClick()

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d("MYAPP", Integer.toString(requestCode));
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 0 && resultCode == Activity.RESULT_OK){
            if ((data != null) && (data.getData() != null)){
                // load file into media player
                filePath = data.getData();
                playerService.loadMp3File(filePath);
                // send this instance to service class
                playerService.setMainInstance(this);
                // start Runnable time counter
                progressSeekbar.postDelayed(updateProgressEverySecond, 1000);
                // switch Play and Pause buttons
                playBtn.setVisibility(View.INVISIBLE);
                pauseBtn.setVisibility(View.VISIBLE);
                progressSeekbar.setEnabled(true);
                fileLoaded = true;
            }
        }
    } // end of method onActivityResult()

    // connect to the service
    private ServiceConnection musicConnection = new ServiceConnection(){
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            PlayerBinder binder = (PlayerBinder) service;
            Log.d("MYAPP", "Service CONNECTED");
            playerService = binder.getService();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.d("MYAPP", "Service DISCONNECTED");
        }
    };

    // time counter (for seekbar)
    private Runnable updateProgressEverySecond = new Runnable() {
        @Override
        public void run() {
            if (!STOP_FREQ_UPDATE && (playerService.getMp3Duration() != 0)) {
                // (seekbar_progress / seekbar_max) = (current_progress / mp3_duration)
                int prog = playerService.getMp3Progress() *
                        progressSeekbar.getMax() / playerService.getMp3Duration();

                progressSeekbar.setProgress(prog);

                if(playerService.getState().equals(MP3Player.MP3PlayerState.PLAYING)) {
                    progressSeekbar.postDelayed(updateProgressEverySecond, 1000);
                }
                // update time TextViews
                progressTextView.setText(playerService.getFormattedDuration(playerService.getMp3Progress()));
                durationTextView.setText(playerService.getFormattedDuration(playerService.getMp3Duration()));
            }
        } // end of method run()
    };
} // end of class MainActivity.java
