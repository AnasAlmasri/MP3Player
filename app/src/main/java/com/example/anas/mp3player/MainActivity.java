package com.example.anas.mp3player;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {
    MP3Player mp3Player;
    TextView fileNameTextView;
    SeekBar progressSeekbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // initialize external class objects
        mp3Player = new MP3Player();

        // initialize widget controller objects
        fileNameTextView = findViewById(R.id.fileNameTextView);
        progressSeekbar = findViewById(R.id.progressSeekbar);

        // progressSeekbar change listener
        progressSeekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) { }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) { }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 0 && resultCode == Activity.RESULT_OK){
            if ((data != null) && (data.getData() != null)){
                Uri audioFileUri = data.getData();
                mp3Player.load(this, audioFileUri.toString());
                Log.d("MYAPP", audioFileUri.toString());
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
        mp3Player.play();
        MP3Player.MP3PlayerState state = mp3Player.getState();
        Log.i("MYAPP", "State: " + state.toString());
    }

    public void pauseBtnOnClick(View v) {
        mp3Player.pause();
    }

    public void stopBtnOnClick(View v) {
        mp3Player.stop();
    }
}
