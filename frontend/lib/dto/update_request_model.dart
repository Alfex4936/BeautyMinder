class UpdateRequestModel {
  UpdateRequestModel({this.nickname, this.password, this.phone});

  late final String? nickname;
  late final String? password;
  late final String? phone;

  UpdateRequestModel.fromJson(Map<String, dynamic> json) {
    nickname = json['nickname'];
    password = json['password'];
    phone = json['phone'];
  }

  Map<String, dynamic> toJson() {
    final _data = <String, dynamic>{};
    _data['nickname'] = nickname;
    _data['password'] = password;
    _data['phone'] = phone;
    return _data;
  }
}
