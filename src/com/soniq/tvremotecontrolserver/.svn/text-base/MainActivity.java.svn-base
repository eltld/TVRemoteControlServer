package com.soniq.tvremotecontrolserver;

import java.util.List;

import com.soniq.utils.MyUtils;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;


public class MainActivity extends Activity {
	
	private TextView _textView;
	private TextView _textViewDeviceName;
	private TextView _textViewVersion;
	private ImageView _imgViewStatus;
	private Button _button;
	private Intent _intent;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.layout_main);
		
		
		_intent = new Intent(this, MainService.class);
		
		_imgViewStatus = (ImageView)this.findViewById(R.id.imageViewStatus);
		
		_textView = (TextView)this.findViewById(R.id.textView1);
		_textViewDeviceName = (TextView)this.findViewById(R.id.textViewDeviceName);
		_textViewVersion = (TextView)this.findViewById(R.id.textViewVersion);
		
		String s = String.format(this.getResources().getString(R.string.version_info), MyUtils.getVersionName(this));
		_textViewVersion.setText(s);
		
		String deviceName = MainData.get_profile_string_value(this, MainData.PROFILE_SERVER_NAME, MainData.DEFAULT_SERVER_NAME);
		s = String.format(this.getResources().getString(R.string.device_name), deviceName);
		_textViewDeviceName.setText(s);

		_button = (Button)this.findViewById(R.id.button1);
		_button.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				
				int flag = ((Integer)v.getTag()).intValue();
				
				if( flag == 1 )
				{
					// 停止
					stopService(_intent);
				}
				else
				{
					// 启动
					startService(_intent);
				}
				
				
				updateServiceStatus();
				
			}
			
		});
		
		
		updateServiceStatus();
		
	}
	
	
	public void updateServiceStatus()
	{
		boolean b = isServiceRunning(this, "com.soniq.tvremotecontrolserver.MainService");
		
		if( b )
		{
			_imgViewStatus.setImageResource(R.drawable.service_running);
			_textView.setText(this.getResources().getString(R.string.status_running));// "运行中...");
//			_button.setText(this.getResources().getString(R.string.btn_stop_service));
			_button.setTag(1);
			_button.setBackgroundResource(R.drawable.btn_service_stop_selector);
		}
		else
		{
			_imgViewStatus.setImageResource(R.drawable.service_stopped);
			_textView.setText(this.getResources().getString(R.string.status_stopped));
//			_button.setText(this.getResources().getString(R.string.btn_start_service));
			_button.setTag(0);
			_button.setBackgroundResource(R.drawable.btn_service_start_selector);
		}
	}
	
	
	public boolean isServiceRunning(Context mContext,String className) {
        boolean isRunning = false;
ActivityManager activityManager = (ActivityManager)
mContext.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningServiceInfo> serviceList
        = activityManager.getRunningServices(30);
       if (!(serviceList.size()>0)) {
            return false;
        }
        for (int i=0; i<serviceList.size(); i++) {
            if (serviceList.get(i).service.getClassName().equals(className) == true) {
                isRunning = true;
                break;
            }
        }
        return isRunning;
    }	
}
