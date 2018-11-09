package com.example.anas.mp3player;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.Uri;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import com.example.anas.mp3player.PlayerService.PlayerBinder;

public class MainActivity extends AppCompatActivity {
    PlayerService playerService;
    Intent playerIntent;
    TextView fileNameTextView;
    TextView progressTextView;
    TextView durationTextView;
    ImageView musicPlayerImageView;
    ImageView playBtn;
    ImageView pauseBtn;
    ImageView loopBtn;
    SeekBar progressSeekbar;
    private boolean SERVICE_BOUND = false;
    private boolean STOP_FREQ_UPDATE = false;
    private boolean loopOn = false;
    private boolean fileLoaded = false;

    @Override
    protected void onStart() {
        Log.d("MYAPP", "MainActivity onStart()");
        super.onStart();
        if(playerIntent==null){
            playerIntent = new Intent(this, PlayerService.class);
            startService(playerIntent);
            bindService(playerIntent, musicConnection, Context.BIND_AUTO_CREATE);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Remove title bar
        getSupportActionBar().hide();
        setContentView(R.layout.activity_main);
        Log.d("MYAPP", "MainActivity onCreate()");

        // initialize widget controller objects
        fileNameTextView = findViewById(R.id.fileNameTextView);
        progressSeekbar = findViewById(R.id.progressSeekbar);
        progressTextView = findViewById(R.id.progressTextView);
        durationTextView = findViewById(R.id.durationTextView);
        musicPlayerImageView = findViewById(R.id.musicPlayerImageView);
        playBtn = findViewById(R.id.playBtn);
        pauseBtn = findViewById(R.id.pauseBtn);
        loopBtn = findViewById(R.id.loopBtn);
        musicPlayerImageView.bringToFront();
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
                    playerService.jumpToTime(playerService.getMp3Duration() *
                            progressSeekbar.getProgress() / progressSeekbar.getMax());
                }
            }
        });
    }

    //connect to the service
    private ServiceConnection musicConnection = new ServiceConnection(){

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            PlayerBinder binder = (PlayerBinder) service;
            //get service
            playerService = binder.getService();
            SERVICE_BOUND = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.d("MYAPP", "Service DISCONNECTED");
            SERVICE_BOUND = false;
        }
    };

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 0 && resultCode == Activity.RESULT_OK){
            if ((data != null) && (data.getData() != null)){
                Uri audioFileUri = data.getData();
                playerService.loadMp3File(audioFileUri);
                progressSeekbar.postDelayed(updateProgressEverySecond, 1000);
                playBtn.setVisibility(View.INVISIBLE);
                pauseBtn.setVisibility(View.VISIBLE);
                fileLoaded = true;
            }
        }
    }

    public void loadBtnOnClick(View v) {
        Intent intent = new Intent();
        intent.setType("audio/mp3/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select MP3 file"), 0);
    }

    public void playBtnOnClick(View v) {
        playerService.playMp3File();
        if (fileLoaded) {
            playBtn.setVisibility(View.INVISIBLE);
            pauseBtn.setVisibility(View.VISIBLE);
        }
    }

    public void pauseBtnOnClick(View v) {
        playerService.pauseMp3File();
        pauseBtn.setVisibility(View.INVISIBLE);
        playBtn.setVisibility(View.VISIBLE);
    }

    public void loopBtnOnClick(View v) {
        if (fileLoaded) {
            playerService.loopMp3File();
            if (!loopOn) {
                loopBtn.setImageResource(R.drawable.ic_loop_green);
                loopOn = true;
            } else {
                loopBtn.setImageResource(R.drawable.ic_loop_blue);
                loopOn = false;
            }
        }
    }

    public void stopBtnOnClick(View v) {
        playerService.stopMp3File();
    }

    private Runnable updateProgressEverySecond = new Runnable() {

        @Override
        public void run() {
            if (!STOP_FREQ_UPDATE && (playerService.getMp3Duration() != 0)) {
                int prog = playerService.getMp3Progress() *
                        progressSeekbar.getMax() / playerService.getMp3Duration();

                progressSeekbar.setProgress(prog);

                if(playerService.getState().equals(MP3Player.MP3PlayerState.PLAYING)) {
                    progressSeekbar.postDelayed(updateProgressEverySecond, 1000);
                }

                progressTextView.setText(playerService.getFormattedDuration(playerService.getMp3Progress()));
                durationTextView.setText(playerService.getFormattedDuration(playerService.getMp3Duration()));
                Log.d("MYAPP", "DURATION = " +
                        playerService.getFormattedDuration(playerService.getMp3Progress()) +
                " OUT OF = " + playerService.getFormattedDuration(playerService.getMp3Duration()));
            }
        }
    };

    @Override
    protected void onStop() {
        Log.d("MYAPP", "MainActivity onStop()");
        super.onStop();
        Log.d("MYAPP", "State: " + playerService.getState().name());
    }

    @Override
    protected void onDestroy() {
        Log.d("MYAPP", "MainActivity onDestroy()");
        super.onDestroy();
        Log.d("MYAPP", "State: " + playerService.getState().name());
    }
}
