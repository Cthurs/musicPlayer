package com.cha.mp3;


import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import java.io.IOException;
import java.io.Serializable;
import java.util.concurrent.TimeUnit;


public class MainActivity extends Activity implements Serializable{
    private Button b5;
    private TextView tx1, tx2, tx3;
    private ImageView iv;

    private MediaPlayer mediaPlayer;
    private double startTime = 0;
    private double finalTime = 0;
    private Handler myHandler = new Handler();

    private int forwardTime = 5000;//5초 후
    private int backwardTime = 5000;//5초 전
    private SeekBar seekbar;

    private double[] save = new double[100];
    private double[] save2 = new double[100];
    private int count = 0;
    private int count2 = 0;

    private int bc = 3;
    private int c=0;
    private int sw=1;

    private Thread thread;
    private Handler handler;
    private Runnable task;

    private double start_a;
    private double end_a;
    private double term_a;

    private int currentPosition = 0;

    private TextView tv_title;
    private ImageView album;

    private Cursor cursor2;//음악 인포를 담은 커서
    private String mi_tile;
    private String mi_artist;
    private long mi_album;
    private int size;
    private String path;
    private Uri aw = Uri.parse("content://media/external/audio/albumart");
    private Uri aa;


    //DB
    private Cursor cursor;
    private MyDB mydb;
    private SQLiteDatabase db;

    private boolean stopbtn;//정지눌렀을 때 재생시 그자리에서 재생

    private int isplay=0;//정지 재생 switch

    private ImageButton play, next, prev ,musiclist;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        stopbtn=true;

        //마지막 곡 저장
        mydb = new MyDB(getApplicationContext());
        db = mydb.getReadableDatabase();
        String sql = "SELECT * FROM currentP;";
        cursor = db.rawQuery(sql, null);

        cursor.moveToFirst();
        currentPosition = cursor.getInt(cursor.getColumnIndex("cp"));

        //---------------------------------------------------------------------------------------------------------------
        //음악리스트 호출 / 뮤직인포 첫 곡으로 초기화
        ContentResolver contentResolver = getContentResolver();

        String[] ColumnsList = {MediaStore.Audio.Media.ARTIST, MediaStore.Audio.Media.TITLE, MediaStore.Audio.Media.DATA, MediaStore.Audio.Media.ALBUM_ID};

        cursor2 = contentResolver.query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, ColumnsList, null, null, null);
        cursor2.moveToPosition(currentPosition);

        size=cursor2.getCount();//음악 갯수 호출
        Log.d("커런트포지션 ", currentPosition+"");
        Log.d("사이즈확인 ", size+ "");

        //---------------------------------------------------------------------------------------------------------------

/********************************************************************************************************************/
        //저장버튼
        Button btn = (Button) findViewById(R.id.btn_save);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //북마크 저장
                Intent intent = new Intent(getApplicationContext(), BookmarkSave.class);
                intent.putExtra("TITLE", mi_tile );
                intent.putExtra("START",startTime);
                intent.putExtra("PATH", path);
                intent.putExtra("CP", currentPosition);
                startActivity(intent);
            }
        });

        //조회버튼
        Button btn2 = (Button) findViewById(R.id.btn_see);
        btn2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //북마크 리스트 호출
                Intent intent = new Intent(getApplicationContext(), BookmarkList.class);
                startActivityForResult(intent, 1);
            }
        });
        //음악리스트
        musiclist = (ImageButton)findViewById(R.id.btn_musiclist);

        musiclist.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), MusicList.class);
                intent.putExtra("CP", currentPosition);
                startActivityForResult(intent, 2);
            }
        });
/**********************************************************************************************************/



        play = (ImageButton)findViewById(R.id.btn_play);
        next = (ImageButton)findViewById(R.id.btn_next);
        prev = (ImageButton)findViewById(R.id.btn_prev);


        b5 = (Button) findViewById(R.id.button5);//구간반복

        tx1 = (TextView) findViewById(R.id.textView2);//현재 재생 시간
        tx2 = (TextView) findViewById(R.id.textView3);//총 재생 시간
        tx3 = (TextView) findViewById(R.id.textView4);//노래 제목

        iv = (ImageView) findViewById(R.id.imageView);//앨범아트

        music_info(currentPosition);
        aa = ContentUris.withAppendedId(aw, mi_album);
        iv.setImageURI(aa);
        tx3.setText(mi_artist+" - "+mi_tile);


        mediaPlayer = MediaPlayer.create(this, R.raw.song);
        mediaPlayer.setLooping(true);//무한반복 설정
        seekbar = (SeekBar) findViewById(R.id.seekBar);
        seekbar.setClickable(false);

        b5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {//구간반복 버튼

                c = bc % 3;
                startTime = mediaPlayer.getCurrentPosition();
                Log.d("구간반복 버튼 클릭 sw, bc, c ", sw+", "+bc+", "+ c);
                switch (c) {
                    case 0://첫클릭
                        Toast.makeText(getApplicationContext(), "구간반복 시작", Toast.LENGTH_SHORT).show();
                        save[count] = startTime;//A구간 저장
                        count++;
                        break;

                    case 1://두번째 클릭
                        sw = 1;
                        save2[count2] = startTime;//B구간 저장
                        count2++;

                        start_a = save[count - 1];
                        end_a = save2[count2 - 1];
                        term_a = save2[count2 - 1] - save[count - 1];

                        mediaPlayer.seekTo((int) start_a);//반복
                        thread = new Thread(task);
                        thread.start();

                        break;

                    case 2://세번째 클릭
                        Toast.makeText(getApplicationContext(), "구간반복끝", Toast.LENGTH_SHORT).show();
                        break;
                }
                    bc++;
            }
        });


        //시커바 조종
        seekbar.setMax(mediaPlayer.getDuration());//노래의 총재생시간값
        seekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // TODO Auto-generated method stub
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // TODO Auto-generated method stub
            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                // TODO Auto-generated method stub
                if (fromUser) {
                    if (sw==1&&c == 1) {
                        sw=1;
                        c=0;
                        bc++;
                        Toast.makeText(getApplicationContext(), "구간반복끝", Toast.LENGTH_SHORT).show();
                    }
                    else if(sw==2&&c==1){
                        sw=1;
                        c=0;
                        bc=3;
                        Toast.makeText(getApplicationContext(), "구간반복끝", Toast.LENGTH_SHORT).show();

                    }
                    mediaPlayer.seekTo(progress);
                }

            }
        });
        //재생버튼
        play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (isplay) {
                    case 0:
                        playSong(path);
                        stopbtn = true;
                        break;
                    case 1:
                        Toast.makeText(getApplicationContext(), "정지", Toast.LENGTH_SHORT).show();
                        mediaPlayer.pause();
                        play.setImageResource(R.drawable.play);
                        stopbtn = false;
                        isplay = 0;
                        break;
                }
            }
        });

        //이전곡
        prev.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                stopbtn=true;
                preSong();
            }
        });
        prev.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                //5초전 버튼
                int temp = (int) startTime;

                if ((temp - backwardTime) > 0) {
                    startTime = startTime - backwardTime;
                    mediaPlayer.seekTo((int) startTime);
                } else {
                    mediaPlayer.seekTo(0);
                }
                return true;
            }
        });

        //다음곡
        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                stopbtn = true;
                nextSong();

            }
        });
        next.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                int temp = (int) startTime;

                if ((temp + forwardTime) <= finalTime) {
                    startTime = startTime + forwardTime;
                    mediaPlayer.seekTo((int) startTime);
                } else {
                    mediaPlayer.seekTo((int) finalTime);
                }
                return true;
            }
        });

        //핸들러
        handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                if (msg.what == 1) {
                    mediaPlayer.seekTo((int) start_a);//반복
                    Log.d("시작지점 ", start_a + "");
                }
            }
        };
        //스레드
        task = new Runnable() {
            public void run() {
                while (null != thread && !thread.isInterrupted()) {
                    try {
                        if (c != 1) {
                            return;
                        } else if (c == 1) {
                            Thread.sleep((long) term_a);
                        }
                    } catch (InterruptedException e) {
                    }

                    if (c == 1) {
                        handler.sendEmptyMessage(1);
                    }
                }
                thread = null;
            }
        };

    }//온크리에이트 중괄호


    //재생시간
    private Runnable UpdateSongTime = new Runnable() {
        public void run() {
            startTime = mediaPlayer.getCurrentPosition();
            tx1.setText(String.format("%02d : %02d",

                            TimeUnit.MILLISECONDS.toMinutes((long) startTime),
                            TimeUnit.MILLISECONDS.toSeconds((long) startTime) -
                                    TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.
                                            toMinutes((long) startTime)))
            );
            seekbar.setProgress((int) startTime);
            myHandler.postDelayed(this, 100);
        }
    };



    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch(requestCode) {
            case 1:
                if (resultCode ==RESULT_OK) {



                    //북마크 리스트 리절트
                    if (c == 1)
                        Toast.makeText(getApplicationContext(), "구간반복끝", Toast.LENGTH_SHORT).show();
                    c = 0;
                    bc = 3;

                    mi_tile = data.getStringExtra("BM_TITLE");
                    path = data.getStringExtra("BM_PATH");
                    start_a = data.getDoubleExtra("BM_START", 1020);
                    currentPosition = data.getIntExtra("BM_CP", 0);
                    playSong(path);
                    music_info(currentPosition);
                    mediaPlayer.seekTo((int) start_a);
                }

                break;
            case 2:
                if(resultCode==RESULT_OK){
                    sw=1;
                    c=0;
                    bc=3;
                    mediaPlayer.pause();
                    path = data.getStringExtra("PATH");
                    currentPosition = data.getIntExtra("POSITION",1);
                    stopbtn=true;
                    playSong(path);

                }

        }
    }

    private void music_info(int mi_position) {
        cursor2.moveToPosition(mi_position);
        path = cursor2.getString(cursor2.getColumnIndex(MediaStore.Audio.Media.DATA));
        mi_tile = cursor2.getString(cursor2.getColumnIndex(MediaStore.Audio.Media.TITLE));
        mi_artist = cursor2.getString(cursor2.getColumnIndex(MediaStore.Audio.Media.ARTIST));
        mi_album = cursor2.getLong(cursor2.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID));
    }

    private void nextSong() {
        sw=1;
        c=0;
        bc=3;
        if (currentPosition+1 >= size) {
            currentPosition = 0;
            cursor2.moveToPosition(currentPosition);
            String s = cursor2.getString(cursor2.getColumnIndex(MediaStore.Audio.Media.DATA));
            playSong(s);
        }
        else {
            currentPosition++;
            cursor2.moveToPosition(currentPosition);
            String s = cursor2.getString(cursor2.getColumnIndex(MediaStore.Audio.Media.DATA));
            playSong(s);
        }
    }

    private void preSong() {
        sw=1;
        c=0;
        bc=3;

        if (currentPosition-1 < 0) {
            currentPosition = size-1;
            cursor2.moveToPosition(currentPosition);
            String s = cursor2.getString(cursor2.getColumnIndex(MediaStore.Audio.Media.DATA));
            playSong(s);
        }
        else {

            currentPosition--;
            cursor2.moveToPosition(currentPosition);
            String s = cursor2.getString(cursor2.getColumnIndex(MediaStore.Audio.Media.DATA));
            playSong(s);
        }
    }

    private void playSong(String songPath) {

        if (stopbtn) {//만약 정지 버튼을 누른뒤 재생버튼을 누른다면 초기화 하지 않음
            mediaPlayer.reset();

            try {
                mediaPlayer.setDataSource(songPath);
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                mediaPlayer.prepare();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        play.setImageResource(R.drawable.pause);
        isplay = 1;
        stopbtn = true;

        //------------------------------------------------------------------------------------------------------------
        //현재 재생 포지션 저장
        db = mydb.getWritableDatabase();
        String sql = "UPDATE currentP SET cp=" + currentPosition + " WHERE _id=1;";
        Log.d("업데이트 sql 확인 ", sql);
        db.execSQL(sql);
        sql = "SELECT * FROM currentP;";
        cursor = db.rawQuery(sql, null);


        //------------------------------------------------------------------------------------------------------------
        //음악 정보 부분
        music_info(currentPosition);
        tv_title = (TextView) findViewById(R.id.textView4);
        tv_title.setText(mi_artist + " - " + mi_tile);
        album = (ImageView) findViewById(R.id.imageView);
        aa = ContentUris.withAppendedId(aw, mi_album);
        album.setImageURI(aa);
        //------------------------------------------------------------------------------------------------------------
        //재생
        mediaPlayer.start();

        finalTime = mediaPlayer.getDuration();
        startTime = mediaPlayer.getCurrentPosition();

        seekbar.setMax((int) finalTime);

        //총재생 시간
        tx2.setText(String.format("%02d : %02d",
                        TimeUnit.MILLISECONDS.toMinutes((long) finalTime),
                        TimeUnit.MILLISECONDS.toSeconds((long) finalTime) -
                                TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes((long) finalTime)))
        );

        seekbar.setProgress((int) startTime);
        myHandler.postDelayed(UpdateSongTime, 100);
        //------------------------------------------------------------------------------------------------------------

        //자동으로 다음곡
        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            public void onCompletion(MediaPlayer arg0) {
                nextSong();
            }
        });

    }


    @Override


    public boolean onKeyDown(final int pKeyCode, final KeyEvent pKeyEvent) {

        if(pKeyCode==KeyEvent.KEYCODE_BACK){
            AlertDialog.Builder alertDlg = new AlertDialog.Builder(this);

            alertDlg.setMessage("종료 하시겠습니까?");
            alertDlg.setNegativeButton("예", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    System.exit(0);
                }
            });
            alertDlg.setPositiveButton("아니요", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            });
            AlertDialog alert = alertDlg.create();
            alert.setTitle("뒤로가기 버튼 이벤트");
            alert.show();
        }

        return false;
    }

}//메인엑티비티 중괄호





