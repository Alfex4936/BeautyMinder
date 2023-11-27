// API요청으로 받아오는 모델
import 'dart:ffi';

import '../models/login_response_model.dart';
import 'cosmetic_model.dart';

class UsersReviewInfo{
  final String id;
  final String content;
  final Int32 rating;
  final List<String>? images;
  final User user;
  final Cosmetic cosmetic;
  final DateTime createdAt;
  // final nlpAnalysis
  final Bool filtered;

  UsersReviewInfo({
    required this.id,
    required this.content,
    required this.rating,
    required this.images,
    required this.user,
    required this.cosmetic,
    required this.createdAt,
    // required this.nlpAnalysis,
    required this.filtered,
  });

  factory UsersReviewInfo.fromJson(Map<String, dynamic> json){
    return UsersReviewInfo(
      id: json["id"],
      content: json["content"],
      rating: json["rating"],
      images: json["images"],
      user: User.fromJson(json["user"]),
      cosmetic: Cosmetic.fromJson(json["cosmetic"]),
      createdAt: DateTime.parse(json["createdAt"]),
      // nlpAnalysis: Cosmetic.fromJson(json["nlpAnalysis"]),
      filtered: json["filtered"],
    );
  }

  @override
  String toString() {
    return 'UsersReviewInfo{id: $id, content: $content, rating: $rating, images: $images, user: $user, cosmetic: $cosmetic, createdAt: $createdAt, filtered: $filtered}';
  }

}