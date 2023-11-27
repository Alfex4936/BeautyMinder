import 'package:beautyminder/dto/register_request_model.dart';
import 'package:flutter/gestures.dart';
import 'package:flutter/material.dart';
import 'package:snippet_coder_utils/FormHelper.dart';
import 'package:snippet_coder_utils/ProgressHUD.dart';

import '../../config.dart';
import '../../services/api_service.dart';
import '../../widget/registerAppBar.dart';

class RegisterPage extends StatefulWidget {
  const RegisterPage({Key? key}) : super(key: key);

  @override
  _RegisterPageState createState() => _RegisterPageState();
}

class _RegisterPageState extends State<RegisterPage> {
  bool isApiCallProcess = false;
  bool hidePassword = true;
  static final GlobalKey<FormState> globalFormKey = GlobalKey<FormState>();

  String? email;
  String? password;
  String? checkpassword;
  String? nickname;
  String? phoneNumber;

  @override
  void initState() {
    super.initState();
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: RegisterAppBar(),
      backgroundColor: Colors.white,
      body: ProgressHUD(
        child: Form(
          key: globalFormKey,
          child: _registerUI(context),
        ),
        inAsyncCall: isApiCallProcess,
        opacity: 0.3,
        key: UniqueKey(),
      ),
    );
  }

  // 로그인 UI
  Widget _registerUI(BuildContext context) {
    return SingleChildScrollView(
      // SingleChildScrollView로 감싼다
      child: Container(
        padding: EdgeInsets.symmetric(horizontal: 20),
        height: MediaQuery.of(context).size.height,
        child: Column(
          children: <Widget>[
            SizedBox(height: 50),
            _buildEmailField(),
            SizedBox(height: 30),
            _buildNumberField(),
            SizedBox(height: 30),
            _buildNicknameField(),
            SizedBox(height: 30),
            _buildPasswordField(),
            SizedBox(height: 20),
            _buildPasswordCheckField(),
            SizedBox(height: 60),
            _buildRegisterButton(),
            SizedBox(height: 50),
            _buildOrText(),
            SizedBox(height: 30),
            _buildSignupText(),
          ],
        ),
      ),
    );
  }

  // 이메일 필드
  Widget _buildEmailField() {
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        Text(
          "이메일 입력",
          style: TextStyle(
            fontWeight: FontWeight.bold,
            color: Colors.black,
            fontSize: 16,
          ),
        ),
        SizedBox(height: 5), // 제목과 입력 필드 사이의 간격 조절
        TextFormField(
          validator: (val) => val!.isEmpty ? '이메일이 입력되지 않았습니다.' : null,
          onChanged: (val) => email = val,
          obscureText: false,
          style: TextStyle(color: Colors.black),
          decoration: InputDecoration(
            hintText: "이메일을 입력하세요.",
            hintStyle: TextStyle(color: Colors.grey.withOpacity(0.7)),
            focusedBorder: OutlineInputBorder(
              borderRadius: BorderRadius.circular(10),
              borderSide: BorderSide(
                color: Color(0xffd86a04), // 클릭 시 테두리 색상 변경
              ),
            ),
          ),
        ),
      ],
    );
  }

  // 전화번호 필드
  Widget _buildNumberField() {
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        Text(
          "전화번호 입력",
          style: TextStyle(
            fontWeight: FontWeight.bold,
            color: Colors.black,
            fontSize: 16,
          ),
        ),
        SizedBox(height: 5), // 제목과 입력 필드 사이의 간격 조절
        TextFormField(
          validator: (val) => val!.isEmpty ? '전화번호가 입력되지 않았습니다.' : null,
          onChanged: (val) => phoneNumber = val,
          obscureText: false,
          style: TextStyle(color: Colors.black),
          decoration: InputDecoration(
            hintText: "전화번호를 입력하세요.(- 없이 입력하세요.)",
            hintStyle: TextStyle(color: Colors.grey.withOpacity(0.7)),
            focusedBorder: OutlineInputBorder(
              borderRadius: BorderRadius.circular(10),
              borderSide: BorderSide(
                color: Color(0xffd86a04), // 클릭 시 테두리 색상 변경
              ),
            ),
          ),
        ),
      ],
    );
  }

  // 닉네임 필드
  Widget _buildNicknameField() {
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        Text(
          "닉네임 입력",
          style: TextStyle(
            fontWeight: FontWeight.bold,
            color: Colors.black,
            fontSize: 16,
          ),
        ),
        SizedBox(height: 5), // 제목과 입력 필드 사이의 간격 조절
        TextFormField(
          validator: (val) => val!.isEmpty ? '닉네임이 입력되지 않았습니다.' : null,
          onChanged: (val) => nickname = val,
          obscureText: false,
          style: TextStyle(color: Colors.black),
          decoration: InputDecoration(
            hintText: "사용하실 닉네임을 입력하세요.",
            hintStyle: TextStyle(color: Colors.grey.withOpacity(0.7)),
            focusedBorder: OutlineInputBorder(
              borderRadius: BorderRadius.circular(10),
              borderSide: BorderSide(
                color: Color(0xffd86a04), // 클릭 시 테두리 색상 변경
              ),
            ),
          ),
        ),
      ],
    );
  }

  // 비밀번호 필드
  Widget _buildPasswordField() {
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        Text(
          "비밀번호 입력",
          style: TextStyle(
            fontWeight: FontWeight.bold,
            color: Colors.black,
            fontSize: 16,
          ),
        ),
        SizedBox(height: 5),
        TextFormField(
          onChanged: (val) => password = val,
          validator: (val) => val!.isEmpty ? '비밀번호가 입력되지 않았습니다.' : null,
          obscureText: hidePassword,
          style: TextStyle(color: Colors.black),
          decoration: InputDecoration(
            hintText: "비밀번호를 입력하세요.",
            hintStyle: TextStyle(color: Colors.grey.withOpacity(0.7)),
            suffixIcon: IconButton(
              onPressed: () {
                setState(() {
                  hidePassword = !hidePassword;
                });
              },
              color: hidePassword
                  ? Colors.grey.withOpacity(0.7)
                  : Color(0xffd86a04),
              icon: Icon(
                hidePassword ? Icons.visibility_off : Icons.visibility,
                color: hidePassword
                    ? Colors.grey.withOpacity(0.7)
                    : Color(0xffd86a04),
              ),
            ),
            focusedBorder: OutlineInputBorder(
              borderRadius: BorderRadius.circular(10),
              borderSide: BorderSide(
                color: Color(0xffd86a04), // 클릭 시 테두리 색상 변경
              ),
            ),
          ),
        ),
      ],
    );
  }

  // 비밀번호 확인 필드
  Widget _buildPasswordCheckField() {
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        TextFormField(
          onChanged: (val) => checkpassword = val,
          validator: (val) {
            if (val!.isEmpty) {
              return '비밀번호가 입력되지 않았습니다.';
            } else if (val != password) {
              return '비밀번호가 일치하지 않습니다.';
            }
            return null;
          },
          obscureText: hidePassword,
          style: TextStyle(color: Colors.black),
          decoration: InputDecoration(
            hintText: "비밀번호를 한번 더 입력하세요.",
            hintStyle: TextStyle(color: Colors.grey.withOpacity(0.7)),
            focusedBorder: OutlineInputBorder(
              borderRadius: BorderRadius.circular(10),
              borderSide: BorderSide(
                color: Color(0xffd86a04), // 클릭 시 테두리 색상 변경
              ),
            ),
          ),
        ),
      ],
    );
  }

  // 회원가입 버튼
  Widget _buildRegisterButton() {
    double screenWidth = MediaQuery.of(context).size.width;

    return InkWell(
      child: Container(
        width: screenWidth, // 원하는 너비 설정
        height: 50, // 원하는 높이 설정
        decoration: BoxDecoration(
          color: Color(0xfffe9738), // 버튼 배경색 설정
          borderRadius: BorderRadius.circular(10.0), // 원하는 모양 설정
        ),
        child: Center(
          child: Text(
            "등록하기",
            style: TextStyle(
              color: Colors.white, // 텍스트 색상 설정
              fontSize: 18, // 텍스트 크기 설정
            ),
          ),
        ),
      ),
      onTap: () async {
        if (validateAndSave()) {
          setState(() {
            isApiCallProcess = true;
          });

          RegisterRequestModel model = RegisterRequestModel(
            email: email,
            phoneNumber: phoneNumber,
            nickname: nickname,
            password: password,
          );

          APIService.register(model).then(
            (result) {
              setState(() {
                isApiCallProcess = false;
              });

              if (result.value != null) {
                FormHelper.showSimpleAlertDialog(
                  context,
                  Config.appName,
                  "가입이 완료되었습니다.",
                  "확인",
                  () {
                    Navigator.pushNamedAndRemoveUntil(
                      context,
                      '/login',
                      (route) => false,
                    );
                  },
                );
              } else {
                FormHelper.showSimpleAlertDialog(
                  context,
                  Config.appName,
                  result.error ?? "Register Failed",
                  "OK",
                  () {
                    Navigator.of(context).pop();
                  },
                );
              }
            },
          );
        }
      },
    );
  }

  // OR 텍스트
  Widget _buildOrText() {
    return const Center(
      child: Text(
        "OR",
        style: TextStyle(
          fontSize: 15,
          color: Colors.black,
        ),
      ),
    );
  }

  // 로그인 텍스트
  Widget _buildSignupText() {
    return Align(
      alignment: Alignment.center,
      child: Padding(
        padding: const EdgeInsets.only(right: 25),
        child: RichText(
          text: TextSpan(
            style: const TextStyle(color: Colors.black, fontSize: 14.0),
            children: <TextSpan>[
              const TextSpan(text: '이미 등록된 계정이 있으신가요? '),
              TextSpan(
                text: '로그인',
                style: const TextStyle(
                  color: Color(0xffd86a04),
                  fontWeight: FontWeight.bold,
                ),
                recognizer: TapGestureRecognizer()
                  ..onTap = () {
                    Navigator.pushNamed(context, '/login');
                  },
              ),
            ],
          ),
        ),
      ),
    );
  }

  // 입력 유효성 검사
  bool validateAndSave() {
    final form = globalFormKey.currentState;
    if (form!.validate()) {
      form.save();
      return true;
    }
    return false;
  }
}
