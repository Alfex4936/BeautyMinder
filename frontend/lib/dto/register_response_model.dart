import 'user_model.dart';

// RegisterResponseModel registerResponseJson(String str) =>
//     RegisterResponseModel.fromJson(json.decode(str));

RegisterResponseModel registerResponseJson(Map<String, dynamic> json) =>
    RegisterResponseModel.fromJson(json);

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
