import 'package:beautyminder/dto/cosmetic_model.dart';
import 'package:beautyminder/pages/product/review_page.dart';
import 'package:beautyminder/services/gptReview_service.dart';
import 'package:carousel_slider/carousel_slider.dart';
import 'package:flutter/material.dart';
import 'package:flutter_rating_bar/flutter_rating_bar.dart';
import 'package:flutter_spinkit/flutter_spinkit.dart';
import 'package:shared_preferences/shared_preferences.dart';

import '../../dto/gptReview_model.dart';
import '../../services/favorites_service.dart';
import '../../widget/commonAppBar.dart';

class ProductDetailPage extends StatefulWidget {
  const ProductDetailPage({Key? key, required this.searchResults})
      : super(key: key);

  final Cosmetic searchResults;

  @override
  _ProductDetailPageState createState() => _ProductDetailPageState();
}

class _ProductDetailPageState extends State<ProductDetailPage> {
  late Future<Result<GPTReviewInfo>> _gptReviewInfo;
  bool showPositiveReview = true;
  bool isFavorite = false;

  @override
  void initState() {
    print("start page : $isFavorite");
    super.initState();
    _gptReviewInfo = GPTReviewService.getGPTReviews(widget.searchResults.id);
    _loadFavoriteState(widget.searchResults.id);
  }

  Future<void> _loadFavoriteState(String prouctId) async {
    isFavorite = await FavoriteManager().getFavoriteState(prouctId);
    setState(() {}); // Trigger a rebuild to reflect the initial state.
  }

  Future<void> _saveFavoriteState(String prouctId) async {
    await FavoriteManager().setFavoriteState(prouctId, isFavorite);
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: CommonAppBar(),
      body: SingleChildScrollView(
        child: _productDetailPageUI(),
      ),
    );
  }

  Widget _productDetailPageUI() {
    return Padding(
      padding: const EdgeInsets.symmetric(horizontal: 20),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          const SizedBox(height: 40),
          _displayingName(),
          const SizedBox(height: 20),
          _displayImages(),
          const SizedBox(height: 10),
          Row(
            mainAxisAlignment: MainAxisAlignment.spaceBetween,
            children: [
              _displayBrand(),
              _likesBtn(),
            ],
          ),
          _displayCategory(),
          _displayKeywords(),
          _displayRatingStars(),
          const SizedBox(height: 20),
          _gptBox(),
          const SizedBox(height: 80),
        ],
      ),
    );
  }

  Widget _displayingName() {
    return Padding(
      padding: const EdgeInsets.all(8.0),
      child: Center(
        child: Text(
          widget.searchResults.name,
          style: TextStyle(fontSize: 23, fontWeight: FontWeight.bold),
          textAlign: TextAlign.center,
        ),
      ),
    );
  }

  Widget _displayImages() {
    return Container(
      height: 200,
      child: CarouselSlider(
        options: CarouselOptions(
          height: 500,
          enableInfiniteScroll: false, //무한스크롤 비활성
          viewportFraction: 1.0, //이미지 전체 화면 사용
          aspectRatio: 16 / 9, //가로 세로 비율 유지
        ),
        items: widget.searchResults.images.map((image) {
          return Padding(
            padding: const EdgeInsets.all(8.0),
            child: Image.network(
              image,
              width: double.infinity,
              fit: BoxFit.contain,
            ),
          );
        }).toList(),
      ),
    );
  }

  // Widget _likesBtn() {
  //   return IconButton(
  //     onPressed: () async {
  //       setState(() {
  //         isFavorite  = !isFavorite;
  //       });
  //       await _saveFavoriteState(widget.searchResults.id);
  //       // Call FavoritesService to upload favorites when the heart icon is pressed
  //       if (isFavorite) {
  //         try {
  //           // Assuming you have the cosmeticId from your widget
  //           String cosmeticId = widget.searchResults.id;
  //
  //           // Call the uploadFavorites method from FavoritesService
  //           String result = await FavoritesService.uploadFavorites(cosmeticId);
  //
  //           if (result == "success") {
  //             print("Favorites uploaded successfully! : $isFavorite");
  //           } else {
  //             print("Failed to upload favorites");
  //           }
  //         } catch (e) {
  //           print("An error occurred while uploading favorites: $e");
  //         }
  //       }
  //     },
  //     icon: Icon(
  //       isFavorite ? Icons.favorite : Icons.favorite_border,
  //       color: isFavorite ? Colors.red : null,
  //     ),
  //   );
  // }
  Widget _likesBtn() {
    return IconButton(
      onPressed: () async {
        setState(() {
          isFavorite = !isFavorite;
        });
        await _saveFavoriteState(widget.searchResults.id);

        // Call FavoritesService to upload or delete favorites based on the button state
        try {
          // Assuming you have the cosmeticId from your widget
          String cosmeticId = widget.searchResults.id;

          // Call the appropriate method based on the button state
          if (isFavorite) {
            // Call the uploadFavorites method from FavoritesService
            String result = await FavoritesService.uploadFavorites(cosmeticId);

            if (result == "success upload user favorites") {
              print("Favorites uploaded successfully! : $isFavorite");
            } else {
              print("Failed to upload favorites");
            }
          } else {
            // Call the deleteFavorites method from FavoritesService
            String result = await FavoritesService.deleteFavorites(cosmeticId);

            if (result == "success deleted user favorites") {
              print("Favorites deleted successfully! : $isFavorite");
            } else {
              print("Failed to delete favorites");
            }
          }
        } catch (e) {
          print("An error occurred while handling favorites: $e");
        }
      },
      icon: Icon(
        isFavorite ? Icons.favorite : Icons.favorite_border,
        color: isFavorite ? Colors.red : null,
      ),
    );
  }

  Widget _displayBrand() {
    return Padding(
      padding: const EdgeInsets.all(8.0),
      child: Text(
        '브랜드: ${widget.searchResults.brand}',
        style: TextStyle(fontSize: 18),
      ),
    );
  }

  Widget _displayCategory() {
    return Padding(
      padding: const EdgeInsets.all(8.0),
      child: Text(
        '카테고리: ${widget.searchResults.category}',
        style: TextStyle(fontSize: 18),
      ),
    );
  }

  Widget _displayKeywords() {
    return Padding(
      padding: const EdgeInsets.all(8.0),
      child: Text(
        '키워드: ${widget.searchResults.keywords}',
        style: TextStyle(fontSize: 18),
      ),
    );
  }

  Widget _displayRatingStars() {
    return Padding(
      padding: const EdgeInsets.all(8.0),
      child: Expanded(
        child: Row(
          children: [
            Text(
              '별점: ',
              style: TextStyle(fontSize: 18),
            ),
            AbsorbPointer(
              absorbing: true, // Set absorbing to true
              child: RatingBar.builder(
                initialRating: widget.searchResults.averageRating,
                minRating: 1,
                direction: Axis.horizontal,
                allowHalfRating: true,
                itemCount: 5,
                itemSize: 20,
                itemPadding: EdgeInsets.symmetric(horizontal: 1.0),
                itemBuilder: (context, _) => Icon(
                  Icons.star,
                  color: Colors.amber,
                ),
                onRatingUpdate: (rating) {},
              ),
            ),
            Text(
              '(${widget.searchResults.averageRating})',
              style: TextStyle(fontSize: 18),
            ),
          ],
        ),
      ),
    );
  }

  Widget _displayGPTReview(GPTReviewInfo gptReviewInfo, bool isPositive) {
    // bool isPositive = showPositiveReview;

    return Column(
      children: [
        Padding(
          padding: const EdgeInsets.all(8.0),
          child: Row(
            mainAxisAlignment: MainAxisAlignment.center,
            children: [
              Container(
                height: 30,
                child: Theme(
                  data: Theme.of(context).copyWith(
                    toggleButtonsTheme: ToggleButtonsThemeData(
                      selectedColor: Color(0xffd86a04),
                      selectedBorderColor: Color(0xffd86a04),
                    ),
                  ),
                  child: ToggleButtons(
                    children: [
                      Padding(
                        padding:
                            EdgeInsets.symmetric(horizontal: 16, vertical: 0),
                        child: Text(
                          '높은 평정 요약',
                          style: TextStyle(
                            color:
                                isPositive ? Color(0xffd86a04) : Colors.black,
                            fontWeight: isPositive
                                ? FontWeight.bold
                                : FontWeight.normal,
                          ),
                        ),
                      ),
                      Padding(
                        padding:
                            EdgeInsets.symmetric(horizontal: 16, vertical: 0),
                        child: Text(
                          '낮은 평점 요약',
                          style: TextStyle(
                            color:
                                !isPositive ? Color(0xffd86a04) : Colors.black,
                            fontWeight: !isPositive
                                ? FontWeight.bold
                                : FontWeight.normal,
                          ),
                        ),
                      ),
                    ],
                    isSelected: [showPositiveReview, !showPositiveReview],
                    onPressed: (index) {
                      setState(() {
                        showPositiveReview = index == 0;
                      });
                    },
                    fillColor: Colors.white,
                    constraints: BoxConstraints.expand(
                        width: MediaQuery.of(context).size.width / 2 - 46),
                    // color: Colors.grey,
                  ),
                ),
              ),
            ],
          ),
        ),
        Padding(
          padding: const EdgeInsets.all(8.0),
          child: Container(
            decoration: BoxDecoration(
              color: Colors.white,
              borderRadius: BorderRadius.circular(8.0),
            ),
            padding: EdgeInsets.all(8.0),
            child: Text(
              showPositiveReview
                  ? gptReviewInfo.positive
                  : gptReviewInfo.negative,
              style: TextStyle(fontSize: 16),
              textAlign: TextAlign.justify,
            ),
          ),
        ),
      ],
    );
  }

  //GPT리뷰요약 상세내용
  Widget _gptReviewContent() {
    bool isPositive = showPositiveReview;

    return FutureBuilder<Result<GPTReviewInfo>>(
      future: _gptReviewInfo,
      builder: (context, snapshot) {
        if (snapshot.connectionState == ConnectionState.waiting) {
          return SpinKitThreeInOut(
            color: Color(0xffd86a04),
            size: 25.0,
            duration: Duration(seconds: 2),
          );
        } else if (snapshot.hasError) {
          return Text('Error: ${snapshot.error}');
        } else if (!snapshot.hasData || !snapshot.data!.isSuccess) {
          return Column(
            children: [
              Padding(
                padding: const EdgeInsets.all(8.0),
                child: Row(
                  mainAxisAlignment: MainAxisAlignment.center,
                  children: [
                    Container(
                      height: 30,
                      child: Theme(
                        data: Theme.of(context).copyWith(
                          toggleButtonsTheme: ToggleButtonsThemeData(
                            selectedColor: Color(0xffd86a04),
                            selectedBorderColor: Color(0xffd86a04),
                          ),
                        ),
                        child: ToggleButtons(
                          children: [
                            Padding(
                              padding: EdgeInsets.symmetric(
                                  horizontal: 16, vertical: 0),
                              child: Text(
                                '높은 평정 요약',
                                style: TextStyle(
                                  color: isPositive
                                      ? Color(0xffd86a04)
                                      : Colors.black,
                                  fontWeight: isPositive
                                      ? FontWeight.bold
                                      : FontWeight.normal,
                                ),
                              ),
                            ),
                            Padding(
                              padding: EdgeInsets.symmetric(
                                  horizontal: 16, vertical: 0),
                              child: Text(
                                '낮은 평점 요약',
                                style: TextStyle(
                                  color: !isPositive
                                      ? Color(0xffd86a04)
                                      : Colors.black,
                                  fontWeight: !isPositive
                                      ? FontWeight.bold
                                      : FontWeight.normal,
                                ),
                              ),
                            ),
                          ],
                          isSelected: [showPositiveReview, !showPositiveReview],
                          onPressed: (index) {
                            setState(() {
                              showPositiveReview = index == 0;
                            });
                          },
                          fillColor: Colors.white,
                          constraints: BoxConstraints.expand(
                              width:
                                  MediaQuery.of(context).size.width / 2 - 46),
                          // color: Colors.grey,
                        ),
                      ),
                    ),
                  ],
                ),
              ),
              Padding(
                padding:
                    const EdgeInsets.symmetric(horizontal: 10, vertical: 30),
                child: Center(
                  child: Text(
                    '요약된 GPT Review가 없습니다.',
                    textAlign: TextAlign.center,
                  ),
                ),
              ),
            ],
          );
        } else {
          final gptReviewInfo = snapshot.data!.value!;
          return Container(
            width: double.infinity, // Set the width to maximum
            child: _displayGPTReview(gptReviewInfo, isPositive),
          );
        }
      },
    );
  }

  //리뷰 전체보기 버튼
  Widget _watchAllReviewsButton() {
    return Container(
      width: double.infinity,
      child: ElevatedButton(
        onPressed: () {
          // 클릭 시 AllReviewPage로 이동
          Navigator.push(
            context,
            MaterialPageRoute(
                builder: (context) => CosmeticReviewPage(
                      cosmeticId: widget.searchResults.id,
                    )),
          );
        },
        style: ElevatedButton.styleFrom(
          backgroundColor: Colors.white, // Set background color to transparent
          elevation: 0, // Remove the button shadow
        ),
        child: Text(
          '작성된 후기 전체보기  >',
          style: TextStyle(
            color: Colors.black,
            fontSize: 18,
            // fontWeight: FontWeight.bold,
          ),
        ),
      ),
    );
  }

  Widget _gptBox() {
    return Container(
      decoration: BoxDecoration(
        color: Colors.white,
        border: Border.all(color: Colors.grey), // Set border color to grey
        borderRadius: BorderRadius.circular(10), // Adjust the radius as needed
      ),
      padding: const EdgeInsets.all(10),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          const SizedBox(height: 10),
          Padding(
            padding: const EdgeInsets.symmetric(horizontal: 10),
            child: Text(
              "ChatGPT로 최근 후기를 요약했어요",
              style: TextStyle(
                color: Colors.black,
                fontSize: 18,
                fontWeight: FontWeight.normal,
              ),
            ),
          ),
          const SizedBox(height: 10),
          _gptReviewContent(),
          const SizedBox(height: 10),
          _warningBox(),
          const SizedBox(height: 10),
          _divider(),
          Center(
            child: _watchAllReviewsButton(),
          ),
        ],
      ),
    );
  }

  Widget _warningBox() {
    return Padding(
      padding: const EdgeInsets.symmetric(horizontal: 10),
      child: Container(
        decoration: BoxDecoration(
          color: Color(0xffefefef),
          border: Border.all(color: Color(0xffc6c6c6)),
          borderRadius: BorderRadius.circular(5),
        ),
        padding: const EdgeInsets.all(10),
        child: Center(
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              Text(
                "현재 ChatGPT 기술 수준에서는 후기 요약이 정확하지 않거나\n표현이 어색할 수 있습니다.",
                style: TextStyle(
                  color: Colors.black,
                  fontSize: 13,
                  fontWeight: FontWeight.normal,
                ),
              ),
            ],
          ),
        ),
      ),
    );
  }

  Widget _divider() {
    return const Divider(
      height: 20,
      thickness: 1,
      indent: 10,
      endIndent: 10,
      color: Colors.grey,
    );
  }
}

class FavoriteManager {
  static final FavoriteManager _instance = FavoriteManager._internal();

  factory FavoriteManager() {
    return _instance;
  }

  FavoriteManager._internal();

  Future<bool> getFavoriteState(String productId) async {
    SharedPreferences prefs = await SharedPreferences.getInstance();
    return prefs.getBool('isFavorite_$productId') ?? false;
  }

  Future<void> setFavoriteState(String productId, bool value) async {
    SharedPreferences prefs = await SharedPreferences.getInstance();
    prefs.setBool('isFavorite_$productId', value);
  }
}
