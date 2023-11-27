import 'package:flutter/material.dart';

class SearchAppBar extends AppBar {
  SearchAppBar({Key? key})
      : super(
    key: key,
    backgroundColor: Color(0xffffecda),
    elevation: 0,
    centerTitle: false,
    title: Row(
      mainAxisAlignment: MainAxisAlignment.start,
      mainAxisSize: MainAxisSize.max,
      children: [
        // Icon(
        //   Icons.search,
        //   color: Color(0xffd86a04),
        // ),
        SizedBox(
          width: 8,
        ),
        Flexible(
          flex: 1,
          child: TextField(
            decoration: InputDecoration(
              contentPadding: EdgeInsets.symmetric(
                vertical: 5,
                horizontal: 15,
              ),
              // border: OutlineInputBorder(
              //   borderRadius: BorderRadius.all(
              //     Radius.circular(10),
              //   ),
              // ),
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
        // TextButton(
        //   onPressed: () {},
        //   child: Text('검색'),
        // ),
        IconButton(
          onPressed: () {},
          icon: Icon(
            Icons.search,
            color: Color(0xffd86a04),
          ),
        ),
      ],
    ),
    // title: Text(
    //   "BeautyMinder",
    //   style: TextStyle(color: Color(0xffd86a04)),
    // ),
    iconTheme: IconThemeData(
      color: Color(0xffd86a04),
    ),
  );
}
