import 'dart:async';

import 'package:beautyminder/pages/start/splash_page.dart';
import 'package:flutter/material.dart';

class WelcomePage extends StatefulWidget {
  WelcomePage({Key? key, this.title}) : super(key: key);

  final String? title;

  @override
  _WelcomePageState createState() => _WelcomePageState();
}

class _WelcomePageState extends State<WelcomePage> {

  @override
  void initState() {
    Timer(Duration(milliseconds: 1500), () {
      Navigator.push(context, MaterialPageRoute(
          builder: (context) => SplashPage()
      )
      );
    });
  }

  Widget _title() {
    return Center(
      child: RichText(
        textAlign: TextAlign.center,
        text: TextSpan(
          text: 'Beauty',
          style: TextStyle(
            fontFamily: 'Oswald',
            fontSize: 30,
            fontWeight: FontWeight.w700,
            color: Color(0xfffe9738),
          ),
          children: [
            TextSpan(
              text: 'Minder',
              style: TextStyle(color: Color(0xfffe9738), fontSize: 30),
            ),
          ],
        ),
      ),
    );
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      body: _title(),
    );
  }
}
