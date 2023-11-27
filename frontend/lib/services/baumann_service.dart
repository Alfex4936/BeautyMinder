import 'package:beautyminder/dto/baumann_model.dart';
import 'package:beautyminder/dto/baumann_result_model.dart';
import 'package:beautyminder/services/shared_service.dart';
import 'package:dio/dio.dart';

import '../../config.dart';

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

  //POST 방식으로 JSON 데이터 전송하는 일반 함수
  static Future<Response> postJson(String url, Map<String, dynamic> body,
      {Map<String, String>? headers}) {
    return client.post(
      url,
      options: _httpOptions('POST', headers),
      data: body,
    );
  }

  static Future<BaumResult<SurveyWrapper>> getBaumannSurveys() async {
    // URL 생성
    final url = Uri.http(Config.apiURL, Config.baumannSurveyAPI).toString();

    try {
      // GET 요청
      final response = await client.get(
        url,
        // options: _httpOptions('GET', headers),
      );

      if (response.statusCode == 200) {
        // 사용자 정보 파싱
        final user =
            SurveyWrapper.fromJson(response.data as Map<String, dynamic>);
        print(user);
        return BaumResult.success(user);
      }
      return BaumResult.failure("Failed to get user profile");
    } catch (e) {
      return BaumResult.failure("An error occurred: $e");
    }
  }

  static Future<BaumResult<List<BaumannResult>>> getBaumannHistory() async {
    // 로그인 상세 정보 가져오기
    final user = await SharedService.getUser();
    // AccessToken가지고오기
    final accessToken = await SharedService.getAccessToken();
    final refreshToken = await SharedService.getRefreshToken();

    final userId = user?.id ?? '-1';

    // URL 생성
    final url = Uri.http(Config.apiURL, Config.baumannHistoryAPI).toString();
    print("This is BaumannService : $url");

    // 헤더 설정
    final headers = {
      'Authorization': 'Bearer ${Config.acccessToken}',
      'Cookie': 'XRT=${Config.refreshToken}',
      // 'Authorization': 'Bearer $accessToken',
      // 'Cookie': 'XRT=$refreshToken',
    };

    try {
      print("1");
      // GET 요청
      final response = await client.get(
        url,
        options: _httpOptions('GET', headers),
      );
      print("2");

      if (response.statusCode == 200) {
        print("3");
        print("${response.data}");

        // 사용자 정보 파싱
        // final result = BaumannResult.fromJson(response.data as Map<String, dynamic>);
        final List<dynamic> jsonData = response.data as List<dynamic>;
        final List<BaumannResult> result = jsonData
            .map((dynamic item) =>
                BaumannResult.fromJson(item as Map<String, dynamic>))
            .toList();

        print("This is Baumann Service(getHistory) : $result");

        return BaumResult<List<BaumannResult>>.success(result);
      }
      return BaumResult<List<BaumannResult>>.failure(
          "Failed to get baumann history");
    } catch (e) {
      print("An error occurred: $e");
      return BaumResult<List<BaumannResult>>.failure("An error occurred: $e");
    }
  }
}

// 결과 클래스
class BaumResult<T> {
  final T? value;
  final String? error;

  BaumResult.success(this.value) : error = null; // 성공
  BaumResult.failure(this.error) : value = null; // 실패

  bool get isSuccess => value != null;
}
