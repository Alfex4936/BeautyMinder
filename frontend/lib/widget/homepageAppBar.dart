import 'package:flutter/material.dart';

class HomepageAppBar extends AppBar {
  final List<Widget> actions;

  HomepageAppBar({Key? key, required this.actions, bool automaticallyImplyLeading = false})
      : super(
          key: key,
          automaticallyImplyLeading: automaticallyImplyLeading,
          backgroundColor: Color(0xffffecda),
          elevation: 0,
          centerTitle: false,
          title: const Text(
            "BeautyMinder",
            style: TextStyle(color: Color(0xffd86a04), fontWeight: FontWeight.bold),
          ),
          iconTheme: const IconThemeData(
            color: Color(0xffd86a04),
          ),
          actions: actions,
        );
}
