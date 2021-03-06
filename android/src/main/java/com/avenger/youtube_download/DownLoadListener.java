package com.avenger.youtube_download;

public interface DownLoadListener {
    void onSuccess(String taskId);
    
    void onProgress(String taskId, float progress, long total) ;
    
    void onError(String taskId, String message);
    
    void onUpdateSuccess();

    void onUpdateError( String error);

    void onAlreadyUpdate();

}
