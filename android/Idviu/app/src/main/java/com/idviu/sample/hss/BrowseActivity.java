package com.idviu.sample.hss;


import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.Menu;

import com.labgency.hss.HSSAgent;
import com.labgency.hss.HSSDownload;
import com.labgency.hss.HSSDownloadManager;
import com.idviu.sample.hss.fragments.MediaDetailFragment;
import com.idviu.sample.hss.fragments.MediaListFragment;
import com.idviu.sample.hss.fragments.MediaListFragment.MediaListFragmentCallback;
import com.labgency.hss.HSSPlayer;

import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;

@SuppressLint("NewApi")
public class BrowseActivity extends FragmentActivity implements MediaListFragmentCallback{
	
	private static final String TAG = "BrowseActivity";

	private boolean mTwoPanes = false;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.browse_activity);

		// Manage Intent
        Entry entry = null;
        if (getIntent() != null && getIntent().getData() != null && savedInstanceState == null) {
            try {
                Uri uri = getIntent().getData();
                if (uri.getQueryParameter("movie") != null) {
                    entry = new Entry(new JSONObject(uri.getQueryParameter("movie")));
                }
                else if (uri.getQueryParameter("movies") != null) {
                    try {
                        File file = new File(this.getExternalFilesDir(null), "hss_movies_tmp.json");
                        FileOutputStream outputStream = new FileOutputStream(file);
                        outputStream.write(uri.getQueryParameter("movies").getBytes());
                        outputStream.close();
                    } catch (Exception e) {
                        Log.e(TAG, "Failed to write file : " + e.getMessage());
                    }
                }
            }
            catch (Exception e) {
            }
        }

		mTwoPanes = findViewById(R.id.media_detail) != null;
        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.media_list);
        if (fragment == null){
            fragment = new MediaListFragment();
            getSupportFragmentManager().beginTransaction().add(R.id.media_list, fragment).commit();
        }

        if (entry != null && !entry.isDownload()) {
            Intent playerIntent = new Intent(this, HSSPlayerActivity.class);
            Bundle mBundle = new Bundle();
            mBundle.putSerializable("entry", entry);
            playerIntent.putExtras(mBundle);
            startActivity(playerIntent);
        }

        if (entry != null && entry.isDownload()){
            long did = HSSAgent.getDownloadManager().addDownload(entry.getUrl(), true);
            HSSDownload download = HSSAgent.getDownloadManager().getDownload(did);
            if (download != null) {
                if (entry.getMaxVideoBitrate() != 0)
                    download.setMaxVideoBitrate(entry.getMaxVideoBitrate());
                if (entry.getWidevineUrl() != "")
                    download.setWidevineLicenseUrl(entry.getWidevineUrl());
                if (entry.getMarlinUrl() != "")
                    download.setMarlinLicenseToken(entry.getMarlinUrl());
                if (entry.getPlayreadyUrl() != "")
                    download.setPlayreadyLicenseUrl(entry.getPlayreadyUrl());
                if (entry.getPlayreadyCustomData() != "")
                    download.setPlayreadyCustomData(entry.getPlayreadyCustomData());
                if (entry.getUserAgent() != "")
                    download.setUserAgent(entry.getUserAgent());
                if (entry.getPlayerParameters() != null){
                    if (entry.getPlayerParameters().containsKey(HSSPlayer.PLAYER_PARAM_HD_MAX_PIXELS)){
                        download.setHDPixelsLimit(Long.valueOf(entry.getPlayerParameters().get(HSSPlayer.PLAYER_PARAM_HD_MAX_PIXELS)));
                    }
                    if (entry.getPlayerParameters().containsKey(HSSPlayer.PLAYER_PARAM_HD_MAX_BITRATE)){
                        download.setHDBitrateLimit(Long.valueOf(entry.getPlayerParameters().get(HSSPlayer.PLAYER_PARAM_HD_MAX_BITRATE)));
                    }
                    if (entry.getPlayerParameters().containsKey(HSSPlayer.PLAYER_PARAM_HD_ROOT_ALLOWED)){
                        download.setHDRootAllowed(!"0".equals(entry.getPlayerParameters().get(HSSPlayer.PLAYER_PARAM_HD_ROOT_ALLOWED)));
                    }
                    if (entry.getPlayerParameters().containsKey(HSSPlayer.PLAYER_PARAM_HD_SW_DRM_ALLOWED)){
                        download.setHDSWDRMAllowed(!"0".equals(entry.getPlayerParameters().get(HSSPlayer.PLAYER_PARAM_HD_SW_DRM_ALLOWED)));
                    }

                }
                if (entry.getExternalSubtitleUrl() != null){
                    download.setExtraFileUrl(entry.getExternalSubtitleUrl());
                }
                if (entry.getDownloadAlgo() == "legacy")
                    HSSAgent.getDownloadManager().setQualitySelectionAlgorithm(HSSDownloadManager.QualityPresetAlgorithm.ALGORITHM_LEGACY);
            }
        }
        setIntent(null);
    }

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	@Override
	protected void onPause() {
		super.onPause();
	}

	@Override
	protected void onResume() {
		super.onResume();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
	  super.onCreateOptionsMenu(menu);
	  getMenuInflater().inflate(R.menu.cast_menu, menu);
	  return true;
	} 
	
	@Override
	public boolean shouldAllowSelection() {
		return mTwoPanes;
	}

	@Override
	public void onObjectSelected(Object object) {
		if (mTwoPanes){
			MediaDetailFragment f = (MediaDetailFragment)getSupportFragmentManager().findFragmentById(R.id.media_detail);
			if (f == null){
				f = new MediaDetailFragment();
				getSupportFragmentManager().beginTransaction().add(R.id.media_detail, f).commit();
			}
			f.updateObject(object);
		}else{
			MediaDetailFragment f = new MediaDetailFragment();
			f.updateObject(object);
			getSupportFragmentManager().beginTransaction().replace(R.id.media_list, f).addToBackStack(null).commit();
		}
	}
}