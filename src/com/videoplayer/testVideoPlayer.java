package com.videoplayer;

import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Queue;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import com.proxy.C;
import com.proxy.HttpGetProxy;
import com.proxy.Utils;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnPreparedListener;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.MediaController;
import android.widget.SeekBar;
import android.widget.VideoView;

public class testVideoPlayer extends Activity{
	private final static String TAG="testVideoPlayer";
	private VideoView mVideoView;
	private MediaController mediaController;
	private HttpGetProxy proxy;
	private long startTimeMills;
	private String videoUrl ="http://video.cztv.com/video/rzx/201208/15/1345010952759.mp4";
	private boolean enablePrebuffer=true;//预加载开关
	private long waittime=8000;//等待缓冲时间
	@Override
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		setContentView(R.layout.main);
		setTitle("玩转 Android MediaPlayer之视频预加载(优化)---hellogv");
		
		new File(C.getBufferDir()).mkdirs();//创建预加载文件的文件夹
		Utils.clearCacheFile(C.getBufferDir());//清除前面的预加载文件
		
	
		//初始化VideoView
		mediaController=new MediaController(this);
		mVideoView = (VideoView) findViewById(R.id.surface_view);
		mVideoView.setMediaController(mediaController);
		mVideoView.setOnPreparedListener(mOnPreparedListener);
		if (enablePrebuffer) {//使用预加载
			//初始化代理服务器
			proxy = new HttpGetProxy(9110);
			proxy.asynStartProxy();
			String[] urls = proxy.getLocalURL(videoUrl);
			String mp4Url=urls[0];
			videoUrl=urls[1];
			Log.i(TAG, "videoUrl="+videoUrl);
			try {
				String prebufferFilePath = proxy.prebuffer(mp4Url,
						HttpGetProxy.SIZE);
				
				Log.e(TAG, "预加载文件：" + prebufferFilePath);
			} catch (Exception ex) {
				Log.e(TAG, ex.toString());
				Log.e(TAG, Utils.getExceptionMessage(ex));
			}
			delayToStartPlay.sendEmptyMessageDelayed(0,waittime);
		}else//不使用预加载
			delayToStartPlay.sendEmptyMessageDelayed(0,0);
		
		// 一直显示MediaController
		showController.sendEmptyMessageDelayed(0, 1000);
	}
	
	@Override
	public void onStop(){
		super.onStop();
		finish();
		System.exit(0);
	}
	
	private OnPreparedListener mOnPreparedListener=new OnPreparedListener(){

		@Override
		public void onPrepared(MediaPlayer mp) {
			mVideoView.start();
			long duration=System.currentTimeMillis() - startTimeMills;
			Log.e(TAG,"预加载开关:"+enablePrebuffer+ ",等待缓冲时间:"+waittime+",首次缓冲时间:"+duration);
		}
	};
	
	private Handler delayToStartPlay = new Handler() {
		public void handleMessage(Message msg) {
			startTimeMills=System.currentTimeMillis();
			mVideoView.setVideoPath(videoUrl);
		}
	};
	
	private Handler showController = new Handler() {
		public void handleMessage(Message msg) {
			mediaController.show(0);
		}
	};
}