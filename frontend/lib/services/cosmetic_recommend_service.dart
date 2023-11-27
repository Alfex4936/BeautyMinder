import 'package:beautyminder/config.dart';
import 'package:beautyminder/services/auth_service.dart';
import 'package:beautyminder/services/shared_service.dart';
import 'package:dio/dio.dart';

import '../dto/cosmetic_model.dart';

class CosmeticSearchService {
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

  // Get All Cosmetics
  static Future<Result<List<Cosmetic>>> getAllCosmetics() async {
    // 로그인 상세 정보 가져오기
    final user = await SharedService.getUser();
    // AccessToken가지고오기
    final accessToken = await SharedService.getAccessToken();
    final refreshToken = await SharedService.getRefreshToken();

    final userId = user?.id ?? '-1';

    // final url = Uri.http(Config.apiURL, Config.CosmeticAPI).toString();
    final url = Uri.http(Config.apiURL, Config.RecommendAPI).toString();

    // 헤더 설정
    final headers = {
      'Authorization': 'Bearer ${Config.acccessToken}',
      'Cookie': 'XRT=${Config.refreshToken}',
      // 'Authorization': 'Bearer $accessToken',
      // 'Cookie': 'XRT=$refreshToken',
    };

    try {
      final response = await authClient.get(
        url,
        options: _httpOptions('GET', headers),
      );

      print("response : ${response.data}, statuscode : ${response.statusCode}");

      //print("token : $accessToken | $refreshToken");
      print("statuscode : ${response.statusCode}");

      if (response.statusCode == 200) {
        Map<String, dynamic> decodedResponse;

        if (response.data is List) {
          List<dynamic> dataList = response.data;
          //print("dataList : ${dataList}");
          List<Cosmetic> cosmetics = dataList.map<Cosmetic>((data) {
            if (data is Map<String, dynamic>) {
              return Cosmetic.fromJson(data);
            } else {
              throw Exception("Invalid data type");
            }
          }).toList();

          return Result.success(cosmetics);
        } else if (response.data is Map) {
          print("data is Map");
          decodedResponse = response.data;
        } else {
          print("failure");
          return Result.failure("Unexpected response data type");
        }

        return Result.failure(
            "Failed to serach Cosmetics : No cosmetics key in response");
      }
      return Result.failure("Failed to ge cosmeics");
    } catch (e) {
      print("CosmeticSearch_Service : ${e}");
      return Result.failure("An error Occured : $e");
    }
  }
}

class Result<T> {
  //T는 제네릭타입 반환 타입에 가변적으로 타입을 맞춰줌
  final T? value;
  final String? error;

  Result.success(this.value) : error = null;

  Result.failure(this.error) : value = null;
}
