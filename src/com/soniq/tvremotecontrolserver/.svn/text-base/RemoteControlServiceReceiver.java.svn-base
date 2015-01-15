package com.soniq.tvremotecontrolserver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

public class RemoteControlServiceReceiver extends BroadcastReceiver {
	@Override
	public void onReceive(Context context, Intent intent) {
		Intent mIntent = new Intent(context, MainService.class);
		
		ConnectivityManager connectMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo wifiNetInfo = connectMgr.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
		NetworkInfo ethNetInfo = connectMgr.getNetworkInfo(ConnectivityManager.TYPE_ETHERNET);
		if((wifiNetInfo != null) && (ethNetInfo != null)) {
			if (wifiNetInfo.isConnected() || ethNetInfo.isConnected()) {
				// connect network
				MainData.showLog("Network connect");
		        context.startService(mIntent);
			} else {
				// unconnect network
				MainData.showLog("Network unconnect");
				context.stopService(mIntent);
			}			
		} else if((wifiNetInfo != null) && (ethNetInfo == null)) {
			if (wifiNetInfo.isConnected()) {
				// connect network
				MainData.showLog("Wifi network connect");
		        context.startService(mIntent);
			} else {
				// unconnect network
				MainData.showLog("Wifi network unconnect");
				context.stopService(mIntent);
			}
			
		} else if ((wifiNetInfo == null) && (ethNetInfo != null)) {
			if (ethNetInfo.isConnected()) {
				// connect network
				MainData.showLog("Ethernet network connect");
		        context.startService(mIntent);
			} else {
				// unconnect network
				MainData.showLog("Ethernet network unconnect");
				context.stopService(mIntent);
			}
		}
	}
}