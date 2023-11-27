import 'package:beautyminder/dto/baumann_model.dart';
import 'package:beautyminder/dto/gptReview_model.dart';
import 'package:dio/dio.dart';

import '../../config.dart';

class GPTReviewService {
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

  static Future<Result<GPTReviewInfo>> getGPTReviews(String id) async {

    // URL 생성
    final url = Uri.http(Config.apiURL, '${Config.getGPTReviewAPI}/$id').toString();
    print("******$url\n");
    try {
      // GET 요청
      final response = await client.get(
        url,
        // options: _httpOptions('GET', headers),
      );

      print("hehehehehehh-----$response");

      if (response.statusCode == 200) {
        // 정보 파싱
        // final user = SurveyWrapper.fromJson(response.data as Map<String, dynamic>);
        final gptReviewInfo = GPTReviewInfo.fromJson(response.data as Map<String, dynamic>);
        print("dfdssdfsdfsfdsfdsfds\n");
        print(gptReviewInfo);
        print("aaaa\n");

        return Result.success(gptReviewInfo);
      }
      print("fdffff\n");
      return Result.failure("Failed to get GPT review information");
    } catch (e) {
      print("pppppeoeoooekdkkdk\n");
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

  bool get isSuccess => value != null;
}
