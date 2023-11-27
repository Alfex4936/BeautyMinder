import 'package:beautyminder/dto/update_request_model.dart';
import 'package:beautyminder/pages/my/user_info_page.dart';
import 'package:beautyminder/pages/my/widgets/my_divider.dart';
import 'package:beautyminder/pages/my/widgets/my_page_header.dart';
import 'package:beautyminder/pages/my/widgets/pop_up.dart';
import 'package:beautyminder/services/api_service.dart';
import 'package:beautyminder/services/shared_service.dart';
import 'package:flutter/material.dart';
import 'package:flutter_spinkit/flutter_spinkit.dart';
import 'package:image_picker/image_picker.dart';

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
  String? image;

  Future<void> onImageChanged(String? imagePath) async {
    final updatedUser = User(
        id: user!.id,
        email: user!.email,
        password: user!.password,
        nickname: user!.nickname,
        profileImage: imagePath,
        createdAt: user!.createdAt,
        authorities: user!.authorities,
        phoneNumber: user!.phoneNumber,
        baumann: user?.baumann,
        baumannScores: user?.baumannScores);

    await SharedService.updateUser(updatedUser);

    setState(() {
      user = updatedUser;
      print(image);
    });
  }

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
        user = info?.user;
        isLoading = false;
      });
    } catch (e) {
      print(e);
    }
  }

  @override
  Widget build(BuildContext context) {
    //print("fdsfdsf : $image");
    //print("dsadsadas : ${user!.profileImage}");
    return Scaffold(
        appBar: CommonAppBar(),
        body: isLoading
            ? SpinKitThreeInOut(
                color: Color(0xffd86a04),
                size: 50.0,
                duration: Duration(seconds: 2),
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
                            onTap: _pickImage, // 수정: _pickImage 함수를 onTap으로 전달
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
                                  if (image != null) {
                                    APIService.sendEditInfo(UpdateRequestModel(
                                      nickname: nicknameController.text,
                                      password: passwordController.text,
                                      phone: phoneController.text,
                                      image: image,
                                      // image: image ?? XFile(''),
                                    ));
                                  }
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

  Future<void> _pickImage() async {
    final pickedFile =
        await ImagePicker().pickImage(source: ImageSource.gallery);

    if (pickedFile != null) {
      setState(() {
        image = pickedFile.path;
      });

      // Call the editProfileImgInfo method with the UpdateRequestModel
      final newImageUrl = await APIService.editProfileImgInfo(image!);

      onImageChanged(newImageUrl);
    }
  }
}

class UserInfoProfile extends StatelessWidget {
  final String nickname;
  final String? profileImage;
  final VoidCallback? onTap;

  UserInfoProfile({
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
              ? Icon(
                  Icons.camera_alt,
                  size: 50,
                  color: Colors.grey,
                )
              : CircleAvatar(
                  radius: 50,
                  backgroundImage: NetworkImage(profileImage!),
                ),
          SizedBox(height: 10),
          Text(
            nickname,
            style: TextStyle(fontSize: 18, fontWeight: FontWeight.bold),
          ),
        ],
      ),
    );
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
