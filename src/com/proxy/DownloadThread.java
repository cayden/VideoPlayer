package com.proxy;


import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import android.util.Log;
/**
 * 下载模块，支持断点下载
 * @author hellogv
 * 
 */
public class DownloadThread extends Thread {
	static private final String TAG="DownloadThread";
	private String mUrl;
	private String mPath;
	private long mDownloadSize;
	private int mTargetSize;
	private boolean mStop;
	private boolean mDownloading;
	private boolean mStarted;
	private boolean mError;
	
	public DownloadThread(String url, String savePath,int targetSize) {
		mUrl = url;
		mPath = savePath;
		
		//如果文件存在，则继续
		File file=new File(mPath);
		if(file.exists()){
			mDownloadSize =  file.length();
		}else{
			mDownloadSize = 0;
		}
		
		mTargetSize=targetSize;
		mStop = false;
		mDownloading = false;
		mStarted = false;
		mError=false;
	}

	@Override
	public void run() {
		mDownloading = true;
		download();
	}
	
	/** 启动下载线程 */
	public void startThread() {
		if (!mStarted) {
			this.start();

			// 只能启动一次
			mStarted = true;
		}
	}

	/** 停止下载线程*/
	public void stopThread() {
		mStop = true;
	}

	/** 是否正在下载 */
	public boolean isDownloading() {
		return mDownloading;
	}

	/**
	 * 是否下载异常
	 * @return
	 */
	public boolean isError(){
		return mError;
	}
	
	public long getDownloadedSize() {
		return mDownloadSize;
	}

	/** 是否下载成功 */
	public boolean isDownloadSuccessed() {
		return (mDownloadSize != 0 && mDownloadSize >= mTargetSize);
	}

	private void download() {
		//下载成功则关闭
		if(isDownloadSuccessed()){
			Log.i(TAG,"...DownloadSuccessed...");
			return;
		}
		InputStream is = null;
		FileOutputStream os = null;
		if (mStop) {
			return;
		}
		try {
			URL url = new URL(mUrl);
			HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
			urlConnection.setRequestMethod("GET");
			urlConnection.setInstanceFollowRedirects(true);//允许重定向
			is = urlConnection.getInputStream();
			if(mDownloadSize==0){//全新文件
				os = new FileOutputStream(mPath);
				Log.i(TAG,"download file:"+mPath);
			}
			else{//追加数据
				os = new FileOutputStream(mPath,true);
				Log.i(TAG,"append exists file:"+mPath);
			}
			int len = 0;
			byte[] bs = new byte[1024];
			if (mStop) {
				return;
			}
			while (!mStop //未强制停止
					&& mDownloadSize<mTargetSize //未下载足够
					&& ((len = is.read(bs)) != -1)) {//未全部读取
				os.write(bs, 0, len);
				mDownloadSize += len;
			}
		} catch (Exception e) {
			mError=true;
			Log.i(TAG,"download error:"+e.toString()+"");
			Log.i(TAG,Utils.getExceptionMessage(e));
		} finally {
			if (os != null) {
				try {
					os.close();
				} catch (IOException e) {}
			}

			if (is != null) {
				try {
					is.close();
				} catch (IOException e){}
			}
			mDownloading = false;
			
			//清除空文件
			File nullFile = new File(mPath);
			if(nullFile.exists() && nullFile.length()==0)
				nullFile.delete();
			
			Log.i(TAG,"mDownloadSize:"+mDownloadSize+",mTargetSize:"+mTargetSize);
		}
	}
}
