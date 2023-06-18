package com.example.mobile8;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MainActivity extends AppCompatActivity {
    private static final int PERMISSION_REQUEST_READ_EXTERNAL_STORAGE = 1;
    private List<String> musicFiles;
    private MediaPlayer mediaPlayer;
    private int currentIndex = 0;
    private int playbackPosition = 0;
    private TextView songname;
    private ImageView favoris;
    private boolean Favorite = false;
    private SharedPreferences sharedPreferences;
    private Set<String> favoriteSongsSet;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);



        songname = findViewById(R.id.sName);
        favoris = findViewById(R.id.fav);

        ImageView playBtn = findViewById(R.id.play);
        playBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                favoris.setVisibility(View.VISIBLE);
                if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                    mediaPlayer.pause();
                    playbackPosition = mediaPlayer.getCurrentPosition();
                    playBtn.setImageResource(R.drawable.pause);


                } else {
                    if (checkPermission()) {
                        if (musicFiles != null && musicFiles.size() > 0) {
                            if (mediaPlayer != null) {
                                mediaPlayer.release();
                                mediaPlayer = null;
                            }
                            playMusic(musicFiles.get(currentIndex));
                            playBtn.setImageResource(R.drawable.play);


                        } else {
                            new LoadMusicTask().execute();
                        }
                    } else {
                        requestPermission();
                    }
                }
            }
        });

        favoris.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Favorite = !Favorite;
                Favicon();
                if (Favorite) {
                    addfav(musicFiles.get(currentIndex));
                } else {
                    removefav(musicFiles.get(currentIndex));
                }
            }
        });
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        favoriteSongsSet = sharedPreferences.getStringSet("favoriteSongs", new HashSet<>());

        ImageView prevBtn = findViewById(R.id.prev);
        prevBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (musicFiles != null && musicFiles.size() > 0) {
                    currentIndex = (currentIndex - 1 + musicFiles.size()) % musicFiles.size();
                    if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                        mediaPlayer.stop();
                        mediaPlayer.release();
                        mediaPlayer = null;
                    }
                    playbackPosition = 0;
                    playMusic(musicFiles.get(currentIndex));
                    playBtn.setImageResource(R.drawable.pause);
                }
            }
        });
        ImageView nextBtn = findViewById(R.id.next);
        nextBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (musicFiles != null && musicFiles.size() > 0) {
                    currentIndex = (currentIndex + 1) % musicFiles.size();
                    if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                        mediaPlayer.stop();
                        mediaPlayer.release();
                        mediaPlayer = null;
                    }
                    playbackPosition = 0;
                    playMusic(musicFiles.get(currentIndex));
                    playBtn.setImageResource(R.drawable.pause);

                }
            }
        });

  

    }
    private boolean checkPermission() {
        int result = ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_EXTERNAL_STORAGE);
        return result == PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermission() {
        ActivityCompat.requestPermissions(
                this,
                new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                PERMISSION_REQUEST_READ_EXTERNAL_STORAGE
        );
    }

    private void loadMusicFiles() {
        musicFiles = new ArrayList<>();

        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        String[] projection = {MediaStore.Audio.Media.DATA, MediaStore.Audio.Media.TITLE};
        String selection = MediaStore.Audio.Media.IS_MUSIC + "!= 0";
        Cursor cursor = getContentResolver().query(uri, projection, selection, null, null);
        if (cursor != null) {
            while (cursor.moveToNext()) {
                String filePath = cursor.getString(0);
                String songName = cursor.getString(1);
                musicFiles.add(filePath);
            }
            cursor.close();
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_READ_EXTERNAL_STORAGE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                new LoadMusicTask().execute();
            }
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.mmenu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.favo:
                Intent first = new Intent(this, FavActivity.class);
                startActivity(first);
                return true;
            case R.id.down:
                Intent second = new Intent(MainActivity.this, DownActivity.class);
                startActivity(second);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void Favicon() {
        ImageView favoriteIcon = findViewById(R.id.fav);
        if (Favorite) {
            favoriteIcon.setImageResource(R.drawable.heart);
        } else {
            favoriteIcon.setImageResource(R.drawable.tour);
        }
    }

    private void addfav(String songName) {
        favoriteSongsSet.add(songName);
        saveFavoriteSongs();
        Toast.makeText(this, "Added to favorites", Toast.LENGTH_SHORT).show();
    }

    private void removefav(String songName) {
        favoriteSongsSet.remove(songName);
        saveFavoriteSongs();
        Toast.makeText(this, "Removed from favorites", Toast.LENGTH_SHORT).show();
    }

    private void saveFavoriteSongs() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putStringSet("favoriteSongs", favoriteSongsSet);
        editor.apply();
    }


    private void playMusic(String musicFilePath) {
        mediaPlayer = new MediaPlayer();
        try {
            mediaPlayer.setDataSource(musicFilePath);
            mediaPlayer.prepare();
            mediaPlayer.seekTo(playbackPosition);
            mediaPlayer.start();
            updateName();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void updateName() {
        if (musicFiles != null && musicFiles.size() > 0) {
            String songPath = musicFiles.get(currentIndex);
            String songName = songPath.substring(songPath.lastIndexOf("/") + 1);
            songname.setText(songName);
        } else {
            songname.setText("");
        }
    }




    //asynchrone mode

    private class LoadMusicTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... voids) {
            loadMusicFiles();
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            if (musicFiles != null && musicFiles.size() > 0) {
                currentIndex = 0;
                playMusic(musicFiles.get(currentIndex));
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