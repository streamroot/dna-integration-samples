package com.idviu.sample.hss;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import com.labgency.hss.HSSAgent;
import com.idviu.sample.hss.SampleHSSSecurityHandler.SecurityHandlerListener;

public class SplashActivity extends Activity implements SecurityHandlerListener{

	private Handler mHandler = new Handler(){

		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			if (msg.what == 133){
				Intent startIntent = new Intent(SplashActivity.this, BrowseActivity.class);
				SplashActivity.this.startActivity(startIntent);
				overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
			}
		}
		
	};
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.splash);
		HSSSampleApplication application = (HSSSampleApplication)getApplication();
		if (application.HSSInitializationFailed){
			String message = null;
			switch(application.HSSInitializationFailedReason){
			case HSSAgent.HSS_FAILED_EXPIRED:
				message = "The evaluation period is over. Request a new license to your Labgency representative";
				break;
			case HSSAgent.HSS_FAILED_NO_LICENSE:
			case HSSAgent.HSS_FAILED_NO_LICENSE_KEY:
				message = "No valid license was found. You won't be able to use HSS features";
				break;
			case HSSAgent.HSS_FAILED_CORRUPTED:
				message = "HSS data is corrupted. Please reinstall";
				break;
			default:
				message = "HSS could not initialize. You won't be able to use its features";
				break;
			}
			if (message != null){
				new AlertDialog.Builder(this).setTitle("HSS").setIcon(R.mipmap.ic_launcher).setMessage(message)
				.setPositiveButton(android.R.string.ok, new OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						finish();
					}
				}).create().show();
			}
			return;
		}
		
		mHandler.sendEmptyMessageDelayed(133, 2000);
	}

	
	
	@Override
	protected void onPause() {
		super.onPause();
		SampleHSSSecurityHandler.getInstance().unregisterListener(this);
	}



	@Override
	protected void onResume() {
		super.onResume();
		SampleHSSSecurityHandler.getInstance().registerListener(this);
	}



	@Override
	protected void onDestroy() {
		super.onDestroy();
		mHandler.removeMessages(133);
	}



	@Override
	public void onDisplayAlert(String alert) {
		mHandler.removeMessages(133);
		new AlertDialog.Builder(this)
		.setIcon(R.mipmap.ic_launcher)
		.setTitle("HSS")
		.setMessage(alert)
		.setPositiveButton(android.R.string.ok, new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				mHandler.sendEmptyMessageDelayed(133, 2000);
			}
		}).setOnCancelListener(new OnCancelListener() {
			@Override
			public void onCancel(DialogInterface dialog) {
				mHandler.sendEmptyMessageDelayed(133, 2000);
			}
		})
		.setCancelable(false)
		.create().show();
	}
	
}
