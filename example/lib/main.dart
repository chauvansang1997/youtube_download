import 'dart:async';

import 'package:flutter/material.dart';
import 'package:youtube_download/youtube_download.dart';

void main() {
  runApp(MyApp());
}

class MyApp extends StatefulWidget {
  @override
  _MyAppState createState() => _MyAppState();
}

class _MyAppState extends State<MyApp> {
  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      home: ExamplePage(),
    );
  }
}

class ExamplePage extends StatefulWidget {
  @override
  _ExamplePageState createState() => _ExamplePageState();
}

class _ExamplePageState extends State<ExamplePage> {
  YoutubeDownload _youtubeDownload;
  StreamSubscription _downloadProgressSubscription;
  final TextEditingController _textEditingController = TextEditingController();

  @override
  void initState() {
    super.initState();
    _textEditingController.text = 'https://www.youtube.com/watch?v=qBwAeqlJEeU';
    _youtubeDownload = YoutubeDownload();
    _youtubeDownload.downloadErrorStream.listen((event) {
      ScaffoldMessenger.of(context).removeCurrentSnackBar();
      print(event.error);
      ScaffoldMessenger.of(context).showSnackBar(SnackBar(
        content: Text(
            'Downloading task ${event.taskId} failed with error: ${event.error}'),
      ));
    });
    _downloadProgressSubscription =
        _youtubeDownload.downloadProgressStream.listen((event) {
      ScaffoldMessenger.of(context).removeCurrentSnackBar();
      ScaffoldMessenger.of(context).showSnackBar(SnackBar(
        content: Text(
            'Downloading task ${event.taskId} ${event.progress * 1.0 / event.total}%'),
      ));
    });
  }

  @override
  void dispose() {
    _downloadProgressSubscription.cancel();
    _textEditingController.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('Download youtube example app'),
      ),
      body: Center(
        child: Column(
          children: [
            TextField(
              controller: _textEditingController,
            ),
            FlatButton(
              child: Text('Download'),
              onPressed: () {
                _youtubeDownload.downloadUrl(url: _textEditingController.text);
              },
            )
          ],
        ),
      ),
    );
  }
}
