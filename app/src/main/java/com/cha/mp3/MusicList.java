package com.cha.mp3;

import android.annotation.TargetApi;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListView;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;

public class MusicList extends AppCompatActivity {
    ML_Adapter adapter;
    private int m_cp;
    private ListView listView;
    private int now;
    private int top;
    //MediaPlayer music;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_music_list);

        Intent intent = getIntent();
        m_cp = intent.getIntExtra("CP", 0);




        ContentResolver contentResolver = getContentResolver();

        String[] ColumnsList = {MediaStore.Audio.Media.ARTIST, MediaStore.Audio.Media.TITLE, MediaStore.Audio.Media.DATA, MediaStore.Audio.Media.ALBUM_ID};

        final Cursor cursor2 = contentResolver.query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, ColumnsList, null, null, null);
        cursor2.moveToFirst();
        adapter = new ML_Adapter(cursor2, getApplicationContext(), R.layout.ml_item);
        listView = (ListView) findViewById(R.id.listView2);
        listView.setAdapter(adapter);
        listView.setSelectionFromTop(m_cp-2,0);//마지막 스크롤 장소 저장


        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                if(m_cp==i){
                    finish();
                }

                cursor2.moveToPosition(i);
                String path = cursor2.getString(cursor2.getColumnIndex(MediaStore.Audio.Media.DATA));
                Log.d("패쓰 ", path);

                //--------------------------------------------------------------------------------
                Intent intent = new Intent();
                intent.putExtra("PATH", path);
                intent.putExtra("TITLE", cursor2.getString(cursor2.getColumnIndex(MediaStore.Audio.Media.TITLE)));
                intent.putExtra("ALBUM", cursor2.getLong(cursor2.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID)));
                intent.putExtra("POSITION", i);
                setResult(RESULT_OK, intent);
                finish();
                //--------------------------------------------------------------------------------

            }

        });


    }


}

