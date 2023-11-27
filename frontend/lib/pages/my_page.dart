import 'package:beautyminder/pages/pouch_page.dart';
import 'package:beautyminder/pages/recommend_page.dart';
import 'package:beautyminder/pages/todo_page.dart';
import 'package:flutter/cupertino.dart';
import 'package:flutter/material.dart';

import '../widget/commonAppBar.dart';
import '../widget/commonBottomNavigationBar.dart';
import 'home_page.dart';

class MyPage extends StatefulWidget {
  const MyPage({Key? key}) : super(key: key);

  @override
  _MyPageState createState() => _MyPageState();
}

class _MyPageState extends State<MyPage> {
  int _currentIndex = 4;

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: CommonAppBar(),
      body: Text('my'),
      bottomNavigationBar: CommonBottomNavigationBar(
        currentIndex: _currentIndex,
        onTap: (int index) {
          // 페이지 전환 로직 추가
          if (index == 0) {
            Navigator.of(context).push(MaterialPageRoute(builder: (context) => const RecPage()));
          }
          else if (index == 1) {
            Navigator.of(context).push(MaterialPageRoute(builder: (context) => const PouchPage()));
          }
          else if (index == 2) {
            Navigator.of(context).push(MaterialPageRoute(builder: (context) => const HomePage()));
          }
          else if (index == 3) {
            Navigator.of(context).push(MaterialPageRoute(builder: (context) => const TodoPage()));
          }
          else if (index == 4) {
            Navigator.of(context).push(MaterialPageRoute(builder: (context) => const MyPage()));
          }
        }
      ),
    );
  }
}