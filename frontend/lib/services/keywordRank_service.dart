import 'package:dio/dio.dart';

import '../../config.dart';
import '../dto/keywordRank_model.dart';

class KeywordRankService {
  // Dio 객체 생성
  static final Dio client = Dio();

  // JSON 헤더 설정
  static const Map<String, String> jsonHeaders = {
    'Content-Type': 'application/json',
  };

  static Future<Result<KeyWordRank>> getKeywordRank() async {
    // URL 생성
    final url = Uri.http(Config.apiURL, Config.keywordRankAPI).toString();

    try {
      // GET 요청
      final response = await client.get(
        url,
      );

      if (response.statusCode == 200) {
        // 사용자 정보 파싱
        final user = KeyWordRank.fromJson(response.data);
        print(user);
        return Result.success(user);
      }

      return Result.failure("Failed to get user profile");

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

  bool get isSuccess => value != null;
}
