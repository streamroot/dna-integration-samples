package com.idviu.sample.hss.fragments;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.Html;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.textclassifier.TextClassifier;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.labgency.hss.HSSAgent;
import com.labgency.hss.HSSDownload;
import com.labgency.hss.HSSDownloadManager;
import com.labgency.hss.HSSPlayer;
import com.labgency.hss.data.HSSPlaylist;
import com.labgency.hss.data.Movie;
import com.labgency.hss.downloads.HSSDownloadError;
import com.labgency.hss.downloads.HSSDownloadState;
import com.labgency.hss.downloads.HSSDownloadStatus;
import com.labgency.hss.listeners.HSSDownloadListener;
import com.idviu.sample.hss.Entry;
import com.idviu.sample.hss.HSSPlayerActivity;
import com.idviu.sample.hss.R;

/**
 * Shows details about a {@link Movie}
 * @author benoit.vannesson@labgency.us
 *
 */
public class MediaDetailFragment extends Fragment implements HSSDownloadListener, OnClickListener{
	
	private static final int MSG_UPDATE = 198;
	
	private TextView mTitle = null;
	private TextView mTitleBig = null;
	private TextView mCID = null;
	private TextView mStatus = null;
	private ProgressBar mProgress = null;
	private TextView mDownloadProgress = null;
	private Button mStreamBtn = null;
	private Button mDownloadBtn = null;
	private Button mPlayBtn = null;
	private Button mDeleteBtn = null;
	private Button mPauseBtn = null;
	private Button mUnpauseBtn = null;
	private Button mRestartBtn = null;
	private ToggleButton mForceLocalBtn = null;
	private ToggleButton mHDRootAllowed = null;
	private ToggleButton mHDSWDRMAllowed = null;
	private EditText mHDBitrate = null;
	private EditText mHDResolution = null;
	
	private LinearLayout mOtherLayout = null;
	
	private Entry mMovie = null;
	private HSSDownload mDownload = null;
	
	private HSSDownloadManager mDownloadManager = HSSAgent.getDownloadManager();
	
	private static final int TOKEN_DOWNLOAD_ID = 0;
	private static final int TOKEN_STREAM_ID = 1;
	private static final int TOKEN_PLAY_ID = 2;
	
	private Handler mHandler = new Handler(){
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch(msg.what){
			case MSG_UPDATE:
				update();
				break;
			}
		}
		
	};
	

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (savedInstanceState != null){
			mMovie = (Entry) savedInstanceState.getSerializable("entry");
			mDownload = (HSSDownload) savedInstanceState.getSerializable("download");
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		return inflater.inflate(R.layout.media_detail_fragment, container, false);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		mTitle = (TextView) view.findViewById(R.id.detail_title);
		mTitleBig = (TextView) view.findViewById(R.id.title_big);
		mCID = (TextView) view.findViewById(R.id.detail_cid);
		mStatus = (TextView) view.findViewById(R.id.detail_status);
		mProgress = (ProgressBar) view.findViewById(R.id.detail_progress);
		mStreamBtn = (Button) view.findViewById(R.id.detail_stream);
		mDownloadBtn = (Button) view.findViewById(R.id.detail_download);
		mPlayBtn = (Button) view.findViewById(R.id.detail_play_local);
		mDeleteBtn = (Button) view.findViewById(R.id.detail_delete);
		mPauseBtn = (Button) view.findViewById(R.id.detail_pause);
		mUnpauseBtn = (Button) view.findViewById(R.id.detail_unpause);
		mRestartBtn = (Button) view.findViewById(R.id.detail_restart);
		mOtherLayout = (LinearLayout) view.findViewById(R.id.detail_others);
		mDownloadProgress = (TextView) view.findViewById(R.id.detail_status_progress);
		mForceLocalBtn = (ToggleButton) view.findViewById(R.id.detail_local_mode);
		mHDRootAllowed = (ToggleButton) view.findViewById(R.id.detail_hd_root_allowed);
		mHDRootAllowed.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
				if (mMovie != null){
					mMovie.getPlayerParameters().put(HSSPlayer.PLAYER_PARAM_HD_ROOT_ALLOWED, b ? "1" : "0");
				}
			}
		});
		mHDSWDRMAllowed = (ToggleButton) view.findViewById(R.id.detail_hd_sw_drm_allowed);
		mHDSWDRMAllowed.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
				if (mMovie != null){
					mMovie.getPlayerParameters().put(HSSPlayer.PLAYER_PARAM_HD_SW_DRM_ALLOWED, b ? "1" : "0");
				}
			}
		});
		mHDBitrate = (EditText) view.findViewById(R.id.detail_hd_bitrate);
		mHDBitrate.setInputType(InputType.TYPE_CLASS_NUMBER);
		mHDBitrate.addTextChangedListener(new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

			}

			@Override
			public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

			}

			@Override
			public void afterTextChanged(Editable editable) {
				if (mMovie != null){
					mMovie.getPlayerParameters().put(HSSPlayer.PLAYER_PARAM_HD_MAX_BITRATE, editable.toString());
				}
			}
		});
		mHDResolution = (EditText) view.findViewById(R.id.detail_hd_resolution);
		mHDResolution.setInputType(InputType.TYPE_CLASS_NUMBER);
		mHDResolution.addTextChangedListener(new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

			}

			@Override
			public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

			}

			@Override
			public void afterTextChanged(Editable editable) {
				if (mMovie != null){
					mMovie.getPlayerParameters().put(HSSPlayer.PLAYER_PARAM_HD_MAX_PIXELS, editable.toString());
				}
			}
		});
		mProgress.setMax(10000);
		mStreamBtn.setOnClickListener(this);
		mDownloadBtn.setOnClickListener(this);
		mPlayBtn.setOnClickListener(this);
		mDeleteBtn.setOnClickListener(this);
		mPauseBtn.setOnClickListener(this);
		mUnpauseBtn.setOnClickListener(this);
		mRestartBtn.setOnClickListener(this);
		if (mMovie != null){ //movie was set before view was constructed
			updateObject(mMovie);
		}else{
			updateObject(mDownload);
		}
	}
	
	public synchronized void updateObject(Object m){
		if (m instanceof Entry){
			mMovie = (Entry)m;
			mDownload = null;
			for (HSSDownload d : mDownloadManager.getAllDownloads()){
				if (mMovie.getTitle() != null && mMovie.getTitle().equals(d.getExtra1())){
					mDownload = d;
				}
			}
			if (getView() == null) return;
			String title = TextUtils.isEmpty(mMovie.getTitle()) ? getString(R.string.no_title) : mMovie.getTitle();
			mTitle.setText(Html.fromHtml(getString(R.string.detail_title, title)));
			mTitleBig.setText(title);
			mCID.setText("");
			mOtherLayout.removeAllViews();

			if (HSSAgent.getInstance().isHDOverridable()){
				mHDBitrate.setEnabled(true);
				mHDResolution.setEnabled(true);
				mHDSWDRMAllowed.setEnabled(true);
				mHDRootAllowed.setEnabled(true);
				if (mMovie.getPlayerParameters().containsKey(HSSPlayer.PLAYER_PARAM_HD_ROOT_ALLOWED)){
					mHDRootAllowed.setChecked(!"0".equals(mMovie.getPlayerParameters().get(HSSPlayer.PLAYER_PARAM_HD_ROOT_ALLOWED)));
				}else{
					mHDRootAllowed.setChecked(HSSAgent.getInstance().isHDRootAllowed());
				}
				if (mMovie.getPlayerParameters().containsKey(HSSPlayer.PLAYER_PARAM_HD_SW_DRM_ALLOWED)){
					mHDSWDRMAllowed.setChecked(!"0".equals(mMovie.getPlayerParameters().get(HSSPlayer.PLAYER_PARAM_HD_SW_DRM_ALLOWED)));
				}else{
					mHDSWDRMAllowed.setChecked(HSSAgent.getInstance().isHDSWDRMAllowed());
				}
				if (mMovie.getPlayerParameters().containsKey(HSSPlayer.PLAYER_PARAM_HD_MAX_BITRATE)){
					mHDBitrate.setText(mMovie.getPlayerParameters().get(HSSPlayer.PLAYER_PARAM_HD_MAX_BITRATE));
				}else{
					mHDBitrate.setText("" + HSSAgent.getInstance().getHDMaxBitrate());
				}
				if (mMovie.getPlayerParameters().containsKey(HSSPlayer.PLAYER_PARAM_HD_MAX_PIXELS)){
					mHDResolution.setText(mMovie.getPlayerParameters().get(HSSPlayer.PLAYER_PARAM_HD_MAX_PIXELS));
				}else{
					mHDResolution.setText("" + HSSAgent.getInstance().getHDMaxPixels());
				}
			}else{
				mHDBitrate.setEnabled(false);
				mHDResolution.setEnabled(false);
				mHDSWDRMAllowed.setEnabled(false);
				mHDRootAllowed.setEnabled(false);
				mHDRootAllowed.setChecked(HSSAgent.getInstance().isHDRootAllowed());
				mHDSWDRMAllowed.setChecked(HSSAgent.getInstance().isHDSWDRMAllowed());
				mHDBitrate.setText("" + HSSAgent.getInstance().getHDMaxBitrate());
				mHDResolution.setText("" + HSSAgent.getInstance().getHDMaxPixels());
			}
		}else{
			if (m == null){
				return;
			}
			mDownload = (HSSDownload)m;
			if (getView() == null) return;
			mTitle.setText(Html.fromHtml(getString(R.string.detail_title, "Download " + mDownload.getId())));
			mTitleBig.setText("Download " + mDownload.getId());
			mCID.setText(Html.fromHtml(getString(R.string.detail_cid, mDownload.getExtra1() != null ? mDownload.getExtra1() : "unavailable")));
			mOtherLayout.removeAllViews();
			mHDBitrate.setEnabled(false);
			mHDResolution.setEnabled(false);
			mHDSWDRMAllowed.setEnabled(false);
			mHDRootAllowed.setEnabled(false);
			mHDRootAllowed.setChecked(mDownload.isHDRootAllowed());
			mHDSWDRMAllowed.setChecked(mDownload.isHDSWDRMAllowed());
			mHDBitrate.setText("" + mDownload.getHDBitrateLimit());
			mHDResolution.setText("" + mDownload.getHDPixelsLimit());
		}
		
		update();
	}
		
	private void setErrorMessage(){
		int errorInt = mDownload.getError().type;
		for (Field f : HSSDownloadError.class.getFields()){
			try {
				if (f.getType() == int.class && Modifier.isStatic(f.getModifiers()) && f.getInt(null) == errorInt){
					mStatus.setText(getString(R.string.status_error, f.getName()));
				}
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			}
		}
		
	}
	
	private String formatSize(long value){
		if (value > 1024 * 1024){ //MB
			return String.format("%.2fM", value / 1024.0 / 1024.0);
		}else if (value > 1024){
			return String.format("%.2fk", value / 1024.0);
		}else{
			return String.valueOf(value);
		}
	}
	
	private void update(){
		if (getView() == null) return;
		mStreamBtn.setEnabled(mMovie != null);
		if (mDownload != null){
			mDeleteBtn.setVisibility(View.VISIBLE);
			mDownloadBtn.setVisibility(View.GONE);
			mPlayBtn.setVisibility(View.VISIBLE);
			mRestartBtn.setVisibility(View.VISIBLE);
			mPlayBtn.setEnabled(mDownloadManager.canStartPlayingDownload(mDownload.getId()));
			double progress = mDownload.getPercentComplete() * 100;
			mProgress.setProgress((int)progress);
			mProgress.setVisibility(progress > 0 ? View.VISIBLE : View.INVISIBLE);
			mStatus.setVisibility(View.VISIBLE);
			if (mDownload.getError() != null){
				mRestartBtn.setEnabled(true);
				setErrorMessage();
				return;
			}
			if (mDownload.getSize() != 0){
				mDownloadProgress.setText(formatSize(mDownload.getBytesDownloaded()) + " / " + formatSize(mDownload.getSize()) + " (" + String.format("%.2f", mDownload.getPercentComplete()) + "%)");
			}
			mRestartBtn.setEnabled(false);
			mPauseBtn.setVisibility(View.VISIBLE);
			mUnpauseBtn.setVisibility(View.VISIBLE);
			switch(mDownload.getState()){
			case PAUSED:
				mPauseBtn.setVisibility(View.GONE);
				mRestartBtn.setEnabled(true);
				mStatus.setText(R.string.status_paused);
				break;
			case DONE:
				mProgress.setProgress(10000);
				mPauseBtn.setVisibility(View.INVISIBLE);
				mUnpauseBtn.setVisibility(View.GONE);
				mRestartBtn.setVisibility(View.INVISIBLE);
				mStatus.setText(R.string.status_done);
				break;
			case RUNNING:
				mUnpauseBtn.setVisibility(View.GONE);
				mStatus.setText(R.string.status_running);
				break;
			case WAITING:
				mUnpauseBtn.setVisibility(View.GONE);
				mRestartBtn.setEnabled(true);
				mStatus.setText(R.string.status_waiting);
				break;
			default:
				mStatus.setText("");
			}
			
		}else{
			mDownloadProgress.setText("");
			mStatus.setVisibility(View.INVISIBLE);
			mProgress.setVisibility(View.INVISIBLE);
			mDeleteBtn.setVisibility(View.GONE);
			mPauseBtn.setVisibility(View.INVISIBLE);
			mUnpauseBtn.setVisibility(View.GONE);
			mRestartBtn.setVisibility(View.INVISIBLE);
			if (mMovie != null){
				mDownloadBtn.setVisibility(View.VISIBLE);
				mPlayBtn.setVisibility(View.VISIBLE);
				mPlayBtn.setEnabled(false);
			}else{
				mDownloadBtn.setVisibility(View.GONE);
				mPlayBtn.setVisibility(View.GONE);
			}
		}
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		mDownloadManager.registerDownloadListener(this);
	}

	@Override
	public void onDetach() {
		super.onDetach();
		mDownloadManager.unregisterDownloadListener(this);
	}

	@Override
	public synchronized void onDownloadErrorChanged(HSSDownload d, HSSDownloadError error) {
		if (d.equals(mDownload)){
			mHandler.sendEmptyMessage(MSG_UPDATE);
		}
	}

	@Override
	public synchronized void onDownloadProgressChanged(HSSDownload d, long download, long total, double progress) {
		if (d.equals(mDownload)){
			mHandler.sendEmptyMessage(MSG_UPDATE);
		}
	}

	@Override
	public synchronized void onDownloadStateChanged(HSSDownload d, HSSDownloadState state) {
		if (state == HSSDownloadState.REMOVING || state == HSSDownloadState.REMOVED){
			synchronized (this) {
				if (mMovie != null && mMovie.getTitle() != null && mMovie.getTitle().equals(d.getExtra1()))
					mDownload = null;
			}
		}
		if (d.equals(mDownload)){
			mHandler.sendEmptyMessage(MSG_UPDATE);
		}
	}

	@Override
	public synchronized void onDownloadStatusChanged(HSSDownload d, HSSDownloadStatus status) {
		if ((status == HSSDownloadStatus.STATUS_INIT || status == HSSDownloadStatus.STATUS_RETRIEVING_INFOS) && mDownload == null){
			synchronized (this) {
				if (mMovie != null && mMovie.getTitle() != null && mMovie.getTitle().equals(d.getExtra1())){
					mDownload = d;
				}
			}
		}
		if (d.equals(mDownload)){
			mHandler.sendEmptyMessage(MSG_UPDATE);
		}
	}

	@Override
	public void onClick(View v) {
		int id = v.getId();
		if (id == R.id.detail_stream) {
			Intent streamIntent = new Intent(getActivity(), HSSPlayerActivity.class);
			Bundle mBundle = new Bundle();
			mBundle.putSerializable("entry", mMovie);
			mBundle.putBoolean("duplicate", true);
			streamIntent.putExtras(mBundle);
			startActivity(streamIntent);
		} else if (id == R.id.detail_download) {
			long downloadId = mDownloadManager.addDownload(mMovie.getUrl(), true);
			HSSDownload download = mDownloadManager.getDownload(downloadId);
			if (download != null){
				download.setPlayreadyLicenseUrl(mMovie.getPlayreadyUrl());
				download.setWidevineLicenseUrl(mMovie.getWidevineUrl());
				download.setWidevineCustomData(mMovie.getWidevineCustomData());
				download.setPlayreadyCustomData(mMovie.getPlayreadyCustomData());
				download.setExtra1(mMovie.getTitle());
				download.setUserAgent(mMovie.getUserAgent());
				download.setExtraFileUrl(mMovie.getExternalSubtitleUrl());
				if (mMovie.getMaxVideoBitrate() != 0){
					download.setMaxVideoBitrate(mMovie.getMaxVideoBitrate());
				}
				if (mMovie.getPlayerParameters() != null){
					if (mMovie.getPlayerParameters().containsKey(HSSPlayer.PLAYER_PARAM_HD_MAX_PIXELS)){
						download.setHDPixelsLimit(Long.valueOf(mMovie.getPlayerParameters().get(HSSPlayer.PLAYER_PARAM_HD_MAX_PIXELS)));
					}
					if (mMovie.getPlayerParameters().containsKey(HSSPlayer.PLAYER_PARAM_HD_MAX_BITRATE)){
						download.setHDBitrateLimit(Long.valueOf(mMovie.getPlayerParameters().get(HSSPlayer.PLAYER_PARAM_HD_MAX_BITRATE)));
					}
					if (mMovie.getPlayerParameters().containsKey(HSSPlayer.PLAYER_PARAM_HD_ROOT_ALLOWED)){
						download.setHDRootAllowed(!"0".equals(mMovie.getPlayerParameters().get(HSSPlayer.PLAYER_PARAM_HD_ROOT_ALLOWED)));
					}
					if (mMovie.getPlayerParameters().containsKey(HSSPlayer.PLAYER_PARAM_HD_SW_DRM_ALLOWED)){
						download.setHDSWDRMAllowed(!"0".equals(mMovie.getPlayerParameters().get(HSSPlayer.PLAYER_PARAM_HD_SW_DRM_ALLOWED)));
					}

				}
			}
			mDownloadManager.unpauseDownload(downloadId);
			mDownload = download;
			update();
		} else if (id == R.id.detail_delete) {
			if (mDownload != null){
				mDownloadManager.deleteDownload(mDownload.getId());
			}
			mDownload = null;
			update();
		} else if (id == R.id.detail_play_local) {
			if (mDownload != null){
				Intent playIntent = new Intent(getActivity(), HSSPlayerActivity.class);
				Entry mMovie = new Entry(new JSONObject());
				mMovie.downloadId = mDownload.getId();
				mMovie.downloadForceLocalMode = mForceLocalBtn.isChecked();
				Bundle mBundle = new Bundle();
				mBundle.putSerializable("entry", mMovie);
				playIntent.putExtras(mBundle);
				startActivity(playIntent);
			}
		}else if (id == R.id.detail_pause){
			if (mDownload != null){
				mDownloadManager.pauseDownload(mDownload.getId());
			}
		}else if (id == R.id.detail_unpause){
			if (mDownload != null){
				mDownloadManager.unpauseDownload(mDownload.getId());
			}
		}else if (id == R.id.detail_restart){
			if (mDownload != null){
				mDownloadManager.startDownload(mDownload.getId());
			}
		}
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		if (mMovie != null){
			outState.putSerializable("entry", mMovie);
		}
		if (mDownload != null){
			outState.putSerializable("download", mDownload);
		}
	}
}
