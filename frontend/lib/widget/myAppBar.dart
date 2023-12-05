import 'package:flutter/material.dart';

class MyAppBar extends AppBar {
  final VoidCallback? onBack;
  final BuildContext context;

  MyAppBar({Key? key, this.onBack, required this.context, bool automaticallyImplyLeading = false})
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
    leading: IconButton(
      onPressed: () {
        if (onBack != null) {
          onBack!();
        } else {
          Navigator.pop(context, true);
        }
      },
      icon: Icon(Icons.arrow_back_ios),
    ),
  );
}
