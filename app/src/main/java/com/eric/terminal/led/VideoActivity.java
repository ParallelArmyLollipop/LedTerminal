package com.eric.terminal.led;

import android.app.Activity;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Display;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.io.IOException;

/**
 * Created by Eric on 2016/6/2.
 */
public class VideoActivity extends Activity {
    private Display currDisplay;
    private SurfaceView surfaceView;
    private SurfaceHolder holder;
    private MediaPlayer player;
    private int vWidth,vHeight;
    //private boolean readyToPlay = false;

    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_video);

        surfaceView = (SurfaceView)this.findViewById(R.id.video_surface);
        //给SurfaceView添加CallBack监听
        holder = surfaceView.getHolder();
        holder.addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                // 当SurfaceView中的Surface被创建的时候被调用
                //在这里我们指定MediaPlayer在当前的Surface中进行播放
                player.setDisplay(holder);
                //在指定了MediaPlayer播放的容器后，我们就可以使用prepare或者prepareAsync来准备播放了
                player.prepareAsync();


            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
                // 当Surface尺寸等参数改变时触发
                Log.v("Surface Change:::", "surfaceChanged called");
            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
                Log.v("Surface Destory:::", "surfaceDestroyed called");
            }
        });
        //为了可以播放视频或者使用Camera预览，我们需要指定其Buffer类型
        holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

        //下面开始实例化MediaPlayer对象
        player = new MediaPlayer();
        player.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                // 当MediaPlayer播放完成后触发
                Log.v("Play Over:::", "onComletion called");
                player.reset();
                VideoActivity.this.finish();
            }
        });
        player.setOnErrorListener(new MediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(MediaPlayer mp, int whatError, int extra) {
                Log.v("Play Error:::", "onError called");
                switch (whatError) {
                    case MediaPlayer.MEDIA_ERROR_SERVER_DIED:
                        Log.v("Play Error:::", "MEDIA_ERROR_SERVER_DIED");
                        break;
                    case MediaPlayer.MEDIA_ERROR_UNKNOWN:
                        Log.v("Play Error:::", "MEDIA_ERROR_UNKNOWN");
                        break;
                    default:
                        break;
                }
                return false;
            }
        });
        player.setOnInfoListener(new MediaPlayer.OnInfoListener() {
            @Override
            public boolean onInfo(MediaPlayer mp, int whatInfo, int extra) {
                // 当一些特定信息出现或者警告时触发
                switch(whatInfo){
                    case MediaPlayer.MEDIA_INFO_BAD_INTERLEAVING:
                        break;
                    case MediaPlayer.MEDIA_INFO_METADATA_UPDATE:
                        break;
                    case MediaPlayer.MEDIA_INFO_VIDEO_TRACK_LAGGING:
                        break;
                    case MediaPlayer.MEDIA_INFO_NOT_SEEKABLE:
                        break;
                }
                return false;
            }
        });
        player.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                player.start();//播放视频
            }
        });
        player.setOnSeekCompleteListener(new MediaPlayer.OnSeekCompleteListener() {
            @Override
            public void onSeekComplete(MediaPlayer mp) {
                // seek操作完成时触发
                Log.v("Seek Completion", "onSeekComplete called");
            }
        });
        player.setOnVideoSizeChangedListener(new MediaPlayer.OnVideoSizeChangedListener() {
            @Override
            public void onVideoSizeChanged(MediaPlayer mp, int width, int height) {
                // 当video大小改变时触发
                //这个方法在设置player的source后至少触发一次
                Log.v("Video Size Change", "onVideoSizeChanged called");
            }
        });
        Log.v("Begin:::", "surfaceDestroyed called");
        //然后指定需要播放文件的路径，初始化MediaPlayer
        String dataPath = Environment.getExternalStorageDirectory().getPath()+"/Download/aa.avi";
        //String dataPath = Environment.getExternalStorageDirectory().getPath()+"/Download/bb.mp4";
        try {
            player.setDataSource(dataPath);
            Log.v("Next:::", "surfaceDestroyed called");
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (IllegalStateException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        //然后，我们取得当前Display对象
        currDisplay = this.getWindowManager().getDefaultDisplay();
    }
}
