import 'package:beautyminder/dto/user_model.dart';
import 'package:beautyminder/services/shared_service.dart';
import 'package:dio/dio.dart';

import '../../config.dart';

class HomeService {
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

  static Future<HomePageResult<User>> getUserInfo(String userId) async {
    // 로그인 상세 정보 가져오기
    final user = await SharedService.getUser();
    // AccessToken가지고오기
    final accessToken = await SharedService.getAccessToken();
    final refreshToken = await SharedService.getRefreshToken();

    final userId = user?.id ?? '-1';

    // URL 생성
    final url = Uri.http(Config.apiURL, Config.getUserInfo + userId).toString();

    // 헤더 설정
    final headers = {
      'Authorization': 'Bearer ${Config.acccessToken}',
      'Cookie': 'XRT=${Config.refreshToken}',
      // 'Authorization': 'Bearer $accessToken',
      // 'Cookie': 'XRT=$refreshToken',
    };

    try {
      // GET 요청
      final response = await client.get(
        url,
        options: _httpOptions('GET', headers),
      );

      print("Here is HomeService response : $response");

      if (response.statusCode == 200) {
        // 사용자 정보 파싱
        final user = User.fromJson(response.data as Map<String, dynamic>);
        print("Here is HomeService user : $user");
        return HomePageResult.success(user);
      }
      return HomePageResult.failure("Failed to get user profile");
    } catch (e) {
      return HomePageResult.failure("An error occurred: $e");
    }
  }
}

// 결과 클래스
class HomePageResult<T> {
  final T? value;
  final String? error;

  HomePageResult.success(this.value) : error = null; // 성공
  HomePageResult.failure(this.error) : value = null; // 실패

  bool get isSuccess => value != null;
}
