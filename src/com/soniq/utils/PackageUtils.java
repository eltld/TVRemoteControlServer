package com.soniq.utils;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.IPackageDeleteObserver;
import android.content.pm.IPackageInstallObserver;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.os.RemoteException;
import android.util.Log;

public class PackageUtils {
	private static int INSTALL_REPLACE_EXISTING = 2;

	
	public static  boolean isPackageExisted(String targetPackage, Context context){
		PackageManager pm = context.getPackageManager();
		try {
			pm.getPackageInfo(targetPackage, PackageManager.GET_META_DATA);
		} catch (NameNotFoundException e) {
			return false;
		}
		return true;
	}
	
	

    public static int chmod(File path, int mode) throws Exception {
		Class<?> fileUtils = Class.forName("android.os.FileUtils");
		Method setPermissions = fileUtils.getMethod("setPermissions", String.class, int.class, int.class, int.class);
		return (Integer) setPermissions.invoke(null, path.getAbsolutePath(), mode, -1, -1);
   	}
    
    public static int silentUninstall(Context context, String packageName, String appName, InstallApkCallback callback)
    {
    	final String _appName = appName;
    	final InstallApkCallback _callback = callback;
		return silentUninstall(context, packageName, new IPackageDeleteObserver.Stub() {
			
			@Override
			public void packageDeleted(String packageName, int returnCode)
					throws RemoteException {
				if( _callback != null )
				{
					_callback.onUninstallFinished(packageName, _appName, returnCode);
				}
				
			}
		});
    }
    
	private static int silentUninstall(Context context, String packageName, IPackageDeleteObserver.Stub observer)
	{
    	PackageManager pm = context.getPackageManager();
    	
    	Class<?>[] types = new Class[] {String.class, IPackageDeleteObserver.class, int.class};
    	Method method = null;
    	try {
    		
			method = pm.getClass().getMethod("deletePackage", types);	
			
			//  pm.installPackage(Uri.fromFile(file), observer, installFlags, packageName);
			// pm.deletePackage(packageName, observer, 0);
			method.invoke(pm, new Object[] {packageName, observer, 0});
		} 
    	catch (InvocationTargetException invocateErr) 
    	{
    		invocateErr.printStackTrace();
    		return 2;
    	}
    	catch (Exception e) {
    		e.printStackTrace();
    		return 1;
		}
    	
    	return 0;
	}
    
    
    public static int silentInstall(Context context, File downloaded,InstallApkCallback callback, int requestCode){
    	final InstallApkCallback _callback = callback;
    	final int code = requestCode;
		return silentInstall(context, downloaded, new IPackageInstallObserver.Stub() {
			
			@Override
			public void packageInstalled(String packageName, int returnCode)
					throws RemoteException {
				
				if( _callback != null )
				{
					_callback.onInstallFinished(packageName, returnCode, code);
				}
				
			}
		});
    }
    
	private static int silentInstall(Context context, File downloaded, IPackageInstallObserver.Stub observer){

    	PackageManager pm = context.getPackageManager();
    	
    	Class<?>[] types = new Class[] {Uri.class, IPackageInstallObserver.class, int.class, String.class};
    	Method method = null;
    	try {
    		chmod(downloaded, 0755);
    		
			method = pm.getClass().getMethod("installPackage", types);		
			method.invoke(pm, new Object[] {Uri.fromFile(downloaded), observer, INSTALL_REPLACE_EXISTING, "com.soniq.tvmarket"});
		} 
    	catch (InvocationTargetException invocateErr) 
    	{
//    		Intent intent = new Intent(Intent.ACTION_VIEW); 
//	   		intent.setDataAndType(Uri.fromFile(downloaded), "application/vnd.android.package-archive"); 
//	   		//mContext.startActivity(intent);
//	   		context.startActivityForResult(intent, 1);
    		invocateErr.printStackTrace();
    		return 2;
    	}
    	catch (Exception e) {
    		e.printStackTrace();
    		return 1;
		}
    	
    	return 0;
	}

	
	
	public interface InstallApkCallback{
		public void onInstallFinished(String packageName, int resultCode, int requestCode);
		public void onUninstallFinished(String packageName, String appName, int resultCode);
	}
	
	
	public static int installApk(Context context, String filename, int requestCode, InstallApkCallback callback)
	{
		int iret = 1;
		try
		{
			File file = new File(filename);
			if( !file.exists() )
				return 1;
	
			iret = 2;
	    	iret = silentInstall(context, file, callback, requestCode);// != 0 )
//	    	{
//	        	
//	    		Intent nn = new Intent(Intent.ACTION_VIEW);
//	    		nn.setDataAndType(Uri.parse("file://"+file.toString()), "application/vnd.android.package-archive");
//	    		nn.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK); 
//	    		activity.startActivity(nn);//ForResult(nn, requestCode);
//	        	iret = 0;
//	    	}
		}
		catch(Exception e)
		{
			e.printStackTrace();
			iret = 3;
		}
    	
    	return iret;
		
	}

}
