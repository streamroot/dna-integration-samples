package com.idviu.sample.hss;

import java.util.HashMap;
import java.util.Map;

import android.content.Context;
import android.util.Base64;
import android.util.Log;

import com.labgency.hss.HSSAgent;
import com.labgency.hss.HSSApplication;
import com.labgency.hss.HSSLicenseAcquisitionDeledate;
import com.labgency.hss.HSSParams;
import com.labgency.hss.handlers.HSSSecurityHandler;
import com.labgency.hss.views.HSSPlayerView;
import com.labgency.tools.requests.RQMLog;
import com.labgency.tools.requests.Request;
import com.labgency.tools.requests.RequestManager;
import com.labgency.tools.requests.handlers.RequestErrors;
import com.labgency.tools.requests.listeners.IRequestStateChangeListener;

import org.apache.http.Header;

public class HSSSampleApplication extends HSSApplication {

	private static final String TAG = "HSSSampleApplication";
		
	public Context mContext = null;
	public HSSCustomLicenseRequestHandler customRequestHandler;
  
	@Override
	public void onCreate() {
		super.onCreate();
		Log.i(TAG,"creating the application");
	}
	

	@Override
	public HSSParams getHSSParams() {
		HSSParams params = new HSSParams();
		//make sure you placed a license named application.key in the assets folder !
		params.autoDeleteExpired = true; //auto delete expired downloads
		params.pauseDownloadsWhenPlaying = true; //this will pause download (except running one) when playing to avoid framedrops
		params.downloadLibsRightAway = false; // set to true to download libs just after startup, if you do this with a Playready SDK, you will be charged for royalties for that device 
		params.autoSelectSaveLocation = true; // if set to true, each new download will be placed where there is more space left (internal sd/external sd)
		params.maxSimultaneousDownloads = 1; // how many downloads can run in parallel
		params.hssLogsEnabled = true; //Set to true to enable HSS logs
		params.playerLogsEnabled = true; //set to true to enable player logs
		return params;
	}

	@Override
	public HSSSecurityHandler getHSSSecurityHandler() {
		SampleHSSSecurityHandler.initialize(this);
		return SampleHSSSecurityHandler.getInstance();
	}

	@Override
	public void onHSSInitialized() {
		Log.i(TAG,"onHSSInitialized");	
		//here, you can set a delegate for license requests:
		//this is fully optional, you don't need it most of the time.
		HSSAgent.setLicenseAcquisitionDelegate(HSSCustomLicenseRequestHandler.instance());
		RQMLog.setLogLevel(100); //uncomment to show logs from the request manager


		RequestManager.getInstance().addRequest("testhttps", "https://www.google.com");
	}

	@Override
	public byte[] getLicense() {
		return null;
	}

	public int HSSInitializationFailedReason = 0;
	public boolean HSSInitializationFailed = false;
	
	@Override
	public void onHSSInitializeFailed(int reason) {
		Log.e(TAG, "could not initialize because of reason " + reason);
		HSSInitializationFailed = true;
		HSSInitializationFailedReason = reason;
	}
	
	public static class HSSCustomLicenseRequestHandler implements HSSLicenseAcquisitionDeledate, IRequestStateChangeListener{

		private HashMap<Integer, Integer> mRequestIdToLicenseId = new HashMap<Integer, Integer>();
		private RequestManager mRequestManager = RequestManager.getInstance();
		private static HSSCustomLicenseRequestHandler sInstance = null;
		private HSSPlayerView mPlayerView;
		
		public static HSSCustomLicenseRequestHandler instance(){
			if (sInstance == null){
				sInstance = new HSSCustomLicenseRequestHandler();
			}
			return sInstance;
		}
		
		private HSSCustomLicenseRequestHandler() {
			mRequestManager.registerStateChangeListener(this);
		}
		
		public void setPlayerView(HSSPlayerView view){
			mPlayerView = view;
		}
		
		@Override
		public boolean onHSSShouldSendLicenseRequest(int id, int type,
				byte[] payload, byte[] kid, String streamUrl, String suggestedUrl,
				Map<String, String> suggestedHeaders) {
			
			synchronized (mRequestIdToLicenseId) {
				if (type == HSSLicenseAcquisitionDeledate.REQUEST_TYPE_PLAYREADY_CHALLENGE){
					Log.i(TAG, "challenge url is " + suggestedUrl);
					Log.i(TAG, "challenge is " + new String(payload));
				}else if (type == HSSLicenseAcquisitionDeledate.REQUEST_TYPE_WIDEVINE_PROVISIONING_REQUEST){
					Log.i(TAG, "widevine provisioning request, url: " + suggestedUrl);
				}else if (type == HSSLicenseAcquisitionDeledate.REQUEST_TYPE_WIDEVINE_LICENSE_REQUEST){
					Log.i(TAG, "widevine license request, url: " + suggestedUrl + ", payload size: " + payload.length);
				}
				if (suggestedHeaders == null){
					suggestedHeaders = new HashMap<String, String>();
				}


				if (mPlayerView != null && mPlayerView.getPlaylist().getCurrentItem() != null && mPlayerView.getPlaylist().getCurrentItem().url() != null && mPlayerView.getPlaylist().getCurrentItem().url().equals(streamUrl)){
					suggestedHeaders.putAll(mPlayerView.getPlaylist().getCurrentItem().allHeaders());
				}
				int req_id = mRequestManager.addRequest("license", suggestedUrl, Request.HTTP_POST_REQUEST, payload, 0, new HashMap<String, String>(suggestedHeaders));
				mRequestIdToLicenseId.put(req_id, id);
			}
			
			return true;
		}

		@Override
		public void onRequestComplete(int reqid, byte[] payload, String path, Header[] headers) {
			int lic_id = -1;
			synchronized (mRequestIdToLicenseId) {
				if (mRequestIdToLicenseId.containsKey(reqid)){
					lic_id = mRequestIdToLicenseId.remove(reqid);
				}
			}
			if (lic_id >= 0){
				Log.i(TAG, "result payload size is " + (payload != null ? payload.length : 0));
				try{
					String s= new String(payload);
					Log.i(TAG, "result string is is " + s);
					int start = s.indexOf("<LICENSE>");
					int end = s.indexOf("</LICENSE>");
					if (start >= 0 && end > 0){
						payload = Base64.decode(s.substring(start + 9, end), 0);
					}
				}catch(Exception e){

				}
				HSSAgent.processLicenseResponse(lic_id, payload);
			}
		}

		@Override
		public void onRequestError(int reqid, RequestErrors error, String path, byte[] payload, Header[] headers) {
			int lic_id = -1;
			synchronized (mRequestIdToLicenseId) {
				if (mRequestIdToLicenseId.containsKey(reqid)){
					lic_id = mRequestIdToLicenseId.remove(reqid);
				}
			}
			if (lic_id >= 0){
				Log.i(TAG, "license request error");
				if (headers != null){
					for (Header h : headers){
						Log.i(TAG, "\\t" + h.getName() + ": " + h.getValue());
					}
					
				}
				if (payload != null){
					Log.i(TAG, new String(payload));
				}
				HSSAgent.processLicenseResponse(lic_id, null);
			}
		}

		@Override
		public void onRequestStarted(int reqid, String path) {
			
		}
		
	}

}
