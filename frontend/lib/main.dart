import 'package:beautyminder/pages/baumann/baumann_test_start_page.dart';
import 'package:beautyminder/pages/my/my_page.dart';
import 'package:beautyminder/pages/pouch/expiry_page.dart';
import 'package:beautyminder/pages/recommend/recommend_bloc_screen.dart';
import 'package:beautyminder/pages/todo/todo_page.dart';
import 'package:flutter/foundation.dart';
import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:flutter_inappwebview/flutter_inappwebview.dart';

import 'Bloc/RecommendPageBloc.dart';
import 'Bloc/TodoPageBloc.dart';
import 'pages/start/login_page.dart';
import 'pages/start/register_page.dart';

// Widget _defaultHome = WelcomePage();

void main() async {
  WidgetsFlutterBinding.ensureInitialized();

  if (defaultTargetPlatform == TargetPlatform.android) {
    await AndroidInAppWebViewController.setWebContentsDebuggingEnabled(true);
  }

  runApp(MultiBlocProvider(
      providers: [
        BlocProvider<RecommendPageBloc>(
          create: (context) => RecommendPageBloc(),
        ),
        BlocProvider<TodoPageBloc>(create: (create) => TodoPageBloc())
      ],
      child: MaterialApp(
        title: 'BeautyMinder',
        theme: ThemeData(
          primaryColor: const Color(0xffffb876),
        ),
        home: MyApp(),
      )));
}

class MyApp extends StatelessWidget {
  const MyApp({Key? key}) : super(key: key);

  // This widget is the root of your application.
  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'BeautyMinder',
      theme: ThemeData(
        primaryColor: const Color(0xffffb876),
      ),
      // home: BaumannStartPage(),
      home: const LoginPage(),
      // home: const HomePage(),
      // home: CosmeticReviewPage(),
      // home: ProductDetailPage(),
      routes: {
        // '/': (context) => _defaultHome,
        '/login': (context) => const LoginPage(),
        '/user/signup': (context) => const RegisterPage(),
        '/recommend': (context) => const RecPage(),
        '/pouch': (context) => CosmeticExpiryPage(),
        // '/home': (context) => const HomePage(),
        '/todo': (context) => const CalendarPage(),
        '/my': (context) => const MyPage(),
        // '/baumann/survey' : (context) => BaumannTestPage(),
        '/baumann/test': (context) => BaumannStartPage(),
      },
    );
  }
}
