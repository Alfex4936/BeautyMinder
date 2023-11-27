import 'package:beautyminder/config.dart';
import 'package:beautyminder/dto/cosmetic_model.dart';
import 'package:beautyminder/services/shared_service.dart';
import 'package:dio/dio.dart';

class SearchService {
  static final Dio client = Dio();

  // JSON 헤더 설정
  static const Map<String, String> jsonHeaders = {
    'Content-Type': 'application/json',
  };

  // 공통 HTTP 옵션 설정 함수
  static Options _httpOptions(String method, Map<String, String>? headers) {
    return Options(
      method: method,
      headers: headers,
    );
  }

  // 이름으로 화장품 검색
  static Future<List<Cosmetic>> searchCosmeticsByName(String name) async {
    // 로그인 상세 정보 가져오기
    final user = await SharedService.getUser();
    // AccessToken가지고오기
    final accessToken = await SharedService.getAccessToken();
    final refreshToken = await SharedService.getRefreshToken();

    final userId = user?.id ?? '-1';

    final parameters = {
      'name': '$name',
    };

    final url =
        Uri.http(Config.apiURL, Config.searchCosmeticsbyName, parameters)
            .toString();
// 헤더 설정
    final headers = {
      'Authorization': 'Bearer $accessToken',
      'Cookie': 'XRT=$refreshToken',
      // 'Authorization': 'Bearer $accessToken',
      // 'Cookie': 'XRT=$refreshToken',
    };

    final response =
        await client.get(url, options: _httpOptions('GET', headers));

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
  static Future<List<Cosmetic>> searchCosmeticsByCategory(
      String category) async {
    // 로그인 상세 정보 가져오기
    final user = await SharedService.getUser();
    // AccessToken가지고오기
    final accessToken = await SharedService.getAccessToken();
    final refreshToken = await SharedService.getRefreshToken();

    final userId = user?.id ?? '-1';

    final parameters = {
      'category': '$category',
    };

    final url =
        Uri.http(Config.apiURL, Config.searchCosmeticsbyCategory, parameters)
            .toString();

    // 헤더 설정
    final headers = {
      'Authorization': 'Bearer $accessToken',
      'Cookie': 'XRT=$refreshToken',
      // 'Authorization': 'Bearer $accessToken',
      // 'Cookie': 'XRT=$refreshToken',
    };

    final response = await client.get(
      url,
      options: _httpOptions('GET', headers),
    );

    if (response.statusCode == 200) {
      List<dynamic> jsonData = response.data;
      return jsonData.map((data) => Cosmetic.fromJson(data)).toList();
    } else {
      throw Exception("Failed to search cosmetics by category");
    }
  }

  // 키워드로 화장품 검색
  static Future<List<Cosmetic>> searchCosmeticsByKeyword(String keyword) async {
    // 로그인 상세 정보 가져오기
    final user = await SharedService.getUser();
    // AccessToken가지고오기
    final accessToken = await SharedService.getAccessToken();
    final refreshToken = await SharedService.getRefreshToken();

    final userId = user?.id ?? '-1';

    final parameters = {
      'keyword': '$keyword',
    };

    final url =
        Uri.http(Config.apiURL, Config.searchCosmeticsbyKeyword, parameters)
            .toString();

    // 헤더 설정
    final headers = {
      'Authorization': 'Bearer $accessToken',
      'Cookie': 'XRT=$refreshToken',
      // 'Authorization': 'Bearer $accessToken',
      // 'Cookie': 'XRT=$refreshToken',
    };

    final response = await client.get(
      url,
      options: _httpOptions('GET', headers),
    );

    if (response.statusCode == 200) {
      List<dynamic> jsonData = response.data;
      return jsonData.map((data) => Cosmetic.fromJson(data)).toList();
    } else {
      throw Exception("Failed to search cosmetics by keyword");
    }
  }

  // 일반 검색
  static Future<List<Cosmetic>> searchAnything(String anything) async {
    // 로그인 상세 정보 가져오기
    final user = await SharedService.getUser();
    // AccessToken가지고오기
    final accessToken = await SharedService.getAccessToken();
    final refreshToken = await SharedService.getRefreshToken();

    final userId = user?.id ?? '-1';

    final parameters = {
      'anything': '$anything',
    };

    final url = Uri.http(Config.apiURL, Config.homeSearchKeywordAPI, parameters)
        .toString();
// 헤더 설정
    final headers = {
      'Authorization': 'Bearer $accessToken',
      'Cookie': 'XRT=$refreshToken',
      // 'Authorization': 'Bearer $accessToken',
      // 'Cookie': 'XRT=$refreshToken',
    };

    try {
      final response =
          await client.get(url, options: _httpOptions("GET", headers));

      if (response.statusCode == 200) {
        List<dynamic> jsonData = response.data;
        return jsonData.map((data) => Cosmetic.fromJson(data)).toList();
      } else {
        throw Exception("Failed to search cosmetics by keyword");
      }
    } catch (e) {
      print("Error during cosmetics search: $e");
      // return []; // 빈 리스트를 반환하거나 다른 적절한 기본값을 반환할 수 있어요.
      return [];
    }
  }
}
