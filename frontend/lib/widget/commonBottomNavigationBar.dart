import 'package:flutter/material.dart';

class CommonBottomNavigationBar extends BottomNavigationBar {

    CommonBottomNavigationBar({Key? key, required int currentIndex, required Function(int) onTap})
      : super(
    backgroundColor: Color(0xffffecda),
    key: key,
    type: BottomNavigationBarType.fixed,
    selectedItemColor: Color(0xffd86a04),
    currentIndex: currentIndex,
    onTap: onTap,
    items: [
      BottomNavigationBarItem(
        icon: Icon(Icons.favorite),
        label: 'REC',
      ),
      BottomNavigationBarItem(
        icon: Icon(Icons.business_center),
        label: 'POUCH',
      ),
      BottomNavigationBarItem(
        icon: Icon(Icons.home),
        label: 'HOME',
      ),
      BottomNavigationBarItem(
        icon: Icon(Icons.check_box),
        label: 'TODO',
      ),
      BottomNavigationBarItem(
        icon: Icon(Icons.account_circle),
        label: 'MY',
      ),
    ],
  );
}


