import 'package:beautyminder/dto/update_request_model.dart';
import 'package:beautyminder/pages/my/user_info_page.dart';
import 'package:beautyminder/pages/my/widgets/my_divider.dart';
import 'package:beautyminder/pages/my/widgets/my_page_header.dart';
import 'package:beautyminder/pages/my/widgets/pop_up.dart';
import 'package:beautyminder/services/api_service.dart';
import 'package:beautyminder/services/shared_service.dart';
import 'package:flutter/material.dart';

import '../../dto/user_model.dart';
import '../../widget/commonAppBar.dart';

class UserInfoModifyPage extends StatefulWidget {
  const UserInfoModifyPage({super.key});

  @override
  State<UserInfoModifyPage> createState() => _UserInfoModifyPageState();
}

class _UserInfoModifyPageState extends State<UserInfoModifyPage> {
  User? user;
  bool isLoading = true;
  TextEditingController nicknameController = TextEditingController();
  TextEditingController nowPasswordController = TextEditingController();
  TextEditingController passwordController = TextEditingController();
  TextEditingController passwordConfirmController = TextEditingController();
  TextEditingController phoneController = TextEditingController();

  @override
  void initState() {
    // TODO: implement initState
    super.initState();
    getUserInfo();
  }

  getUserInfo() async {
    try {
      final info = await SharedService.loginDetails();
      setState(() {
        user = info!.user;
        isLoading = false;
      });
    } catch (e) {
      print(e);
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
        appBar: CommonAppBar(),
        body: isLoading
            ? Center(
                child: Text('로딩 중'),
              )
            : Stack(
                children: [
                  Padding(
                      padding: EdgeInsets.symmetric(horizontal: 10),
                      child: SingleChildScrollView(
                        child: Column(children: [
                          MyPageHeader('회원정보 수정'),
                          SizedBox(height: 20),
                          UserInfoProfile(
                            nickname: user!.nickname ?? user!.email,
                            profileImage: user!.profileImage ?? '',
                          ),
                          SizedBox(height: 20),
                          MyDivider(),
                          UserInfoItem(title: '아이디', content: user!.id),
                          MyDivider(),
                          UserInfoEditItem(
                              title: '전화번호', controller: phoneController),
                          MyDivider(),
                          UserInfoEditItem(
                              title: '닉네임', controller: nicknameController),
                          MyDivider(),
                          UserInfoEditItem(
                              title: '현재 비밀번호',
                              controller: nowPasswordController),
                          UserInfoEditItem(
                              title: '변경할 비밀번호',
                              controller: passwordController),
                          UserInfoEditItem(
                              title: '비밀번호 재확인',
                              controller: passwordConfirmController),
                          SizedBox(height: 150),
                        ]),
                      )),
                  Positioned(
                    bottom: 10, // 원하는 위치에 배치
                    left: 10, // 원하는 위치에 배치
                    right: 10, // 원하는 위치에 배치
                    child: Padding(
                      padding: const EdgeInsets.symmetric(horizontal: 20),
                      child: Row(
                        mainAxisAlignment: MainAxisAlignment.spaceBetween,
                        children: [
                          Expanded(
                            child: ElevatedButton(
                              style: ElevatedButton.styleFrom(
                                backgroundColor: const Color(0xFFFFFFFF),
                                side: const BorderSide(
                                    width: 1.0, color: Color(0xFFFF820E)),
                              ),
                              onPressed: () {
                                Navigator.pop(context);
                              },
                              child: const Text('취소',
                                  style: TextStyle(color: Color(0xFFFF820E))),
                            ),
                          ),
                          const SizedBox(width: 20),
                          Expanded(
                            child: ElevatedButton(
                              style: ElevatedButton.styleFrom(
                                backgroundColor: const Color(0xFFFF820E),
                              ),
                              onPressed: () async {
                                final ok = await popUp(
                                  title: '회원 정보를 수정하시겠습니까?',
                                  context: context,
                                );
                                if (ok) {
                                  APIService.sendEditInfo(UpdateRequestModel(
                                      nickname: nicknameController.text,
                                      password: passwordController.text,
                                      phone: phoneController.text));
                                }
                              },
                              child: const Text('수정'),
                            ),
                          ),
                        ],
                      ),
                    ),
                  ),
                ],
              ));
  }
}

class UserInfoEditItem extends StatelessWidget {
  final String title;
  final TextEditingController controller;

  UserInfoEditItem({super.key, required this.title, required this.controller});

  @override
  Widget build(BuildContext context) {
    return Padding(
      padding: const EdgeInsets.symmetric(horizontal: 20, vertical: 10),
      child: Row(
        mainAxisAlignment: MainAxisAlignment.spaceBetween,
        children: [
          Flexible(
            child: Text(
              title,
              style: const TextStyle(fontSize: 16, color: Color(0xFF868383)),
              textAlign: TextAlign.left,
            ),
          ),
          Flexible(
              child: TextField(
            controller: controller,
          )),
        ],
      ),
    );
  }
}
