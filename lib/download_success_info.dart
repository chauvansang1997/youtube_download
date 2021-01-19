class DownloadSuccessInfo {
  String taskId;
  int progress;
  int totalSeconds;

  DownloadSuccessInfo.fromJson(Map<String, dynamic> json){
    taskId = json['taskId'];
    progress = json['progress'];
    totalSeconds = json['total'];
  }
}
