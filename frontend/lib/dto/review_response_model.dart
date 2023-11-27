import 'package:beautyminder/dto/cosmetic_model.dart';
import 'package:beautyminder/dto/user_model.dart';

class ReviewResponse {
  final String id;
  final String content;
  final int rating;
  final List<String> images;
  final User user;
  final Cosmetic cosmetic;
  final DateTime createdAt;
  final String nlpAnalysis; // NLP 분석 결과
  final bool isFiltered; // 필터링 여부

  ReviewResponse({
    required this.id,
    required this.content,
    required this.rating,
    required this.images,
    required this.user,
    required this.cosmetic,
    required this.createdAt,
    required this.nlpAnalysis,
    required this.isFiltered,
  });

  factory ReviewResponse.fromJson(Map<String, dynamic> json) {
    return ReviewResponse(
      id: json['id'] as String,
      content: json['content'] as String,
      rating: json['rating'] as int,
      images: (json['images'] as List<dynamic>)
          .map((item) => item as String)
          .toList(),
      user: User.fromJson(json['user'] as Map<String, dynamic>),
      cosmetic: Cosmetic.fromJson(json['cosmetic'] as Map<String, dynamic>),
      createdAt: DateTime.parse(json['createdAt'] as String),
      nlpAnalysis:
          json['nlpAnalysis'] != null ? json['nlpAnalysis'].toString() : '',
      isFiltered: json['filtered'] as bool? ?? false,
    );
  }
}
