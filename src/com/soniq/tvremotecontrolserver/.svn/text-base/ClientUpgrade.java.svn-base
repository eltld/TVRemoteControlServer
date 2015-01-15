package com.soniq.tvremotecontrolserver;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

import com.soniq.utils.MyUtils;
import com.soniq.utils.PackageUtils;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.util.Log;


public class ClientUpgrade {
	
	private Context _context;
	private Thread _download_thread;
	
	private String _downloadApkUrl = null;
	private String _savePath = null;
	private String _saveFilename = null;
	private boolean _interceptFlag = false;
	private int _progress = 0;
	
	
	public final static String localUpgradeFilename = "upgrade-tmp.apk";
	
	private ClientUpgradeCallback _callback = null;
	
	private final static int DOWN_UPDATE = 1;
	private final static int DOWN_OVER = 2;
	private final static int DOWN_ERROR = 10;
	
	public final static int STATE_ALREADY_NEW_VERSION = 1;
	public final static int STATE_CHECK_ERROR = 2;
	public final static int STATE_UPGRADE = 3;
	
	public ClientUpgrade(Context context)
	{
		_context = context;
	}
	
	public interface ClientUpgradeCallback
	{
		public void onCheckFinished(int state);
	}
	
	private void doCallback(int state)
	{
		if( _callback != null )
			_callback.onCheckFinished(state);
	}
	
	private class CheckVersionAsyncTask extends AsyncTask<String,Integer,String>
	{

		@Override
		protected String doInBackground(String... params) {


			return checkVersion();
		}
		
		@Override
		protected void onPostExecute(String result)
		{
			int state = doParseResult(result);
			doCallback(state);
		}
		
		private int doParseResult(String result)
		{
			if( result ==  null )
				return -1;
			
			String[] ss = result.split("\\|");
			if( ss != null && ss.length > 0 && ss[0].equalsIgnoreCase("NOT") )
				return STATE_ALREADY_NEW_VERSION;
			
			Log.i("", "result=" + ss.length);
			if( ss == null || ss.length != 5 )
				return -2;
			
//			boolean force = false;
//			if( ss[4].equalsIgnoreCase("yes") )
//				force = true;
//			
			
			_downloadApkUrl = ss[3];
			String[] tt = _downloadApkUrl.split("/");
			if( tt.length < 2 )
				return -3;
			
			
			_savePath = "";
			_saveFilename = String.format("%s/%s",_context.getFilesDir(), localUpgradeFilename);//tt[tt.length - 1]);
			MainData.showLog("path=" + _savePath);
			MainData.showLog("filename=" + _saveFilename);
			
			
			downloadApk();

			return STATE_UPGRADE;
		}
	}
	
	
		
	public void startCheckVersion(ClientUpgradeCallback callback)
	{
		_callback = callback;
		
		CheckVersionAsyncTask ct = new CheckVersionAsyncTask();
		ct.execute(null, null, null);
	}	
	
	private void downloadApk()
	{
		_download_thread = new Thread(mDownloadRunnable);
		_download_thread.start();
	}
	
	private Runnable mDownloadRunnable = new Runnable() {

		@Override
		public void run() {
			try
			{
				URL url = new URL(_downloadApkUrl);
				
				HttpURLConnection conn = (HttpURLConnection)url.openConnection();
				conn.connect();
				
				int length = conn.getContentLength();
				InputStream is = conn.getInputStream();
				

				if( _savePath.length() > 0 )
				{
					File file = new File(_savePath);
					if( !file.exists() )
						file.mkdirs();
				}
				
				String apkFilename = _saveFilename;
				File apkFile = new File(apkFilename);
				if( apkFile.exists() )
					apkFile.delete();
				
				FileOutputStream fos = new FileOutputStream(apkFile);
				
//	    		FileOutputStream fos = _context.openFileOutput(_saveFilename, Context.MODE_PRIVATE);
				int count = 0;
				byte buf[] = new byte[1024];
				
				do{
					int numread = is.read(buf);
					count += numread;
					
					MainData.showLog("download: " + count + " total: " + length);
					_progress = (int)(((float)count/length) * 100);
					
//					mHandler.sendEmptyMessage(DOWN_UPDATE);
					if( numread <= 0 )
					{
						mHandler.sendEmptyMessage(DOWN_OVER);
						break;
					}
					
					fos.write(buf, 0, numread);
				}while( !_interceptFlag );
				
				fos.close();
				is.close();
				
			}
			catch(Exception e)
			{
				e.printStackTrace();
				mHandler.sendEmptyMessage(DOWN_ERROR);
			}
		}
		
	};
	
	
	private Handler mHandler = new Handler()
	{
		public void handleMessage(Message msg)
		{
			switch(msg.what)
			{
			case DOWN_UPDATE:
				break;
			case DOWN_OVER:
				installApk();
				break;
			case DOWN_ERROR:
				break;
			default:
				break;
			}
		}
	};
	
	
	
private PackageUtils.InstallApkCallback _installApkCallback = new PackageUtils.InstallApkCallback() {
		
		@Override
		public void onInstallFinished(String packageName, int resultCode, int requestCode) {
			MainData.showLog("**********upgrade:" + packageName + " resultCode:" + resultCode + " requestCode:" + requestCode);
			if( resultCode == 1 )
			{
				// install ok
			}
			else
			{
			}
			
			MyUtils.deleteFile(_saveFilename);
		}

		@Override
		public void onUninstallFinished(String packageName, String appName,
				int resultCode) {
			// TODO Auto-generated method stub
			
		}
	};	
	
	private int installApk()
	{
		File file = new File(_saveFilename);
		if( !file.exists() )
			return 1;
		
    	MyUtils.execCmd("chmod 777 " + file.toString());
		
    	MainData.showLog("install apk:" + _saveFilename);

    	
    	int ret = 0;
    	
		Intent nn = new Intent(Intent.ACTION_VIEW);
		nn.setDataAndType(Uri.parse("file://"+file.toString()), "application/vnd.android.package-archive");
		nn.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK); 
		_context.startActivity(nn);//ForResult(nn, requestCode);

//    	int ret = PackageUtils.installApk(_context, _saveFilename, 0, _installApkCallback);
//    	MainData.showLog("installApk return " + ret);
    	return ret;
	}
	

	public static boolean hasNewVersion(String local_version_string, String server_version_string)
	{
		String[] local_version = local_version_string.split("\\.");
		String[] server_version = server_version_string.split("\\.");
		
//		MainData.showLog("local=" + local_version.length);
//		MainData.showLog("server=" + server_version.length);
		
		
		for(int i = 0; i < local_version.length || i < server_version.length; i++ )
		{
			int cur_code, new_code;
			if( i >= local_version.length )
				cur_code = 0;
			else
				cur_code = Integer.parseInt(local_version[i]);
			
			if( i >= server_version.length )
				new_code = 0;
			else
				new_code = Integer.parseInt(server_version[i]);

			if( new_code > cur_code )
				return true;
			else if( new_code < cur_code )
				return false;
			
		}

		return false;
	}
		
	public String checkVersion()
	{
		try
		{
			String currentVersion = MyUtils.getVersionCode(_context);
			String urlString = WAPI.addGeneralParams(_context,WAPI.WAPI_CHECK_VERSION_URL);
			
			
			MainData.showLog(urlString);
			
			String content = WAPI.get_content_from_remote_url(urlString);
			if( content == null )
				return null;

			ArrayList<String> fieldList = new ArrayList<String>();
			int iret = WAPI.parseVersionInfoResponse(_context, content, fieldList);
			if( iret == 0 && fieldList.size() == 4)
			{
				String version = fieldList.get(0);
				String desc = fieldList.get(1);
				String downloadurl = fieldList.get(2);
				String force = fieldList.get(3);
				
				MainData.showLog("local=" + currentVersion + " server=" + version);
				if( hasNewVersion(currentVersion, version) )
				{
					;//
					String result = String.format("UPGRADE|%s|%s|%s|%s", version, desc, downloadurl, force);
					MainData.showLog(result);
					return result;
				}
				else
				{
					return "NOT";
				}
			}
			
		}
		catch(Exception e)
		{
			
		}
		
		return null;
		
	}
}
