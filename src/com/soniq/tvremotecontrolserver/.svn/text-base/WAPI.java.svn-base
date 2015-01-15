package com.soniq.tvremotecontrolserver;

import java.util.ArrayList;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import com.soniq.utils.MyUtils;

import android.content.Context;


public class WAPI {
	public final static int TASK_STATUS_NO_DOWNLOAD			= 0;
	public final static int TASK_STATUS_DOWNLOADING 		= 1;
	public final static int TASK_STATUS_DOWNLOAD_SUCCESS	= 2;
	public final static int TASK_STATUS_DOWNLOAD_FAILED		= 3;
	public final static int TASK_STATUS_INSTALL_SUCCESS	= 4;
	public final static int TASK_STATUS_INSTALL_FAILED	= 5;
	
			
	public final static String WAPI_BASE_URL = "http://www.timesyw.com:8080/tvmarket/WAPI";
	
	public final static String WAPI_GET_DOWNLOAD_TASK_URL	= WAPI_BASE_URL + "/smc/getdownloadtask.jsp";
	public final static String WAPI_SET_TASK_STATUS_URL		= WAPI_BASE_URL + "/smc/settaskstatus.jsp";//?userid=234&taskid=232&status=2"
	
	public final static String WAPI_CHECK_VERSION_URL		= WAPI_BASE_URL + "/checkversion.jsp?client=remotecontrolservice";
	
	public static String addGeneralParams(Context context, String urlString)
	{
		String newURLString;
		String splitString = "?";
		if( urlString.indexOf("?")  >= 0 )
		{
			splitString = "&";
		}
		
		newURLString = String.format("%s%svercode=%s&vername=%s",
				urlString, splitString, 
				MyUtils.getVersionCode(context), 
				MyUtils.getVersionName(context));
		
		return newURLString;
	}
	

	public static String getSetTaskStatusURLString(Context context, int userId, int taskId, int status)
	{
		String urlString = String.format("%s?userid=%d&taskid=%d&status=%d", 
				WAPI_SET_TASK_STATUS_URL, userId, taskId, status);
		urlString = addGeneralParams(context, urlString);
		
		return urlString;
		
	}

	public static String getGetDownloadTaskURLString(Context context, int userId, String devid, String devname)
	{
		String urlString = String.format("%s?userid=%d&devid=%s&devname=%s", 
				WAPI_GET_DOWNLOAD_TASK_URL, userId, devid, devname);
		urlString = addGeneralParams(context, urlString);
		
		return urlString;
		
	}
	
	
	public static int parseGeneralResponse(String jsonString)
	{
		
		try{
			JSONObject jsonObject = new JSONObject(jsonString);
			JSONObject resultObject = jsonObject.getJSONObject("result");
			return resultObject.getInt("code");
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		
		return -1;		
	}	
	
	public static TaskInfo parseDownloadTaskInfo(String jsonString)
	{
		
		try{
			JSONObject jsonObject = new JSONObject(jsonString);
			JSONObject resultObject = jsonObject.getJSONObject("result");
			int code = resultObject.getInt("code");
			if( code == 0 )
			{
				JSONObject obj = jsonObject.getJSONObject("taskinfo");
				
				TaskInfo ti = new TaskInfo();
				ti.taskId = obj.getInt("taskid");
				ti.appId = obj.getInt("appid");
				ti.status = obj.getInt("status");
				ti.downUrl = obj.getString("downurl");
				ti.appName = obj.getString("appname");
				ti.packageName = obj.getString("pkgname");
				
				return ti;
			}
			
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		
		return null;		
	}
	
	
	public static String http_get_content(String url)
	{
		HttpGet request = new HttpGet(url);
//		request.setHeader("User-Agent", MyProfile.http_user_agent);
		
		HttpClient httpClient = new DefaultHttpClient();
		try
		{
			httpClient.getParams().setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, 15000);
			httpClient.getParams().setParameter(CoreConnectionPNames.SO_TIMEOUT, 15000);
			HttpResponse response = httpClient.execute(request);
			if( response.getStatusLine().getStatusCode() == HttpStatus.SC_OK )
			{
				String str = EntityUtils.toString(response.getEntity());
				return str;
			}
		}
		catch(ClientProtocolException e)
		{
			e.printStackTrace();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		return null;
	}
	

	public static String get_content_from_remote_url(String url)
	{
		MyUtils.showLog(url);
		
		try
		{
			String scontent = http_get_content(url);
			if( scontent == null || scontent == "" )
				return null;
			
			return scontent;
		}
		catch(Exception e)
		{
			
		}
		
		return null;
		
	}
	
	

	public static int getJsonInt(JSONObject jsonObject, String name, int defaultValue)
	{
		try
		{
			int n = jsonObject.getInt(name);
			
			return n;
		}
		catch(Exception e)
		{
			
		}
		
		return defaultValue;
	}
	
	public static String getJsonString(JSONObject jsonObject, String name)
	{
		try
		{
			return jsonObject.getString(name);
		}
		catch(Exception e)
		{
			
		}
		
		return "";
	}
	
	public static JSONObject getJsonObject(JSONObject jsonObject, String name)
	{
		try
		{
			return jsonObject.getJSONObject(name);
		}
		catch(Exception e)
		{
			
		}
		
		return null;
	}
	
	public static JSONArray getJsonArray(JSONObject jsonObject, String name)
	{
		try
		{
			return jsonObject.getJSONArray(name);
		}
		catch(Exception e)
		{
			
		}
		
		return null;
	}
		
	public static int parseVersionInfoResponse(Context context, String responseString, 
			ArrayList<String> fieldList)
	{
		int ret = 1;
		
		try{
			JSONObject jsonObject = new JSONObject(responseString);
			JSONObject resultObject = jsonObject.getJSONObject("result");
			int code = resultObject.getInt("code");
			if( code == 0 )
			{
				jsonObject = jsonObject.getJSONObject("versioninfo");
				
				String version = jsonObject.getString("versioncode");
				String desc = jsonObject.getString("desc");
				String force = getJsonString(jsonObject, "force");
				String downloadurl = jsonObject.getString("downloadurl");
				
				if( force == null || force.length() < 1)
					force = "no";
				
				fieldList.add(version);
				fieldList.add(desc);
				fieldList.add(downloadurl);
				fieldList.add(force);
				
				ret = 0;
			}
			
		}
		catch(Exception e)
		{
			
		}
		
		return ret;
	}	
}
