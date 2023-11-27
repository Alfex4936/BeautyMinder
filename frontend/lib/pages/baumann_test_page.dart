import 'package:beautyminder/pages/pouch_page.dart';
import 'package:beautyminder/pages/todo_page.dart';
import 'package:flutter/cupertino.dart';
import 'package:flutter/material.dart';

import '../widget/commonAppBar.dart';
import '../widget/commonBottomNavigationBar.dart';
import 'home_page.dart';
import 'my_page.dart';

class BaumannTestPage extends StatefulWidget {
  const BaumannTestPage({Key? key}) : super(key: key);

  @override
  _BaumannTestPageState createState() => _BaumannTestPageState();
}

class _BaumannTestPageState extends State<BaumannTestPage> {

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: CommonAppBar(),
      body: Text('Baumann Test'),
    );
  }
}