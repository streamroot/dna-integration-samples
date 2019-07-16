package com.idviu.sample.hss;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.Serializable;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

public class Entry implements Serializable {

    private static final String TAG = "Entry";

    public boolean downloadForceLocalMode = false;
    public boolean streamrootDnaEnabled;
    public long downloadId = -1;

    private static final long serialVersionUID = 1L;

    private String title;
    private String url;
    private String licenseUrl;
    private String playreadyUrl;
    private String widevineUrl;
    private String marlinUrl;
    private String playreadyCustomData;
    private String widevineCustomData;
    private String userAgent;
    private String externalSubtitleUrl;
    private int startTime;
    private long maxVideoBitrate;
    private boolean download;
    private String downloadAlgo;
    private int bonusTime;
    private HashMap<String, String> headers;
    private ArrayList<String> filteredHeaders;
    private HashMap<String, String> playerParameters;

    public Entry(JSONObject content) {
        this.title = content.optString("title");
        this.url =  content.optString("url");
        this.licenseUrl = content.optString("license-url");
        this.playreadyUrl = content.optString("playready-url");
        this.playreadyCustomData = content.optString("playready-customdata");
        this.widevineCustomData = content.optString("widevine-customdata");
        this.externalSubtitleUrl = content.optString("external-subtitle-url");
        this.widevineUrl = content.optString("widevine-url");
        this.marlinUrl = content.optString("marlin-url");
        this.userAgent = content.optString("user-agent");
        this.startTime =  content.optInt("start-time") * 1000;
        this.download = Boolean.valueOf(content.optString("download"));
        this.downloadAlgo = content.optString("download-algo");
        this.maxVideoBitrate = content.optLong("max-video-bitrate");
        this.bonusTime = content.optInt("bonus-time") * 1000;

        JSONArray filteredHeaders = content.optJSONArray("http-headers-filter");
        if (filteredHeaders != null){
            this.filteredHeaders = new ArrayList<>();
            for (int i = 0; i < filteredHeaders.length(); i++){
                String v = filteredHeaders.optString(i);
                if (v != null){
                    this.filteredHeaders.add(v);
                }
            }
        }

        this.headers = new HashMap<String, String>();
        JSONObject headers = content.optJSONObject("headers");
        if (headers != null) {
            Iterator<String> keys = headers.keys();
            while (keys.hasNext()) {
                String key = (String) keys.next();
                this.headers.put(key, headers.optString(key));
            }
        }

        this.playerParameters = new HashMap<>();
        JSONObject parameters = content.optJSONObject("player-parameters");
        if (parameters != null) {
            Iterator<String> keys = parameters.keys();
            while (keys.hasNext()) {
                String key = (String) keys.next();
                this.playerParameters.put(key, parameters.optString(key));
            }
        }
    }

    public String getTitle() {
        return title;
    }

    public String getUrl() {
        return url;
    }

    public String getLicenseUrl() {
        return licenseUrl;
    }

    public String getPlayreadyUrl() {
        return playreadyUrl;
    }

    public String getWidevineUrl() {
        return widevineUrl;
    }

    public String getWidevineCustomData() {
		return widevineCustomData;
	}

    public String getMarlinUrl() {
        return marlinUrl;
    }

    public String getPlayreadyCustomData() {
        return playreadyCustomData;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public int getStartTime() {
        return startTime;
    }

    public boolean isDownload() {
        return download;
    }

    public String getDownloadAlgo() {
        return downloadAlgo;
    }

    public long getMaxVideoBitrate() {
        return maxVideoBitrate;
    }

    public String getExternalSubtitleUrl() { return externalSubtitleUrl; }

    public int getBonusTime() {
        return bonusTime;
    }

    public HashMap<String, String> getHeaders() {
        return headers;
    }

    public ArrayList<String> getFilteredHeaders() { return filteredHeaders; }

    public HashMap<String, String> getPlayerParameters() {
        return playerParameters;
    }

}