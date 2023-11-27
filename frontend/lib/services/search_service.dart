import 'package:dio/dio.dart';
import '../config.dart';
import '../dto/cosmetic_model.dart';
import '../dto/review_response_model.dart';

class SearchService {
  static final Dio client = Dio();
  static String accessToken = 'eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJpc3MiOiJiZWF1dHltaW5kZXIiLCJpYXQiOjE2OTk5NDQ2MzksImV4cCI6MTcwMDU0OTQzOSwic3ViIjoidG9rZW5AdGVzdCIsImlkIjoiNjU1MGFmZWYxYWI2ZDU4YjNmMTVmZTFjIn0.-tq20j-ZRmL9WRdBZEPrELjpxrbOJ0JUztzfGHCwLKM';
  static String refreshToken = 'eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJpc3MiOiJiZWF1dHltaW5kZXIiLCJpYXQiOjE2OTk5NDQ2MzksImV4cCI6MTcwMTE1NDIzOSwic3ViIjoidG9rZW5AdGVzdCIsImlkIjoiNjU1MGFmZWYxYWI2ZDU4YjNmMTVmZTFjIn0.dAXFUJI2vpjiQKakrRC_UTqgpG_BD_Df4vOeQq46HWQ';

  // 액세스 토큰 설정
  static void setAccessToken() {
    client.options.headers['Authorization'] = 'Bearer $accessToken';
  }


  // 이름으로 화장품 검색
  static Future<List<Cosmetic>> searchCosmeticsByName(String name) async {
    setAccessToken();
    final url = '${Config.apiURL}/search/cosmetic?name=$name';
    final response = await client.get(url);
    if (response.statusCode == 200) {
      List<dynamic> jsonData = response.data;
      return jsonData.map((data) => Cosmetic.fromJson(data)).toList();
    } else {
      throw Exception("Failed to search cosmetics by name");
    }
  }

  // 콘텐츠로 리뷰 검색
  static Future<List<ReviewResponse>> searchReviewsByContent(String content) async {
    setAccessToken();
    final url = '${Config.apiURL}/search/review?content=$content';
    final response = await client.get(url);
    if (response.statusCode == 200) {
      List<dynamic> jsonData = response.data;
      return jsonData.map((data) => ReviewResponse.fromJson(data)).toList();
    } else {
      throw Exception("Failed to search reviews by content");
    }
  }

  // 카테고리로 화장품 검색
  static Future<List<Cosmetic>> searchCosmeticsByCategory(String category) async {
    setAccessToken();
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
    setAccessToken();
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
    setAccessToken();
    final url = '${Config.apiURL}/search?anything=$anything';
    final response = await client.get(url);
    if (response.statusCode == 200) {
      return "Searched: $anything";
    } else {
      throw Exception("Failed to search anything");
    }
  }
}
