class DownloadSuccessInfo {
  String taskId;
  int progress;
  int total;

  DownloadSuccessInfo.fromJson(Map<String, dynamic> json){
    taskId = json['taskId'];
    progress = json['progress'];
    total = json['total'];
  }
}
