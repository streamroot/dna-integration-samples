package com.idviu.sample.hss.fragments;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.labgency.hss.HSSAgent;
import com.labgency.hss.HSSDownload;
import com.labgency.hss.downloads.HSSDownloadError;
import com.labgency.hss.downloads.HSSDownloadState;
import com.labgency.hss.downloads.HSSDownloadStatus;
import com.labgency.hss.listeners.HSSDownloadListener;
import com.idviu.sample.hss.Entry;
import com.idviu.sample.hss.R;

public class MediaListFragment extends Fragment implements OnItemClickListener, HSSDownloadListener {

	private static final String TAG = "MediaListFragment";

	private ListView mList = null;
	private List<Entry> mMovies = null;
	private List<HSSDownload> mDownloads = null;
	private MediaListFragmentCallback mCallback = null;
	private MovieAdapter mAdapter = null;
	private int mListIndex = 0;
	private int mListTop = 0;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}
	
	private void loadMovies(){
		ArrayList<Entry> movies = new ArrayList<Entry>();
		Activity activity = getActivity();
		if (activity != null){
			try {
				JSONArray arr = null;
				// check before is movies_temp.json is available
				InputStream is;
				File file = new File(getContext().getExternalFilesDir(null), "hss_movies_tmp.json");
				if ( file.exists() )
					is = new FileInputStream(file);
				else
					is = activity.getAssets().open("movies.json");

				ByteArrayOutputStream bos = new ByteArrayOutputStream();
				int read;
				byte[] buffer = new byte[64];
				while ((read = is.read(buffer)) > 0) {
					bos.write(buffer, 0, read);
				}
				arr = new JSONArray(new String(bos.toByteArray()));

				for (int i = 0; i < arr.length(); i++){
					JSONObject o = arr.getJSONObject(i);
					Entry sm = new Entry(arr.getJSONObject(i));
					movies.add(sm);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		updateData(movies, HSSAgent.getDownloadManager().getAllDownloads());
	}	
	
	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		if (mList != null){
			mListIndex = mList.getFirstVisiblePosition();
			View v = mList.getChildAt(0);
			mListTop = (v == null) ? 0 : v.getTop();
			outState.putInt("list_index", mListIndex);
			outState.putInt("list_top", mListTop);
		}
	}	
	
	@Override
	public void onDestroyView() {
		super.onDestroyView();
		if (mList != null){
			mListIndex = mList.getFirstVisiblePosition();
			View v = mList.getChildAt(0);
			mListTop = (v == null) ? 0 : v.getTop();
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.media_list_fragment, container, false);
		return view;
	}	

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		mList = (ListView) view.findViewById(R.id.list_media);	
		mList.setOnItemClickListener(this);
		if (mCallback != null){
			mList.setChoiceMode(mCallback.shouldAllowSelection() ? ListView.CHOICE_MODE_SINGLE : ListView.CHOICE_MODE_NONE);
		}
		if (savedInstanceState != null){
			mListIndex = savedInstanceState.getInt("list_index", -1);
			mListTop = savedInstanceState.getInt("list_top", 0);
		}
		updateData(mMovies, mDownloads);
		mList.setAdapter(mAdapter);
		if (mListIndex >= 0){
			mList.setSelectionFromTop(mListIndex, mListTop);
		}
	}	
	
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		mCallback = (MediaListFragmentCallback) activity;
		if (mList != null){
			mList.setChoiceMode(mCallback.shouldAllowSelection() ? ListView.CHOICE_MODE_SINGLE : ListView.CHOICE_MODE_NONE);
		}
		HSSAgent.getDownloadManager().registerDownloadListener(this);
		loadMovies();
	}
	
	

	@Override
	public void onDetach() {
		super.onDetach();
		mCallback = null;
		HSSAgent.getDownloadManager().unregisterDownloadListener(this);
	}
	
	public void updateData(List<Entry> movies, List<HSSDownload> downloads){
		mMovies = movies;
		mDownloads = downloads;
		if (mAdapter == null){
			mAdapter = new MovieAdapter(getActivity(), movies, mDownloads);
			if (mList != null) mList.setAdapter(mAdapter);
		}else{
			mAdapter.switchMovies(movies, mDownloads);
		}
		if (mList != null && mListIndex >= 0 && mAdapter.getCount() > 0){
			mListIndex = -1;
			mListTop = 0;
			mList.setSelectionFromTop(mListIndex, mListTop);
		}
	}
	
	public interface MediaListFragmentCallback{
		/**
		 * Checks whether the hosting activity is a two or single pane layout
		 * @return true for two-panes
		 */
		public boolean shouldAllowSelection();
		
		/**
		 * Called when a movie or download is selected in the list
		 * @param object the selected movie or download
		 */
		public void onObjectSelected(Object object); 
		
	}
	
	public static class MovieAdapter extends BaseAdapter{
		
		private List<Entry> mMovies = null;
		private List<HSSDownload> mDownloads = null;
		private LayoutInflater mInflater = null;
		
		public MovieAdapter(Context context, List<Entry> movies, List<HSSDownload> downloads){
			mMovies = movies;
			mDownloads = downloads;
			mInflater = LayoutInflater.from(context);
		}		

		@Override
		public boolean hasStableIds() {
			return false;
		}

		/**
		 * Gets the index of the movies (from catalog) header
		 * @return
		 */
		private int getMoviesIndex(){
			return mDownloads == null ? 1 : mDownloads.size() + 1;
		}
		
		@Override
		public int getItemViewType(int position) {
			if (position == 0 || position == getMoviesIndex()){
				return 0;
			}else{
				return 1;
			}
		}

		@Override
		public int getViewTypeCount() {
			return 2; //0: headers, 1: entries
		}

		@Override
		public int getCount() {
			return (mMovies == null ? 0 : mMovies.size() ) + (mDownloads == null ? 0 : mDownloads.size()) + 2;
		}

		@Override
		public Object getItem(int position) {
			int moviesIndex = getMoviesIndex();
			if (position == 0){
				return "Downloads";
			}else if (position == moviesIndex){
				return "Movies";
			}else if (position < moviesIndex){
				return mDownloads.get(position - 1);
			}else if (position > moviesIndex){
				return mMovies.get(position - moviesIndex - 1);
			}
			return null;
		}

		@Override
		public long getItemId(int position) {
			return position;
		}
		
		
		@Override
		public boolean isEnabled(int position) {
			return getItemViewType(position) == 1;
 		}

		public void switchMovies(List<Entry> movies, List<HSSDownload> downloads){
			mMovies = movies;
			mDownloads = downloads;
			notifyDataSetChanged();
		}
		
		private View getViewHeader(int position, View convertView, ViewGroup parent){
			if (convertView == null){
				convertView = mInflater.inflate(R.layout.header_entry, parent, false);
			}
			if (position == 0){
				((TextView)convertView).setText("Downloads");
			}else{
				((TextView)convertView).setText("Movies");
			}
			return convertView;
		}
		
		private View getViewEntry(int position, View convertView, ViewGroup parent){
			ViewHolder holder = null;
			if (convertView == null){
				convertView = mInflater.inflate(R.layout.media_entry, parent, false);
				holder = new ViewHolder();
				holder.title = (TextView) convertView.findViewById(R.id.title);
				holder.cid = (TextView) convertView.findViewById(R.id.cid);
				convertView.setTag(holder);
			}else{
				holder = (ViewHolder) convertView.getTag();
			}

			Object o = getItem(position);
			if (o instanceof Entry){
				Entry m = (Entry)o;
				holder.title.setText(m.getTitle());
			}
			else{
				HSSDownload d = (HSSDownload)o;
				holder.title.setText("Download " + d.getId());
			}
			return convertView;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			if (getItemViewType(position) == 0){
				return getViewHeader(position, convertView, parent);
			}else{
				return getViewEntry(position, convertView, parent);
			}
		}
		
		public static class ViewHolder{
			public TextView title;
			public TextView cid;
		}
		
	}

	@Override
	public void onItemClick(AdapterView<?> adapter, View view, int position, long id) {
		if (mCallback != null){
			mCallback.onObjectSelected(adapter.getItemAtPosition(position));
		}
	}

	@Override
	public void onDownloadErrorChanged(HSSDownload download, HSSDownloadError error) {
		
	}

	@Override
	public void onDownloadProgressChanged(HSSDownload download, long downloaded, long total, double percent) {
		
	}

	@Override
	public void onDownloadStateChanged(HSSDownload download, HSSDownloadState state) {
		if (state == HSSDownloadState.REMOVED){
			if (getActivity() != null){
				getActivity().runOnUiThread(new Runnable(){
					public void run(){
						updateData(mMovies, HSSAgent.getDownloadManager().getAllDownloads());
					}
				});
			}
		}
	}

	@Override
	public void onDownloadStatusChanged(HSSDownload download, HSSDownloadStatus status) {
		if (status == HSSDownloadStatus.STATUS_INIT){ //new download
			if (getActivity() != null){
				getActivity().runOnUiThread(new Runnable(){
					public void run(){
						updateData(mMovies, HSSAgent.getDownloadManager().getAllDownloads());
					}
				});
			}
		}
	}

}
