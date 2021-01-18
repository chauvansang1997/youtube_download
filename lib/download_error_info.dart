class DownloadErrorInfo {
  DownloadErrorInfo({this.error, this.taskId});

  DownloadErrorInfo.fromJson(Map<String, dynamic> json) {
    taskId = json['taskId'];
    error = json['error'];
  }

  String taskId;
  String error;
}
