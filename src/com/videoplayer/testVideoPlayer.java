package com.videoplayer;

import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashMap;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import com.proxy.HttpGetProxy;
import com.proxy.Utils;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnPreparedListener;
import android.os.Bundle;
import android.os.Environment;
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
	static private final int PREBUFFER_SIZE= 4*1024*1024;
	
	private VideoView mVideoView;
	private MediaController mediaController;
	private HttpGetProxy proxy;
	private long startTimeMills;
	private String videoUrl ="http://10.0.0.221/jiuzai.mp4";
	private String id=null;
	private long waittime=8000;//等待缓冲时间
	@Override
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		setContentView(R.layout.main);
		setTitle("玩转 Android MediaPlayer之视频预加载(优化)---hellogv");

		//创建预加载视频文件存放文件夹
		new File( getBufferDir()).mkdirs();
		
		// 初始化VideoView
		mediaController = new MediaController(this);
		mVideoView = (VideoView) findViewById(R.id.surface_view);
		mVideoView.setMediaController(mediaController);
		mVideoView.setOnPreparedListener(mOnPreparedListener);

		// 初始化代理服务器
		proxy = new HttpGetProxy(getBufferDir(),// 预加载视频文件存放路径
				PREBUFFER_SIZE,// 预加载体积
				10);// 预加载文件上限

		id = System.currentTimeMillis() + "";
		try {
			proxy.startDownload(id, videoUrl, true);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		delayToStartPlay.sendEmptyMessageDelayed(0, waittime);

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
			Log.e(TAG,"等待缓冲时间:"+waittime+",首次缓冲时间:"+duration);
		}
	};
	
	private Handler delayToStartPlay = new Handler() {
		public void handleMessage(Message msg) {
			startTimeMills=System.currentTimeMillis();
			String proxyUrl = proxy.getLocalURL(id);
			mVideoView.setVideoPath(proxyUrl);
		}
	};
	
	private Handler showController = new Handler() {
		public void handleMessage(Message msg) {
			mediaController.show(0);
		}
	};
	
	static public String getBufferDir(){
		String bufferDir = Environment.getExternalStorageDirectory()
		.getAbsolutePath() + "/ProxyBuffer/files";
		return bufferDir;
	}
}