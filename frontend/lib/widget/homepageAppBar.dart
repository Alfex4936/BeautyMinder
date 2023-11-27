import 'package:flutter/material.dart';

import '../pages/search/search_page.dart';

class HomepageAppBar extends AppBar {
  final List<Widget> actions;
  HomepageAppBar({Key? key, required this.actions})
      : super(
    key: key,
    backgroundColor: Color(0xffffecda),
    elevation: 0,
    centerTitle: false,
    title: const Text(
      "BeautyMinder",
      style: TextStyle(color: Color(0xffd86a04)),
    ),
    iconTheme: const IconThemeData(
      color: Color(0xffd86a04),
    ),
    actions: actions,
  );
}


