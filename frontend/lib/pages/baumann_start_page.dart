import 'package:beautyminder/pages/pouch_page.dart';
import 'package:beautyminder/pages/todo_page.dart';
import 'package:flutter/cupertino.dart';
import 'package:flutter/material.dart';

import '../widget/commonAppBar.dart';
import '../widget/commonBottomNavigationBar.dart';
import 'home_page.dart';
import 'my_page.dart';

class BaumannStartPage extends StatefulWidget {
  const BaumannStartPage({Key? key}) : super(key: key);

  @override
  _BaumannStartPageState createState() => _BaumannStartPageState();
}

class _BaumannStartPageState extends State<BaumannStartPage> {

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: CommonAppBar(),
      body: Text('Baumann Start'),
    );
  }
}