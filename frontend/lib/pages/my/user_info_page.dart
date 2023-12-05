import 'package:beautyminder/dto/delete_request_model.dart';
import 'package:beautyminder/dto/user_model.dart';
import 'package:beautyminder/pages/my/password_modify_page.dart';
import 'package:beautyminder/pages/my/widgets/change_dialog.dart';
import 'package:beautyminder/pages/my/widgets/my_divider.dart';
import 'package:beautyminder/pages/my/widgets/my_page_header.dart';
import 'package:beautyminder/pages/my/widgets/pop_up.dart';
import 'package:beautyminder/services/api_service.dart';
import 'package:beautyminder/services/baumann_service.dart';
import 'package:beautyminder/services/shared_service.dart';
import 'package:beautyminder/widget/commonAppBar.dart';
import 'package:flutter/material.dart';
import 'package:flutter_spinkit/flutter_spinkit.dart';
import 'package:image_picker/image_picker.dart';

import '../../dto/baumann_result_model.dart';

class UserInfoPage extends StatefulWidget {
  const UserInfoPage({super.key});

  @override
  State<UserInfoPage> createState() => _UserInfoPageState();
}

class _UserInfoPageState extends State<UserInfoPage> {
  User? user;
  List<BaumannResult> baumannresultList = [];

  bool isLoading = true;

  @override
  void initState() {
    super.initState();
    getUserInfo();
  }

  getUserInfo() async {
    try {
      // final info = await SharedService.getUser();
      final info = await SharedService.loginDetails();
      final loadedBaumannResult = await BaumannService.getBaumannHistory();
      setState(() {
        user = info!.user ?? null;
        baumannresultList = loadedBaumannResult.value ?? [];
        isLoading = false;
      });
      print("hihi user info page2 : ${user?.baumann}");
    } catch (e) {
      print(e);
    }
  }

  @override
  Widget build(BuildContext context) {
    print("hihi user info page3 : ${user?.baumann}");
    return Scaffold(
        appBar: CommonAppBar(automaticallyImplyLeading: true, context: context,),
        body: isLoading
            ? SpinKitThreeInOut(
                color: Color(0xffd86a04),
                size: 50.0,
                duration: Duration(seconds: 2),
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
                          profileImage: user!.profileImage ?? '',
                          onImageTap: _changeProfileImage,
                          onButtonTap: _changeNickName,
                        ),
                        SizedBox(height: 20),
                        MyDivider(),
                        PhoneInfo(
                          title: '전화번호',
                          content: user!.phoneNumber ?? '',
                          onTap: _changePhone,
                        ),
                        MyDivider(),
                        UserInfoItem(title: '이메일', content: user!.email),
                        MyDivider(),
                        UserInfoItem(title: '피부타입', content: (baumannresultList.isEmpty)? "없음" : baumannresultList.last.baumannType),
                        MyDivider(),
                        UserInfoItem(title: '가입시각', content: user!.createdAt.toString()),
                        MyDivider(),
                        // SizedBox(height: 200),
                      ])),
                  Positioned(
                    bottom: 70, // 원하는 위치에 배치
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
                                elevation: 0,
                                shape: RoundedRectangleBorder(
                                  borderRadius: BorderRadius.circular(5.0), // 적절한 값을 선택하세요
                                ),
                              ),
                              onPressed: () async {
                                if (user != null) {
                                  await Navigator.push(
                                    context,
                                    MaterialPageRoute(
                                        builder: (context) =>
                                            const PasswordModifyPage()),
                                  );

                                  getUserInfo();
                                }
                              },
                              child: const Text(
                                '비밀번호 변경',
                                style: TextStyle(
                                  color: Colors.white,
                                  fontSize: 18
                                ),
                              ),
                            ),
                          ),
                          const SizedBox(width: 20), // 간격 조절
                          SizedBox(
                            width: double.infinity,
                            child: ElevatedButton(
                              style: ElevatedButton.styleFrom(
                                backgroundColor: const Color(0xFFFFFFFF),
                                shape: RoundedRectangleBorder(
                                  borderRadius: BorderRadius.circular(5.0), // 적절한 값을 선택하세요
                                ),
                                elevation: 0,
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
                                style: TextStyle(
                                    color: Color(0xFFFF820E),
                                    fontSize: 18
                                )),
                            ),
                          ),
                        ],
                      ),
                    ),
                  ),
                ],
              ));
  }

  Future<void> _changeProfileImage() async {
    final pickedFile =
        await ImagePicker().pickImage(source: ImageSource.gallery);

    if (pickedFile != null) {
      final image = pickedFile.path;
      final newImageUrl = await APIService.editProfileImgInfo(image);

      await _updateUser(imageUrl: newImageUrl);
    }
  }

  Future<void> _changeNickName() async {
    final newNickname = await showDialog(
      context: context,
      builder: (context) {
        return const ChnageDialog(
          title: '닉네임',
          subtitle: '닉네임을 입력해주세요',
        );
      },
    );

    if (newNickname != null) {
      await APIService.updateUserInfo({'nickname': newNickname});
      await _updateUser(nickname: newNickname);
    }
  }

  Future<void> _changePhone() async {
    final newphoneNumber = await showDialog(
      context: context,
      builder: (context) {
        return const ChnageDialog(
          title: '전화번호',
          subtitle: '전화번호를 입력해주세요',
        );
      },
    );

    if (newphoneNumber != null) {
      await APIService.updateUserInfo({'phoneNumber': newphoneNumber});
      await _updateUser(phoneNumber: newphoneNumber);
    }
  }


  Future<void> _updateUser({
    String? imageUrl,
    String? nickname,
    String? phoneNumber,
  }) async {
    final updatedUser = User(
      id: user!.id,
      email: user!.email,
      password: user!.password,
      nickname: nickname ?? user!.nickname,
      profileImage: imageUrl ?? user!.profileImage,
      createdAt: user!.createdAt,
      authorities: user!.authorities,
      phoneNumber: phoneNumber ?? user!.phoneNumber,
      baumann: user!.baumann,
      baumannScores: user!.baumannScores,
    );

    await SharedService.updateUser(updatedUser);

    setState(() {
      user = updatedUser;
    });
  }
}

class UserInfoProfile extends StatelessWidget {
  final String nickname;
  final String profileImage;
  final VoidCallback? onImageTap;
  final VoidCallback? onButtonTap;

  const UserInfoProfile({
    super.key,
    required this.nickname,
    required this.profileImage,
    this.onImageTap,
    this.onButtonTap,
  });

  @override
  Widget build(BuildContext context) {
    return Padding(
      padding: const EdgeInsets.symmetric(horizontal: 20),
      child: Row(
        children: [
          GestureDetector(
            onTap: onImageTap,
            child: CircleAvatar(
              radius: 30,
              backgroundImage: NetworkImage(profileImage),
            ),
          ),
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
                  nickname,
                  style: const TextStyle(
                    fontSize: 20,
                    color: Color(0xFF585555),
                    fontWeight: FontWeight.bold,
                  ),
                  textAlign: TextAlign.left,
                ),
                MaterialButton(
                  onPressed: onButtonTap,
                  padding: EdgeInsets.zero,
                  minWidth: 0,
                  child: Container(
                    padding:
                        const EdgeInsets.symmetric(horizontal: 10, vertical: 5),
                    decoration: BoxDecoration(
                      border: Border.all(
                        color: const Color(0xFF868383),
                      ),
                      borderRadius: BorderRadius.circular(5),
                    ),
                    child: const Text(
                      '변경',
                      style: TextStyle(
                        fontSize: 15,
                        color: Color(0xFF868383),
                      ),
                    ),
                  ),
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

class PhoneInfo extends StatelessWidget {
  final String title;
  final String content;
  final VoidCallback? onTap;

  const PhoneInfo({
    super.key,
    required this.title,
    required this.content,
    this.onTap,
  });

  @override
  Widget build(BuildContext context) {
    return Padding(
      padding: const EdgeInsets.symmetric(horizontal: 20, vertical: 20),
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
          MaterialButton(
            onPressed: onTap,
            padding: EdgeInsets.zero,
            minWidth: 0,
            child: Container(
              padding: const EdgeInsets.symmetric(horizontal: 10, vertical: 5),
              decoration: BoxDecoration(
                border: Border.all(
                  color: const Color(0xFF868383),
                ),
                borderRadius: BorderRadius.circular(5),
              ),
              child: const Text(
                '변경',
                style: TextStyle(
                  fontSize: 15,
                  color: Color(0xFF868383),
                ),
              ),
            ),
          ),
        ],
      ),
    );
  }
}
