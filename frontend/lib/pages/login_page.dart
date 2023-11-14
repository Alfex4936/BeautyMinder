import 'package:flutter/gestures.dart';
import 'package:flutter/material.dart';
import 'package:fluttertoast/fluttertoast.dart';
import 'package:snippet_coder_utils/FormHelper.dart';
import 'package:snippet_coder_utils/ProgressHUD.dart';
import 'package:snippet_coder_utils/hex_color.dart';

import '../services/api_service.dart';
import '../config.dart';
import '../dto/login_request_model.dart';

class LoginPage extends StatefulWidget {
  const LoginPage({Key? key}) : super(key: key);

  @override
  _LoginPageState createState() => _LoginPageState();
}

class _LoginPageState extends State<LoginPage> {
  bool isApiCallProcess = false;
  bool hidePassword = true;
  GlobalKey<FormState> globalFormKey = GlobalKey<FormState>();
  String? email;
  String? password;
  String? nickname; // 별명 필드 추가

  @override
  Widget build(BuildContext context) {
    return SafeArea(
      child: Scaffold(
        backgroundColor: HexColor("#283B71"),
        body: ProgressHUD(
          child: Form(
            key: globalFormKey,
            child: _loginUI(context),
          ),
          inAsyncCall: isApiCallProcess,
          opacity: 0.3,
          key: UniqueKey(),
        ),
      ),
    );
  }

  // 로그인 UI
  Widget _loginUI(BuildContext context) {
    return SingleChildScrollView(
      child: Column(
        children: <Widget>[
          _buildHeader(), // 헤더 구성
          _buildEmailField(), // 이메일 필드
          _buildPasswordField(), // 비밀번호 필드
          _buildForgetPassword(), // 비밀번호 찾기
          _buildLoginButton(), // 로그인 버튼
          _buildOrText(), // OR 텍스트
          _buildSignupText(), // 회원가입 텍스트
        ],
      ),
    );
  }

  Widget _buildHeader() {
    return const Padding(
      padding: EdgeInsets.only(left: 20, bottom: 30, top: 50),
      child: Text(
        "Login",
        style: TextStyle(
          fontWeight: FontWeight.bold,
          fontSize: 25,
          color: Colors.white,
        ),
      ),
    );
  }

  // 이메일 필드
  Widget _buildEmailField() {
    return FormHelper.inputFieldWidget(
      context,
      "email",
      "Email",
      (val) => val.isEmpty ? 'Email can\'t be empty.' : null,
      (val) => email = val,
      obscureText: false,
      textColor: Colors.white,
      hintColor: Colors.white.withOpacity(0.7),
      prefixIcon: const Icon(Icons.person),
    );
  }

  // 비밀번호 필드
  Widget _buildPasswordField() {
    return FormHelper.inputFieldWidget(
      context,
      "password",
      "Password",
      (val) => val.isEmpty ? 'Password can\'t be empty.' : null,
      (val) => password = val,
      obscureText: hidePassword,
      textColor: Colors.white,
      hintColor: Colors.white.withOpacity(0.7),
      prefixIcon: const Icon(Icons.lock),
      suffixIcon: IconButton(
        onPressed: () {
          setState(() {
            hidePassword = !hidePassword;
          });
        },
        color: Colors.white.withOpacity(0.7),
        icon: Icon(
          hidePassword ? Icons.visibility_off : Icons.visibility,
        ),
      ),
    );
  }

  // 비밀번호 찾기
  Widget _buildForgetPassword() {
    return Align(
      alignment: Alignment.bottomRight,
      child: Padding(
        padding: const EdgeInsets.only(right: 25),
        child: RichText(
          text: TextSpan(
            style: const TextStyle(color: Colors.grey, fontSize: 14.0),
            children: <TextSpan>[
              TextSpan(
                text: 'Forget Password ?',
                style: const TextStyle(
                  color: Colors.white,
                  decoration: TextDecoration.underline,
                ),
                recognizer: TapGestureRecognizer()..onTap = () {},
              ),
            ],
          ),
        ),
      ),
    );
  }

  // 로그인 버튼
  Widget _buildLoginButton() {
    return Center(
      child: FormHelper.submitButton("Login", () async {
        if (validateAndSave()) {
          setState(() {
            isApiCallProcess = true;
          });
          try {
            // 로그인 API 호출
            final model = LoginRequestModel(email: email, password: password);
            final result = await APIService.login(model);

            if (result.value == true) {
              Navigator.pushNamedAndRemoveUntil(
                  context, '/home', (route) => false);
            } else {
              // 에러 토스트 메시지
              Fluttertoast.showToast(
                msg: result.error ?? "Login Failed",
                toastLength: Toast.LENGTH_SHORT,
                gravity: ToastGravity.BOTTOM,
              );
            }
          } finally {
            setState(() {
              isApiCallProcess = false;
            });
          }
        }
      }),
    );
  }

  // OR 텍스트
  Widget _buildOrText() {
    return const Center(
      child: Text(
        "OR",
        style: TextStyle(
          fontWeight: FontWeight.bold,
          fontSize: 18,
          color: Colors.white,
        ),
      ),
    );
  }

  // 회원가입 텍스트
  Widget _buildSignupText() {
    return Align(
      alignment: Alignment.center,
      child: Padding(
        padding: const EdgeInsets.only(right: 25),
        child: RichText(
          text: TextSpan(
            style: const TextStyle(color: Colors.white, fontSize: 14.0),
            children: <TextSpan>[
              const TextSpan(text: 'Don\'t have an account? '),
              TextSpan(
                text: 'Sign up',
                style: const TextStyle(
                  color: Colors.white,
                  fontWeight: FontWeight.bold,
                ),
                recognizer: TapGestureRecognizer()
                  ..onTap = () {
                    Navigator.pushNamed(context, '/register');
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
