import 'package:flutter/cupertino.dart';
import 'package:flutter/material.dart';

import '../../widget/commonAppBar.dart';

class BaumannTestPage extends StatefulWidget {
  const BaumannTestPage({Key? key}) : super(key: key);

  @override
  _BaumannTestPageState createState() => _BaumannTestPageState();
}

class _BaumannTestPageState extends State<BaumannTestPage> {

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      // appBar: CommonAppBar(),
      body: Text('Baumann Result'),
    );
  }

}