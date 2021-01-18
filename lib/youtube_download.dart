import 'dart:async';
import 'dart:convert';

import 'package:flutter/cupertino.dart';
import 'package:flutter/services.dart';
import 'package:youtube_download/download_error_info.dart';
import 'package:youtube_download/download_progress_info.dart';

enum DownLoadStatus { DONE, ERROR }
enum UpdateStatus { DONE, ALREADY, ERROR }

class YoutubeDownload {
  StreamController<String> _errorStreamController;
  StreamController<UpdateStatus> _updateStatusStreamController;
  StreamController<DownloadErrorInfo> _downloadErrorStreamController;
  StreamController<String> _downloadSuccessStreamController;
  StreamController<DownloadProgressInfo> _progressDownloadStreamController;

  Stream<String> get errorStream => _errorStreamController.stream;

  Stream<UpdateStatus> get updateStatusStream =>
      _updateStatusStreamController.stream;

  Stream<DownloadErrorInfo> get downloadErrorStream =>
      _downloadErrorStreamController.stream;

  Stream<String> get downloadSuccessStream =>
      _downloadSuccessStreamController.stream;

  Stream<DownloadProgressInfo> get downloadProgressStream =>
      _progressDownloadStreamController.stream;

  MethodChannel _channel;

  static final YoutubeDownload _singleton = YoutubeDownload._internal();

  factory YoutubeDownload() {
    return _singleton;
  }

  YoutubeDownload._internal() {
    _errorStreamController = StreamController<String>.broadcast();
    _updateStatusStreamController = StreamController<UpdateStatus>.broadcast();
    _downloadErrorStreamController =
        StreamController<DownloadErrorInfo>.broadcast();
    _downloadSuccessStreamController = StreamController<String>.broadcast();
    _progressDownloadStreamController =
        StreamController<DownloadProgressInfo>.broadcast();
    _channel = const MethodChannel('youtube_download');
    _channel.setMethodCallHandler((MethodCall call) async {
      switch (call.method) {
        case 'youtube.download_error':
          Map<String, dynamic> json = jsonDecode(call.arguments);
          DownloadErrorInfo downloadErrorInfo =
          DownloadErrorInfo.fromJson(json);
          _downloadErrorStreamController.add(downloadErrorInfo);

          break;
        case 'youtube.json_error':

          DownloadErrorInfo downloadErrorInfo = DownloadErrorInfo(
              error: 'Convert json error', taskId: call.arguments);
          _downloadErrorStreamController.add(downloadErrorInfo);
          break;
        case 'youtube.update_success':
          _updateStatusStreamController.add(UpdateStatus.DONE);
          break;
        case 'youtube.update_error':
          _updateStatusStreamController.add(UpdateStatus.ERROR);
          break;
        case 'youtube.already_update':
          _updateStatusStreamController.add(UpdateStatus.ALREADY);
          break;
        case 'youtube.download_progress':
          Map<String, dynamic> json = jsonDecode(call.arguments);
          DownloadProgressInfo downloadProgressInfo =
              DownloadProgressInfo.fromJson(json);
          _progressDownloadStreamController.add(downloadProgressInfo);
          break;
        case 'youtube.download_success':
          _downloadSuccessStreamController.add(call.arguments);
          break;
      }
    });
  }

  ///return taskId
  Future<String> downloadUrl({@required String url}) async {
    return await _channel.invokeMethod('youtube.download', {'url': url});
  }

  void dispose() {
    _errorStreamController.close();
    _downloadSuccessStreamController.close();
    _progressDownloadStreamController.close();
    _downloadErrorStreamController.close();
  }
}
