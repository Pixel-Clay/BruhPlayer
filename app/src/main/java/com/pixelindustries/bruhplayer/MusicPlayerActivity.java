package com.pixelindustries.bruhplayer;

import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.ColorUtils;
import androidx.palette.graphics.Palette;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

public class MusicPlayerActivity extends AppCompatActivity {

    TextView titleTv, currentTimeTv, totalTimeTv, artistTv;
    SeekBar seekBar;
    ImageView pausePlay, nextBtn, previousBtn, musicIcon;
    ArrayList<AudioModel> songsList;
    AudioModel currentSong;
    MediaPlayer mediaPlayer = MyMediaPlayer.getInstance();
    RelativeLayout background;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_music_player);

        titleTv = findViewById(R.id.song_title);
        currentTimeTv = findViewById(R.id.current_time);
        totalTimeTv = findViewById(R.id.total_time);
        seekBar = findViewById(R.id.seek_bar);
        pausePlay = findViewById(R.id.pause_play);
        nextBtn = findViewById(R.id.next);
        previousBtn = findViewById(R.id.previous);
        musicIcon = findViewById(R.id.music_icon_big);
        background = findViewById(R.id.backgroundColor);
        artistTv = findViewById(R.id.song_artist);

        titleTv.setSelected(true);
        artistTv.setSelected(true);


        songsList = (ArrayList<AudioModel>) getIntent().getSerializableExtra("LIST");

        setResourcesWithMusic();

        updateDecorations();


        MusicPlayerActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (mediaPlayer != null) {
                    seekBar.setProgress(mediaPlayer.getCurrentPosition());
                    currentTimeTv.setText(convertToMMSS(mediaPlayer.getCurrentPosition() + ""));

                    if (mediaPlayer.isPlaying()) {
                        pausePlay.setImageResource(R.drawable.ic_baseline_pause_circle_outline_24);
                    } else {
                        pausePlay.setImageResource(R.drawable.ic_baseline_play_circle_outline_24);
                    }

                }
                new Handler().postDelayed(this, 100);
            }
        });

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (mediaPlayer != null && fromUser) {
                    mediaPlayer.seekTo(progress);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });


    }

    private void updateDecorations() {

        try {
            Bitmap albumArt = createAlbumArt(songsList.get(MyMediaPlayer.currentIndex).getPath());
            musicIcon.setImageBitmap(albumArt);
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            Bitmap bitmap = ((BitmapDrawable) musicIcon.getDrawable()).getBitmap();
            Palette.from(bitmap).generate(palette -> {
                int vibrant = palette.getVibrantColor(0x000000); // <=== color you want
                int vibrantLight = palette.getLightVibrantColor(0x000000);
                int vibrantDark = palette.getDarkVibrantColor(0x000000);
                int muted = palette.getMutedColor(0x000000);
                int mutedLight = palette.getLightMutedColor(0x000000);
                int mutedDark = palette.getDarkMutedColor(0x000000);

                if (mutedDark > Color.parseColor("#FEFEFE"))
                    mutedDark = ColorUtils.blendARGB(vibrantDark, Color.BLACK, 0.2f);


                titleTv.setTextColor(vibrant);
                artistTv.setTextColor(vibrantLight);
                currentTimeTv.setTextColor(vibrant);
                totalTimeTv.setTextColor(vibrant);
                seekBar.setThumbTintList(ColorStateList.valueOf(vibrant));
                seekBar.setProgressTintList(ColorStateList.valueOf(vibrantLight));
                seekBar.setProgressBackgroundTintList(ColorStateList.valueOf(muted));
                background.setBackgroundColor(mutedDark);
            });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    void setResourcesWithMusic() {
        currentSong = songsList.get(MyMediaPlayer.currentIndex);

        titleTv.setText(currentSong.getTitle());
        artistTv.setText(getArtistName(songsList.get(MyMediaPlayer.currentIndex).getPath()));

        totalTimeTv.setText(convertToMMSS(currentSong.getDuration()));

        pausePlay.setOnClickListener(v -> pausePlay());
        nextBtn.setOnClickListener(v -> playNextSong());
        previousBtn.setOnClickListener(v -> playPreviousSong());

        playMusic();


    }


    private void playMusic() {

        mediaPlayer.reset();
        try {
            mediaPlayer.setDataSource(currentSong.getPath());
            mediaPlayer.prepare();
            mediaPlayer.start();
            seekBar.setProgress(0);
            seekBar.setMax(mediaPlayer.getDuration());
            updateDecorations();
        } catch (IOException e) {
            e.printStackTrace();
        }


    }

    private void playNextSong() {

        if (MyMediaPlayer.currentIndex == songsList.size() - 1)
            return;
        MyMediaPlayer.currentIndex += 1;
        mediaPlayer.reset();
        setResourcesWithMusic();

    }

    private void playPreviousSong() {
        if (MyMediaPlayer.currentIndex == 0)
            return;
        MyMediaPlayer.currentIndex -= 1;
        mediaPlayer.reset();
        setResourcesWithMusic();
    }

    private void pausePlay() {
        if (mediaPlayer.isPlaying())
            mediaPlayer.pause();
        else
            mediaPlayer.start();
    }


    public static String convertToMMSS(String duration) {
        Long millis = Long.parseLong(duration);
        return String.format("%02d:%02d",
                TimeUnit.MILLISECONDS.toMinutes(millis) % TimeUnit.HOURS.toMinutes(1),
                TimeUnit.MILLISECONDS.toSeconds(millis) % TimeUnit.MINUTES.toSeconds(1));
    }

    public  Bitmap createAlbumArt(final String filePath) {
        Bitmap bitmap = null;
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        try {
            retriever.setDataSource(filePath);
            byte[] embedPic = retriever.getEmbeddedPicture();
            bitmap = BitmapFactory.decodeByteArray(embedPic, 0, embedPic.length);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                retriever.release();
            } catch (Exception e2) {
                e2.printStackTrace();
            }
        }
        return bitmap;
    }

    public static String getArtistName(final String filePath) {
        String artist = null;
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        retriever.setDataSource(filePath);

        try {
            artist = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                retriever.release();
            } catch (Exception e2) {
                e2.printStackTrace();
            }
        }
        return artist;
    }
}