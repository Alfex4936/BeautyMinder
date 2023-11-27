import 'dart:ffi';

class BaumannResult {
  final String id;
  final String date;
  final String userId;
  final DateTime createdAt;
  final String baumannType;
  final List<Int32> surveyAnswers;
  final Object baumannScores;

  BaumannResult({
    required this.id,
    required this.date,
    required this.userId,
    required this.createdAt,
    required this.baumannType,
    required this.surveyAnswers,
    required this.baumannScores
  });

  factory BaumannResult.fromJson(Map<String, dynamic> json) {
    return BaumannResult(
      id: json['id'] ?? '',
      date: json['date'] ?? '',
      userId: json['userId'] ?? '',
      createdAt: json['createdAt'] ?? '',
      baumannType: json['baumannType'] ?? '',
      surveyAnswers: List<Int32>.from(json['surveyAnswers'] ?? []),
      baumannScores: json['baumannScores'] ?? '',
    );
  }

  Map<String, dynamic> toJson() {
    return {
      'id': id,
      'date': date,
      'userId': userId,
      'createdAt': createdAt,
      'baumannType': baumannType,
      'surveyAnswers': surveyAnswers,
      'baumannScores': baumannScores
    };
  }

  @override
  String toString() {
    return 'CosmeticModel{id: $id,\n date: $date,\n userId: $userId,\n createdAt: $createdAt,\n baumannType: $baumannType,\n surveyAnswers: ${surveyAnswers?.join(', ')},\n baumannScores: $baumannScores}';
  }
}
