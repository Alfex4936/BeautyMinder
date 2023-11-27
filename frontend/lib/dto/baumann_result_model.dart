class BaumannResult {
  final String id;
  final String date;
  final String userId;
  final DateTime createdAt;
  final String baumannType;
  final List<dynamic> surveyAnswers;
  final Map<String, dynamic> baumannScores;

  BaumannResult({
    required this.id,
    required this.date,
    required this.userId,
    required this.createdAt,
    required this.baumannType,
    required this.surveyAnswers,
    required this.baumannScores,
  });

  factory BaumannResult.fromJson(Map<String, dynamic> json) {
    return BaumannResult(
      id: json['id'] ?? '',
      date: json['date'] ?? '',
      userId: json['userId'] ?? '',
      createdAt: DateTime.tryParse(json['createdAt'] ?? '') ?? DateTime.now(),
      baumannType: json['baumannType'] ?? '',
      surveyAnswers: List<dynamic>.from(json['surveyAnswers'] ?? []),
      baumannScores: Map<String, dynamic>.from(json['baumannScores'] ?? {}),
    );
  }

  Map<String, dynamic> toJson() {
    return {
      'id': id,
      'date': date,
      'userId': userId,
      'createdAt': createdAt.toIso8601String(),
      'baumannType': baumannType,
      'surveyAnswers': surveyAnswers,
      'baumannScores': baumannScores,
    };
  }

  @override
  String toString() {
    return 'BaumannResult{id: $id,\n date: $date,\n userId: $userId,\n createdAt: $createdAt,\n baumannType: $baumannType,\n surveyAnswers: ${surveyAnswers?.join(', ')},\n baumannScores: $baumannScores}';
  }
}
