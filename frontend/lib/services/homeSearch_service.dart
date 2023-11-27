import 'package:dio/dio.dart';

import '../config.dart';
import '../dto/cosmetic_model.dart';

class SearchService {
  static final Dio client = Dio();

  // 이름으로 화장품 검색
  static Future<List<Cosmetic>> searchCosmeticsByName(String name) async {
    final url = '${Config.apiURL}/search/cosmetic?name=$name';
    final response = await client.get(url);
    if (response.statusCode == 200) {
      List<dynamic> jsonData = response.data;
      return jsonData.map((data) => Cosmetic.fromJson(data)).toList();
    } else {
      throw Exception("Failed to search cosmetics by name");
    }
  }

  // // 콘텐츠로 리뷰 검색
  // static Future<List<Review>> searchReviewsByContent(String content) async {
  //   final url = '${Config.apiURL}/search/review?content=$content';
  //   final response = await client.get(url);
  //   if (response.statusCode == 200) {
  //     List<dynamic> jsonData = response.data;
  //     return jsonData.map((data) => Review.fromJson(data)).toList();
  //   } else {
  //     throw Exception("Failed to search reviews by content");
  //   }
  // }

  // 카테고리로 화장품 검색
  static Future<List<Cosmetic>> searchCosmeticsByCategory(String category) async {
    final url = '${Config.apiURL}/search/category?category=$category';
    final response = await client.get(url);
    if (response.statusCode == 200) {
      List<dynamic> jsonData = response.data;
      return jsonData.map((data) => Cosmetic.fromJson(data)).toList();
    } else {
      throw Exception("Failed to search cosmetics by category");
    }
  }

  // 키워드로 화장품 검색
  static Future<List<Cosmetic>> searchCosmeticsByKeyword(String keyword) async {
    final url = '${Config.apiURL}/search/keyword?keyword=$keyword';
    final response = await client.get(url);
    if (response.statusCode == 200) {
      List<dynamic> jsonData = response.data;
      return jsonData.map((data) => Cosmetic.fromJson(data)).toList();
    } else {
      throw Exception("Failed to search cosmetics by keyword");
    }
  }

  // 일반 검색
  static Future<String> searchAnything(String anything) async {
    final url = '${Config.apiURL}/search?anything=$anything';
    final response = await client.get(url);
    if (response.statusCode == 200) {
      return "Searched: $anything";
    } else {
      throw Exception("Failed to search anything");
    }
  }
}