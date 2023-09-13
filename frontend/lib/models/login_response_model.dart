import 'dart:convert';

LoginResponseModel loginResponseJson(String str) =>
    LoginResponseModel.fromJson(json.decode(str));

class LoginResponseModel {
  LoginResponseModel({
    required this.accessToken,
    required this.refreshToken,
    required this.user,
  });

  late final String accessToken;
  late final String refreshToken;
  late final User user;

  LoginResponseModel.fromJson(Map<String, dynamic> json) {
    accessToken = json['accessToken'];
    refreshToken = json['refreshToken'];
    user = User.fromJson(json['user']);
  }

  Map<String, dynamic> toJson() {
    final _data = <String, dynamic>{};
    _data['accessToken'] = accessToken;
    _data['refreshToken'] = refreshToken;
    _data['user'] = user.toJson();
    return _data;
  }
}


class User {
  User({
    required this.id,
    required this.email,
    required this.password,
    this.nickname,
    this.profileImage,
    required this.createdAt,
    required this.authorities,
  });

  late final int id;
  late final String email;
  late final String password;
  late final String? nickname;
  late final String? profileImage;
  late final DateTime createdAt;
  late final List<String> authorities;

  factory User.fromJson(Map<String, dynamic> json) {
    return User(
      id: json['id'],
      email: json['email'],
      password: json['password'],
      nickname: json['nickname'],
      profileImage: json['profileImage'],
      createdAt: toDateTime(json['createdAt']),
      authorities: List<String>.from(json['authorities'].map((obj) => obj.toString())),
    );
  }

  Map<String, dynamic> toJson() {
    return {
      'id': id,
      'email': email,
      'password': password,
      'nickname': nickname,
      'profileImage': profileImage,
      'createdAt': fromDateTime(createdAt),
      'authorities': authorities.map((authority) => {'authority': authority}).toList(),
    };
  }

  // Convert the createdAt array to a DateTime object
  static DateTime toDateTime(List<dynamic> createdAtArray) {
    return DateTime(
      createdAtArray[0],
      createdAtArray[1],
      createdAtArray[2],
      createdAtArray[3],
      createdAtArray[4],
      createdAtArray[5],
      createdAtArray[6] ~/ 1000000, // Nanoseconds to milliseconds
      createdAtArray[6] % 1000000 ~/ 1000, // Remaining nanoseconds to microseconds
    );
  }

  // Convert a DateTime object to the createdAt array
  static List<int> fromDateTime(DateTime createdAt) {
    return [
      createdAt.year,
      createdAt.month,
      createdAt.day,
      createdAt.hour,
      createdAt.minute,
      createdAt.second,
      createdAt.millisecond * 1000000 + createdAt.microsecond,
    ];
  }
}
