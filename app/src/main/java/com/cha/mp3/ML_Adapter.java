package com.cha.mp3;

import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.net.Uri;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Created by C on 2015-12-09.
 */
public class ML_Adapter extends BaseAdapter{
    MediaPlayer music;
    Cursor cursor;
    Context context;
    int ml_item;
    LayoutInflater inflater;
    public ML_Adapter(Cursor cursor2,Context context, int ml_item) {
        this.cursor = cursor2;
        this.context = context;
        this.ml_item = ml_item;

        inflater = (LayoutInflater)context.getSystemService(context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return cursor.getCount();
    }

    @Override
    public Object getItem(int i) {
        cursor.moveToPosition(i);
        return cursor;
    }
    @Override
    public long getItemId(int i) {
        return i;
    }


    @Override
    public View getView(int i, View view, ViewGroup parent) {
        if(view==null){
            view = inflater.inflate(ml_item,null);
        }
        cursor.moveToPosition(i);
        TextView textView = (TextView)view.findViewById(R.id.tv_title);
        textView.setText(cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Playlists.Members.TITLE)));
        textView.setTextColor(Color.BLACK);

        TextView textView2 = (TextView)view.findViewById(R.id.tv_singer);
        textView2.setTextColor(Color.BLACK);
        textView2.setText(cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST)));

        ImageView imageView = (ImageView)view.findViewById(R.id.ml_iv);
        long albumId = cursor.getLong(cursor.getColumnIndex(android.provider.MediaStore.Audio.Media.ALBUM_ID));
        Uri sArtworkUri = Uri.parse("content://media/external/audio/albumart");
        Uri sAlbumArtUri = ContentUris.withAppendedId(sArtworkUri, albumId);

        imageView.setImageURI(sAlbumArtUri);

        return view;
    }
}
