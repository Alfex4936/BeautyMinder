// import 'package:flutter/material.dart';
//
// class SearchAppBar extends AppBar {
//   final Widget title;
//
//   SearchAppBar({Key? key, required this.title, bool automaticallyImplyLeading = false})
//       : super(
//           key: key,
//           backgroundColor: Color(0xffffecda),
//           elevation: 0,
//           centerTitle: false,
//           title: title,
//           iconTheme: IconThemeData(
//             color: Color(0xffd86a04),
//           ),
//           leading: IconButton(
//             onPressed: () {
//               // 페이지를 닫을 때 이전 페이지로 데이터 전달
//               Navigator.pop(context, true);
//             },
//             icon: Icon(Icons.arrow_back_ios)
//           ),
//         );
// }

import 'package:flutter/material.dart';

class SearchAppBar extends AppBar {
  final Widget title;
  final VoidCallback? onBack;
  final BuildContext context; // Add this line

  SearchAppBar({Key? key, required this.title, this.onBack, required this.context})
      : super(
    key: key,
    backgroundColor: Color(0xffffecda),
    elevation: 0,
    centerTitle: false,
    title: title,
    iconTheme: IconThemeData(
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
