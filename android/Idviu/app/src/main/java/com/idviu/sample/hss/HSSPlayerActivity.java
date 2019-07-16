/**
 *
 */
package com.idviu.sample.hss;

import java.util.ArrayList;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import com.labgency.hss.HSSPlayer;
import com.labgency.hss.data.HSSPlaylistItem;
import com.labgency.hss.views.HSSPlayerView;
import com.labgency.hss.views.HSSPlayerViewListeners.OnCloseEventListener;
import com.labgency.player.LgyPlayer.AdaptiveStreamingListener;
import com.labgency.player.LgyTrack;
import com.labgency.player.LgyTrack.TrackType;
import com.idviu.sample.hss.HSSSampleApplication.HSSCustomLicenseRequestHandler;

import io.streamroot.dna.core.BandwidthListener;
import io.streamroot.dna.core.DnaClient;
import io.streamroot.dna.core.PlayerInteractor;
import io.streamroot.dna.core.QosModule;
import io.streamroot.dna.utils.stats.StatsView;
import io.streamroot.dna.utils.stats.StreamStatsManager;

/**
 * Sample Player Activity using HSSPlayer
 */
@SuppressLint("NewApi")
public class HSSPlayerActivity extends FragmentActivity implements OnCloseEventListener, OnErrorListener, OnCompletionListener, AdaptiveStreamingListener {

    private static final String TAG = "HSSPlayerActivity";

    private ProgressBar mProgress = null;
    private RelativeLayout mRootView;
	private HSSPlayerView mPlayerView = null;
	private DnaClient dnaClient;
	private StreamStatsManager streamStatsManager;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB){
			getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
		}
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		
		setContentView(R.layout.player);
		mPlayerView = (HSSPlayerView) findViewById(R.id.playerView);
		mPlayerView.setRetainedNonInstanceStateObject(getLastCustomNonConfigurationInstance());
		mPlayerView.setOnCompletionListener(this);
		mPlayerView.showDebugInfos(true);
		mPlayerView.setOnErrorListener(this);
		mPlayerView.setAdaptiveStreamingListener(this);
		mPlayerView.setOnCloseEventListener(this); //set this listener to show the "close" button and be able to stop playback
		mPlayerView.setPlayerParam("connect_timeout", "10000"); //only 10s for connection timeout, reduces the risk of HTTP 403 for some HLS streams with temporary sessions
		mPlayerView.setPlayerParam("start_low_profile", "1");
		mPlayerView.setWidevineOfflineMode(false); //set to true if you want to retrieve a persistent license for Widevine
		mPlayerView.setOnPreparedListener(new OnPreparedListener() {

			@Override
			public void onPrepared(MediaPlayer mp) {
				Log.i(TAG, "redirected url: " + mPlayerView.getPlayer().getRedirectedUrl());
			}
		});
		HSSCustomLicenseRequestHandler.instance().setPlayerView(mPlayerView);

		Intent intent = this.getIntent();
		Bundle bundle = intent.getExtras();
		Entry entry = (Entry)bundle.getSerializable("entry");
		if (mPlayerView.getPlaylist().getSize() == 0 && entry != null ){ //only if empty
			HSSPlaylistItem item = null;
				if (entry.downloadId >= 0){
					item = HSSPlaylistItem.createWithDownloadId(entry.downloadId);
					if (entry.downloadForceLocalMode)
						item.setForceLocalMode(entry.downloadForceLocalMode);
					mPlayerView.getPlaylist().addItem(item);
				}else {
					if (entry.streamrootDnaEnabled) {
						startDnaClient(entry);
						item = HSSPlaylistItem.createWithUrl(dnaClient.getManifestUrl().toString());
					} else {
						item = HSSPlaylistItem.createWithUrl(entry.getUrl());
					}
					if(entry.getStartTime() != 0)
						item.setStartPosition(entry.getStartTime());
					if(entry.getBonusTime() != 0)
						item.setBonusDRMTime(entry.getBonusTime());

					item.setPlayreadyLicenseUrlAndCustomData(entry.getPlayreadyUrl(), entry.getPlayreadyCustomData());
					item.setWidevineLicenseUrl(entry.getWidevineUrl());
					item.setWidevineCustomData(entry.getWidevineCustomData());
					item.setMarlinLicenseToken(entry.getMarlinUrl());
					if (entry.downloadForceLocalMode)
						item.setForceLocalMode(entry.downloadForceLocalMode);
					if (entry.getMaxVideoBitrate() != 0)
						item.setMaxVideoBitrate(entry.getMaxVideoBitrate());
					if (entry.getUserAgent() != null && entry.getUserAgent() != "" )
						item.setUserAgent(entry.getUserAgent());
					item.setPlayerParameters(entry.getPlayerParameters());

					item.setFilteredHeaders(entry.getFilteredHeaders());
					for (String keys : entry.getHeaders().keySet())
						item.addLicenseHeader(keys, entry.getHeaders().get(keys));

					mPlayerView.getPlaylist().addItem(item);
				}
		}
	}


	/**
	 * Start Streamroot DNAClient
	 */
	private void startDnaClient(Entry entry) {
		// Init the player interactor
		PlayerInteractor playerInteractor = new IdviuInteractor(this.mPlayerView);

		// Init the player bandwidth listener
		BandwidthListener bandwidthListener = new IdviuBandwithMeter(this.mPlayerView);

		// Init qosModule (optional)
		QosModule qosModule = new IdviuQoSModule(this.mPlayerView);

		// Build and start the DNAClient
		try {
			this.dnaClient = DnaClient.newBuilder()
					.context(getApplicationContext())
					.playerInteractor(playerInteractor)
					.bandwidthListener(bandwidthListener)
					.qosModule(qosModule)
					.latency(20)
					.contentId(entry.getTitle())
					.start(Uri.parse(entry.getUrl()));
		} catch (Exception e){
			Log.e("Streamroot", e.getLocalizedMessage());
		}
	}

	/**
	 * Start Streamroot debug stats view
	 */
	private void startStatsView() {
		if (dnaClient != null) {
			Log.i("STREAMROOT", "started stats");
			StatsView streamrootStatsView = findViewById(R.id.streamrootStatsView);
			streamrootStatsView.setVisibility(View.VISIBLE);
			streamStatsManager = new StreamStatsManager(1_000L, dnaClient, streamrootStatsView);
		}
	}
	
	@Override
	protected void onStop() {
		super.onStop();
		mPlayerView.pause();
		if (dnaClient != null) {
			dnaClient.close();
		}

		if (streamStatsManager != null) {
			streamStatsManager.close();
		}
	}

	@Override
	protected void onStart() {
		super.onStart();
		mPlayerView.play();
		startStatsView();
	}

	private String getRealPathFromURI(Uri contentUri) {
		// can post image
		String[] proj = { MediaStore.Images.Media.DATA };
		Cursor cursor = managedQuery(contentUri, proj, // Which columns to
														// return
				null, // WHERE clause; which rows to return (all rows)
				null, // WHERE clause selection arguments (none)
				null); // Order-by clause (ascending by name)
		int column_index = cursor
				.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
		cursor.moveToFirst();

		return cursor.getString(column_index);
	}
		
	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_1){
			if (mPlayerView != null && mPlayerView.getPlayer() != null){
				HSSPlayer player = mPlayerView.getPlayer();
				LgyTrack[] tracks = player.getTracks();
				ArrayList<LgyTrack> audio = new ArrayList<LgyTrack>();
				for (LgyTrack t : tracks){
					if (t.getType() == LgyTrack.TrackType.TYPE_AUDIO){
						audio.add(t);
					}
				}
				int selected = player.getSelectedTrackIndex(TrackType.TYPE_AUDIO);
				if (audio.size() > 1){
					for (int i = 0; i < audio.size(); i = (i + 1) % audio.size()){
						if (audio.get(i).getIndex() != selected){
							player.selectTrack(TrackType.TYPE_AUDIO, audio.get(i).getIndex());
							break;
						}
					}
					return true;
				}
			}
			return false;
		}else if (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT){
			if (mPlayerView != null && mPlayerView.getPlayer() != null){
				HSSPlayer player = mPlayerView.getPlayer();
				player.setPosition(Math.min(player.getPosition() + 60 * 1000, player.getDuration()));
				mPlayerView.show();
			}
			return true;
		}else if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT){
			if (mPlayerView != null && mPlayerView.getPlayer() != null){
				HSSPlayer player = mPlayerView.getPlayer();
				player.setPosition(Math.max(0, player.getPosition() - 60 * 1000));
				mPlayerView.show();
			}
			return true;
		}else{
			return super.onKeyUp(keyCode, event);
		}
	}

	@Override
	public Object onRetainCustomNonConfigurationInstance() {
		return mPlayerView.getRetainNonInstanceStateObject();
	}

	@Override
	public void onCloseButtonClicked() {
		Log.i(TAG, "onCloseButtonClicked");
		if (mPlayerView != null){
			mPlayerView.release();
		}
		finish();
	}

	@Override
	public boolean onError(MediaPlayer mp, int what, int extra) {
		if (mPlayerView.getPlayer() != null){
			Log.i(TAG, "will copy VMX logs");
			boolean result = mPlayerView.getPlayer().copyVMXLogs("/sdcard/vmxlogs.txt");
			if (!result){
				Log.e(TAG, "could not copy logs");
			}
		}
		return false; //so that the view displays error messages
	}

	@Override
	public void onCompletion(MediaPlayer mp) {
		finish(); //we only have one item in the playlist, so for this sample app, close the player activity when playback is complete
	}

	@Override
	public void onNewAudioLevelSelected(int level, int bandwidth) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onNewVideoLevelSelected(int level, int bandwidth) {
		Log.i(TAG, "now using bandwidth " + (bandwidth/1024) + "kpbs for video");
	}
}