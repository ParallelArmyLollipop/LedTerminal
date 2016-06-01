package com.eric.terminal.led;

import android.app.Activity;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnBufferingUpdateListener;
import android.net.Uri;
import android.os.Bundle;
import android.view.MotionEvent;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.MediaController.MediaPlayerControl;

public class PlayerActivity extends Activity {
	MediaController mController;
	MediaPlayer mPlayer;
	ImageView coverImage;
	int bufferPercent = 0;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_player);

		coverImage = (ImageView) findViewById(R.id.coverImage);
		mController = new MediaController(this);
		mController.setAnchorView(findViewById(R.id.root));
	}

	@Override
	public void onResume() {
		super.onResume();
		mPlayer = new MediaPlayer();
		mPlayer.setOnBufferingUpdateListener(bul);
		// 设置音频数据源
		try {
			String s = "android.resource://"+getPackageName()+"/"+R.raw.a;
			//String s = "android.resource://"+getPackageName();
			mPlayer.setDataSource(this, Uri.parse(s));
			mPlayer.prepare();
		} catch (Exception e) {
			e.printStackTrace();
		}
		// 设置专辑封面的图片
		//coverImage.setImageResource(R.drawable.contact_gr);
		mController.setMediaPlayer(new MediaPlayerControl() {
			
			@Override
			public void start() {
				mPlayer.start();
			}
			
			@Override
			public void seekTo(int pos) {
				mPlayer.seekTo(pos);
			}
			
			@Override
			public void pause() {
				mPlayer.pause();
			}
			
			@Override
			public boolean isPlaying() {
				return mPlayer.isPlaying();
			}
			
			@Override
			public int getDuration() {
				return mPlayer.getDuration();
			}
			
			@Override
			public int getCurrentPosition() {
				return mPlayer.getCurrentPosition();
			}
			
			@Override
			public int getBufferPercentage() {
				return bufferPercent;
			}
			
			@Override
			public int getAudioSessionId() {
				return mPlayer.getAudioSessionId();
			}
			
			@Override
			public boolean canSeekForward() {
				return true;
			}
			
			@Override
			public boolean canSeekBackward() {
				return true;
			}
			
			@Override
			public boolean canPause() {
				return true;
			}
		});
		mController.setEnabled(true);
	}
	
	@Override
	public void onPause() {
		super.onPause();
		mPlayer.release();
		mPlayer = null;
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		mController.show();
		return super.onTouchEvent(event);
	}
	
	OnBufferingUpdateListener bul = new OnBufferingUpdateListener() {
		
		@Override
		public void onBufferingUpdate(MediaPlayer mp, int percent) {
			bufferPercent = percent;
		}
	};

}
