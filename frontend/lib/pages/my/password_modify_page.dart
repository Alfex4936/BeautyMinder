import 'package:beautyminder/pages/my/widgets/my_divider.dart';
import 'package:beautyminder/pages/my/widgets/my_page_header.dart';
import 'package:beautyminder/pages/my/widgets/pop_up.dart';
import 'package:beautyminder/services/api_service.dart';
import 'package:beautyminder/services/shared_service.dart';
import 'package:flutter/material.dart';
import 'package:flutter_spinkit/flutter_spinkit.dart';

import '../../dto/user_model.dart';
import '../../widget/commonAppBar.dart';

class PasswordModifyPage extends StatefulWidget {
  const PasswordModifyPage({super.key});

  @override
  State<PasswordModifyPage> createState() => _PasswordModifyPageState();
}

class _PasswordModifyPageState extends State<PasswordModifyPage> {
  User? user;
  bool isLoading = true;

  final _nowPasswordController = TextEditingController();
  final _passwordController = TextEditingController();
  final _passwordConfirmController = TextEditingController();

  @override
  void initState() {
    super.initState();

    getUserInfo();
  }

  Future<void> getUserInfo() async {
    try {
      final info = await SharedService.loginDetails();
      setState(() {
        user = info?.user;
        isLoading = false;
      });
    } catch (e) {
      print(e);
    }
  }

  @override
  void dispose() {
    _nowPasswordController.dispose();
    _passwordController.dispose();
    _passwordConfirmController.dispose();

    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
        appBar: CommonAppBar(automaticallyImplyLeading: true, context: context,),
        body: isLoading
            ? const SpinKitThreeInOut(
                color: Color(0xffd86a04),
                size: 50.0,
                duration: Duration(seconds: 2),
              )
            : Stack(
                children: [
                  Padding(
                      padding: const EdgeInsets.symmetric(horizontal: 10),
                      child: SingleChildScrollView(
                        child: Column(children: [
                          const MyPageHeader('비밀번호 변경'),
                          const SizedBox(height: 20),
                          UserInfoProfile(
                            nickname: user!.nickname ?? user!.email,
                            profileImage: user!.profileImage ?? '',
                          ),
                          const SizedBox(height: 20),
                          const MyDivider(),
                          UserInfoEditItem(
                              title: '현재 비밀번호',
                              controller: _nowPasswordController),
                          UserInfoEditItem(
                              title: '변경할 비밀번호',
                              controller: _passwordController),
                          UserInfoEditItem(
                              title: '비밀번호 재확인',
                              controller: _passwordConfirmController),
                          const SizedBox(height: 150),
                        ]),
                      )),
                  Positioned(
                    bottom: 50, // 원하는 위치에 배치
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
                                elevation: 0,
                                shape: RoundedRectangleBorder(
                                  borderRadius: BorderRadius.circular(5.0), // 적절한 값을 선택하세요
                                ),
                                side: const BorderSide(
                                    width: 1.0, color: Color(0xFFFF820E)),
                              ),
                              onPressed: () {
                                Navigator.pop(context);
                              },
                              child: const Text('취소',
                                style: TextStyle(
                                    color: Color(0xFFFF820E),
                                    fontSize: 18
                                )),
                            ),
                          ),
                          const SizedBox(width: 20),
                          Expanded(
                            child: ElevatedButton(
                              style: ElevatedButton.styleFrom(
                                backgroundColor: const Color(0xFFFF820E),
                                elevation: 0,
                                shape: RoundedRectangleBorder(
                                  borderRadius: BorderRadius.circular(5.0), // 적절한 값을 선택하세요
                                ),
                              ),
                              onPressed: _changePassword,
                              child: const Text(
                                '수정',
                                style: TextStyle(
                                  color: Colors.white,
                                  fontSize: 18
                                ),
                              ),
                            ),
                          ),
                        ],
                      ),
                    ),
                  ),
                ],
              ));
  }

  Future<void> _changePassword() async {
    if (_passwordController.text != _passwordConfirmController.text) {
      await popUp(
        title: '비밀번호가 일치하지 않습니다.',
        context: context,
      );
      return;
    }

    final ok = await popUp(
      title: '비밀번호를 수정하시겠습니까?',
      context: context,
    );
    if (ok) {
      await APIService.changePassword(
        currentPassword: _nowPasswordController.text,
        newPassword: _passwordController.text,
      );
    }
  }
}

class UserInfoProfile extends StatelessWidget {
  final String nickname;
  final String? profileImage;
  final VoidCallback? onTap;

  const UserInfoProfile({
    Key? key,
    required this.nickname,
    required this.profileImage,
    this.onTap,
  }) : super(key: key);

  @override
  Widget build(BuildContext context) {
    return GestureDetector(
      onTap: onTap,
      child: Column(
        children: [
          profileImage == null
              ? const Icon(
                  Icons.camera_alt,
                  size: 50,
                  color: Colors.grey,
                )
              : CircleAvatar(
                  radius: 50,
                  backgroundImage: NetworkImage(profileImage!),
                ),
          const SizedBox(height: 10),
          Text(
            nickname,
            style: const TextStyle(fontSize: 18, fontWeight: FontWeight.bold),
          ),
        ],
      ),
    );
  }
}

class UserInfoEditItem extends StatelessWidget {
  final String title;
  final TextEditingController controller;

  const UserInfoEditItem(
      {super.key, required this.title, required this.controller});

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
