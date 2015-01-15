package com.soniq.tvremotecontrolserver;

import java.io.BufferedInputStream;
import java.util.TimerTask;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Instrumentation;
import android.app.Notification;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PixelFormat;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.StrictMode;
import android.os.SystemClock;
import android.util.Base64;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

import com.soniq.utils.MyUtils;
import com.soniq.utils.PackageUtils;

public class MainService extends Service {
	private String _localip;
	private String _sendip;
	private boolean isget;

	private ImageView _mouseCursor = null;
	
	private DownloadThread _downloadThread = null;

	private final static int MSG_SHOW_TIPS = 2000;
	private final static int MSG_DO_TEST = 3000;
	private final static int MSG_MOUSE_MOVE = 4000;
	private final static int MSG_MOUSE_CLICK = 4001;
	private final static int MSG_MOUSE_CHECK = 4002;

	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
    public void onCreate() {
        super.onCreate();
        
        startForeground(1, new Notification());//提高服务优先级 防止清理内存时被杀掉
        
        Log.v(MainData.TAG, "soniqtvremotecontrolserver Start");        
        if (android.os.Build.VERSION.SDK_INT > 9) {
        	//严苛模式
		    StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
		    StrictMode.setThreadPolicy(policy);
		}        
        
        //启动守护线程
		new DeamonThread().start();
        
        _localip = MainData.getLocalIpAddress();//获取本机IP
        Log.v(MainData.TAG, "localip:" + _localip);

        _sendip = "255.255.255.255";
        
        Log.v(MainData.TAG, "sendip:" + _sendip);
//        MainData.sendMessage(_sendip, MainData.sysCopy(MainData.getSendByte(MainData.iptoInt(_localip), 1)));
//        MainData.sendMessage(_sendip, MainData.CMD_SERVER_INIT, null);
        String serverName = MainData.get_profile_string_value(this, MainData.PROFILE_SERVER_NAME, MainData.DEFAULT_SERVER_NAME);
		String content = String.format("ip=%s;name=%s;id=%s", _localip, serverName, MainData.getDeviceId(this));
		MainData.sendMessage(_sendip, MainData.CMD_SERVER_INIT, content);//广播消息
        
        isget = true;
        new Thread(new Runnable() {
            @Override
            public void run() {
                getMessage();
            }
        }).start();
        
        
        
        // 每次启动时检查是否有更新
        ClientUpgrade cu = new ClientUpgrade(this.getApplicationContext());
        cu.startCheckVersion(null);
        
        _downloadThread =  new DownloadThread(this.getApplicationContext());
        
        _downloadThread.start();
        
	}
	
	@Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // TODO Auto-generated method stub         
        flags =  START_STICKY;
        
        return super.onStartCommand(intent, flags, startId);
    }
	
	@Override  
    public void onDestroy() {
        super.onDestroy();        
       
        
        _downloadThread.bExitFlag = true;
        
        Log.v(MainData.TAG, "soniqtvremotecontrolserver Stop");
        isget = false;
//        android.os.Process.killProcess(android.os.Process.myPid());
	}
	
	private void getMessage() {
        byte[] buf = new byte[64];
        try {
            DatagramSocket getSocket;
            getSocket = new DatagramSocket(MainData.UDP_PORT);
            while(isget) {
                DatagramPacket getPacket = new DatagramPacket(buf, buf.length);
                Log.v(MainData.TAG, "receive...");
                getSocket.receive(getPacket);
                String clientIp = getPacket.getAddress().getHostAddress();
                Log.v(MainData.TAG, "client=" + clientIp);
                byte[] data = getPacket.getData();
                int value = MainData.byteArrayToInt(data, 0);
                Log.v(MainData.TAG, "getMessage:" + String.valueOf(value));
                switch(value) {
                	case MainData.CMD_CLIENT_INIT://初始化
                	{
                        String serverName = MainData.get_profile_string_value(this, MainData.PROFILE_SERVER_NAME, MainData.DEFAULT_SERVER_NAME);
                		String content = String.format("ip=%s;name=%s;id=%s", _localip, serverName,
                				MainData.getDeviceId(this));
                		MainData.sendMessage(clientIp, MainData.CMD_SERVER_INIT, content);
                	}

                		break;
                	case MainData.CMD_CLIENT_PING:
                	{
                		MainData.sendMessage(clientIp, MainData.CMD_SERVER_PING, "");
                	}
                		break;
                	case MainData.CMD_CLIENT_KEY:
                	{
                		int length = MainData.byteArrayToInt(data, 4);
                		if( length == 4 )
                		{
                			int keyCode = MainData.byteArrayToInt(data, 8);
                            Log.v(MainData.TAG, "CMD_CLIENT_KEY:" + String.valueOf(keyCode));
                			MainData.sendKeyCodeToSystem(keyCode);
                		}
                	}
                	break;
                	case MainData.CMD_CLIENT_SETNAME://设置名称
                	{
                		int length = MainData.byteArrayToInt(data, 4);
                		if( length > 0 )
                		{
                			byte[] bc = new byte[length];
                			System.arraycopy(data, 8, bc, 0, length);
                			
                			String name = new String(bc, "utf-8");
                            Log.v(MainData.TAG, "CMD_CLIENT_SETNAME:" +name);
                            MainData.save_profile_string_value(this, MainData.PROFILE_SERVER_NAME, name);
                    		String content = String.format("ip=%s;name=%s;id=%s", _localip, name,MainData.getDeviceId(this));
                    		MainData.sendMessage(clientIp, MainData.CMD_SERVER_INIT, content);
                		}
                	}
                	break;
                	case MainData.CMD_CLIENT_OPEN_APP:
                	{
                		int length = MainData.byteArrayToInt(data, 4);
                		if( length > 0 )
                		{
                			byte[] bc = new byte[length];
                			System.arraycopy(data, 8, bc, 0, length);
                			
                			String name = new String(bc, "utf-8");
                            Log.v(MainData.TAG, "CMD_CLIENT_OPEN_APP:" +name);
                            
                            openApp(name);
                		}
                	}
                	break;
                	case MainData.CMD_CLIENT_UNINSTALL_APP:
                	{
                		int length = MainData.byteArrayToInt(data, 4);
                		if( length > 0 )
                		{
                			byte[] bc = new byte[length];
                			System.arraycopy(data, 8, bc, 0, length);
                			
                			String name = new String(bc, "utf-8");
                            Log.v(MainData.TAG, "CMD_CLIENT_OPEN_APP:" +name);
                            
                            uninstallApp(name);
                		}
                	}
                	break;
//                	case MainData.CMD_CLIENT_MOUSEMOVE:
//                	{
//                		int length = MainData.byteArrayToInt(data, 4);
//                		if( length > 0 )
//                		{
//                			byte[] bc = new byte[length];
//                			System.arraycopy(data, 8, bc, 0, length);
//                			
//                			String offsetString = new String(bc, "utf-8");
//                            Log.v(MainData.TAG, "CMD_CLIENT_MOUSEMOVE:" +offsetString);
////                            this.doMouseMove(name);
//                            sendMessage(MSG_MOUSE_MOVE, offsetString);
//                		}
//                	}
//                	break;

                	case MainData.CMD_CLIENT_MOUSECLICK:
                	{
                		doMouseClick("");
                	}
                	break;
//                    case MainData.CMD_SERVER_OPEN:
//                    	MainData.sendMessage(_sendip, MainData.sysCopy(MainData.getSendByte(MainData.iptoInt(_localip), 1)));
//                        break;
//                    case MainData.CMD_SEND_KEY_EVENT:
//                        int KeyCode = MainData.byteArrayToInt(data, 4);
//                        Log.v(MainData.TAG, "SEND_KEY_EVENT:" + String.valueOf(KeyCode));
//                        MainData.sendKeyCodeToSystem(KeyCode);
//                        break;

                }
            }
            getSocket.close();
        } catch (SocketException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
	
	private static byte[] bitmapToBytes(Bitmap bitmap){
		  if (bitmap == null) {
		     return null;
		  }
		  final ByteArrayOutputStream os = new ByteArrayOutputStream();
		  // 将Bitmap压缩成PNG编码，质量为100%存储
		  bitmap.compress(Bitmap.CompressFormat.PNG, 100, os);//除了PNG还有很多常见格式，如jpeg等。
		  return os.toByteArray();
		 }	

	
	 public static String getContentFromClient(DataInputStream dis)
	 {		 
		 String content = "";
		 
		 try{
				int length = dis.readInt();
				if( length > 0 )
				{
					// 接收
					byte[] buf = new byte[length];
	
					int left = length;
					int recved = 0;
	
					boolean b = true;
					while (left > 0) {
						int n = dis.read(buf, recved, left);
						if (n <= 0) {
							b = false;
							break;
						}
	
						recved += n;
						left -= n;
					}

					if (b) 
						content = new String(buf);
				}
		 }
		 catch(Exception e)
		 {
			 e.printStackTrace();
		 }
		 
		 return content;
	 }
		
	 private void doMouseClick(String offsetString)
	 {
			try
			{
				initMouseView();
				
				 WindowManager.LayoutParams wlp = (WindowManager.LayoutParams)_mouseCursor.getLayoutParams();
					 
					 int x = wlp.x;
					 int y = wlp.y;
					 
				
				Instrumentation inst=new Instrumentation();
		
				inst.sendPointerSync(MotionEvent.obtain(SystemClock.uptimeMillis(),SystemClock.uptimeMillis(), MotionEvent.ACTION_DOWN, x, y, 0));		
				inst.sendPointerSync(MotionEvent.obtain(SystemClock.uptimeMillis(),SystemClock.uptimeMillis(), MotionEvent.ACTION_UP, x, y, 0));		
				
				writeLog("server: do click....x=" + x + " y=" + y);
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
	 
	 }
	 
	 private void doMouseMove(String offsetString)
	 {
		 
		 String[] ss = offsetString.split(",");
		 if( ss.length == 2 )
		 {
			 float x = Float.parseFloat(ss[0]);
			 float y = Float.parseFloat(ss[1]);
			 
			 
			 initMouseView();
			 
			 WindowManager.LayoutParams wlp = (WindowManager.LayoutParams)_mouseCursor.getLayoutParams();
			 
				WindowManager wm = (WindowManager)this.getBaseContext().getSystemService(Context.WINDOW_SERVICE); 
		        DisplayMetrics dm = new DisplayMetrics();
		        wm.getDefaultDisplay().getMetrics(dm);
		        
			 
			 
			 int x1 = wlp.x;
			 int y1 = wlp.y;
			 
			 x1 += x;
			 y1 += y;
			 
			 
			 if( x1 < 0 )
				 x1 = 0;
			 if( y1 < 0 )
				 y1 = 0;
			 
		        if( x1 > dm.widthPixels )
		        	x1 = dm.widthPixels;
		        
		        if( y1 > dm.heightPixels )
		        	y1 = dm.heightPixels;
			 
			 
			 
			 wlp.x = x1;
			 wlp.y = y1;
			 Log.v(MainData.TAG, "x=" + x1 + " y=" + y1);
			 wm.updateViewLayout(_mouseCursor, wlp);
			 

//			 int curX = location[0];
//			 int curY = location[1];
//			 writeLog("server: curX=" + curX + " curY=" + curY);
//			 
//			 
//			 curX += (int)x;
//			 curY += (int)y;
//			 
//			 writeLog("server: set x=" + curX + " y=" + curY);
			 
//			 WindowManager.LayoutParams wlp1 = new WindowManager.LayoutParams();
//			 wlp1.x = curX;//(int)curX;
//			 wlp1.y = curY;//(int)curY;
//			 s = String.format("server: 5 %d, %d", wlp1.x, wlp1.y);
//			 writeLog(s);
////			 _mouseCursor.setLayoutParams(wlp1);
//			 _mouseCursor.setX(100);
//			 _mouseCursor.setY(200);

//				_mouseCursor.requestLayout();
			 
			 
			 
		 }
		 
	 }
	 
	 private void on_mouse_move(DataInputStream dis, DataOutputStream dos)
	 {
		 try
		 {
			 String offsetString = getContentFromClient(dis);
			 
			 writeLog("server: " + offsetString);
			 
			 startCheckMouse();
			 _lastMouseTime = MainData.getTickCount();
			 
			 sendMessage(MSG_MOUSE_MOVE, offsetString);
		 }
		 catch(Exception e)
		 {
			 
		 }
	 }
	 
	 private void on_device_bind(DataInputStream dis, DataOutputStream dos)
	 {
		 try
		 {
			 String infoString = getContentFromClient(dis);
			 
			 writeLog("bind string: " + infoString);
			 
			 
			 String userid = MainData.getTagValue(infoString, "userid");
			 
			 
             MainData.save_profile_string_value(this, MainData.PROFILE_BIND_USERID, userid);
			 
//			dos.writeInt(MainData.CMD_SERVER_BIND);
             String serverName = MainData.get_profile_string_value(this, MainData.PROFILE_SERVER_NAME, MainData.DEFAULT_SERVER_NAME);

			String response = String.format("result=yes;devid=%s;name=%s;", MainData.getDeviceId(this), serverName);
			int len = response.getBytes().length;
			dos.writeInt(len);
			writeLog("len=" + len);
			dos.write(response.getBytes(), 0, response.getBytes().length);
		}
		 catch(Exception e)
		 {
			 
		 }
	 }
	 	 
	 
	 private void on_mouse_click(DataInputStream dis, DataOutputStream dos)
	 {
		 try
		 {
			 String offsetString = getContentFromClient(dis);
			 
			 doMouseClick(offsetString);
			 
			 startCheckMouse();
			 _lastMouseTime = MainData.getTickCount();
//			 writeLog("server: " + offsetString);
//			 
//			 sendMessage(MSG_MOUSE_CLICK, offsetString);
		 }
		 catch(Exception e)
		 {
			 
		 }
	 }
	 
	 
	 private boolean uninstallApp(String packageName){
			try{
				
				if( packageName != null && packageName.length() > 0 )
				{
					writeLog("uninstall " + packageName);
					
					ApplicationInfo appInfo = this.getPackageManager().getApplicationInfo(packageName, 0);
					
					//applicationInfo.loadLabel(
//					getPackageManager()).toString()
					String appName = appInfo.loadLabel(this.getPackageManager()).toString();
					PackageUtils.silentUninstall(this.getApplicationContext(), packageName,appName, new PackageUtils.InstallApkCallback() {
						
						@Override
						public void onUninstallFinished(String packageName, String appName, int resultCode) {
							// TODO Auto-generated method stub
							
							
							String fmt_ok = getApplicationContext().getResources().getString(R.string.format_uninstall_ok);
							String fmt_error = getApplicationContext().getResources().getString(R.string.format_uninstall_failed);
							String s = "";
							if( resultCode == 1 )
								s = String.format(fmt_ok, appName);
							else
								s = String.format(fmt_error, appName);
							
							writeLog(s);
							showTips(s);
							
						}

						@Override
						public void onInstallFinished(String packageName,
								int resultCode, int requestCode) {
						}
					});
					
					// 常规
//					Uri packageURI = Uri.parse("package:" + packageName);
//					Intent intent = new Intent(Intent.ACTION_DELETE, packageURI);
//					startActivity(intent);	

//					Intent LaunchIntent = getPackageManager().getLaunchIntentForPackage(packageName);  
//				    startActivity(LaunchIntent);
					
					return true;
				}
				
			}
			catch(Exception e)
			{
				
			}
		 
			
			return false;
	 }

		// 获取应用列表
		private void on_uninstall_app(DataInputStream dis, DataOutputStream dos)
		{
			String result = "ERROR";
			try{
				
				String packageName = getContentFromClient(dis);
				if( packageName != null && packageName.length() > 0 )
				{
					writeLog("uninstall " + packageName);
					
					ApplicationInfo appInfo = this.getPackageManager().getApplicationInfo(packageName, 0);
					
					//applicationInfo.loadLabel(
//					getPackageManager()).toString()
					String appName = appInfo.loadLabel(this.getPackageManager()).toString();
					PackageUtils.silentUninstall(this.getApplicationContext(), packageName,appName, new PackageUtils.InstallApkCallback() {
						
						@Override
						public void onUninstallFinished(String packageName, String appName, int resultCode) {
							// TODO Auto-generated method stub
							
							String s = "";
							if( resultCode == 1 )
								s = String.format("%s 卸载成功！", appName);
							else
								s = String.format("%s 卸载失败！", appName);
							
							writeLog(s);
							showTips(s);
							
						}

						@Override
						public void onInstallFinished(String packageName,
								int resultCode, int requestCode) {
						}
					});
					
					// 常规
//					Uri packageURI = Uri.parse("package:" + packageName);
//					Intent intent = new Intent(Intent.ACTION_DELETE, packageURI);
//					startActivity(intent);	

//					Intent LaunchIntent = getPackageManager().getLaunchIntentForPackage(packageName);  
//				    startActivity(LaunchIntent);
				}
				
			    result = "OK";
				dos.writeInt(result.getBytes().length);
				dos.write(result.getBytes(), 0, result.getBytes().length);

			}
			catch(Exception e)
			{
				
			}
			
		}	 
		
		
		private boolean openApp(String packageName)
		{
			try{
				if( packageName != null && packageName.length() > 0 )
				{
				    Intent LaunchIntent = getPackageManager().getLaunchIntentForPackage(packageName);  
				    startActivity(LaunchIntent);
				    return true;
				}
			}
			catch(Exception e)
			{
				
			}
			
			
			return false;
		}
	
	// 获取应用列表
	private void on_open_app(DataInputStream dis, DataOutputStream dos)
	{
		String result = "ERROR";
		try{
			
			String packageName = getContentFromClient(dis);
			if( packageName != null && packageName.length() > 0 )
			{
			    Intent LaunchIntent = getPackageManager().getLaunchIntentForPackage(packageName);  
			    startActivity(LaunchIntent);
			}
			
		    result = "OK";
			dos.writeInt(result.getBytes().length);
			dos.write(result.getBytes(), 0, result.getBytes().length);

		}
		catch(Exception e)
		{
			
		}
		
	}

	// 获取应用列表
	private void on_get_app_list1(DataInputStream dis, DataOutputStream dos)
	{
		JSONArray appList = new JSONArray();
		
		try{
		
		List<PackageInfo> packages = this.getPackageManager().getInstalledPackages(0);
		for(int i = 0; i < packages.size(); i++ )
		{
			PackageInfo pi = packages.get(i);

			
			boolean add = false;
			
//			ApplicationInfo.FLAG
			if( (pi.applicationInfo.flags & ApplicationInfo.FLAG_UPDATED_SYSTEM_APP ) != 0 )
				add = true;
			if( ( pi.applicationInfo.flags  & ApplicationInfo.FLAG_SYSTEM) <= 0 )
				add = true;
			
			if( add )
			{
				String appName = pi.applicationInfo.loadLabel(
						getPackageManager()).toString();
				
				writeLog("pkg: " + appName + " flags=" + pi.applicationInfo.flags);
				String packageName = pi.packageName;
				
				String versionName  = pi.versionName;
				int versionCode = pi.versionCode;
				
				Drawable appIcon = pi.applicationInfo.loadIcon(getPackageManager());
				BitmapDrawable bd = (BitmapDrawable)appIcon;
				Bitmap bmp = bd.getBitmap();
				byte[] bmp_data = bitmapToBytes(bmp);
				
				String bmp_string = Base64.encodeToString(bmp_data, Base64.DEFAULT);
				
				String dir = pi.applicationInfo.publicSourceDir;
				
				JSONObject obj = new JSONObject();
				obj.put("appName", appName);
				obj.put("packageName", packageName);
				obj.put("versionName", versionName);
				obj.put("versioncode", versionCode);
				obj.put("appIcon", bmp_string);
				
				appList.put(obj);
			}
		}
		
		
		String jsonString = appList.toString();

		Log.v(MainData.TAG, "len=" + jsonString.getBytes().length);
		dos.writeInt(jsonString.getBytes().length);
		dos.write(jsonString.getBytes(), 0, jsonString.getBytes().length);
		}
		catch(Exception e)
		{
			
		}
		
	}

	
	private void on_get_app_list(DataInputStream dis, DataOutputStream dos)
	{
		JSONArray appList = new JSONArray();
		
		try{
		
			PackageManager pm = getPackageManager();
			Intent intent = new Intent(Intent.ACTION_MAIN, null);
			intent.addCategory(Intent.CATEGORY_LAUNCHER);
			List<ResolveInfo> pkgList = pm.queryIntentActivities(intent, 0);
			
			for(int i = 0; i < pkgList.size(); i++ )
			{
				ResolveInfo resInfo = pkgList.get(i);
				String packageName = resInfo.activityInfo.packageName;
				
				if( packageName.equalsIgnoreCase(this.getPackageName()))
					continue;

				PackageInfo  pi = null;
				try{
					pi = pm.getPackageInfo(packageName, 0);
				}
				catch(Exception e)
				{
					continue;
				}


			String uninstall = "no";
			
//			ApplicationInfo.FLAG
			if( (pi.applicationInfo.flags & ApplicationInfo.FLAG_UPDATED_SYSTEM_APP ) != 0 )
				uninstall = "yes";
			if( ( pi.applicationInfo.flags  & ApplicationInfo.FLAG_SYSTEM) <= 0 )
				uninstall = "yes";
			
				String appName = pi.applicationInfo.loadLabel(
						getPackageManager()).toString();
				
				writeLog("pkg: " + appName + " flags=" + pi.applicationInfo.flags);
				
				String versionName  = pi.versionName;
				int versionCode = pi.versionCode;
				
				Drawable appIcon = pi.applicationInfo.loadIcon(getPackageManager());
				BitmapDrawable bd = (BitmapDrawable)appIcon;
				Bitmap bmp = bd.getBitmap();
				byte[] bmp_data = bitmapToBytes(bmp);
				
				String bmp_string = Base64.encodeToString(bmp_data, Base64.DEFAULT);
				
				String dir = pi.applicationInfo.publicSourceDir;
				
				JSONObject obj = new JSONObject();
				obj.put("appName", appName);
				obj.put("packageName", packageName);
				obj.put("versionName", versionName);
				obj.put("versioncode", versionCode);
				obj.put("appIcon", bmp_string);
				obj.put("uninstall", uninstall);
				
				appList.put(obj);
		}
		
		
		String jsonString = appList.toString();

		Log.v(MainData.TAG, "len=" + jsonString.getBytes().length);
		dos.writeInt(jsonString.getBytes().length);
		dos.write(jsonString.getBytes(), 0, jsonString.getBytes().length);
		}
		catch(Exception e)
		{
			
		}
		
	}
	
	
	private void writeLog(String message)
	{
		Log.v(MainData.TAG, message);
	}
	

	private void showTips(String text)
	{
		Message msg = new Message();
		msg.what = MSG_SHOW_TIPS;
		msg.obj = text;
		_handler.sendMessage(msg);
		
	}
	
	private void sendMessage(int what, String text)
	{
		Message msg = new Message();
		msg.what = what;
		msg.obj = text;
		_handler.sendMessage(msg);
	}
	
	
	private void initMouseView()
	{
		if( _mouseCursor != null )
		{
			_mouseCursor.setVisibility(View.VISIBLE);
			return;
		}
		
		WindowManager wm = (WindowManager)this.getBaseContext().getSystemService(Context.WINDOW_SERVICE); 
		ImageView imgView = new ImageView(this.getBaseContext());
		
		imgView.setImageResource(R.drawable.mouse_cursor);
		
		int w = MyUtils.dip2px(this.getBaseContext(), 45);
		int h = MyUtils.dip2px(this.getBaseContext(), 45);
		
		WindowManager.LayoutParams wlp = new WindowManager.LayoutParams(
//				WindowManager.LayoutParams.WRAP_CONTENT,
//				WindowManager.LayoutParams.WRAP_CONTENT,
				w, h,
				WindowManager.LayoutParams.TYPE_SYSTEM_ALERT,
				WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE|
				WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS|
				WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
				PixelFormat.TRANSLUCENT);
		wlp.gravity = Gravity.LEFT | Gravity.TOP;
		
		DisplayMetrics dm = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(dm);
        int x = dm.widthPixels / 2;
        int y = dm.heightPixels / 2;
		
		
		wlp.x = x;
		wlp.y = y;
		wm.addView(imgView, wlp);

		_mouseCursor = imgView;
	}
	
	private void doTest()
	{
		initMouseView();
	}
	
	private void on_test(DataInputStream dis, DataOutputStream dos)
	{
		try
		{
//			Instrumentation inst=new Instrumentation();
//	
//			inst.sendPointerSync(MotionEvent.obtain(SystemClock.uptimeMillis(),SystemClock.uptimeMillis(), MotionEvent.ACTION_DOWN, 240, 400, 0));		
//			inst.sendPointerSync(MotionEvent.obtain(SystemClock.uptimeMillis(),SystemClock.uptimeMillis(), MotionEvent.ACTION_UP, 240, 400, 0));		
			
			writeLog("do test....");
			
			sendMessage(MSG_DO_TEST, null);

			showTips("test...");
			
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	
	private class DeamonThread extends Thread {
		public void run() {
			try {
				ServerSocket server = new ServerSocket(MainData.TCP_PORT);

				while (true) {
					DataInputStream dis = null;
					DataOutputStream dos = null;

					try {
						writeLog("waitting client connect...");
						Socket client = server.accept();

						writeLog("connected!");
						dis = new DataInputStream(new BufferedInputStream(
								client.getInputStream()));
						dos = new DataOutputStream(new BufferedOutputStream(
								client.getOutputStream()));

						int cmd = dis.readInt();

						writeLog("cmd=" + cmd);

						int iret = 1;
//						if (cmd == MainData.CMD_GET_APP_LIST) 
//						{
//							on_get_app_list(dis, dos);
//						}
//						else 
						if( cmd == MainData.CMD_CLIENT_APP_LIST )
						{
							on_get_app_list(dis, dos);
						}
//						else if( cmd == MainData.CMD_OPEN_APP )
//						{
//							on_open_app(dis, dos);
//						}
//						else if( cmd == MainData.CMD_UNINSTALL_APP )
//						{
//							on_uninstall_app(dis, dos);
//						}
//						else if( cmd == MainData.CMD_MOUSE_MOVE )
//						{
//							on_mouse_move(dis, dos);
//						}
						else if( cmd == MainData.CMD_CLIENT_MOUSEMOVE )
						{
							on_mouse_move(dis,dos);
						}
						else if( cmd == MainData.CMD_CLIENT_BIND )
						{
							on_device_bind(dis,dos);
						}
//						else if( cmd == MainData.CMD_MOUSE_CLICK )
//						{
//							on_mouse_click(dis, dos);
//						}
//						else if( cmd == MainData.CMD_TEST )
//						{
//							on_test(dis, dos);
//						}
						else
						{
							dos.writeInt(MainData.CMD_SERVER_INVALID);
//							dos.write(resp.getBytes(), 0, resp.getBytes().length);
						}

						dos.flush();

					} catch (Exception e) {
						e.printStackTrace();
					} finally {
						try{
							if( dis != null )
								dis.close();
							
							if( dos != null )
								dos.close();
						}
						catch(Exception e)
						{
							
						}
					}
				}

			} catch (Exception e) {
				e.printStackTrace();
			}
		}

	}	

	
	Handler _handler = new Handler(){
		@Override
		public void handleMessage(Message msg)
		{
			if( msg.what == MSG_SHOW_TIPS )
			{
				String text = (String)msg.obj;
				Toast.makeText(MainService.this.getApplicationContext(), text, Toast.LENGTH_SHORT).show();
			}
			else if( msg.what == MSG_DO_TEST)
			{
				doTest();
			}
			else if( msg.what == MSG_MOUSE_MOVE )
			{
				String text = (String)msg.obj;
				doMouseMove(text);
			}
			else if( msg.what == MSG_MOUSE_CLICK )
			{
				String text = (String)msg.obj;
				doMouseClick(text);
			}
			else if( msg.what == MSG_MOUSE_CHECK )
			{
				if( _mouseCursor != null )
					_mouseCursor.setVisibility(View.INVISIBLE);
			}
		}
	};
	
	
	TimerTask checkMouseTimeTask = new TimerTask(){

		@Override
		public void run() {
			// TODO Auto-generated method stub
			
			Log.v(MainData.TAG, "check...");
			if( _lastMouseTime > 0 )
			{
				long nowTime = MainData.getTickCount();
				long n = nowTime - _lastMouseTime;
				Log.v(MainData.TAG, "---:" + n);
				if( n >= 5000 )
				{
					// 隐藏鼠标
					sendMessage(MSG_MOUSE_CHECK, "");
				}
				
			}
			
			
		}
		
	};
	
	private Timer _timer = null;
	private long _lastMouseTime = 0;
	
	private void startCheckMouse()
	{
		if( _timer != null )
			return;
		
		_timer = new Timer(true);
		_timer.schedule(checkMouseTimeTask, 5000, 5000);
	}
}
