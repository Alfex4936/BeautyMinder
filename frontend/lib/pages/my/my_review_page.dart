import 'package:beautyminder/dto/review_model.dart';
import 'package:beautyminder/services/api_service.dart';
import 'package:beautyminder/widget/commonAppBar.dart';
import 'package:flutter/material.dart';

class MyReviewPage extends StatefulWidget {
  const MyReviewPage({super.key});

  @override
  State<MyReviewPage> createState() => _MyReviewPageState();
}

class _MyReviewPageState extends State<MyReviewPage> {
  List<dynamic>? reviews;
  bool isLoading = true;

  @override
  void initState() {
    // TODO: implement initState
    super.initState();
    getReviews();
  }

  getReviews() async {
    try {
      final info = await APIService.getReviews();
      setState(() {
        reviews = info.value;
        isLoading = false;
        print(reviews);
      });
    } catch (e) {
      print('error is $e');
    }
  }

  @override
  void dispose() {
    // TODO: implement dispose
    super.dispose();
  }

  // reviews 데이터 사용법
  // 위 reviews = info; 의 주석을 제거하고,
  // reviews[index].brand 처럼 쓰기.
  // itemCounter는 reviews.length 로 하기.

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: CommonAppBar(),
      body: _body(),
    );
  }

  Widget _body() {
    return isLoading
        ? Center(child: Text('로딩 중'))
        : ListView.builder(
            itemCount: reviews?.length ?? 0,
            itemBuilder: (context, index) => Padding(
                  padding: const EdgeInsets.all(8.0),
                  child: Container(
                    decoration: BoxDecoration(
                      borderRadius: BorderRadius.circular(10),
                      color: Colors.orange[50],
                    ),
                    child: Column(
                        crossAxisAlignment: CrossAxisAlignment.start,
                        children: [
                          Padding(
                            padding: const EdgeInsets.all(8),
                            child: Row(children: [
                              SizedBox(
                                  width: 50,
                                  child:
                                      Image.asset('assets/images/profile.jpg')),
                              // SizedBox(
                              //   width: 50,
                              //   child:
                              //       Image.network(reviews?[index]['images'][0],
                              //           loadingBuilder: (BuildContext context,
                              //               Widget child,
                              //               ImageChunkEvent? loadingProgress) {
                              //     if (loadingProgress == null) {
                              //       return child; // 이미지 로딩이 완료되면 정상적으로 표시
                              //     } else {
                              //       return CircularProgressIndicator(
                              //         value:
                              //             loadingProgress.expectedTotalBytes !=
                              //                     null
                              //                 ? loadingProgress
                              //                         .cumulativeBytesLoaded /
                              //                     (loadingProgress
                              //                             .expectedTotalBytes ??
                              //                         1)
                              //                 : null,
                              //       );
                              //     }
                              //   }, errorBuilder: (context, error, stackTrace) {
                              //     return Image.asset(
                              //       'assets/images/profile.jpg',
                              //     );
                              //   }),
                              // ),
                              SizedBox(width: 30),
                              Text(reviews?[index]['cosmetic']['name'])
                            ]),
                          ),
                          Padding(
                            padding: const EdgeInsets.all(8),
                            child: Text(reviews?[index]['content']),
                          ),
                        ]),
                  ),
                ));
  }
}
