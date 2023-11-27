import 'package:beautyminder/dto/cosmetic_model.dart';
import 'package:flutter/material.dart';

import '../../services/homeSearch_service.dart';

class CosmeticSearchWidget extends StatefulWidget {
  @override
  _CosmeticSearchWidgetState createState() => _CosmeticSearchWidgetState();
}

class _CosmeticSearchWidgetState extends State<CosmeticSearchWidget> {
  List<Cosmetic> cosmetics = [];
  String query = '';
  late FocusNode _focusNode;

  @override
  void initState() {
    super.initState();
    _focusNode = FocusNode();
  }

  @override
  void dispose() {
    _focusNode.dispose();
    super.dispose();
  }

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
            focusNode: _focusNode,
            onChanged: (text) {
              query = text;
            },
            onSubmitted: (text) {
              _search();
            },
            decoration: InputDecoration(
              hintText: '화장품을 검색하세요',
              focusedBorder: UnderlineInputBorder(
                borderSide: BorderSide(color: Color(0xffd86a04)),
              ),
            ),
          ),
        ),
        iconTheme: const IconThemeData(
          color: Color(0xffd86a04),
        ),
        actions: [
          IconButton(
            icon: Icon(Icons.search),
            onPressed: () {
              _search();
              // 텍스트 필드에 포커스를 주고자 할 때
              _focusNode.requestFocus();
            },
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
