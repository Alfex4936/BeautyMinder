import 'package:flutter/cupertino.dart';
import 'package:flutter/material.dart';

class SearchResultPage extends StatefulWidget {
  final String searchQuery;

  const SearchResultPage({Key? key, required this.searchQuery}) : super(key: key);

  @override
  _SearchResultPageState createState() => _SearchResultPageState();
}

class _SearchResultPageState extends State<SearchResultPage> {

  @override
  Widget build(BuildContext context) {
    // 이곳에서 검색 결과를 표시하거나 처리할 수 있음
    return Scaffold(
        appBar: AppBar(
          title: Text("Search Results for: ${widget.searchQuery}"),
        ),
        body: Text("검색된 결과입니다 : ${widget.searchQuery}"),
        // 여기에 검색 결과를 표시하는 위젯을 추가
    );
  }
}