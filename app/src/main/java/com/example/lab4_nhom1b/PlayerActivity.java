package com.example.lab4_nhom1b;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;

import java.io.File;
import java.util.ArrayList;

public class PlayerActivity extends AppCompatActivity {
    private TextView txtSongName, tvStart, tvEnd, tvShowLyric,tv_lyric;
    private ImageView img_music;
    private SeekBar seekBar;
    private Button btn_Pre, btn_Play, btn_Next, btn_Stop;
        String songName;
    public static final String EXTRA_NAME = "song_name";
    static MediaPlayer mediaPlayer;
    int position;
    ArrayList<File> mySongs;
    Thread updateSeekbar;




    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId() == android.R.id.home){
            onBackPressed();
            mediaPlayer.stop();
            cancelNotification();
        }

        return super.onOptionsItemSelected(item);
    }



    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);

        getSupportActionBar().setTitle("Music App Playing");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        init();

        if(mediaPlayer != null) {
            mediaPlayer.start();
            mediaPlayer.release();
        }
        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();

        mySongs = (ArrayList)bundle.getParcelableArrayList("songs");
        String sName = intent.getStringExtra("songname");
        position = bundle.getInt("pos", 0);
        txtSongName.setSelected(true);
       setSong();
//

//

        btn_Play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mediaPlayer.isPlaying()){
                    btn_Play.setBackgroundResource(R.drawable.ic_play);
                    mediaPlayer.pause();

                }
                else {
                    btn_Play.setBackgroundResource(R.drawable.ic_pause);
                    mediaPlayer.start();
                }
            }
        });
        btn_Next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mediaPlayer.stop();
                mediaPlayer.release();
                position = (position+1)%mySongs.size();
               setSong();
                findLyric();

            }
        });
        btn_Pre.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mediaPlayer.stop();
                mediaPlayer.release();
                position = ((position-1)<0)?(mySongs.size()-1):position-1;
                setSong();
                findLyric();

            }
        });

        btn_Stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mediaPlayer.stop();
//                mediaPlayer.release();
                cancelNotification();
                startActivity(new Intent(PlayerActivity.this, MainActivity.class));
            }
        });


        tvShowLyric.setOnClickListener(new View.OnClickListener() {
            Boolean flag = false;
            @Override
            public void onClick(View view) {
                findLyric();
                if(flag) {
//                    tv_lyric.setAlpha(1);
                    img_music.setVisibility(View.VISIBLE);
                    tv_lyric.setVisibility(View.GONE);
                    flag = false;
                }else {
                    tv_lyric.setVisibility(View.VISIBLE);
                    img_music.setVisibility(View.GONE);
                    flag = true;
                }
            }
        });



    }
    public void findLyric() {

        String findlyric = mySongs.get(position).getName();
        tv_lyric.setText(findlyric+ "\n No Lyric");

    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public void setSong() {
        Uri uri = Uri.parse(mySongs.get(position).toString());
        songName = mySongs.get(position).getName();
        txtSongName.setText(songName);
        mediaPlayer = MediaPlayer.create(getApplicationContext(),uri);
        mediaPlayer.start();

        clickStartService();


        updateSeekbar = new Thread(){
            @Override
            public void run() {
                int totalDuration = mediaPlayer.getDuration();
                int currentPosition = 0;

                while(currentPosition < totalDuration) {

                    try{
                        sleep(500);
                        currentPosition = mediaPlayer.getCurrentPosition();
                        seekBar.setProgress(currentPosition);
                    }
                    catch (InterruptedException | IllegalStateException e){
                        e.printStackTrace();
                    }
                }

            }
        };
        seekBar.setMax(mediaPlayer.getDuration());
        updateSeekbar.start();
        seekBar.getProgressDrawable().setColorFilter(getResources().getColor(android.R.color.system_accent2_800), PorterDuff.Mode.MULTIPLY);
        seekBar.getThumb().setColorFilter(getResources().getColor(android.R.color.system_accent2_800), PorterDuff.Mode.SRC_IN);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                mediaPlayer.seekTo(seekBar.getProgress());
            }
        });

        String endTime = creatime(mediaPlayer.getDuration());
        tvEnd.setText(endTime);
        final Handler handler = new Handler();
        final int delay = 10;

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                String currentTime = creatime(mediaPlayer.getCurrentPosition());
                tvStart.setText(currentTime);
                handler.postDelayed(this,delay);
            }
        },delay);

        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                btn_Next.performClick();
            }
        });
    }

    public String creatime(int duration) {
        String time = "";
        int min = duration/1000/60;
        int sec = duration/1000%60;

        time = time+min+ ":";
        if(sec<10){
            time+="0";
        }
        time+=sec;
        return time;

    }
    private void init() {
        txtSongName = findViewById(R.id.txt_Song);
        img_music = findViewById(R.id.img_music);
        seekBar = findViewById(R.id.seekbar);
        tvStart = findViewById(R.id.tv_start);
        tvEnd = findViewById(R.id.tv_End);
        tvShowLyric = findViewById(R.id.tv_showLyric);
        tv_lyric =findViewById(R.id.tv_lyric);
        btn_Play = findViewById(R.id.btn_play);
        btn_Pre = findViewById(R.id.btn_pre);
        btn_Next = findViewById(R.id.btn_next);
        btn_Stop = findViewById(R.id.btn_stop);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public void clickStartService() {
        String s = mySongs.get(position).getName();
        Notification notification =
                new NotificationCompat.Builder(this, s)
                .setSmallIcon(R.drawable.ic_music)
                .setContentTitle(s)
                .setContentText("is playing")
                .setColor(getResources().getColor(R.color.notification))
                .setSound(null)
                .build();

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notificationManager.notify(1, notification);

    }
    public void cancelNotification() {
        String ns = Context.NOTIFICATION_SERVICE;
        NotificationManager manager = (NotificationManager)
                getApplicationContext().getSystemService(ns);
        manager.cancel(1);
    }


}