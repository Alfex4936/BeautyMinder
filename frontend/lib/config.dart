import 'dart:io' show Platform;

class Config {
  static const String appName = "BeautyMinder";
  static String get apiURL {
    if (Platform.isAndroid) {
      return '10.0.2.2:8080';
    } else {
      return 'localhost:8080';
    }
  }

  static const loginAPI = "/login";
  static const registerAPI = "/user";
  static const userProfileAPI = "/protected";
}
