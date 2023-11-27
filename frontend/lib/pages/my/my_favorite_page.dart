import 'package:beautyminder/services/api_service.dart';
import 'package:beautyminder/services/shared_service.dart';
import 'package:flutter/material.dart';

import '../../widget/commonAppBar.dart';

class MyFavoritePage extends StatefulWidget {
  const MyFavoritePage({Key? key}) : super(key: key);

  @override
  State<MyFavoritePage> createState() => _MyFavoritePageState();
}

class _MyFavoritePageState extends State<MyFavoritePage> {
  List favorites = [];
  bool isLoading = true;

  @override
  void initState() {
    // TODO: implement initState
    super.initState();
    getfavorites();
  }

  getfavorites() async {
    try {
      final info = await APIService.getFavorites();
      setState(() {
        // favorites = info;
        isLoading = false;
      });
    } catch (e) {
      print(e);
    }
  }

  // favorite 데이터 사용법
  // 위 favorites = info; 의 주석을 제거 하고,
  // favorites[index].brand 처럼 쓰기.
  // itemCounter는 favorites.length 로 하기.

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: CommonAppBar(),
      body: _body(),
      // floatingActionButton: FloatingActionButton(
      //   onPressed: null,
      //   child: Text('+'),
      // ),
    );
  }

  Widget _body() {
    return ListView.builder(
        itemCount: 10,
        itemBuilder: (context, index) => ListTile(
              leading: Image.asset('assets/images/profile.jpg'),
              title: Text('title'),
              subtitle: Text('subTitle'),
            ));
  }
}
