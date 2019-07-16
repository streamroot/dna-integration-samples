package com.idviu.sample.hss;

import android.util.Log;

import com.labgency.hss.HSSAgent;
import com.labgency.hss.HSSDownload;
import com.labgency.hss.views.HSSPlayerView;

import io.streamroot.dna.core.BandwidthListener;

public class IdviuBandwithMeter implements BandwidthListener {
    private HSSPlayerView mPlayerView = null;

    public IdviuBandwithMeter(HSSPlayerView mPlayerView) {
        this.mPlayerView = mPlayerView;
    }

    @Override
    public void onBandwidthChange(long bandwith) {
        long dldId = HSSAgent.getDownloadManager().addDownload(mPlayerView.getPlaylist().items().get(0).url(), true);
        HSSDownload download = HSSAgent.getDownloadManager().getDownload(dldId);
        if (download != null){
            download.setMaxVideoBitrate(bandwith);
            HSSAgent.getDownloadManager().unpauseDownload(dldId);
        }
    }
}
