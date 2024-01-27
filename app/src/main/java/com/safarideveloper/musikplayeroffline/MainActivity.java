package com.safarideveloper.musikplayeroffline;

import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SeekBar;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.IOException;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private ListView listView;
    private ArrayList<String> songList;
    private MediaPlayer mediaPlayer;
    private SeekBar seekBar;
    private ImageView playPauseImageView;
    private boolean isPlaying = false;
    private int selectedPosition = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        listView = findViewById(R.id.listView);
        seekBar = findViewById(R.id.seekBar);
        playPauseImageView = findViewById(R.id.playPauseImageView);
        mediaPlayer = new MediaPlayer();

        // Check for runtime permission (for Android 6.0 and above)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            checkPermission();
        } else {
            loadSongs();
        }

        // Set item click listener for the ListView
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // Stop the currently playing song
                stopSong();

                // Play the selected song
                playSong(position);
            }
        });

        // Set the seek bar change listener
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
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

        // Set click listener for play/pause button
        playPauseImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isPlaying) {
                    pauseSong();
                } else {
                    playSong(selectedPosition);
                }
            }
        });



    }

    // Function to load songs from storage using MediaStore
    private void loadSongs() {
        songList = new ArrayList<>();

        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        String[] projection = {MediaStore.Audio.Media.TITLE};

        Cursor cursor = getContentResolver().query(uri, projection, null, null, null);

        if (cursor != null) {
            int titleColumnIndex = cursor.getColumnIndex(MediaStore.Audio.Media.TITLE);

            while (cursor.moveToNext()) {
                // Check if the column index is valid
                if (titleColumnIndex != -1) {
                    String title = cursor.getString(titleColumnIndex);
                    songList.add(title);
                }
            }

            cursor.close();
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, songList);
        listView.setAdapter(adapter);
    }

    // Function to play the selected song
    // Function to play the selected song
    // Function to play the selected song
    // Fungsi untuk memutar lagu yang dipilih
    private void playSong(int position) {
        try {
            mediaPlayer.reset();

            // Periksa apakah posisi adalah indeks yang valid di songList
            if (position >= 0 && position < songList.size()) {
                Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                String[] projection = {MediaStore.Audio.Media._ID, MediaStore.Audio.Media.DATA};
                Cursor cursor = getContentResolver().query(uri, projection, MediaStore.Audio.Media.TITLE + "=?", new String[]{songList.get(position)}, null);

                if (cursor != null && cursor.moveToFirst()) {
                    int dataIndex = cursor.getColumnIndex(MediaStore.Audio.Media.DATA);

                    // Periksa apakah indeks kolom valid
                    if (dataIndex != -1) {
                        String path = cursor.getString(dataIndex);

                        // Log path file
                        Log.d("Path File", "Path: " + path);

                        cursor.close();

                        // Gunakan Uri.parse untuk membuat Uri dari path file
                        Uri fileUri = Uri.parse("file://" + path);

                        // Atur sumber data menggunakan Uri
                        mediaPlayer.setDataSource(this, fileUri);

                        mediaPlayer.prepare();
                        mediaPlayer.start();

                        // Perbarui seek bar
                        seekBar.setMax(mediaPlayer.getDuration());
                        updateSeekBar();

                        // Ganti gambar tombol putar/jeda
                        playPauseImageView.setImageResource(R.drawable.ic_pause);
                        isPlaying = true;

                        // Perbarui posisi yang dipilih
                        selectedPosition = position;
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }




    // Function to stop the currently playing song
    private void stopSong() {
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.stop();
            mediaPlayer.reset();
            // Change play/pause button image
            playPauseImageView.setImageResource(R.drawable.ic_play);
            isPlaying = false;
        }
    }

    // Function to pause the currently playing song
    private void pauseSong() {
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
            // Change play/pause button image
            playPauseImageView.setImageResource(R.drawable.ic_play);
            isPlaying = false;
        }
    }

    // Function to update seek bar progress
    private void updateSeekBar() {
        seekBar.setProgress(mediaPlayer.getCurrentPosition());

        if (mediaPlayer.isPlaying()) {
            Runnable runnable = new Runnable() {
                @Override
                public void run() {
                    updateSeekBar();
                }
            };
            seekBar.postDelayed(runnable, 1000);
        }
    }

    // Check for runtime permission
    // Check for runtime permission
    // Check for runtime permission
    @RequiresApi(api = Build.VERSION_CODES.M)
    private void checkPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
        } else {
            Log.d("Permission", "Permission already granted");
            loadSongs();
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d("Permission", "Permission granted");
                loadSongs();
            } else {
                Log.d("Permission", "Permission denied");
                // Permission denied. Handle accordingly.
            }
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }
}
