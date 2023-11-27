import 'package:flutter/material.dart';

class RegisterAppBar extends AppBar {
  RegisterAppBar({Key? key})
      : super(
          key: key,
          backgroundColor: Color(0xffffecda),
          elevation: 0,
          centerTitle: false,
          title: const Text(
            "BeautyMinder 회원가입",
            style: TextStyle(color: Color(0xffd86a04)),
          ),
          iconTheme: const IconThemeData(
            color: Color(0xffd86a04),
          ),
        );
}
