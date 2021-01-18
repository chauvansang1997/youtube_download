class DownloadProgressInfo {
  String taskId;
  int progress;
  int total;

  DownloadProgressInfo.fromJson(Map<String, dynamic> json){
    taskId = json['taskId'];
    progress = json['progress'];
    total = json['total'];
  }
}
