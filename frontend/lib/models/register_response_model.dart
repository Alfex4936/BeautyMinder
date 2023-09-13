import 'dart:convert';

RegisterResponseModel registerResponseJson(String str) =>
    RegisterResponseModel.fromJson(json.decode(str));

class RegisterResponseModel {
  RegisterResponseModel({
    required this.message,
    required this.user,
  });

  late final String message;
  late final User? user;

  RegisterResponseModel.fromJson(Map<String, dynamic> json) {
    message = json['message'];
    user = json['user'] != null ? User.fromJson(json['user']) : null;
  }

  Map<String, dynamic> toJson() {
    final _data = <String, dynamic>{};
    _data['message'] = message;
    _data['user'] = user!.toJson();
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
  late final String createdAt;
  late final List<String> authorities;

  factory User.fromJson(Map<String, dynamic> json) {
    return User(
      id: json['id'],
      email: json['email'],
      password: json['password'],
      nickname: json['nickname'],
      profileImage: json['profileImage'],
      createdAt: json['createdAt'],
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
      'createdAt': createdAt,
      'authorities': authorities,
    };
  }
}
