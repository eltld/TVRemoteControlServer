package com.soniq.tvremotecontrolserver;

import android.app.Instrumentation;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.Environment;
import android.provider.Settings.Secure;
import android.util.Log;

import org.apache.http.conn.util.InetAddressUtils;

import com.soniq.utils.MyUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;

public class MainData {
	public static final String TAG = "soniq";
	
    public static final int UDP_PORT = 8078;
    
    public static final int TCP_PORT = 8079;
    
    public static final String DEFAULT_SERVER_NAME = android.os.Build.MODEL; // 手机型号"soniq";
    
    public static final String PROFILE_NAME = "my.config";
    
    public static final String PROFILE_SERVER_NAME = "profile_server_name";
    public static final String PROFILE_BIND_USERID = "profile_bind_userid";
    
    // 新协议
    // server发给client的消息
    
    // 广播服务器地址信息: CMD+length+ip字符串
    public static final int CMD_SERVER_INIT	= 80001001;
    
    public static final int CMD_SERVER_PING	= 80001008;
    
    public static final int CMD_SERVER_BIND = 80004001;
    
    
    public static final int CMD_SERVER_INVALID	= 80008001;
    
    // client给server的消息
    // 广播消息，获取服务器信息
    public static final int CMD_CLIENT_INIT = 10001001;
    public static final int CMD_CLIENT_KEY =  10001002;
    public static final int CMD_CLIENT_SETNAME =  10001003;
    public static final int CMD_CLIENT_PING		= 10001008;
    
    public static final int CMD_CLIENT_MOUSEMOVE =  10002001;
    public static final int CMD_CLIENT_MOUSECLICK =  10002002;

    public static final int CMD_CLIENT_APP_LIST  =   10003001;
    public static final int CMD_CLIENT_OPEN_APP = 	10003002;
    public static final int CMD_CLIENT_UNINSTALL_APP = 10003003;
    
    
    public static final int CMD_CLIENT_BIND = 10004001;
    		

    
//    // udp command
//    public static final int CMD_SERVER_OPEN = 10101001;
//    public static final int CMD_SEND_KEY_EVENT = 1001;
//    public static final int SEND_POINT = 1002;
//    public static final int SEND_POINT_MOVE = 1003;
//    public static final int SEND_POINT_ONCLICK = 1004;
//    
//    // tcp command
//    public static final int CMD_TEST = 2000;
//    public static final int CMD_GET_APP_LIST = 2002;
//    public static final int CMD_OPEN_APP	 = 2003;
//    public static final int CMD_UNINSTALL_APP = 2004;
//    
//    public static final int CMD_MOUSE_MOVE = 3000;
//    public static final int CMD_MOUSE_CLICK = 3001;
//    
    
    public static void showLog(String log)
    {
//    	Log.v(MainData.TAG, log);
    }
    
    public static String getLocalIpAddress() {
        String ipaddress="";
        try 
        {
        	Log.v(MainData.TAG, "getLocalIpAddress");
        	Enumeration<NetworkInterface> en1 = NetworkInterface.getNetworkInterfaces();
        	if( en1 != null )
        	{
            	Log.v(MainData.TAG, "ok");
        		
        		Log.v(MainData.TAG, en1.toString());
        		
        		if( en1.hasMoreElements() )
        		{
        			NetworkInterface intf = en1.nextElement();
        			Log.v(MainData.TAG, intf.toString());
        		}
        		else
        		{
        			Log.v(MainData.TAG, "no elements");
        		}
        	}
        	else
        		Log.v(MainData.TAG, "111");
        	
        	
            for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) 
            {
                NetworkInterface intf = en.nextElement();
                for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements();) 
                {
                    InetAddress inetAddress = enumIpAddr.nextElement();
                    if (!inetAddress.isLoopbackAddress() && InetAddressUtils.isIPv4Address(inetAddress.getHostAddress())) 
                    {
                        ipaddress = inetAddress.getHostAddress().toString();
                    }
                }
            }
        } catch (SocketException ex) {
        	ex.printStackTrace();
            return "没有获取到IP";
        }

        return ipaddress;
    }
    
    public static byte[] buildMessage(int cmd, String content)
    {
    	List<byte[]> bytes = new ArrayList<byte[]>();
    	bytes.add(MainData.intToByteArray(cmd));
    	
    	int length = 0;
    	if( content != null )
    		length = content.getBytes().length;
    	
	    bytes.add(MainData.intToByteArray(length));
    	
    	if( content != null )
    	{
	    	bytes.add(content.getBytes());
    	}
    	
    	return MainData.sysCopy(bytes);
    }
    
    public static int sendMessage(String sendip, int cmd, String content)
    {
    	byte[] data = buildMessage(cmd, content);
    	
    	return sendMessage(sendip, data);
    }

    public static int sendMessage(String sendip, byte[] buf) {
        try {
            DatagramSocket sendSocket = new DatagramSocket();
            InetAddress ip = InetAddress.getByName(sendip);
            Log.v("sendMessage", String.valueOf(ip));
            
            DatagramPacket sendPacket = new DatagramPacket(buf, buf.length, ip, UDP_PORT);
            sendSocket.send(sendPacket);
            sendSocket.close();
            return 0;
        } catch (SocketException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (UnknownHostException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return 1;
    }
    
    

    /***
     * byte array 转化为 int
     */
    public static byte[] intToByteArray(int i) {
        byte[] result = new byte[4];
        result[0] = (byte)((i >> 24) & 0xFF);
        result[1] = (byte)((i >> 16) & 0xFF);
        result[2] = (byte)((i >> 8) & 0xFF);
        result[3] = (byte)(i & 0xFF);

        return result;
    }

    /**
     * 从bytes 格式转化成int格式
     */
    public static int byteArrayToInt(byte[] b, int offset) {
        int value = 0;
        for(int i= 0; i < 4; i++) {
            int shift = (4-1-i) * 8;
            value += (b[i+offset]&0x000000FF) << shift;
        }
        return value;
    }
    
    /**
     * 从bytes 格式转化成double格式
     */
    public static double byteArrayToDouble(byte[] b, int offset) {
        byte[] bytes = new byte[8];
        for(int i = 0; i < 8; i++) {
            bytes[i] = b[i + offset];
        }

        return ByteBuffer.wrap(bytes).getDouble();
    }

    /**
     * 系统提供的数组拷贝方法arraycopy
     * */
    public static byte[] sysCopy(List<byte[]> srcArrays) {
        int len = 0;
        for (byte[] srcArray:srcArrays) {
            len += srcArray.length;
        }
        byte[] destArray = new byte[len];
        int destLen = 0;
        for (byte[] srcArray:srcArrays) {
            System.arraycopy(srcArray, 0, destArray, destLen, srcArray.length);
            destLen += srcArray.length;
        }

        return destArray;
    }

    public static List<byte[]> getSendByte(int[] value, int flag) {
        List<byte[]> bytes = new ArrayList<byte[]>();
        bytes.add(MainData.intToByteArray(value[0]));
        bytes.add(MainData.intToByteArray(value[1]));
        bytes.add(MainData.intToByteArray(value[2]));
        bytes.add(MainData.intToByteArray(value[3]));
        bytes.add(MainData.intToByteArray(flag));

        return bytes;
    }

    public static int[] iptoInt(String strip) {
        int[] ip = new int[4];
        int position1 = strip.indexOf(".");
        int position2 = strip.indexOf(".", position1 + 1);
        int position3 = strip.indexOf(".", position2 + 1);
        ip[0] = Integer.valueOf(strip.substring(0, position1));
        ip[1] = Integer.valueOf(strip.substring(position1 + 1, position2));
        ip[2] = Integer.valueOf(strip.substring(position2 + 1, position3));
        ip[3] = Integer.valueOf(strip.substring(position3 + 1));

        return ip;
    }
    
//    static{
//    	System.loadLibrary("EventInjector");
//    }
//    
//    private native static int sendKeyEvent(int code);

    /**
     * 发送按键值给系统,使系统触发按键事件
     */
    public static void sendKeyCodeToSystem(int KeyCode) {
    	try{
        Instrumentation inst=new Instrumentation();
        inst.sendKeyDownUpSync(KeyCode);
    	}
    	catch(Exception e)
    	{
    		e.printStackTrace();
    	}
    	
//    	String cmd = String.format("input keyevent %d", KeyCode);
//    	MyUtils.execCmd(cmd);
//    	int n = sendKeyEvent(KeyCode);
  //  	Log.v("alex", "code=" + n);
    }

    /**
     * 检查网络状态
     */
    public static boolean isNetwork(Context context){
        if(context!=null){
            ConnectivityManager manager=(ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo info=manager.getActiveNetworkInfo();
            if(info!=null){
                return info.isAvailable();
            }
        }

        return false;
    }

    /**
     * 获取当前版本信息
     */
    public static int getVerCode(Context context) {
        int vercode=-1;
        try {
            vercode = context.getPackageManager().getPackageInfo("com.gaikko.remotecontrolclient",  0).versionCode;
        } catch (NameNotFoundException e) {
            e.printStackTrace();
        }
        return vercode;
    }

    /**
     * 获取版本名称
     */
    public static String getVerName(Context context) {
        String vername="";
        try {
            vername=context.getPackageManager().getPackageInfo("com.gaikko.remotecontrolclient", 0).versionName;
        } catch (NameNotFoundException e) {
            e.printStackTrace();
        }
        return vername;
    }

    public static void takeScreenShot(String filename){
        String savedPath = Environment.getExternalStorageDirectory() + File.separator + filename;
        System.out.println("takeScreenShot to  " + savedPath);
        try {
            Runtime.getRuntime().exec("screencap -p " + savedPath);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static int getStatusBarHeight(Context context){
        Class<?> c = null;
        Object obj = null;
        Field field = null;
        int x = 0, statusBarHeight = 0;
        try {
            c = Class.forName("com.android.internal.R$dimen");
            obj = c.newInstance();
            field = c.getField("status_bar_height");
            x = Integer.parseInt(field.get(obj).toString());
            statusBarHeight = context.getResources().getDimensionPixelSize(x);
        } catch (Exception e1) {
            e1.printStackTrace();
        }
        return statusBarHeight;
    }

    
    
	public static String get_profile_string_value(Context context, String key, String defaultValue)
	{
		try{
			SharedPreferences pre = context.getSharedPreferences(PROFILE_NAME, Context.MODE_PRIVATE);
			
			return pre.getString(key, defaultValue);
		}
		catch(Exception e)
		{
			
		}
		
		return defaultValue;
	}
	
	public static int save_profile_string_value(Context context, String key, String value)
	{
		try{
			SharedPreferences.Editor editor = context.getSharedPreferences(PROFILE_NAME, Context.MODE_PRIVATE).edit();
			
			editor.putString(key, value);
			
			editor.commit();
			
			return 0;
		}
		catch(Exception e)
		{
			
		}
		
		return 1;
	}	
    
	
	public static long getTickCount()
	{
		Date dt = new Date();
		return dt.getTime();
	}
	
	public final static String getDeviceId(Context context)
	{
		String android_id = Secure.getString(context.getContentResolver(), Secure.ANDROID_ID);	
		
		return android_id;
	}
	
	   
    public final static String getTagValue(String content, String tag)
    {
    	String bTag = String.format("%s=", tag);
    	String eTag = ";";
    	
    	int b = content.indexOf(bTag);
    	if( b < 0 )
    		return null;
    	
    	b += bTag.length();
    	
    	int e = content.indexOf(eTag, b);
    	if( e < 0 )
    		return content.substring(b);
    	else
    		return content.substring(b, e);
    }	

}