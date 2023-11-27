class RegisterRequestModel {
  RegisterRequestModel({
    this.email,
    this.phoneNumber,
    this.nickname,
    this.password,
  });

  late final String? email;
  late final String? phoneNumber;
  late final String? nickname;
  late final String? password;

  RegisterRequestModel.fromJson(Map<String, dynamic> json) {
    email = json['email'];
    phoneNumber = json['phoneNumber'];
    nickname = json['nickname'];
    password = json['password'];
  }

  Map<String, dynamic> toJson() {
    final _data = <String, dynamic>{};
    _data['email'] = email;
    _data['phoneNumber'] = phoneNumber;
    _data['nickname'] = nickname;
    _data['password'] = password;
    return _data;
  }
}
