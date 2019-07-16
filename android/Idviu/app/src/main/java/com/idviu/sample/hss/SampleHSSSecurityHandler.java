package com.idviu.sample.hss;

import java.util.ArrayList;
import java.util.HashMap;

import android.content.Context;
import android.os.Handler;

import com.labgency.hss.data.SecurityEvent;
import com.labgency.hss.handlers.HSSSecurityHandler;

public class SampleHSSSecurityHandler extends HSSSecurityHandler {

	private Context mContext = null;
	private Handler mHandler = new Handler();
	private boolean mNotifiedRoot = false;
	
	public interface SecurityHandlerListener{
		public void onDisplayAlert(String alert);
	}
	
	private HashMap<SecurityHandlerListener, SecurityHandlerListener> mListeners = new HashMap<SampleHSSSecurityHandler.SecurityHandlerListener, SampleHSSSecurityHandler.SecurityHandlerListener>();
	
	public void registerListener(SecurityHandlerListener listener){
		mListeners.put(listener, listener);
		if (mSavedAlerts.size() > 0){
			for (String alert : mSavedAlerts){
				ArrayList<SecurityHandlerListener> lists = new ArrayList<SampleHSSSecurityHandler.SecurityHandlerListener>(mListeners.values());
				for (SecurityHandlerListener l : lists){
					try{
						l.onDisplayAlert(alert);
					}catch(Exception e){

					}
				}
			}
			mSavedAlerts.clear();
		}
	}
	
	public void unregisterListener(SecurityHandlerListener listener){
		mListeners.remove(listener);
	}
	
	private SampleHSSSecurityHandler(Context context){
		mContext = context;
	}
	
	private ArrayList<String> mSavedAlerts = new ArrayList<String>();
	

	private static SampleHSSSecurityHandler sInstance = null;
	
	public static void initialize(Context context){
		if (sInstance == null){
			sInstance = new SampleHSSSecurityHandler(context);
		}
	}
	
	public static SampleHSSSecurityHandler getInstance(){
		return sInstance;
	}

	@Override
	public void securityEventReceived(SecurityEvent event, String detail) {
		//Here we are notified that a security event occured,
		String message = null;
		switch(event.type){
		case SecurityEvent.DATA_INTEGRITY_FAILURE:
			message = "Application data is corrupted. You may need to reinstall the app.";
			break;
		case SecurityEvent.SYSTEM_INTEGRITY_FAILURE:
			message = "You updated your system to a new version. You'll need a working internet connection to play movies again.";
			break;
		case SecurityEvent.PHONE_IS_ROOTED:
			if (!mNotifiedRoot){
				message = "Your device is rooted. You may not be able to play protected videos.";
				mNotifiedRoot = true;
			}
			break;
		}
		if (message == null) return;
		final String alert = message;
		mHandler.post(new Runnable(){
			public void run(){
				if (mListeners.size() == 0){
					mSavedAlerts.add(alert);
				}else{
					ArrayList<SecurityHandlerListener> lists = new ArrayList<SampleHSSSecurityHandler.SecurityHandlerListener>(mListeners.values());
					for (SecurityHandlerListener l : lists){
						try{
							l.onDisplayAlert(alert);
						}catch(Exception e){

						}
					}
				}
			}
		});
	}
	
}
