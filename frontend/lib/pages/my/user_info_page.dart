import 'package:beautyminder/dto/delete_request_model.dart';
import 'package:beautyminder/dto/update_request_model.dart';
import 'package:beautyminder/dto/user_model.dart';
import 'package:beautyminder/pages/my/user_info_modify_page.dart';
import 'package:beautyminder/pages/my/widgets/my_divider.dart';
import 'package:beautyminder/pages/my/widgets/my_page_header.dart';
import 'package:beautyminder/pages/my/widgets/pop_up.dart';
import 'package:beautyminder/services/api_service.dart';
import 'package:beautyminder/services/shared_service.dart';
import 'package:beautyminder/widget/commonAppBar.dart';
import 'package:flutter/material.dart';

class UserInfoPage extends StatefulWidget {
  UserInfoPage({super.key});

  @override
  State<UserInfoPage> createState() => _UserInfoPageState();
}

class _UserInfoPageState extends State<UserInfoPage> {
  User? user;

  bool isLoading = true;

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
                      padding: const EdgeInsets.symmetric(horizontal: 20),
                      child: ListView(children: [
                        MyPageHeader('회원정보'),
                        SizedBox(height: 20),
                        UserInfoProfile(
                            nickname: user!.nickname ?? user!.email,
                            profileImage: user!.profileImage ?? ''),
                        SizedBox(height: 20),
                        MyDivider(),
                        UserInfoItem(title: '아이디', content: user!.id),
                        MyDivider(),
                        UserInfoItem(title: '이메일', content: user!.email),
                        MyDivider(),
                        UserInfoItem(
                            title: '전화번호', content: user!.phoneNumber ?? ''),
                        MyDivider(),
                        SizedBox(height: 150),
                      ])),
                  Positioned(
                    bottom: 10, // 원하는 위치에 배치
                    left: 10, // 원하는 위치에 배치
                    right: 10, // 원하는 위치에 배치
                    child: Padding(
                      padding: const EdgeInsets.symmetric(horizontal: 20),
                      child: Column(
                        mainAxisAlignment: MainAxisAlignment.center,
                        children: [
                          SizedBox(
                            width: double.infinity,
                            child: ElevatedButton(
                              style: ElevatedButton.styleFrom(
                                backgroundColor: const Color(0xFFFF820E),
                              ),
                              onPressed: () {
                                Navigator.push(
                                  context,
                                  MaterialPageRoute(
                                      builder: (context) =>
                                          const UserInfoModifyPage()),
                                );
                              },
                              child: const Text('회원정보 수정'),
                            ),
                          ),
                          const SizedBox(width: 20), // 간격 조절
                          SizedBox(
                            width: double.infinity,
                            child: ElevatedButton(
                              style: ElevatedButton.styleFrom(
                                backgroundColor: const Color(0xFFFFFFFF),
                                side: const BorderSide(
                                    width: 1.0, color: Color(0xFFFF820E)),
                              ),
                              onPressed: () async {
                                final ok = await popUp(
                                  title: '정말 탈퇴하시겠습니까?',
                                  context: context,
                                );
                                if (ok) {
                                  APIService.delete(
                                      DeleteRequestModel(userId: user!.id));
                                  SharedService.logout(context);
                                }
                              },
                              child: const Text('회원탈퇴',
                                  style: TextStyle(color: Color(0xFFFF820E))),
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

class UserInfoProfile extends StatelessWidget {
  final String nickname;
  final String profileImage;
  const UserInfoProfile(
      {super.key, required this.nickname, required this.profileImage});

  @override
  Widget build(BuildContext context) {
    return Padding(
      padding: const EdgeInsets.symmetric(horizontal: 20),
      child: Row(
        children: [
          // SizedBox(
          //   width: 50,
          //   child: Image.asset(
          //     'assets/images/profile.jpg', // profileImage,
          //     errorBuilder: (context, error, stackTrace) {
          //       return Image.asset('assets/images/profile.jpg');
          //     },
          //   ),
          // ),
          // Container(
          //     width: 58,
          //     height: 58,
          //     decoration: const BoxDecoration(
          //       color: Color(0xFFD9D9D9),
          //       shape: BoxShape.circle,
          //     ),
          //     child: const Icon(Icons.camera_alt_outlined, size: 35)),
          const SizedBox(width: 20),
          Expanded(
            child: Row(
              mainAxisAlignment: MainAxisAlignment.spaceBetween,
              children: [
                Text(
                  '이름',
                  style: TextStyle(fontSize: 15, color: Color(0xFF868383)),
                  textAlign: TextAlign.left,
                ),
                Text(
                  nickname,
                  style: TextStyle(fontSize: 15, color: Color(0xFF868383)),
                  textAlign: TextAlign.left,
                ),
              ],
            ),
          )
        ],
      ),
    );
  }
}

class UserInfoItem extends StatelessWidget {
  final String title;
  final String content;

  const UserInfoItem({super.key, required this.title, required this.content});

  @override
  Widget build(BuildContext context) {
    return Padding(
      padding: const EdgeInsets.symmetric(horizontal: 20, vertical: 25),
      child: Row(
        mainAxisAlignment: MainAxisAlignment.spaceBetween,
        children: [
          Text(
            title,
            style: const TextStyle(fontSize: 16, color: Color(0xFF868383)),
            textAlign: TextAlign.left,
          ),
          Text(
            content,
            style: const TextStyle(fontSize: 17, color: Color(0xFF5C5B5B)),
            textAlign: TextAlign.left,
          ),
        ],
      ),
    );
  }
}
