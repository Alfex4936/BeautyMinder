import 'dart:convert';

import 'package:flutter/material.dart';
import 'package:beautyminder/dto/register_request_model.dart';
import 'package:beautyminder/services/api_service.dart';
import 'package:flutter/services.dart';
import 'package:snippet_coder_utils/FormHelper.dart';
import 'package:snippet_coder_utils/ProgressHUD.dart';
import 'package:snippet_coder_utils/hex_color.dart';
import 'login_page.dart';

import '../../config.dart';
import 'package:http/http.dart' as http;

class RegisterPage extends StatefulWidget {
  // const RegisterPage({Key? key}) : super(key: key);
  RegisterPage({Key ?key, this.title}) : super(key: key);

  final String? title;

  @override
  _RegisterPageState createState() => _RegisterPageState();
}



class _RegisterPageState extends State<RegisterPage> {

  final TextEditingController nameController = TextEditingController();
  final TextEditingController emailController = TextEditingController();
  final TextEditingController phoneController = TextEditingController();
  final TextEditingController idController = TextEditingController();
  final TextEditingController passwordController = TextEditingController();

  @override
  void initState() {
    super.initState();
  }

  bool isButtonEnabled() {
    return nameController.text.isNotEmpty &&
        emailController.text.isNotEmpty &&
        phoneController.text.isNotEmpty &&
        idController.text.isNotEmpty &&
        passwordController.text.isNotEmpty;
  }

  Widget _entryField(String title, TextEditingController controller,{bool isPassword = false}) {
    return Container(
      margin: EdgeInsets.symmetric(vertical: 10),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: <Widget>[
          Text(
            title,
            style: TextStyle(fontWeight: FontWeight.bold, fontSize: 15),
          ),
          SizedBox(
            height: 10,
          ),
          TextField(
              controller: controller,
              onChanged: (text) {
                setState(() {}); //text 변경시에 화면 다시 그림
              },
              obscureText: isPassword,
              decoration: InputDecoration(
                  border: InputBorder.none,
                  fillColor: Color(0xfff3f3f4),
                  filled: true))
        ],
      ),
    );
  }


  Future<String> tryRegister(String name, String email,String phone, String id, String password) async {
    try {
      var request_body = jsonEncode(
          {
            "name" : "${name}",
            "email" : "${email}",
            "phone" : "${phone}",
            "id" : "${id}",
            "password" : "${password}"
          });

      print(request_body);
      final response = await http.post(
        // Uri.parse("/user/signup"),
        Uri.parse("http://118.34.170.132:8080/user/signup"),
        headers: <String, String>{
          'Content-Type': 'application/json; charset=UTF-8',
        },
        body: request_body,
      );
      print(response.body);
      // if (response.statusCode != 201) {
      //   throw Exception("Failed to send data");
      // } else {
      //   print("User Data sent successfully");
      //   Get.to(const HomePage());
      // }
    } catch (e) {
      print("Failed to send post data: ${e}");
    }
    return "signup check";
  }


  Widget _submitButton() {
    return InkWell(
      onTap: isButtonEnabled() ? () {

        // nameController),
        // _entryField("이메일 입력", emailController),
        // _entryField("전화번호 입력", phoneController),
        // _entryField("아이디 입력", idController),
        // _entryField("비밀번호 입력", passwordController

        tryRegister(nameController.text,emailController.text,phoneController.text,idController.text,passwordController.text);

      } : null,
      child: Container(
        width: MediaQuery.of(context).size.width,
        padding: EdgeInsets.symmetric(vertical: 15),
        alignment: Alignment.center,
        decoration: BoxDecoration(
          borderRadius: BorderRadius.all(Radius.circular(5)),
          border: Border.all(
            color: isButtonEnabled() ? Color(0xfffe9738) : Colors.grey,
            width: 2,
          ),
        ),
        child: Text(
          '회원가입',
          style: TextStyle(
            fontSize: 20,
            color: isButtonEnabled() ? Color(0xfffe9738) : Colors.grey,
          ),
        ),
      ),
    );
  }

  Widget _loginAccountLabel() {
    return InkWell(
      onTap: () {
        Navigator.push(
            context, MaterialPageRoute(builder: (context) => LoginPage()));
      },
      child: Container(
        margin: EdgeInsets.symmetric(vertical: 20),
        padding: EdgeInsets.all(15),
        alignment: Alignment.bottomCenter,
        child: Row(
          mainAxisAlignment: MainAxisAlignment.center,
          children: <Widget>[
            Text(
              '이미 등록된 계정이 있으신가요 ?',
              style: TextStyle(fontSize: 13, fontWeight: FontWeight.w600),
            ),
            SizedBox(
              width: 10,
            ),
            Text(
              '로그인하기',
              style: TextStyle(
                  color: Color(0xfff79c4f),
                  fontSize: 13,
                  fontWeight: FontWeight.w600),
            ),
          ],
        ),
      ),
    );
  }


  Widget _emailPasswordWidget() {
    return Column(
      children: <Widget>[
        _entryField("이름 입력", nameController),
        _entryField("이메일 입력", emailController),
        _entryField("전화번호 입력", phoneController),
        _entryField("아이디 입력", idController),
        _entryField("비밀번호 입력", passwordController, isPassword: true),
      ],
    );
  }



  ///////////////////////
  bool isApiCallProcess = false;
  bool hidePassword = true;
  static final GlobalKey<FormState> globalFormKey = GlobalKey<FormState>();

  String? email;
  String? password;
  String? nickname;

  Widget _registerUI(BuildContext context) {
    return SingleChildScrollView(
      child: Column(
        mainAxisAlignment: MainAxisAlignment.start,
        crossAxisAlignment: CrossAxisAlignment.start,
        children: <Widget>[
          Container(
            width: MediaQuery.of(context).size.width,
            height: MediaQuery.of(context).size.height / 5.2,
            decoration: const BoxDecoration(
              gradient: LinearGradient(
                begin: Alignment.topCenter,
                end: Alignment.bottomCenter,
                colors: [
                  Colors.white,
                  Colors.white,
                ],
              ),
              borderRadius: BorderRadius.only(
                bottomRight: Radius.circular(100),
                bottomLeft: Radius.circular(100),
              ),
            ),
            child: Column(
              mainAxisAlignment: MainAxisAlignment.center,
              children: <Widget>[
                Padding(
                  padding: const EdgeInsets.only(top: 20),
                  child: Center(
                    child: Text(
                      "beautyMinder",
                      style: TextStyle(
                        fontWeight: FontWeight.bold,
                        fontSize: 40,
                        color: HexColor("#283B71"),
                      ),
                    ),
                  ),
                ),
                // Align(
                //   alignment: Alignment.center,
                //   child: Image.asset(
                //     "assets/images/ShoppingAppLogo.png",
                //     fit: BoxFit.contain,
                //     width: 250,
                //   ),
                // ),
              ],
            ),
          ),
          const Padding(
            padding: EdgeInsets.only(left: 20, bottom: 30, top: 50),
            child: Text(
              "Register",
              style: TextStyle(
                fontWeight: FontWeight.bold,
                fontSize: 25,
                color: Colors.white,
              ),
            ),
          ),
          Padding(
            padding: const EdgeInsets.only(bottom: 10),
            child: FormHelper.inputFieldWidget(
              context,
              "email",
              "Email",
                  (onValidateVal) {
                if (onValidateVal.isEmpty) {
                  return 'Email can\'t be empty.';
                }

                return null;
              },
                  (onSavedVal) => {
                email = onSavedVal,
              },
              initialValue: "",
              borderFocusColor: Colors.white,
              prefixIconColor: Colors.white,
              borderColor: Colors.white,
              textColor: Colors.white,
              hintColor: Colors.white.withOpacity(0.7),
              borderRadius: 10,
              prefixIcon: const Icon(Icons.mail),
            ),
          ),
          Padding(
            padding: const EdgeInsets.only(bottom: 10),
            child: FormHelper.inputFieldWidget(
              context,
              "password",
              "Password",
                  (onValidateVal) {
                if (onValidateVal.isEmpty) {
                  return 'Password can\'t be empty.';
                }

                return null;
              },
                  (onSavedVal) => {
                password = onSavedVal,
              },
              initialValue: "",
              obscureText: hidePassword,
              borderFocusColor: Colors.white,
              prefixIconColor: Colors.white,
              borderColor: Colors.white,
              textColor: Colors.white,
              hintColor: Colors.white.withOpacity(0.7),
              borderRadius: 10,
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
            ),
          ),
          // Adding Nickname field
          Padding(
            padding: const EdgeInsets.only(bottom: 10),
            child: FormHelper.inputFieldWidget(
              context,
              "nickName",
              "Nickname (Optional)",
                  (onValidateVal) => null,
                  (onSavedVal) => {
                nickname = onSavedVal,
              },
              initialValue: "",
              borderFocusColor: Colors.white,
              prefixIconColor: Colors.white,
              borderColor: Colors.white,
              textColor: Colors.white,
              hintColor: Colors.white.withOpacity(0.7),
              borderRadius: 10,
              prefixIcon: const Icon(Icons.person),
            ),
          ),

          const SizedBox(
            height: 20,
          ),
          Center(
            child: FormHelper.submitButton(
              "Register",
                  () {
                if (validateAndSave()) {
                  setState(() {
                    isApiCallProcess = true;
                  });

                  RegisterRequestModel model = RegisterRequestModel(
                    // username: userName,
                    password: password,
                    email: email,
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
                          "Registration Successful. Please login to the account",
                          "OK",
                              () {
                            Navigator.pushNamedAndRemoveUntil(
                              context,
                              '/',
                                  (route) => false,
                            );
                          },
                        );
                      } else {
                        FormHelper.showSimpleAlertDialog(
                          context,
                          Config.appName,
                          result.error ?? "Registration Failed",
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
              btnColor: HexColor("283B71"),
              borderColor: Colors.white,
              txtColor: Colors.white,
              borderRadius: 10,
            ),
          ),
          const SizedBox(
            height: 20,
          ),
        ],
      ),
    );
  }




  bool validateAndSave() {
    final form = globalFormKey.currentState;
    if (form!.validate()) {
      form.save();
      return true;
    }
    return false;
  }





  // @override
  // Widget build(BuildContext context) {
  //   final height = MediaQuery.of(context).size.height;
  //
  //   return Scaffold(
  //     appBar: PreferredSize(
  //         preferredSize: Size.fromHeight(60),
  //         child: AppBar(
  //           backgroundColor: Color(0xffffecda),
  //           elevation: 0,
  //           title: Text(
  //             "BeautyMinder 회원가입",
  //             style: TextStyle(color: Color(0xffd86a04)),
  //           ),
  //           iconTheme: IconThemeData(
  //             color: Color(0xffd86a04),
  //           ),
  //         )
  //
  //     ),
  //     body: Container(
  //       height: height,
  //       child: Stack(
  //         children: <Widget>[
  //           Container(
  //             padding: EdgeInsets.symmetric(horizontal: 20),
  //             child: SingleChildScrollView(
  //               child: Column(
  //                 crossAxisAlignment: CrossAxisAlignment.center,
  //                 mainAxisAlignment: MainAxisAlignment.center,
  //                 children: <Widget>[
  //                   // SizedBox(height: height * .2),
  //                   // _title(),
  //                   SizedBox(
  //                     height: 50,
  //                   ),
  //                   _emailPasswordWidget(),
  //                   SizedBox(
  //                     height: 20,
  //                   ),
  //                   _submitButton(),
  //                   SizedBox(height: height * .02),
  //                   _loginAccountLabel(),
  //                 ],
  //               ),
  //             ),
  //           ),
  //           // Positioned(top: 40, left: 0, child: _backButton()),
  //         ],
  //       ),
  //     ),
  //   );
  //
  //
  //
  //   // return SafeArea(
  //   //   child: Scaffold(
  //   //     backgroundColor: HexColor("#283B71"),
  //   //     body: ProgressHUD(
  //   //       child: Form(
  //   //         key: globalFormKey,
  //   //         child: _registerUI(context),
  //   //       ),
  //   //       inAsyncCall: isApiCallProcess,
  //   //       opacity: 0.3,
  //   //       key: UniqueKey(),
  //   //     ),
  //   //   ),
  //   // );
  // }





  @override
  Widget build(BuildContext context) {
    final height = MediaQuery.of(context).size.height;

    return Scaffold(
        appBar: PreferredSize(
          preferredSize: Size.fromHeight(60),
          child: AppBar(
            backgroundColor: Color(0xffffecda),
            elevation: 0,
            centerTitle: false,
            title: Text(
              "BeautyMinder 로그인",
              style: TextStyle(color: Color(0xffd86a04)),
            ),
            iconTheme: IconThemeData(
              color: Color(0xffd86a04),
            ),
          ),
        ),
        body: Container(
          height: height,
          child: Stack(
            children: <Widget>[
              Positioned.fill(
                child: ProgressHUD(
                  child: Form(
                    key: globalFormKey,
                    // child: _loginUI(context), /* 수정 */
                    child: Column(
                      children: <Widget>[
                        Container(
                          padding: EdgeInsets.symmetric(horizontal: 20),
                          child: SingleChildScrollView(
                            child: Column(
                              crossAxisAlignment: CrossAxisAlignment.center,
                              mainAxisAlignment: MainAxisAlignment.center,
                              children: <Widget>[
                                SizedBox(height: height * .05),
                                // _title(),
                                SizedBox(height: 10),
                                _emailPasswordWidget(),
                                SizedBox(height: 20),
                                _submitButton(),
                                Container(
                                  padding: EdgeInsets.symmetric(vertical: 10),
                                  alignment: Alignment.centerRight,
                                  // child: Text('Forgot Password ?',
                                  //     style: TextStyle(
                                  //         fontSize: 14, fontWeight: FontWeight.w500)),
                                ),
                                // _divider(),
                                // _facebookButton(),
                                SizedBox(height: height * .01),
                                _loginAccountLabel(),
                              ],
                            ),
                          ),
                        ),
                        // _entryField("이메일 입력", idController),
                        // _entryField("비밀번호 입력", passwordController, isPassword: true),
                        // _emailPasswordWidget(),

                      ],
                    ),
                  ),
                  inAsyncCall: isApiCallProcess,
                  opacity: 0.3,
                  key: UniqueKey(),
                ),
              ),
              // Positioned(top: 40, left: 0, child: _backButton()),
            ],
          ),
        ));
  }



}
