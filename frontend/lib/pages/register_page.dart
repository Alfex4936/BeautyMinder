import 'package:flutter/material.dart';
import 'package:beautyminder/dto/login_request_model.dart';
import 'package:beautyminder/dto/register_request_model.dart';
import 'package:beautyminder/services/api_service.dart';
import 'package:snippet_coder_utils/FormHelper.dart';
import 'package:snippet_coder_utils/ProgressHUD.dart';
import 'package:snippet_coder_utils/hex_color.dart';

import '../config.dart';

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
  String? nickname;

  @override
  void initState() {
    super.initState();
  }

  @override
  Widget build(BuildContext context) {
    return SafeArea(
      child: Scaffold(
        backgroundColor: HexColor("#283B71"),
        body: ProgressHUD(
          child: Form(
            key: globalFormKey,
            child: _registerUI(context),
          ),
          inAsyncCall: isApiCallProcess,
          opacity: 0.3,
          key: UniqueKey(),
        ),
      ),
    );
  }

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
}
