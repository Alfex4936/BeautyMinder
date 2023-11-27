import 'dart:convert';

import 'package:beautyminder/services/auth_service.dart';
import 'package:dio/dio.dart'; // DIO 패키지를 이용해 HTTP 통신

import '../../config.dart';
import '../dto/baumann_model.dart';
import 'shared_service.dart';

class BaumannService {
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

  // POST 방식으로 JSON 데이터 전송하는 일반 함수
  static Future<Response> _postJson(String url, Map<String, dynamic> body,
      {Map<String, String>? headers}) {
    return client.post(
      url,
      options: _httpOptions('POST', headers),
      data: body,
    );
  }

  // Get All Surveys
  static Future<Result<List<Baumann>>> getAllSurveys() async {
    final user = await SharedService.getUser();
    final accessToken = await SharedService.getAccessToken();
    final refreshToken = await SharedService.getRefreshToken();
    final userId = user?.id ?? '-1';

    // Create the URI with the query parameter
    final url =
    Uri.http(Config.apiURL, Config.baumannSurveyAPI, {'userId': userId}).toString();

    final headers = {
      'Authorization': 'Bearer $accessToken',
      'Cookie': 'XRT=$refreshToken',
    };

    try {
      final response = await authClient.get(
        url,
        options: _httpOptions('GET', headers),
      );

      print("response: ${response.data} ${response.statusCode}");
      print("token: $accessToken | $refreshToken");

      if (response.statusCode == 200) {
        Map<String, dynamic> decodedResponse;
        if (response.data is String) {
          decodedResponse = jsonDecode(response.data);
        } else if (response.data is Map) {
          decodedResponse = response.data;
        } else {
          return Result.failure("Unexpected response data type");
        }

        print("Baumann response: $decodedResponse");
        if (decodedResponse.containsKey('surveys')) {
          List<dynamic> surveyList = decodedResponse['surveys'];
          List<Baumann> surveys =
          surveyList.map((data) => Baumann.fromJson(data)).toList();
          print(surveys);
          return Result.success(surveys);
        }
        return Result.failure("Failed to get surveys: No surveys key in response");
      }
      return Result.failure("Failed to get surveys");
    } catch (e) {
      return Result.failure("An error occurred: $e");
    }
  }
}

// 결과 클래스
class Result<T> {
  final T? value;
  final String? error;

  Result.success(this.value) : error = null; // 성공
  Result.failure(this.error) : value = null; // 실패
}
