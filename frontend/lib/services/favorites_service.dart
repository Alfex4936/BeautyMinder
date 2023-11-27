import 'package:beautyminder/dto/baumann_model.dart';
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
      {Map<String, String>? headers}){

    return client.post(
      url,
      options: _httpOptions('POST', headers),
      data: body,
    );
  }

  static Future<String> uploadFavorites(String cosmeticId) async {
    // 유저 정보 가지고 오기
    final user = await SharedService.getUser();
    // AccessToken가지고오기
    // final accessToken = await SharedService.getAccessToken();
    final accessToken = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJpc3MiOiJiZWF1dHltaW5kZXIiLCJpYXQiOjE2OTk5NDQ2MzksImV4cCI6MTcwMDU0OTQzOSwic3ViIjoidG9rZW5AdGVzdCIsImlkIjoiNjU1MGFmZWYxYWI2ZDU4YjNmMTVmZTFjIn0.-tq20j-ZRmL9WRdBZEPrELjpxrbOJ0JUztzfGHCwLKM";
    //refreshToken 가지고오기
    // final refreshToken = await SharedService.getRefreshToken();
    final refreshToken = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJpc3MiOiJiZWF1dHltaW5kZXIiLCJpYXQiOjE2OTk5NDQ2MzksImV4cCI6MTcwMTE1NDIzOSwic3ViIjoidG9rZW5AdGVzdCIsImlkIjoiNjU1MGFmZWYxYWI2ZDU4YjNmMTVmZTFjIn0.dAXFUJI2vpjiQKakrRC_UTqgpG_BD_Df4vOeQq46HWQ";

    // URL 생성
    final url = Uri.http(Config.apiURL, Config.uploadFavoritesAPI+cosmeticId).toString();

    final headers = {
      'Authorization': 'Bearer $accessToken',
      'Cookie': 'XRT=$refreshToken',
    };

    try {
      // POST 요청
      final response = await client.post(
        url,
        options: _httpOptions('POST', headers),
      );
      print("sdfjkldsjfkd : $response");

      if (response.statusCode == 200) {
        return "success";
      }
      return "Failed to upload user favorites";
    } catch (e) {
      return "An error occurred: $e";
    }
  }

}
