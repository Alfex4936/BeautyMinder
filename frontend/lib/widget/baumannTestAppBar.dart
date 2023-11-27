import 'package:flutter/material.dart';

class BaumannTestAppBar extends AppBar {
  BaumannTestAppBar({Key? key})
      : super(
    key: key,
    backgroundColor: Color(0xffffecda),
    elevation: 0,
    centerTitle: false,
    title: const Text(
      "바우만 테스트 페이지",
      style: TextStyle(color: Color(0xffd86a04)),
    ),
    iconTheme: const IconThemeData(
      color: Color(0xffd86a04),
    ),
  );
}
