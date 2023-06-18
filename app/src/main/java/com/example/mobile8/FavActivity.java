package com.example.mobile8;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class FavActivity extends AppCompatActivity {
    private ListView favoriteSongsListView;
    private SharedPreferences sharedPreferences;
    private Set<String> favoriteSongsSet;
    private MediaPlayer mediaPlayer;
    ArrayList<object> items;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);
        items=new ArrayList<>();
        favoriteSongsListView = findViewById(R.id.list_view);
        objectAdapter adapter = new objectAdapter(FavActivity.this, R.layout.activity_fav,items);

        favoriteSongsListView.setAdapter(adapter);

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        favoriteSongsSet = sharedPreferences.getStringSet("favoriteSongs", new HashSet<>());

        for (String songPath : favoriteSongsSet) {
            File file = new File(songPath);
            items.add(new object(file.getName(),R.drawable.music));

        }

        adapter.notifyDataSetChanged();

        favoriteSongsSet = sharedPreferences.getStringSet("favoriteSongs", new HashSet<>());
        List<String> favoriteSongsList = new ArrayList<>(favoriteSongsSet);

        favoriteSongsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String songPath = favoriteSongsList.get(position);
                Intent NN = new Intent(FavActivity.this,MainActivity.class);
                startActivity(NN);
                playSong(songPath);
            }
        });
        favoriteSongsListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int listItem, long l) {

                new AlertDialog.Builder(FavActivity.this)
                        .setTitle("are you sure you want to delete it ?")
                        .setPositiveButton("yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                items.remove(listItem);
                                adapter.notifyDataSetChanged();
                            }
                        }).setNegativeButton("no", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.dismiss();
                            }
                        }).create().show();

                return true;
            }
        });

    }

    private void playSong(String songPath) {
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }

        mediaPlayer = MediaPlayer.create(this, Uri.parse(songPath));
        mediaPlayer.start();
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