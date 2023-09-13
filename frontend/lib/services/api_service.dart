import 'dart:convert';

import 'package:beautyminder/models/login_request_model.dart';
import 'package:beautyminder/models/login_response_model.dart';
import 'package:beautyminder/models/register_request_model.dart';
import 'package:beautyminder/models/register_response_model.dart';
import 'package:http/http.dart' as http;

import '../../config.dart';
import 'shared_service.dart';

class APIService {
  static var client = http.Client();

  static Future<bool> login(
    LoginRequestModel model,
  ) async {
    Map<String, String> requestHeaders = {
      // 'Content-Type': 'application/x-www-urlencoded',
    };

    // Prepare data
    Map<String, String> body = {
      'username': model.username ?? '',
      'password': model.password ?? '',
    };

    var url = Uri.http(
      Config.apiURL,
      Config.loginAPI,
    );

    var test = Uri(queryParameters: body).query;

    print("====Sending request to $url with body: $body and headers: $requestHeaders"); // Sending request to http://10.0.2.2/login with body: {username: a@com, password: 1234} and headers: {Content-Type: application/x-www-urlencoded}
    print("Sending request to $test"); // Sending request to http://10.0.2.2/login with body: {username: a@com, password: 1234} and headers: {Content-Type: application/x-www-urlencoded}

    var response = await client.post(
      url,
      headers: requestHeaders,
      encoding: Encoding.getByName('utf-8'),
      // body: jsonEncode(model.toJson()),
      // body: Uri(queryParameters: body).query,  // URL encode the body,
      body: body,  // URL encode the body,
    );



    if (response.statusCode == 200) {
      print("===== body: ${response.body}");

      await SharedService.setLoginDetails(
        loginResponseJson(
          response.body,
        ),
      );

      return true;
    } else {
      return false;
    }
  }

  static Future<RegisterResponseModel> register(
    RegisterRequestModel model,
  ) async {
    Map<String, String> requestHeaders = {
      'Content-Type': 'application/json',
    };

    var url = Uri.http(
      Config.apiURL,
      Config.registerAPI,
    );

    var response = await client.post(
      url,
      headers: requestHeaders,
      body: jsonEncode(model.toJson()),
    );

    return registerResponseJson(
      response.body,
    );
  }

  static Future<String> getUserProfile() async {
    var loginDetails = await SharedService.loginDetails();

    Map<String, String> requestHeaders = {
      'Content-Type': 'application/json',
      'Authorization': 'Bearer ${loginDetails!.accessToken}'
    };

    var url = Uri.http(Config.apiURL, Config.userProfileAPI);

    var response = await client.get(
      url,
      headers: requestHeaders,
    );

    if (response.statusCode == 200) {
      return response.body;
    } else {
      return "";
    }
  }
}
