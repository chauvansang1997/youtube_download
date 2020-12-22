package com.avenger.youtube_download;

import org.json.JSONException;

public interface DownLoadListener {
    void onSuccess(String taskId);
    
    void onProgress(String taskId, float progress, long total) ;
    
    void onError(String taskId, String message);
    
    
}
