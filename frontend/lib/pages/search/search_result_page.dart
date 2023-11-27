import 'package:beautyminder/pages/product/product_detail_page.dart';
import 'package:flutter/material.dart';

import '../../dto/cosmetic_model.dart';
import '../../services/homeSearch_service.dart';
import '../../widget/searchAppBar.dart';

class SearchResultPage extends StatefulWidget {
  final List<Cosmetic> searchResults;
  final String searchQuery;

  const SearchResultPage(
      {Key? key, required this.searchQuery, required this.searchResults})
      : super(key: key);

  @override
  _SearchResultPageState createState() => _SearchResultPageState();
}

class _SearchResultPageState extends State<SearchResultPage> {
  String searchQuery = ''; // 검색어를 저장할 변수
  final TextEditingController textController = TextEditingController();

  @override
  Widget build(BuildContext context) {
    // 이곳에서 검색 결과를 표시하거나 처리할 수 있음
    return Scaffold(
      appBar: SearchAppBar(title: _title()),
      body: _searchResultPageUI(),
    );
  }

  Widget _title() {
    return Container(
      height: 40,
      child: Row(
        mainAxisAlignment: MainAxisAlignment.start,
        mainAxisSize: MainAxisSize.max,
        children: [
          SizedBox(
            width: 10,
          ),
          Flexible(
            flex: 1,
            child: TextField(
              controller: textController,
              // onSubmitted: (text) {
              //   Navigator.of(context).push(MaterialPageRoute(builder: (context) => SearchResultPage(searchQuery: text)),);
              // },
              onChanged: (text) {
                searchQuery = text;
              },
              decoration: const InputDecoration(
                contentPadding: EdgeInsets.symmetric(
                  vertical: 3,
                  horizontal: 15,
                ),
                focusedBorder: OutlineInputBorder(
                  borderRadius: BorderRadius.all(
                    Radius.circular(10), //포커스 시
                  ),
                  borderSide: BorderSide(
                    color: Color(0xffd86a04),
                  ),
                ),
                enabledBorder: OutlineInputBorder(
                  borderRadius: BorderRadius.all(
                    Radius.circular(10), // 활성화 상태 모서리를 둥글게 조정
                  ),
                  borderSide: BorderSide(
                    color: Colors.grey,
                  ),
                ),
                hintText: "검색 키워드를 입력해주세요.",
              ),
            ),
          ),
          SizedBox(
            width: 8,
          ),
          IconButton(
            onPressed: () async {
              try {
                final result = await SearchService.searchAnything(searchQuery);
                print(result);

                Navigator.of(context).push(
                  MaterialPageRoute(
                      builder: (context) => SearchResultPage(
                            searchQuery: searchQuery,
                            searchResults: result,
                          )),
                );
                print('////////////searchQuery : $searchQuery');
              } catch (e) {
                print('Error searching anything: $e');
              }
            },
            icon: Icon(
              Icons.search,
              color: Color(0xffd86a04),
            ),
          ),
        ],
      ),
    );
  }

  Widget _searchResultPageUI() {
    return Container(
        child: Column(
      children: <Widget>[
        const SizedBox(height: 40),
        _resultText(),
        _divider(),
        const SizedBox(height: 20),
        widget.searchResults.isEmpty
            ? const Center(
                child: Text(
                  '검색결과가 없습니다',
                  style: TextStyle(
                    fontSize: 18,
                    fontWeight: FontWeight.normal,
                    color: Colors.grey,
                  ),
                ),
              )
            : _productList(),
      ],
    ));
  }

  Widget _resultText() {
    return Padding(
      padding: const EdgeInsets.symmetric(horizontal: 20),
      child: Row(
        mainAxisAlignment: MainAxisAlignment.spaceBetween,
        children: [
          Text(
            "검색 결과 : ${widget.searchQuery}",
            style: TextStyle(
              fontSize: 18,
              fontWeight: FontWeight.bold,
              color: Color(0xffd86a04),
            ),
          ),
          Text(
            "${widget.searchResults.length}개의 제품",
            style: TextStyle(
              fontSize: 15,
              color: Colors.grey,
            ),
          ),
        ],
      ),
    );
  }

  Widget _productList() {
    return Expanded(
      child: ListView.builder(
        itemCount: widget.searchResults.length,
        itemBuilder: (context, index) {
          final product = widget.searchResults![index];
          return GestureDetector(
            onTap: () async {
              _navigateToProductDetailPage(product);
            },
            child: Container(
              height: 70,
              child: ListTile(
                leading: (product?.images != null && product.images!.isNotEmpty)
                    ? Container(
                        width: 70,
                        height: 70,
                        child: Image.network(
                          product.images![0],
                          width: 100,
                          height: 100,
                          fit: BoxFit.cover,
                        ),
                      )
                    : Container(
                        width: 70.0,
                        height: 70.0,
                        color: Colors.white,
                      ),
                title: Text(
                  product.name,
                  style: TextStyle(
                    fontSize: 18,
                    letterSpacing: 0,
                  ),
                ), // 이름 표시
                // 다른 정보도 필요하다면 여기에 추가
              ),
            ),
          );
        },
      ),
    );
  }

  void _navigateToProductDetailPage(Cosmetic product) async {
    try {
      print(product);

      Navigator.of(context).push(MaterialPageRoute(
        builder: (context) => ProductDetailPage(
          searchResults: product,
        ),
      ));
      print('////////////searchQuery : $searchQuery');
    } catch (e) {
      print('Error searching anything: $e');
    }
  }

  Widget _divider() {
    return const Divider(
      height: 20,
      thickness: 1,
      indent: 20,
      endIndent: 20,
      color: Colors.grey,
    );
  }
}
