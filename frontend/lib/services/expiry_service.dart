import 'package:dio/dio.dart';
import '../config.dart';
import '../dto/cosmetic_expiry_model.dart';

class ExpiryService {
  static final Dio client = Dio();
  static String accessToken = 'eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJpc3MiOiJiZWF1dHltaW5kZXIiLCJpYXQiOjE2OTk5NDQ2MzksImV4cCI6MTcwMDU0OTQzOSwic3ViIjoidG9rZW5AdGVzdCIsImlkIjoiNjU1MGFmZWYxYWI2ZDU4YjNmMTVmZTFjIn0.-tq20j-ZRmL9WRdBZEPrELjpxrbOJ0JUztzfGHCwLKM';
  static String refreshToken = 'eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJpc3MiOiJiZWF1dHltaW5kZXIiLCJpYXQiOjE2OTk5NDQ2MzksImV4cCI6MTcwMTE1NDIzOSwic3ViIjoidG9rZW5AdGVzdCIsImlkIjoiNjU1MGFmZWYxYWI2ZDU4YjNmMTVmZTFjIn0.dAXFUJI2vpjiQKakrRC_UTqgpG_BD_Df4vOeQq46HWQ';

  // 액세스 토큰 설정
  static void setAccessToken() {
    client.options.headers['Authorization'] = 'Bearer $accessToken';
  }

  static const Map<String, String> jsonHeaders = {
    'Content-Type': 'application/json',
  };

  static Options _httpOptions(String method, Map<String, String>? headers) {
    return Options(
      method: method,
      headers: headers,
    );
  }

  // Create Expiry Item
  // Create Expiry Item
  static Future<CosmeticExpiry> createCosmeticExpiry(CosmeticExpiry expiry) async {

    setAccessToken();

    final url = Uri.http(Config.apiURL, Config.createCosmeticExpiryAPI).toString();

    try {
      final response = await client.post(url, options: _httpOptions('POST', jsonHeaders), data: expiry.toJson());
      if (response.statusCode == 200) {
        return CosmeticExpiry.fromJson(response.data);
      } else {
        // 에러 메시지에 응답 본문을 포함시킵니다.
        throw Exception("Failed to create cosmetic expiry: Status Code ${response.statusCode}, Data: ${response.data}");
      }
    } on DioError catch (e) {
      // DioError를 캐치하여 더 많은 정보를 로그로 남깁니다.
      print('DioError caught: ${e.response?.data}');
      print('Headers: ${e.response?.headers}');
      print('Request Options: ${e.response?.requestOptions}');
      throw Exception("Failed to create cosmetic expiry: ${e.message}");
    }
  }


  // Get all Expiry Items by UserId
  static Future<List<CosmeticExpiry>> getAllExpiriesByUserId(String userId) async {
    setAccessToken();

    final url = Uri.http(Config.apiURL, Config.getAllExpiryByUserIdAPI + userId).toString();

    final response = await client.get(url, options: _httpOptions('GET', jsonHeaders));
    if (response.statusCode == 200) {
      print("Server response: ${response.data}"); // 로그 출력
      List<dynamic> jsonData = response.data;
      return jsonData.map((data) => CosmeticExpiry.fromJson(data)).toList();
    } else {
      throw Exception("Failed to get expiries by user ID");
    }
  }

  // Get an expiry item by UserId and ExpiryId
  static Future<CosmeticExpiry> getExpiryByUserIdAndExpiryId(String userId, String expiryId) async {
    setAccessToken();

    final url = Uri.http(Config.apiURL, Config.getAllExpiryByUserIdAPI + userId + Config.getExpiryByUserIdandExpiryIdAPI + expiryId).toString();

    final response = await client.get(url, options: _httpOptions('GET', jsonHeaders));
    if (response.statusCode == 200) {
      return CosmeticExpiry.fromJson(response.data);
    } else {
      throw Exception("Failed to get expiry by user ID and expiry ID");
    }
  }

  // Update an expiry item by UserId and ExpiryId
  static Future<CosmeticExpiry> updateExpiryByUserIdAndExpiryId(String userId, String expiryId, CosmeticExpiry updatedExpiry) async {
    setAccessToken();

    final url = Uri.http(Config.apiURL, Config.getAllExpiryByUserIdAPI + userId + Config.getExpiryByUserIdandExpiryIdAPI + expiryId).toString();

    final response = await client.put(url, options: _httpOptions('PUT', jsonHeaders), data: updatedExpiry.toJson());
    if (response.statusCode == 200) {
      return CosmeticExpiry.fromJson(response.data);
    } else {
      throw Exception("Failed to update expiry");
    }
  }

  // Delete an expiry item by UserId and ExpiryId
  static Future<void> deleteExpiryByUserIdAndExpiryId(String userId, String expiryId) async {
    setAccessToken();
    final url = Uri.http(Config.apiURL, Config.getAllExpiryByUserIdAPI + userId + Config.getExpiryByUserIdandExpiryIdAPI + expiryId).toString();
    final response = await client.delete(url, options: _httpOptions('DELETE', jsonHeaders));
    if (response.statusCode != 200) {
      throw Exception("Failed to delete expiry");
    }
  }


}

