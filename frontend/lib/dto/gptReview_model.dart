// API요청으로 받아오는 모델
import 'package:beautyminder/dto/cosmetic_model.dart';

class GPTReviewInfo {
  final String id;
  final String positive;
  final String negative;
  final String gptVersion;
  final DateTime createAt;
  final Cosmetic cosmetic;

  GPTReviewInfo(
      {required this.id,
      required this.positive,
      required this.negative,
      required this.gptVersion,
      required this.createAt,
      required this.cosmetic});

  factory GPTReviewInfo.fromJson(Map<String, dynamic> json) {
    return GPTReviewInfo(
      id: json["id"],
      positive: json["positive"],
      negative: json["negative"],
      gptVersion: json["gptVersion"],
      createAt: DateTime.parse(json["createdAt"]),
      cosmetic: Cosmetic.fromJson(json["cosmetic"]),
    );
  }

  @override
  String toString() {
    return 'GPTReviewInfo{id: $id, positive: $positive, negative: $negative, gptVersion: $gptVersion, createAt: $createAt, cosmetic: $cosmetic}';
  }
}
