// DioClient.class

import 'package:dio/dio.dart';

import '../config.dart'; // DIO 패키지를 이용해 HTTP 통신

class DioClient {
  static final Dio client = Dio(
    // Make this static
    BaseOptions(
      baseUrl: Config.apiURL,
      connectTimeout: const Duration(seconds: 30),
      receiveTimeout: const Duration(seconds: 30),
    ),
  );

  // JSON 헤더 설정
  static const Map<String, String> jsonHeaders = {
    'Content-Type': 'application/json',
  };

  // 모든 http 요청 하나의 함수로
  static Future<Response> sendRequest(
    String method,
    String url, {
    dynamic body, // Map<String, dynamic> or FormData 다 accept
    Map<String, String>? headers,
  }) async {
    Options options = httpOptions(method, headers);

    try {
      switch (method.toUpperCase()) {
        case 'GET':
          return client.get(url, options: options);
        case 'POST':
          return client.post(url, data: body, options: options);
        case 'PUT':
          return client.put(url, data: body, options: options);
        case 'DELETE':
          return client.delete(url, data: body, options: options);
        case 'PATCH':
          return client.patch(url, data: body, options: options);
        default:
          throw Exception('HTTP method not supported');
      }
    } catch (e) {
      rethrow;
    }
  }

  // 헤더
  static Options httpOptions(String method, Map<String, String>? headers) {
    return Options(
      method: method,
      headers: headers,
    );
  }
}
