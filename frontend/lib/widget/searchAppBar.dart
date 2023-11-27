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
                vertical: 1,
                horizontal: 8,
              ),
              border: OutlineInputBorder(
                borderRadius: BorderRadius.all(
                  Radius.circular(1),
                ),
                borderSide: BorderSide(
                  color: Color(0xffd86a04),
                )
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
    // iconTheme: IconThemeData(
    //   color: Color(0xffd86a04),
    // ),
  );
}
