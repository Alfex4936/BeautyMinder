import 'package:beautyminder/dto/delete_request_model.dart';
import 'package:beautyminder/dto/login_request_model.dart';
import 'package:beautyminder/dto/login_response_model.dart';
import 'package:beautyminder/dto/register_request_model.dart';
import 'package:beautyminder/dto/register_response_model.dart';
import 'package:beautyminder/dto/update_request_model.dart';
import 'package:dio/dio.dart';
import 'package:http_parser/src/media_type.dart';
import 'package:mime/src/mime_type.dart';

import '../../config.dart';
import '../dto/user_model.dart';
import 'dio_client.dart';
import 'shared_service.dart';

class APIService {

  static Future<Result<bool>> login(LoginRequestModel model) async {
    final url = Uri.http(Config.apiURL, Config.loginAPI).toString();
    final formData = FormData.fromMap({
      'email': model.email ?? '',
      'password': model.password ?? '',
    });

    try {
      final response = await DioClient.sendRequest('POST', url, body: formData);
      if (response.statusCode == 200) {
        await SharedService.setLoginDetails(loginResponseJson(response.data));
        return Result.success(true);
      }
      return Result.failure("Login failed");
    } catch (e) {
      return Result.failure("An error occurred: $e");
    }
  }

  static Future<Result<RegisterResponseModel>> register(
      RegisterRequestModel model) async {
    // URL 생성
    final url = Uri.http(Config.apiURL, Config.registerAPI).toString();

    try {
      // POST 요청
      final response = await DioClient.sendRequest('POST', url, body: model.toJson());
      return Result.success(
          registerResponseJson(response.data as Map<String, dynamic>));
    } catch (e) {
      return Result.failure("An error occurred: $e");
    }
  }

// 탈퇴 함수
  static Future<Result<bool>> delete(DeleteRequestModel model) async {
    // 로그인 상세 정보 가져오기
    final user = await SharedService.getUser();

    // AccessToken 가지고오기
    final accessToken = await SharedService.getAccessToken();
    final refreshToken = await SharedService.getRefreshToken();

    final userId = user?.id ?? '-1';

    // URL 생성
    final url = Uri.http(Config.apiURL, Config.deleteAPI)
        .toString(); //deleteAPI 뒤에 + userID 지움

    // 헤더 설정
    final headers = {
      'Authorization': 'Bearer $accessToken',
      'Cookie': 'XRT=$refreshToken',
    };

    try {
      await DioClient.sendRequest('DELETE', url, headers: headers);
      return Result.success(true);
    } catch (e) {
      return Result.failure("An error occurred: $e");
    }
  }

  // 사용자 프로필 조회 함수
  static Future<Result<User>> getUserProfile() async {
    // 로그인 상세 정보 가져오기
    // final user = await SharedService.getUser();
    final user = await SharedService.getUser();
    // AccessToken가지고오기
    final accessToken = await SharedService.getAccessToken();
    final refreshToken = await SharedService.getRefreshToken();

    final userId = user?.id ?? '-1';

    // URL 생성
    final url = Uri.http(Config.apiURL, Config.userProfileAPI).toString();

    // 헤더 설정
    final headers = {
      'Authorization': 'Bearer $accessToken',
      'Cookie': 'XRT=$refreshToken',
    };

    try {
      // GET 요청
      final response = await DioClient.sendRequest(
        'GET',
        url,
        headers: headers,
      );

      if (response.statusCode == 200) {
        // 사용자 정보 파싱
        final user = User.fromJson(response.data as Map<String, dynamic>);
        return Result.success(user);
      }

      return Result.failure("Failed to get user profile");
    } catch (e) {
      return Result.failure("An error occurred: $e");
    }
  }

  // 즐겨찾기 조회 함수
  static Future<Result<List<dynamic>>> getFavorites() async {
    /// 로그인 상세 정보 가져오기
    final user = await SharedService.getUser();
    // AccessToken가지고오기
    final accessToken = await SharedService.getAccessToken();
    final refreshToken = await SharedService.getRefreshToken();

    final userId = user?.id ?? '-1';

    // URL 생성
    final url = Uri.http(Config.apiURL, '/user/favorites').toString();

    // 헤더 설정
    final headers = {
      'Authorization': 'Bearer $accessToken',
      'Cookie': 'XRT=$refreshToken',
    };

    try {
      // GET 요청
      final response = await DioClient.sendRequest(
        'GET',
        url,
        headers: headers,
      );

      if (response.statusCode == 200) {

        return Result.success(response.data);
      }

      return Result.failure("Failed to get user favorites");
    } catch (e) {
      print(e);
      return Result.failure("An error occurred: $e");
    }
  }

  // 리뷰 조회 함수
  static Future<Result<List<dynamic>>> getReviews() async {
    // 로그인 상세 정보 가져오기
    final user = await SharedService.getUser();
    // AccessToken가지고오기
    final accessToken = await SharedService.getAccessToken();
    final refreshToken = await SharedService.getRefreshToken();

    final userId = user?.id ?? '-1';

    // URL 생성
    final url = Uri.http(Config.apiURL, Config.getUserReview).toString();

    // 헤더 설정
    final headers = {
      'Authorization': 'Bearer $accessToken',
      'Cookie': 'XRT=$refreshToken',
    };

    try {
      // GET 요청
      final response = await DioClient.sendRequest(
        'GET',
        url,
        headers: headers,
      );

      if (response.statusCode == 200) {
        return Result.success(response.data);
      }

      return Result.failure("Failed to get user reviews");
    } catch (e) {
      print(e);
      return Result.failure("An error occurred: $e");
    }
  }

  // 회원정보 수정 함수
  static Future<Result<bool>> sendEditInfo(UpdateRequestModel model) async {
    // 로그인 상세 정보 가져오기
    final user = await SharedService.getUser();
    // AccessToken가지고오기
    final accessToken = await SharedService.getAccessToken();
    final refreshToken = await SharedService.getRefreshToken();

    final userId = user?.id ?? '-1';

    // URL 생성
    final url = Uri.http(Config.apiURL, Config.editUserInfo).toString();

    // 헤더 설정
    final headers = {
      'Authorization': 'Bearer $accessToken',
      'Cookie': 'XRT=$refreshToken',
    };

    try {
      final response = await DioClient.sendRequest('PATCH', url,
          body: model.toJson(), headers: headers);
      if (response.statusCode == 200) {
        return Result.success(true);
      }
      return Result.failure("Failed to update user profile");
    } catch (e) {
      return Result.failure("An error occurred: $e");
    }
  }

  //프로필 사진 변경
  static Future<String> editProfileImgInfo(String image) async {
    // 로그인 상세 정보 가져오기
    final user = await SharedService.getUser();
    // AccessToken가지고오기
    final accessToken = await SharedService.getAccessToken();
    final refreshToken = await SharedService.getRefreshToken();

    final userId = user?.id ?? '-1';
    // URL 생성
    final url = Uri.http(Config.apiURL, Config.editProfileImg).toString();

    // 헤더 설정
    final headers = {
      'Authorization': 'Bearer $accessToken',
      'Cookie': 'XRT=$refreshToken',
    };

    final MediaType contentType = MediaType.parse(
        lookupMimeType('new_profile.jpg') ?? 'application/octet-stream');

    final formData = FormData.fromMap({
      "image": MultipartFile.fromFileSync(
        image,
        contentType: contentType,
      ),
    });

    // Use Dio's post method for multipart data
    final response = await DioClient.sendRequest(
      'POST',
      url,
      body: formData,
      headers: headers,
    );

    if (response.statusCode == 200) {
      return response.data;
    } else {
      throw Exception('Failed to update review: ${response.statusMessage}');
    }
  }

  // 리뷰 수정 함수
  static Future<Result<List<dynamic>>> updateReview(id) async {
    // 유저 정보 가지고 오기
    final user = await SharedService.getUser();
    // AccessToken가지고오기
    final accessToken = await SharedService.getAccessToken();
    //refreshToken 가지고오기
    final refreshToken = await SharedService.getRefreshToken();

    // user.id가 있으면 userId에 user.id를 저장 없으면 -1을 저장
    final userId = user?.id ?? '-1';

    // URL 생성
    final url = Uri.http(Config.apiURL, Config.getReviewAPI + id).toString();

    final headers = {
      'Authorization': 'Bearer $accessToken',
      'Cookie': 'XRT=$refreshToken',
    };

    try {
      // put 요청
      final response =
          await DioClient.sendRequest('PUT', url, headers: headers);
      return Result.success(response.data);
    } catch (e) {
      print(e);
      return Result.failure("An error occurred: $e");
    }
  }

  // 리뷰 삭제 함수
  static Future<Result<List<dynamic>>> deleteReview(String id) async {
    // AccessToken가지고오기
    final accessToken = await SharedService.getAccessToken();
    //refreshToken 가지고오기
    final refreshToken = await SharedService.getRefreshToken();

    // URL 생성
    final url = Uri.http(Config.apiURL, Config.getReviewAPI + id).toString();

    final headers = {
      'Authorization': 'Bearer $accessToken',
      'Cookie': 'XRT=$refreshToken',
    };

    try {
      // del 요청
      final response =
          await DioClient.sendRequest('DELETE', url, headers: headers);
      print('res is ${response.data}');
      return Result.success(response.data);
    } catch (e) {
      print(e);
      return Result.failure("An error occurred: $e");
    }
  }



  //비밀번호 변경
  static Future<Result<bool>> changePassword({
    required String currentPassword,
    required String newPassword,
  }) async {
    // AccessToken가지고오기
    final accessToken = await SharedService.getAccessToken();
    //refreshToken 가지고오기
    final refreshToken = await SharedService.getRefreshToken();

    final url = Uri.http(Config.apiURL, Config.changePassword).toString();

    final headers = {
      'Authorization': 'Bearer $accessToken',
      'Cookie': 'XRT=$refreshToken',
    };

    final Map<String, dynamic> passwords = {
      'current_password': currentPassword,
      'new_password': newPassword,
    };

    try {
      final response = await DioClient.sendRequest(
        'POST',
        url,
        body: passwords,
        headers: headers,
      );
      if (response.statusCode == 200) {
        return Result.success(true);
      }
      return Result.failure("Failed to change password");
    } catch (e) {
      return Result.failure("An error occurred: $e");
    }
  }

  //비밀번호 리셋
  static Future<Result<bool>> requestResetPassword({
    required String email,
  }) async {
    // AccessToken가지고오기
    final accessToken = await SharedService.getAccessToken();
    //refreshToken 가지고오기
    final refreshToken = await SharedService.getRefreshToken();

    final url = Uri.http(Config.apiURL, Config.requestResetPassword).toString();

    final headers = {
      'Authorization': 'Bearer $accessToken',
      'Cookie': 'XRT=$refreshToken',
    };

    try {
      final response = await DioClient.sendRequest(
        'POST',
        url,
        body: {'email': email},
        headers: headers,
      );
      if (response.statusCode == 200) {
        return Result.success(true);
      }
      return Result.failure("Failed to request reset password");
    } catch (e) {
      return Result.failure("An error occurred: $e");
    }
  }

  //유저 정보 변경
  static Future<Result<bool>> updateUserInfo(
      Map<String, dynamic> userData) async {
    // AccessToken가지고오기
    final accessToken = await SharedService.getAccessToken();
    //refreshToken 가지고오기
    final refreshToken = await SharedService.getRefreshToken();

    final url = Uri.http(Config.apiURL, Config.editUserInfo).toString();

    final headers = {
      'Authorization': 'Bearer $accessToken',
      'Cookie': 'XRT=$refreshToken',
    };

    try {
      final response = await DioClient.sendRequest('PATCH', url,
          body: userData, headers: headers);
      if (response.statusCode == 200) {
        return Result.success(true);
      }
      return Result.failure("Failed to update user profile");
    } catch (e) {
      return Result.failure("An error occurred: $e");
    }
  }

}

// 결과 클래스
class Result<T> {
  final T? value;
  final String? error;

  Result.success(this.value) : error = null;

  Result.failure(this.error) : value = null;

  bool get isSuccess => error == null;

  bool get isFailure => !isSuccess;
}
