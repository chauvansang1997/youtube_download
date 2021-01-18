package com.avenger.youtube_download;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Environment;
import android.text.TextUtils;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;

import com.yausername.ffmpeg.FFmpeg;
import com.yausername.youtubedl_android.DownloadProgressCallback;
import com.yausername.youtubedl_android.YoutubeDL;
import com.yausername.youtubedl_android.YoutubeDLException;
import com.yausername.youtubedl_android.YoutubeDLRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;

import io.flutter.Log;
import io.flutter.embedding.engine.plugins.FlutterPlugin;
import io.flutter.embedding.engine.plugins.activity.ActivityAware;
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.MethodChannel.MethodCallHandler;
import io.flutter.plugin.common.MethodChannel.Result;
import io.flutter.plugin.common.PluginRegistry.Registrar;

import java.io.File;
import java.io.IOException;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

import static android.content.ContentValues.TAG;

/**
 * YoutubeDownloadPlugin
 */
public class YoutubeDownloadPlugin implements FlutterPlugin, MethodCallHandler, ActivityAware {
    /// The MethodChannel that will the communication between Flutter and native Android
    ///
    /// This local reference serves to register the plugin with the Flutter Engine and unregister it
    /// when the Flutter Engine is detached from the Activity
    private MethodChannel channel;
    private Activity activity;

    @Override
    public void onAttachedToEngine(@NonNull FlutterPluginBinding flutterPluginBinding) {
        channel = new MethodChannel(flutterPluginBinding.getBinaryMessenger(), "youtube_download");
        channel.setMethodCallHandler(this);

    }


    @Override
    public void onMethodCall(@NonNull MethodCall call, @NonNull Result result) {
        if (call.method.equals("youtube.download")) {
            String url = call.argument("url");
            DownLoadTask downLoadTask = new DownLoadTask(activity, new DownLoadListener() {

                @Override
                public void onSuccess(String taskId) {
                    channel.invokeMethod("youtube.download_success", taskId);
                }

                @Override
                public void onProgress(String taskId, float progress, long total) {
                    try {
                        JSONObject json = new JSONObject();
                        json.put("taskId", taskId);
                        json.put("progress", progress);
                        json.put("total", total);
                        channel.invokeMethod("youtube.download_progress", json.toString());
                    } catch (JSONException e) {
                        channel.invokeMethod("youtube.json_error", taskId);
                    }

                }

                @Override
                public void onError(String taskId, String message) {
                    try {
                        JSONObject json = new JSONObject();
                        json.put("taskId", taskId);
                        json.put("error", message);
                        channel.invokeMethod("youtube.download_error", json.toString());
                    } catch (JSONException e) {
                        channel.invokeMethod("youtube.json_error", taskId);
                    }

                }

                @Override
                public void onUpdateSuccess() {
                    channel.invokeMethod("youtube.update_success", "Update success");
                }

                @Override
                public void onUpdateError(String error) {
                    channel.invokeMethod("youtube.update_error", error);
                }

                @Override
                public void onAlreadyUpdate() {
                    channel.invokeMethod("youtube.already_update", "Already update");
                }
            });
            downLoadTask.updateAndDownload(url);
            result.success(downLoadTask.taskId);
        } else {
            result.notImplemented();
        }
    }

    @Override
    public void onDetachedFromEngine(@NonNull FlutterPluginBinding binding) {
        channel.setMethodCallHandler(null);
    }

    @Override
    public void onAttachedToActivity(@NonNull ActivityPluginBinding binding) {
        activity = binding.getActivity();
        try {
            YoutubeDL.getInstance().init(activity);
            FFmpeg.getInstance().init(activity);
        } catch (YoutubeDLException e) {
            Log.e(TAG, "failed to initialize youtubedl-android", e);
        }
    }

    @Override
    public void onDetachedFromActivityForConfigChanges() {

    }

    @Override
    public void onReattachedToActivityForConfigChanges(@NonNull ActivityPluginBinding binding) {
        activity = binding.getActivity();
        try {
            YoutubeDL.getInstance().init(activity);
            FFmpeg.getInstance().init(activity);
        } catch (YoutubeDLException e) {
            Log.e(TAG, "failed to initialize youtubedl-android", e);
        }
    }

    @Override
    public void onDetachedFromActivity() {

    }
}
