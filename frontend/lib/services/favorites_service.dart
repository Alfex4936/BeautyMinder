import 'package:beautyminder/services/shared_service.dart';
import 'package:dio/dio.dart';

import '../../config.dart';

class FavoritesService {
  // Dio 객체 생성
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

  //POST 방식으로 JSON 데이터 전송하는 일반 함수
  static Future<Response> postJson(String url, Map<String, dynamic> body,
      {Map<String, String>? headers}) {
    return client.post(
      url,
      options: _httpOptions('POST', headers),
      data: body,
    );
  }

  static Future<String> uploadFavorites(String cosmeticId) async {
    // 로그인 상세 정보 가져오기
    final user = await SharedService.getUser();
    // AccessToken가지고오기
    final accessToken = await SharedService.getAccessToken();
    final refreshToken = await SharedService.getRefreshToken();

    final userId = user?.id ?? '-1';
    // URL 생성
    final url = Uri.http(Config.apiURL, Config.uploadFavoritesAPI + cosmeticId)
        .toString();

    // 헤더 설정
    final headers = {
      'Authorization': 'Bearer ${Config.acccessToken}',
      'Cookie': 'XRT=${Config.refreshToken}',
      // 'Authorization': 'Bearer $accessToken',
      // 'Cookie': 'XRT=$refreshToken',
    };

    try {
      // POST 요청
      final response = await client.post(
        url,
        options: _httpOptions('POST', headers),
      );
      print("sdfjkldsjfkd : $response");

      if (response.statusCode == 200) {
        return "success upload user favorites";
      }
      return "Failed to upload user favorites";
    } catch (e) {
      return "An error occurred: $e";
    }
  }

  static Future<String> deleteFavorites(String cosmeticId) async {
    // 로그인 상세 정보 가져오기
    final user = await SharedService.getUser();
    // AccessToken가지고오기
    final accessToken = await SharedService.getAccessToken();
    final refreshToken = await SharedService.getRefreshToken();

    final userId = user?.id ?? '-1';
    // URL 생성
    final url = Uri.http(Config.apiURL, Config.uploadFavoritesAPI + cosmeticId)
        .toString();
// 헤더 설정
    final headers = {
      'Authorization': 'Bearer ${Config.acccessToken}',
      'Cookie': 'XRT=${Config.refreshToken}',
      // 'Authorization': 'Bearer $accessToken',
      // 'Cookie': 'XRT=$refreshToken',
    };

    try {
      // POST 요청
      final response = await client.delete(
        url,
        options: _httpOptions('DELETE', headers),
      );
      print("sdfjkldsjfkd : $response");

      if (response.statusCode == 200) {
        return "success deleted user favorites";
      }
      return "Failed to deleted user favorites";
    } catch (e) {
      return "An error occurred: $e";
    }
  }
}
