import 'package:beautyminder/services/api_service.dart';
import 'package:flutter/material.dart';
import 'package:flutter_spinkit/flutter_spinkit.dart';

import '../../dto/cosmetic_model.dart';
import '../../widget/commonAppBar.dart';
import '../product/product_detail_page.dart';

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
        favorites = info.value!;
        isLoading = false;
      });
    } catch (e) {
      print(e);
    }
  }

  // favorite 데이터 사용법
  // 위 favorites = info; 의 주석을 제거,
  // favorites[index].brand 처럼 쓰기.
  // itemCounter는 favorites.length 로 하기.

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: CommonAppBar(),
      body: isLoading
          ? SpinKitThreeInOut(
              color: Color(0xffd86a04),
              size: 50.0,
              duration: Duration(seconds: 2),
            )
          : _body(),
    );
  }

  // Widget _body() {
  //   print(favorites.first['images']);
  //   return ListView.builder(
  //       itemCount: favorites.length,
  //       itemBuilder: (context, index) => ListTile(
  //         leading: Image.network(favorites[index]['images'][0] ?? ''),
  //         title: Text(favorites[index]['name']),
  //         subtitle: Text(favorites[index]['createdAt']),
  //       ));
  // }
  Widget _body() {
    print(favorites.first['images']);
    return ListView.builder(
      itemCount: favorites.length,
      itemBuilder: (context, index) => GestureDetector(
        onTap: () {
          // Navigate to ProductDetailPage with the selected favorite
          Navigator.push(
            context,
            MaterialPageRoute(
              builder: (context) => ProductDetailPage(
                searchResults: Cosmetic(
                  id: favorites[index]['id'] ?? '',
                  name: favorites[index]['name'] ?? '',
                  brand: favorites[index]['brand'],
                  images: List<String>.from(favorites[index]['images'] ?? []),
                  glowpickUrl: favorites[index]['glowpickUrl'],
                  expirationDate: favorites[index]['expirationDate'] != null
                      ? DateTime.tryParse(favorites[index]['expirationDate'])
                      : null,
                  createdAt: favorites[index]['createdAt'] != null
                      ? DateTime.parse(favorites[index]['createdAt'])
                      : DateTime.now(),
                  purchasedDate: favorites[index]['purchasedDate'] != null
                      ? DateTime.tryParse(favorites[index]['purchasedDate'])
                      : null,
                  category: favorites[index]['category'] ?? 'Unknown',
                  averageRating:
                      (favorites[index]['averageRating'] as num?)?.toDouble() ??
                          0.0,
                  reviewCount: favorites[index]['reviewCount'] as int? ?? 0,
                  totalRating: favorites[index]['totalRating'] as int? ?? 0,
                  keywords:
                      List<String>.from(favorites[index]['keywords'] ?? []),
                ),
              ),
            ),
          );
        },
        child: ListTile(
          contentPadding: EdgeInsets.all(10),
          leading: Image.network(
            favorites[index]['images'][0] ?? '',
            height: 80, // 이미지의 높이를 40으로 고정
            width: 80,
          ),
          title: Text(favorites[index]['name']),
          // subtitle: Text(favorites[index]['createdAt']),
        ),
      ),
    );
  }
}
