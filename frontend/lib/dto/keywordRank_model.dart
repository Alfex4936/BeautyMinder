// API요청으로 받아오는 모델
import 'package:beautyminder/dto/cosmetic_model.dart';

class KeyWordRank {
  final List<String>? keywords;
  final String? updatedAt;

  KeyWordRank({
    required this.keywords,
    required this.updatedAt,
  });

  factory KeyWordRank.fromJson(Map<String, dynamic> json) {
    return KeyWordRank(
      keywords: List<String>.from(json['keywords']),
      updatedAt: json['updatedAt'],
    );
  }

  @override
  String toString() {
    return 'KeyWordRank{keywords: ${keywords?.join(', ')}, updatedAt: $updatedAt';
  }
}

class ProductRank {
  final List<Cosmetic> cosmetics;
  final String? updatedAt;

  ProductRank({
    required this.cosmetics,
    required this.updatedAt,
  });

  factory ProductRank.fromJson(Map<String, dynamic> json) {
    List<Cosmetic> cosmeticList = (json['cosmetics'] as List<dynamic>? ?? [])
        .map((item) => Cosmetic.fromJson(item as Map<String, dynamic>))
        .toList();
    return ProductRank(
      cosmetics: cosmeticList,
      updatedAt: json['updatedAt'],
    );
  }

  @override
  String toString() {
    return 'ProductRank{cosmetics: $cosmetics, updatedAt: $updatedAt';
  }
}
