
import 'dart:async';

import 'package:flutter/services.dart';

class YoutubeDownload {
  static const MethodChannel _channel =
      const MethodChannel('youtube_download');

  static Future<String> get platformVersion async {
    final String version = await _channel.invokeMethod('getPlatformVersion');
    return version;
  }
}
