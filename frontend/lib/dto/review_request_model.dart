import 'dart:io';

class ReviewRequest {
  final String content;
  final int rating;
  final String cosmeticId; // 새로 추가
  final String userId; // 새로 추가

  ReviewRequest({
    required this.content,
    required this.rating,
    required this.cosmeticId, // 새로 추가
    required this.userId, // 새로 추가
  });

  Map<String, dynamic> toJson() {
    return {
      'content': content,
      'rating': rating,
      'cosmeticId': cosmeticId, // 맵에 추가
      'userId': userId, // 맵에 추가
    };
  }
}


