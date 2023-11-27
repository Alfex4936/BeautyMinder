import 'package:beautyminder/pages/baumann/baumann_history_page.dart';
import 'package:beautyminder/pages/todo/todo_page.dart';
import 'package:beautyminder/services/Cosmetic_Recommend_Service.dart';
import 'package:beautyminder/services/keywordRank_service.dart';
import 'package:beautyminder/widget/homepageAppBar.dart';
import 'package:flutter/material.dart';
import 'package:intl/intl.dart';

import '../../dto/baumann_result_model.dart';
import '../../dto/cosmetic_expiry_model.dart';
import '../../dto/todo_model.dart';
import '../../dto/user_model.dart';
import '../../services/baumann_service.dart';
import '../../services/expiry_service.dart';
import '../../services/homeSearch_service.dart';
import '../../services/todo_service.dart';
import '../../widget/commonBottomNavigationBar.dart';
import '../baumann/baumann_test_start_page.dart';
import '../chat/chat_page.dart';
import '../my/my_page.dart';
import '../pouch/expiry_page.dart';
import '../recommend/recommend_bloc_screen.dart';
import '../search/search_page.dart';

class HomePage extends StatefulWidget {
  const HomePage({Key? key, required this.user}) : super(key: key);

  // final dynamic responseData;
  final User? user;

  @override
  State<HomePage> createState() => _HomePageState();
}

class _HomePageState extends State<HomePage> {
  final int _currentIndex = 2;
  bool isApiCallProcess = false;

  List<CosmeticExpiry> expiries = [];

  // List favorites = [];
  List recommends = [];
  Todo? todayTodos;

  bool isLoading = true;

  @override
  void initState() {
    super.initState();
    _getExpiries();
    // _getFavorites();
    _getRecommends();
    _getTodayTodos();
    print("hello this is : ${recommends}");
    print("hello this is : ${todayTodos}");
    // futureTodoList = TodoService.getAllTodos();
  }

  // Future<void> _getExpiries() async {
  //   try {
  //     expiries = await ExpiryService.getAllExpiries();
  //     // Force a rebuild of the UI after fetching data
  //     if (mounted) {
  //       setState(() {});
  //     }
  //   } catch (e) {
  //     print('An error occurred while loading expiries: $e');
  //   }
  // }

  Future<void> _getExpiries() async {
    setState(() {
      isLoading = true;
    });

    try {
      List<CosmeticExpiry> loadedExpiries =
          await ExpiryService.getAllExpiries();

      // 각 expiry에 대한 이미지 URL 로드
      for (var expiry in loadedExpiries) {
        try {
          // 예시: productName을 이용하여 관련 이미지 URL 검색
          var cosmetic =
              await SearchService.searchCosmeticsByName(expiry.productName);
          if (cosmetic.isNotEmpty) {
            expiry.imageUrl = cosmetic.first.images.isNotEmpty
                ? cosmetic.first.images.first
                : null;
          }
        } catch (e) {
          print("Error loading image for ${expiry.productName}: $e");
        }
      }

      setState(() {
        expiries = loadedExpiries;
        isLoading = false;
      });
    } catch (e) {
      print('An error occurred while loading expiries: $e');
      setState(() {
        isLoading = false;
      });
    }
  }

  // Future<void> _getFavorites() async {
  //   try {
  //     final info = await APIService.getFavorites();
  //     setState(() {
  //       favorites = info.value!;
  //       isLoading = false;
  //     });
  //   } catch (e) {
  //     print('An error occurred while loading expiries: $e');
  //   }
  // }

  Future<void> _getRecommends() async {
    try {
      final info = await CosmeticSearchService.getAllCosmetics();
      setState(() {
        recommends = info.value!;
        isLoading = false;
      });
    } catch (e) {
      print('An error occurred while loading expiries: $e');
    }
  }

  Future<void> _getTodayTodos() async {
    try {
      String todayFormatted = DateFormat('yyyy-MM-dd').format(DateTime.now());

      final info = await TodoService.getTodoOf(todayFormatted);
      setState(() {
        todayTodos = info.value!;
        isLoading = false;
      });
    } catch (e) {
      print('An error occurred while loading expiries: $e');
    }
  }

  @override
  Widget build(BuildContext context) {
    print("Here is Home Page : ${widget.user?.id}");
    print("Here is Home Page : ${widget.user}");

    return Scaffold(
      appBar: HomepageAppBar(actions: <Widget>[
        IconButton(
          icon: Icon(Icons.search),
          onPressed: () async {
            // 이미 API 호출이 진행 중인지 확인
            if (isApiCallProcess) {
              return;
            }
            // API 호출 중임을 표시
            setState(() {
              isApiCallProcess = true;
            });
            try {
              final result = await KeywordRankService.getKeywordRank();
              final result2 = await KeywordRankService.getProductRank();

              print('fdsfd keyword rank : ${result.value}');
              print('dkdkd product rank : ${result2.value}');

              if (result.isSuccess) {
                // SearchPage로 이동하고 가져온 데이터를 전달합니다.
                Navigator.push(
                  context,
                  MaterialPageRoute(
                    builder: (context) => SearchPage(
                      data: result.value!,
                      data2: result2.value!,
                    ),
                  ),
                );
              } else {
                // API 호출 실패를 처리합니다.
                Navigator.push(
                  context,
                  MaterialPageRoute(
                    builder: (context) => SearchPage(
                      data: null,
                      data2: null,
                    ),
                  ),
                );
              }
            } catch (e) {
              Navigator.push(
                context,
                MaterialPageRoute(
                  builder: (context) => SearchPage(data: null, data2: null),
                ),
              );
            } finally {
              // API 호출 상태를 초기화합니다.
              setState(() {
                isApiCallProcess = false;
              });
            }
          },
        ),
      ]),
      body: SingleChildScrollView(
        child: _homePageUI(),
      ),
      bottomNavigationBar: _underNavigation(),
    );
  }

  Widget _homePageUI() {
    return Container(
      padding: EdgeInsets.symmetric(horizontal: 20),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.center,
        mainAxisAlignment: MainAxisAlignment.spaceEvenly,
        children: <Widget>[
          SizedBox(
            height: 40,
          ),
          _invalidProductBtn(),
          SizedBox(
            height: 20,
          ),
          Row(
            children: <Widget>[
              Expanded(child: _recommendProductBtn()),
              SizedBox(
                width: 30,
              ),
              Column(
                // mainAxisAlignment: MainAxisAlignment.spaceAround,
                children: <Widget>[
                  _personalSkinTypeBtn(),
                  SizedBox(
                    height: 25,
                  ),
                  _chatBtn(),
                ],
              )
            ],
          ),
          SizedBox(
            height: 20,
          ),
          _routineBtn(),
          // _label()
        ],
      ),
    );
  }

  Widget _invalidProductBtn() {
    final screenWidth = MediaQuery.of(context).size.width;

    return ElevatedButton(
      onPressed: () async {
        if (isApiCallProcess) {
          return;
        }
        setState(() {
          isApiCallProcess = true;
        });
        try {
          Navigator.of(context).push(
              MaterialPageRoute(builder: (context) => CosmeticExpiryPage()));
        } catch (e) {
          // Handle the error case
          print('An error occurred: $e');
        } finally {
          // API 호출 상태를 초기화합니다.
          setState(() {
            isApiCallProcess = false;
          });
        }
      },
      style: ElevatedButton.styleFrom(
        backgroundColor: Color(0xffffb876),
        // 버튼의 배경색을 검정색으로 설정
        foregroundColor: Colors.white,
        // 버튼의 글씨색을 하얀색으로 설정
        elevation: 0,
        // 그림자 없애기
        minimumSize: Size(screenWidth, 200.0),
        shape: RoundedRectangleBorder(
          borderRadius: BorderRadius.circular(10.0), // 모서리를 더 둥글게 설정
        ),
      ),
      child: Align(
          alignment: Alignment.topLeft,
          child: (expiries.isNotEmpty && expiries.length != 0)
              ? Center(
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.center,
                    children: [
                      Row(
                        mainAxisAlignment: MainAxisAlignment.spaceBetween,
                        children: [
                          Text(
                            "유통기한 임박 화장품 ",
                            style: TextStyle(
                              // fontWeight: FontWeight.bold,
                              fontSize: 18,
                            ),
                          ),
                          Icon(
                            Icons.arrow_forward_ios,
                            size: 15,
                          ),
                        ],
                      ),
                      _buildExpiryInfo(),
                    ],
                  ),
                )
              : Center(
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.center,
                    children: [
                      Row(
                        mainAxisAlignment: MainAxisAlignment.spaceBetween,
                        children: [
                          Text(
                            "유통기한 임박 화장품 ",
                            style: TextStyle(
                              // fontWeight: FontWeight.bold,
                              fontSize: 18,
                            ),
                          ),
                          Icon(
                            Icons.arrow_forward_ios,
                            size: 15,
                          ),
                        ],
                      ),
                      _buildDefaultText(),
                    ],
                  ),
                )),
    );
  }

  Widget _buildExpiryInfo() {
    return SingleChildScrollView(
      scrollDirection: Axis.horizontal,
      child: Row(
        mainAxisAlignment: MainAxisAlignment.center,
        children: expiries.take(3).map((expiry) {
          DateTime now = DateTime.now();
          DateTime expiryDate = expiry.expiryDate ?? DateTime.now();
          Duration difference = expiryDate.difference(now);
          bool isDatePassed = difference.isNegative;
          // Customize this part according to your expiry model
          return Container(
            margin: EdgeInsets.all(8.0),
            child: Column(
              children: [
                SizedBox(
                  height: 10,
                ),
                Container(
                    width: 95,
                    height: 95,
                    decoration: BoxDecoration(
                      color: Colors.grey, // 네모 박스의 색상
                      borderRadius: BorderRadius.circular(8.0),
                    ),
                    child: (expiry.imageUrl != null)
                        ? Image.network(
                            expiry.imageUrl!,
                            width: 10,
                            height: 10,
                            fit: BoxFit.cover,
                          )
                        : Image.asset(
                            'assets/images/noImg.jpg',
                            fit: BoxFit.cover,
                          ) // 이미지가 없는 경우
                    ),
                SizedBox(
                  height: 10,
                ),
                // Display D-day or any other information here
                Text(
                  isDatePassed
                      ? 'D+${difference.inDays.abs() + 1}'
                      : 'D-${difference.inDays}',
                  style: TextStyle(fontSize: 18, fontWeight: FontWeight.bold),
                ),
              ],
            ),
          );
        }).toList(),
      ),
    );
  }

  Widget _buildDefaultText() {
    return Center(
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.center,
        children: [
          SizedBox(height: 5),
          Text(
            "등록된 화장품이 없습니다.\n화장품 등록하기",
            style: TextStyle(fontSize: 15, fontWeight: FontWeight.bold),
          ),
        ],
      ),
    );
  }

  Widget _recommendProductBtn() {
    final screenWidth = MediaQuery.of(context).size.width / 2 - 40;

    return ElevatedButton(
      onPressed: () {
        Navigator.of(context)
            .push(MaterialPageRoute(builder: (context) => const RecPage()));
        // Navigator.of(context)
        //     .push(MaterialPageRoute(builder: (context) => const MyFavoritePage()));
      },
      style: ElevatedButton.styleFrom(
        backgroundColor: Color(0xffffecda),
        // 버튼의 배경색을 검정색으로 설정
        foregroundColor: Color(0xffff820e),
        // 버튼의 글씨색을 하얀색으로 설정
        elevation: 0,
        // 그림자 없애기
        minimumSize: Size(screenWidth, 200.0),
        shape: RoundedRectangleBorder(
          borderRadius: BorderRadius.circular(10.0), // 모서리를 더 둥글게 설정
        ),
      ),
      child: Align(
          alignment: Alignment.topLeft,
          child: (recommends.isNotEmpty && recommends.length != 0)
              ? Center(
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.center,
                    children: [
                      Row(
                        mainAxisAlignment: MainAxisAlignment.spaceBetween,
                        children: [
                          Text(
                            "추천 제품 ",
                            style: TextStyle(
                              // fontWeight: FontWeight.bold,
                              fontSize: 18,
                            ),
                          ),
                          Icon(
                            Icons.arrow_forward_ios,
                            size: 15,
                          ),
                        ],
                      ),
                      SizedBox(
                        height: 15,
                      ),
                      _buildRecommendText(),
                    ],
                  ),
                )
              : Center(
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.center,
                    children: [
                      Row(
                        mainAxisAlignment: MainAxisAlignment.spaceBetween,
                        children: [
                          Text(
                            "추천 제품 ",
                            style: TextStyle(
                              // fontWeight: FontWeight.bold,
                              fontSize: 18,
                            ),
                          ),
                          Icon(
                            Icons.arrow_forward_ios,
                            size: 15,
                          ),
                        ],
                      ),
                      _buildRecommendDefaultText(),
                    ],
                  ),
                )),
    );
  }

  // Widget _buildFavoriteText(){
  //   return Column(
  //       mainAxisAlignment: MainAxisAlignment.center,
  //       children: favorites.take(3).map((item) {
  //         return Container(
  //           margin: EdgeInsets.all(8.0),
  //           child: Row(
  //             mainAxisAlignment: MainAxisAlignment.start,
  //             children: [
  //               SizedBox(width: 10,),
  //               Container(
  //                 width: MediaQuery.of(context).size.width / 2 - 100,
  //                 child: Text(
  //                   item['name'],
  //                   style: TextStyle(fontSize: 15),
  //                   overflow: TextOverflow.ellipsis,
  //                 ),
  //               ),
  //             ],
  //           ),
  //         );
  //       }).toList(),
  //     );
  // }
  //
  // Widget _buildFavoriteDefaultText() {
  //   return Center(
  //     child: Column(
  //       crossAxisAlignment: CrossAxisAlignment.center,
  //       children: [
  //         SizedBox(height: 5),
  //         Text(
  //           "즐겨찾기된 화장품이 없습니다.\n화장품 등록하기",
  //           style: TextStyle(fontSize: 15, fontWeight: FontWeight.bold),
  //         ),
  //       ],
  //     ),
  //   );
  // }

  // Widget _buildRecommendText(){
  //   return Row(
  //     mainAxisAlignment: MainAxisAlignment.center,
  //     children: recommends.take(1).map((item) {
  //       return Container(
  //         margin: EdgeInsets.all(8.0),
  //         child: Column(
  //           mainAxisAlignment: MainAxisAlignment.start,
  //           children: [
  //             Container(
  //                 decoration: BoxDecoration(
  //                   color: Colors.grey, // 네모 박스의 색상
  //                   borderRadius: BorderRadius.circular(8.0),
  //                 ),
  //                 child:
  //                 (item.images[0] != null)
  //                     ? Image.network(
  //                   item.images[0],
  //                   width: 90,
  //                   height: 90,
  //                   fit: BoxFit.cover,
  //                 )
  //                     :
  //                 Image.asset('assets/images/noImg.jpg', fit: BoxFit.cover,)// 이미지가 없는 경우
  //             ),
  //             SizedBox(height: 5,),
  //             Container(
  //               width: MediaQuery.of(context).size.width / 2 - 100,
  //               child: Text(
  //                 item.name,
  //                 style: TextStyle(color: Colors.black, fontSize: 15),
  //                 overflow: TextOverflow.ellipsis,
  //               ),
  //             ),
  //           ],
  //         ),
  //       );
  //     }).toList(),
  //   );
  // }
  Widget _buildRecommendText() {
    return Row(
      mainAxisAlignment: MainAxisAlignment.center,
      children: recommends.take(1).map((item) {
        return Container(
          // margin: EdgeInsets.all(8.0),
          child: Column(
            mainAxisAlignment: MainAxisAlignment.start,
            children: [
              CircleAvatar(
                radius: 50, // Adjust the radius as needed
                backgroundColor: Colors.grey,
                backgroundImage: (item.images[0] != null)
                    ? NetworkImage(item.images[0])
                    : AssetImage('assets/images/noImg.jpg') as ImageProvider,
              ),
              SizedBox(height: 10),
              Container(
                width: MediaQuery.of(context).size.width / 2 - 100,
                child: Text(
                  item.name,
                  style: TextStyle(color: Colors.black, fontSize: 15),
                  overflow: TextOverflow.ellipsis,
                ),
              ),
            ],
          ),
        );
      }).toList(),
    );
  }

  Widget _buildRecommendDefaultText() {
    return Center(
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.center,
        children: [
          SizedBox(height: 5),
          Text(
            "추천 화장품이 없습니다.\n화장품 추천받기",
            style: TextStyle(fontSize: 15, fontWeight: FontWeight.bold),
          ),
        ],
      ),
    );
  }

  Widget _personalSkinTypeBtn() {
    final screenWidth = MediaQuery.of(context).size.width / 2 - 30;
    BaumResult<List<BaumannResult>> result =
        BaumResult<List<BaumannResult>>.success([]);

    return ElevatedButton(
      onPressed: () async {
        // 이미 API 호출이 진행 중인지 확인
        if (isApiCallProcess) {
          return;
        }
        // API 호출 중임을 표시
        setState(() {
          isApiCallProcess = true;
        });
        try {
          result = await BaumannService.getBaumannHistory();

          print("This is Baumann Button in Home Page : ${result.value}");

          if (result.isSuccess && result.value!.isNotEmpty) {
            Navigator.of(context).push(MaterialPageRoute(
                builder: (context) =>
                    BaumannHistoryPage(resultData: result.value)));
            print("This is BaumannButton in HomePage2 : ${result.value}");
          } else {
            Navigator.of(context).push(
                MaterialPageRoute(builder: (context) => BaumannStartPage()));
            print("This is Baumann Button in Home Page2 : ${result.isSuccess}");
          }
        } catch (e) {
          // Handle the error case
          print('An error occurred: $e');
        } finally {
          // API 호출 상태를 초기화합니다.
          setState(() {
            isApiCallProcess = false;
          });
        }
      },
      style: ElevatedButton.styleFrom(
        backgroundColor: Color(0xfffe9738),
        // 버튼의 배경색을 검정색으로 설정
        foregroundColor: Colors.white,
        // 버튼의 글씨색을 하얀색으로 설정
        elevation: 0,
        // 그림자 없애기
        minimumSize: Size(screenWidth, 90.0),
        shape: RoundedRectangleBorder(
          borderRadius: BorderRadius.circular(10.0), // 모서리를 더 둥글게 설정
        ),
        // padding: EdgeInsets.zero,
      ),
      child: Align(
        alignment: Alignment.topLeft,
        child: Center(
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.center,
            children: [
              Row(
                mainAxisAlignment: MainAxisAlignment.spaceBetween,
                children: [
                  Text(
                    "내 피부 타입 ",
                    style: TextStyle(
                      // fontWeight: FontWeight.bold,
                      fontSize: 18,
                    ),
                  ),
                  Icon(
                    Icons.arrow_forward_ios,
                    size: 15,
                    // Add any other styling properties as needed
                  ),
                ],
              ),
              SizedBox(height: 5),
              Text((result.value != null) ? "${widget.user?.baumann}" : "테스트하기",
                  style: TextStyle(fontSize: 25, fontWeight: FontWeight.bold)),
            ],
          ),
        ),
      ),
    );
  }

  Widget _chatBtn() {
    final screenWidth = MediaQuery.of(context).size.width / 2 - 30;

    return ElevatedButton(
      onPressed: () {
        Navigator.of(context)
            .push(MaterialPageRoute(builder: (context) => ChatPage()));
      },
      style: ElevatedButton.styleFrom(
        backgroundColor: Color(0xffffd1a6),
        // 버튼의 배경색을 검정색으로 설정
        foregroundColor: Color(0xffd86a04),
        // 버튼의 글씨색을 하얀색으로 설정
        elevation: 0,
        // 그림자 없애기
        minimumSize: Size(screenWidth, 90.0),
        shape: RoundedRectangleBorder(
          borderRadius: BorderRadius.circular(10.0), // 모서리를 더 둥글게 설정
        ),
      ),
      child: Align(
        alignment: Alignment.topLeft,
        child: Row(
          mainAxisAlignment: MainAxisAlignment.spaceBetween,
          children: [
            Text(
              "소통방 가기 ",
              style: TextStyle(
                // fontWeight: FontWeight.bold,
                fontSize: 18,
              ),
            ),
            Icon(
              Icons.arrow_forward_ios,
              size: 15,
              // Add any other styling properties as needed
            ),
          ],
        ),
      ),
    );
  }

  Widget _routineBtn() {
    final screenWidth = MediaQuery.of(context).size.width;

    return ElevatedButton(
      onPressed: () {
        Navigator.of(context)
            .push(MaterialPageRoute(builder: (context) => CalendarPage()));
      },
      style: ElevatedButton.styleFrom(
        backgroundColor: Color(0xffe7e4e1),
        // 버튼의 배경색을 검정색으로 설정
        foregroundColor: Color(0xffff820e),
        // 버튼의 글씨색을 하얀색으로 설정
        elevation: 0,
        // 그림자 없애기
        minimumSize: Size(screenWidth, 200.0),
        shape: RoundedRectangleBorder(
          borderRadius: BorderRadius.circular(10.0), // 모서리를 더 둥글게 설정
        ),
      ),
      child: Align(
          alignment: Alignment.topLeft,
          child: (recommends.isNotEmpty && recommends.length != 0)
              ? Center(
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.center,
                    children: [
                      Row(
                        mainAxisAlignment: MainAxisAlignment.spaceBetween,
                        children: [
                          Text(
                            "나의 루틴 확인하기 ",
                            style: TextStyle(
                              // fontWeight: FontWeight.bold,
                              fontSize: 18,
                            ),
                          ),
                          Icon(
                            Icons.arrow_forward_ios,
                            size: 15,
                          ),
                        ],
                      ),
                      SizedBox(
                        height: 15,
                      ),
                      _buildTodoText(),
                    ],
                  ),
                )
              : Center(
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.center,
                    children: [
                      Row(
                        mainAxisAlignment: MainAxisAlignment.spaceBetween,
                        children: [
                          Text(
                            "나의 루틴 확인하기 ",
                            style: TextStyle(
                              // fontWeight: FontWeight.bold,
                              fontSize: 18,
                            ),
                          ),
                          Icon(
                            Icons.arrow_forward_ios,
                            size: 15,
                          ),
                        ],
                      ),
                      _buildTodoDefaultText(),
                    ],
                  ),
                )),
    );
  }

  Widget _buildTodoText() {
    print("hello this is 2: ${todayTodos}");
    return Row(
      mainAxisAlignment: MainAxisAlignment.center,
      children: [
        Container(
          margin: EdgeInsets.all(8.0),
          child: Column(
            mainAxisAlignment: MainAxisAlignment.start,
            children: [
              SizedBox(height: 5),
              Container(
                width: MediaQuery.of(context).size.width / 2 - 60,
                child: todayTodos != null && todayTodos!.tasks.isNotEmpty
                    ? Column(
                        children: todayTodos!.tasks
                            .map((task) => Text(
                                  task.description,
                                  style: TextStyle(
                                      color: Colors.black, fontSize: 15),
                                ))
                            .toList(),
                      )
                    : Text(
                        '등록된 루틴이 없습니다',
                        style: TextStyle(fontSize: 15),
                      ),
              ),
            ],
          ),
        ),
      ],
    );
  }

  Widget _buildTodoDefaultText() {
    return Center(
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.center,
        children: [
          SizedBox(height: 5),
          Text(
            "등록된 루틴이 없습니다.\n화장품 사용 루틴 등록하기",
            style: TextStyle(fontSize: 15, fontWeight: FontWeight.bold),
          ),
        ],
      ),
    );
  }

  Widget _underNavigation() {
    return CommonBottomNavigationBar(
        currentIndex: _currentIndex,
        onTap: (int index) {
          // 페이지 전환 로직 추가
          if (index == 0) {
            Navigator.of(context)
                .push(MaterialPageRoute(builder: (context) => const RecPage()));
          } else if (index == 1) {
            Navigator.of(context).push(
                MaterialPageRoute(builder: (context) => CosmeticExpiryPage()));
          } else if (index == 3) {
            Navigator.of(context).push(
                MaterialPageRoute(builder: (context) => const CalendarPage()));
          } else if (index == 4) {
            Navigator.of(context)
                .push(MaterialPageRoute(builder: (context) => const MyPage()));
          }
        });
  }
}
