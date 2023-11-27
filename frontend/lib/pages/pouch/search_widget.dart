import 'package:beautyminder/dto/cosmetic_model.dart';
import 'package:flutter/cupertino.dart';
import 'package:flutter/material.dart';

import '../../services/homeSearch_service.dart';



class CosmeticSearchWidget extends StatefulWidget {
  @override
  _CosmeticSearchWidgetState createState() => _CosmeticSearchWidgetState();
}

class _CosmeticSearchWidgetState extends State<CosmeticSearchWidget> {
  List<Cosmetic> cosmetics = [];
  String query = '';

  void _search() async {
    if (query.isNotEmpty) {
      try {
        // SearchService를 사용하여 서버에서 화장품을 검색
        cosmetics = await SearchService.searchCosmeticsByName(query);
        setState(() {
          // 검색 결과로 UI를 업데이트
        });
      } catch (e) {
        print('Search error: $e');
        // 필요하면 setState를 사용하여 FI에 에러 메시지를 표시
      }
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        backgroundColor: Color(0xffffecda),
        elevation: 0,
        centerTitle: false,
        title: Container(
          height: 40,
          child: TextField(
            onChanged: (text) {
              query = text;
            },
            onSubmitted: (text) {
              _search();
            },
            decoration: InputDecoration(
              hintText: '화장품을 검색하세요',
            ),
          ),
        ),
        iconTheme: const IconThemeData(
          color: Color(0xffd86a04),
        ),
        actions: [
          IconButton(
            icon: Icon(Icons.search),
            onPressed: _search,
          ),
        ],
      ),
      body: ListView.builder(
        itemCount: cosmetics.length,
        itemBuilder: (context, index) {
          final cosmetic = cosmetics[index];
          return ListTile(
            title: Text(cosmetic.name),
            onTap: () {
              Navigator.of(context).pop(cosmetic);
            },
          );
        },
      ),
    );
  }
}