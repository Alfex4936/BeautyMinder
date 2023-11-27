import 'package:flutter/material.dart';

class LoginAppBar extends AppBar {
  LoginAppBar({Key? key})
      : super(
          key: key,
          backgroundColor: Color(0xffffecda),
          elevation: 0,
          centerTitle: false,
          title: const Text(
            "BeautyMinder 로그인",
            style: TextStyle(color: Color(0xffd86a04)),
          ),
          iconTheme: const IconThemeData(
            color: Color(0xffd86a04),
          ),
        );
}
