package com.soniq.utils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.Date;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import android.app.ActivityManager;
import android.app.ActivityManager.MemoryInfo;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Environment;
import android.text.format.Formatter;
import android.util.Log;

public class MyUtils {

	public static final int NETWORK_NONE = 0;
	public static final int NETWORK_WIFI = 1;
	public static final int NETWORK_MOBILE = 2;

	public static int dip2px(Context context, float dipValue) {
		final float scale = context.getResources().getDisplayMetrics().density;
		return (int) (dipValue * scale + 0.5f);
	}

	public static int px2dip(Context context, float pxValue) {
		final float scale = context.getResources().getDisplayMetrics().density;
		return (int) (pxValue / scale + 0.5f);
	}

	public static void showLog(String log) {
		System.out.println(log);
	}

	public static String get_duration_string(int duration) {
		if (duration < 0)
			duration = 0;

		String s = "";

		if (duration < 60) // 秒
			s = String.format("%d秒", duration);
		else if (duration < 60 * 60) // 分
		{
			int minute = duration / 60;
			int sec = duration % 60;
			s = String.format("%d分%d秒", minute, sec);
		} else {
			int hour = duration / 3600;
			int minute = (duration % 3600) / 60;
			int sec = (duration % 3600) % 60;
			s = String.format("%d时%d分%d秒", hour, minute, sec);
		}

		return s;
	}

	public static String get_filename_from_url(String url, boolean bHasExt) {
		String[] p1 = url.split("\\/", -1);
		if (p1.length <= 0)
			return "";

		String fname = p1[p1.length - 1];

		if (bHasExt)
			return fname;

		String[] p2 = fname.split("\\.", -1);
		if (p2.length <= 0)
			return "";

		return p2[0];
	}

	public static String get_filename_ext_from_url(String url) {
		String[] p1 = url.split("\\/", -1);
		if (p1.length <= 0)
			return "";

		String fname = p1[p1.length - 1];

		String[] p2 = fname.split("\\.", -1);
		if (p2.length <= 1)
			return "";

		return p2[1];
	}

	public static String getTimeMaskString() {
		SimpleDateFormat sDateFormat = new SimpleDateFormat("yyyyMMddhhmmssSS");
		String date = sDateFormat.format(new java.util.Date());

		return date;
	}

	public static long getTickCount() {
		Date dt = new Date();
		return dt.getTime();
	}

	public static boolean checkSDCardExists() {
		if (android.os.Environment.getExternalStorageState().equals(
				android.os.Environment.MEDIA_MOUNTED))
			return true;

		return false;
	}

	public static String getSDCardPath() {
		File path = android.os.Environment.getExternalStorageDirectory();

		return path.toString();
	}

	public static boolean isHttpUrl(String url) {
		if (url != null && url.length() > 10
				&& url.substring(0, 7).compareToIgnoreCase("http://") == 0)
			return true;

		return false;
	}

	public static boolean FileExist(String filename) {
		File file = new File(filename);
		if (file.exists())
			return true;

		return false;
	}

	public static boolean PrivateFileExist(Context context, String filename) {
		if (filename == null || filename.length() < 1)
			return false;

		try {
			FileInputStream fin = context.openFileInput(filename);
			fin.close();
			return true;
		} catch (FileNotFoundException e) {

		} catch (Exception e) {

		}

		return false;
	}

	public static BitmapDrawable loadPrivateBitmapFile(Context context,
			String filename) {
		if (context == null || filename == null)
			return null;

		BitmapDrawable bd = null;
		try {
			FileInputStream fin = context.openFileInput(filename);

			BitmapDrawable bd1 = new BitmapDrawable(fin);
			fin.close();

			bd = bd1;
		} catch (Exception e) {

		}

		return bd;
	}

	public static BitmapDrawable loadBitmapFile(String filename) {
		if (filename == null)
			return null;

		BitmapDrawable bd = null;
		try {
			FileInputStream fin = new FileInputStream(filename);

			BitmapDrawable bd1 = new BitmapDrawable(fin);
			fin.close();

			bd = bd1;
		} catch (Exception e) {

		}

		return bd;
	}

	public static Bitmap loadBitmapFromFile(String filename) {
		FileInputStream fis = null;
		try {
			fis = new FileInputStream(filename);

			ByteArrayOutputStream outStream = new ByteArrayOutputStream();
			byte[] buffer = new byte[1024];
			int len = 0;
			while ((len = fis.read(buffer)) != -1) {
				outStream.write(buffer, 0, len);
			}
			outStream.close();

			byte[] data = outStream.toByteArray();

			Bitmap bmp = BitmapFactory.decodeByteArray(data, 0, data.length);
			// Drawable frame = new BitmapDrawable(context.getResources(), bmp);

			return bmp;

		} catch (Exception e) {

		} finally {
			if (fis != null) {
				try {
					fis.close();
				} catch (Exception e) {

				}
			}
		}

		return null;
	}

	public static boolean copyPrivateFileToSDCard(Context context,
			String src_fname, String dst_fname) {
		try {
			FileInputStream fin = context.openFileInput(src_fname);

			FileOutputStream fout = new FileOutputStream(dst_fname);

			byte[] buf = new byte[1024];
			int len;

			while ((len = fin.read(buf)) > 0) {
				fout.write(buf, 0, len);
			}

			fout.close();
			fin.close();

			return true;
		} catch (Exception e) {

		}
		return false;
	}

	public static boolean makeSureDirExists(String dirname) {
		File file = new File(dirname);
		if (file.exists())
			return true;

		return file.mkdirs();
	}

	public static boolean deleteFile(String filename) {
		try {
			File file = new File(filename);
			if (file.exists())
				return file.delete();

			return true;
		} catch (Exception e) {

		}

		return false;
	}

	public static boolean renameFile(String src_file, String dst_file) {
		try {
			File fileSrc = new File(src_file);
			File fileDst = new File(dst_file);
			fileSrc.renameTo(fileDst);

			return true;
		} catch (Exception e) {

		}

		return false;
	}

	public static boolean writeToFile(String content, String filename) {
		try {
			FileOutputStream fos = new FileOutputStream(filename);
			byte[] bytes = content.getBytes();
			fos.write(bytes);
			fos.close();
			return true;
		} catch (Exception e) {

		}

		return false;
	}

	public static String getStorageRootPath() {
		return Environment.getExternalStorageDirectory().getPath();
	}

	public static String GetSizeString(long size) {
		DecimalFormat df = new DecimalFormat();
		String style = "0.0";// 定义要显示的数字的格式
		df.applyPattern(style);// 将格式应用于格式化器
		if (size > 1024 * 1024 * 1024) // G
		{
			// sprintf(ch, "%.02fG", (float)size / (float)(1024 * 1024 * 1024));
			double d = (double) size / (double) (1024 * 1024 * 1024);
			df.applyPattern("0.0G");
			return df.format(d);
		} else if (size > 1024 * 1024) // M
		{
			// sprintf(ch, "%.02fM", (float)size / (float)(1024 * 1024));
			double d = (double) size / (double) (1024 * 1024);
			df.applyPattern("0.0M");
			return df.format(d);
		} else if (size > 1024) {

			;// sprintf(ch, "%.02fK", (float)size / (float)1024);
			double d = (double) size / (double) 1024;
			df.applyPattern("0.0K");
			return df.format(d);
		} else {

			;// sprintf(ch, "%.0fB", (float)size);
			double d = size;
			df.applyPattern("0.0B");
			return df.format(d);
		}
	}

	public static int getNetworkType(Context context) {
		ConnectivityManager connectivityManager = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);// 获取代表联网状态的NetWorkInfo对象
		NetworkInfo activeNetInfo = connectivityManager.getActiveNetworkInfo();// 获取当前的网络连接是否可用

		if (activeNetInfo != null) {

			if (activeNetInfo.isAvailable()) {
				if (activeNetInfo.getType() == ConnectivityManager.TYPE_WIFI) {
					// 判断WIFI网
					return NETWORK_WIFI;
				} else if (activeNetInfo.getType() == ConnectivityManager.TYPE_MOBILE) {
					// 判断3G网
					return NETWORK_MOBILE;
				}

				return NETWORK_MOBILE;
			}
		}

		return NETWORK_NONE;
	}

	public static boolean isValidURLString(String urlString) {
		if (urlString == null)
			return false;

		String s = urlString.toLowerCase();

		if (s.startsWith("http://"))
			return true;

		return false;
	}

	public static boolean isSDCardMounted() {
		String state = Environment.getExternalStorageState();
		if (Environment.MEDIA_MOUNTED.equals(state))
			return true;

		return false;
	}

	public static void execCmd(String cmd) {
		try {
			Runtime.getRuntime().exec(cmd);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// 获取版本号
	public static String getVersion(Context context) {
		try {
			PackageManager manager = context.getPackageManager();
			PackageInfo info = manager.getPackageInfo(context.getPackageName(),
					0);
			String version = info.versionName;
			return version;
		} catch (Exception e) {
			e.printStackTrace();
			return "";
		}
	}

	public static String load_content_from_file(Context context, String filename) {
		try {
			FileInputStream fis = new FileInputStream(filename);
			ByteArrayOutputStream outStream = new ByteArrayOutputStream();
			byte[] buffer = new byte[1024];
			int len = 0;
			while ((len = fis.read(buffer)) != -1) {
				outStream.write(buffer, 0, len);
			}
			outStream.close();

			byte[] data = outStream.toByteArray();
			return new String(data);
		} catch (Exception e) {
		}

		return null;
	}

	public static int compareVersionString(String version1, String version2) {
		String[] version1List = version1.split("\\.");
		String[] version2List = version2.split("\\.");

		for (int i = 0; i < version1List.length || i < version2List.length; i++) {
			int code1, code2;

			if (i >= version1List.length)
				code1 = 0;
			else
				code1 = Integer.parseInt(version1List[i]);

			if (i >= version2List.length)
				code2 = 0;
			else
				code2 = Integer.parseInt(version2List[i]);

			if (code1 > code2)
				return 1;
			else if (code1 < code2)
				return -1;

		}

		return 0;
	}

	public static boolean isNetworkConnect(Context context) {
		try {
			// 获取手机所有连接管理对象（包括对wi-fi,net等连接的管理）
			ConnectivityManager connectivity = (ConnectivityManager) context
					.getSystemService(Context.CONNECTIVITY_SERVICE);
			if (connectivity != null) {
				// 获取网络连接管理的对象
				NetworkInfo info = connectivity.getActiveNetworkInfo();
				if (info != null && info.isConnected()) {
					// 判断当前网络是否已经连接
					if (info.getState() == NetworkInfo.State.CONNECTED) {
						return true;
					}
				}
			}
		} catch (Exception e) {
		}

		return false;
	}

	public static String getAvailMemory(Context context) {// 获取android当前可用内存大小
		ActivityManager am = (ActivityManager) context
				.getSystemService(Context.ACTIVITY_SERVICE);
		MemoryInfo mi = new MemoryInfo();
		am.getMemoryInfo(mi);
		// mi.availMem; 当前系统的可用内存

		return Formatter.formatFileSize(context, mi.availMem);// 将获取的内存大小规格化
	}

	public static void showMemoryInfo(Context context, String tag) {
		// Log.v(AppConfig.TAG, "[" + tag + "] AvailMemory: " +
		// MyUtils.getAvailMemory(context));

		// VMRuntime.getRuntime().setMinimumHeapSize(CWJ_HEAP_SIZE);

	}
	
	
	/**
	 * 获取版本号
	 * @return 当前应用的版本号
	 */
	public static String getVersionName(Context context) {
	    try {
	        PackageManager manager = context.getPackageManager();
	        PackageInfo info = manager.getPackageInfo(context.getPackageName(), 0);
	        String version = info.versionName;
	        return version;
	    } catch (Exception e) {
	        e.printStackTrace();
	        return "";
	    }
	}	
	
	public static String getVersionCode(Context context) {
	    try {
	        PackageManager manager = context.getPackageManager();
	        PackageInfo info = manager.getPackageInfo(context.getPackageName(), 0);
	        String version = String.format("%d", info.versionCode);
	        return version;
	    } catch (Exception e) {
	        e.printStackTrace();
	        return "";
	    }
	}	
}
