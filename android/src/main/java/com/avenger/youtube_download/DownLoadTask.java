
package com.avenger.youtube_download;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Environment;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;

import com.yausername.youtubedl_android.DownloadProgressCallback;
import com.yausername.youtubedl_android.YoutubeDL;
import com.yausername.youtubedl_android.YoutubeDLRequest;

import java.io.File;
import java.util.UUID;

import io.flutter.Log;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class DownLoadTask {
    private final CompositeDisposable compositeDisposable;
    private final DownLoadListener downLoadListener;
    private final Activity activity;
    public final String taskId;
    private boolean isUpdate;

    DownLoadTask(Activity activity, DownLoadListener downLoadListener) {
        this.activity = activity;
        this.downLoadListener = downLoadListener;
        taskId = UUID.randomUUID().toString();
        compositeDisposable = new CompositeDisposable();

        SharedPreferences sharedPref = this.activity.getPreferences(this.activity.MODE_PRIVATE);
        isUpdate = sharedPref.getBoolean("youtube_download_update", false);
        sharedPref.edit().putBoolean("youtube_download_update", true);
    }

    private DownloadProgressCallback callback = new DownloadProgressCallback() {
        @Override
        public void onProgressUpdate(float progress, long etaInSeconds) {
            activity.runOnUiThread(() -> {
                        Log.i("YoutubeDownloadPlugin", "Progress" + String.valueOf(progress));
                        if (downLoadListener != null) {
                            downLoadListener.onProgress(taskId, progress, etaInSeconds);
                        }
                    }
            );
        }
    };


    public boolean isStoragePermissionGranted() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (activity.checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
                return true;
            } else {
                ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                return false;
            }
        } else {
            return true;
        }
    }

    @NonNull
    private File getDownloadLocation() {
        File downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        File youtubeDLDir = new File(downloadsDir, "youtubedl-android");
        if (!youtubeDLDir.exists()) {
            boolean isCreate = youtubeDLDir.mkdir();
            if (!isCreate) {
                if (downLoadListener != null) {
                    downLoadListener.onError(taskId, "Can not create file");
                }
            }
        }
        return youtubeDLDir;
    }

    public void startDownload(String url) {
        if (!isStoragePermissionGranted()) {
            return;
        }

        YoutubeDLRequest request = new YoutubeDLRequest(url);
        File youtubeDLDir = getDownloadLocation();
        request.addOption("-o", youtubeDLDir.getAbsolutePath() + "/%(title)s.%(ext)s");

        Disposable disposable = Observable.fromCallable(() -> YoutubeDL.getInstance().execute(request, callback))
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(youtubeDLResponse -> {

                    downLoadListener.onSuccess(taskId);
                }, e -> {
                    downLoadListener.onError(taskId, e.toString());
                });
        compositeDisposable.add(disposable);
    }

    public void updateAndDownload(String url) {
        if (!isUpdate) {

            if (!isStoragePermissionGranted()) {
                return;
            }

            Disposable disposable = Observable.fromCallable(() -> YoutubeDL.getInstance().updateYoutubeDL(
                    activity))
                    .subscribeOn(Schedulers.newThread())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(status -> {
                        switch (status) {
                            case DONE:
                            case ALREADY_UP_TO_DATE:
                                startDownload(url);
                                SharedPreferences sharedPref = this.activity.getPreferences(Context.MODE_PRIVATE);
                                SharedPreferences.Editor editor = sharedPref.edit();
                                editor.putBoolean("youtube_download_update", true);
                                editor.apply();
                                isUpdate = true;
                                break;
                            default:
                                break;
                        }

                    }, e -> {
                        if (downLoadListener != null) {
                            downLoadListener.onUpdateError(e.toString());
                        }
                        startDownload(url);
                    });
            compositeDisposable.add(disposable);
        } else {
            startDownload(url);
        }
    }

    public void update() {
        Disposable disposable = Observable.fromCallable(() -> YoutubeDL.getInstance().updateYoutubeDL(
                activity))
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(status -> {
                    switch (status) {
                        case DONE:
                            if (downLoadListener != null) {
                                downLoadListener.onUpdateSuccess();
                            }
                            SharedPreferences sharedPref = this.activity.getPreferences(Context.MODE_PRIVATE);
                            SharedPreferences.Editor editor = sharedPref.edit();
                            editor.putBoolean("youtube_download_update", true);
                            editor.apply();
                            isUpdate = true;
                            break;
                        case ALREADY_UP_TO_DATE:
                            if (downLoadListener != null) {
                                downLoadListener.onAlreadyUpdate();
                            }
                            break;
                        default:

                            break;
                    }

                }, e -> {
                    if (downLoadListener != null) {
                        downLoadListener.onUpdateError(e.toString());
                    }
                });
        compositeDisposable.add(disposable);
    }

    public void destroy() {
        compositeDisposable.dispose();
    }
}
