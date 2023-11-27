import 'dart:developer';

import 'package:beautyminder/widget/commonAppBar.dart';
import 'package:file_picker/file_picker.dart';
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:flutter_spinkit/flutter_spinkit.dart';

import '/dto/user_model.dart';
import '/services/shared_service.dart';
import '../../dto/review_request_model.dart';
import '../../dto/review_response_model.dart';
import '../../services/review_service.dart';

class CosmeticReviewPage extends StatefulWidget {
  final String cosmeticId;

  CosmeticReviewPage({Key? key, required this.cosmeticId}) : super(key: key);

  @override
  _CosmeticReviewPageState createState() => _CosmeticReviewPageState();
}

class _CosmeticReviewPageState extends State<CosmeticReviewPage> {
  final TextEditingController _searchController = TextEditingController();

  //List<Cosmetic> _searchResults = [];
  List<ReviewResponse> _cosmeticReviews = []; // ReviewResponse 리스트로 변경
  bool _isLoading = false;

  //String _selectedCosmeticId = '';
  List<PlatformFile>? _imageFiles;

  @override
  void initState() {
    super.initState();
    _fetchReviewsForCosmetic(widget.cosmeticId);
  }

  /***@override
      void dispose() {
      _searchController.dispose();
      super.dispose();
      }

      void _searchCosmetics() async {
      setState(() => _isLoading = true);
      try {
      var results = await SearchService.searchCosmeticsByName(_searchController.text);
      setState(() {
      _searchResults = results;
      _isLoading = false;
      });
      } catch (e) {
      setState(() => _isLoading = false);
      _showSnackBar('Search failed: $e');
      }
      }
   ***/
  void _fetchReviewsForCosmetic(String cosmeticId) async {
    setState(() => _isLoading = true);
    try {
      var reviews = await ReviewService.getReviewsForCosmetic(cosmeticId);
      setState(() {
        _cosmeticReviews = reviews;
        //_selectedCosmeticId = cosmeticId;
        _isLoading = false;
      });
    } catch (e) {
      setState(() => _isLoading = false);
      _showSnackBar('Failed to load reviews: $e');
    }
  }

  void getImages() async {
    try {
      _imageFiles = (await FilePicker.platform.pickFiles(
        type: FileType.custom,
        onFileLoading: (FilePickerStatus status) => print(status),
        allowMultiple: false,
        allowedExtensions: ['png', 'jpg', 'jpeg', 'heic'],
      ))
          ?.files;
    } on PlatformException catch (e) {
      log('Unsupported operation : ' + e.toString());
    } catch (e) {
      log(e.toString());
    }
  }

  void _addOrEditReview(ReviewResponse? review) async {
    User? user = await SharedService.getUser();
    if (user != null) {
      String userId = user.id;
      _showReviewDialog(reviewToUpdate: review, userId: userId);
    } else {
      _showSnackBar('You need to be logged in to add a review.');
    }
  }

  void _showReviewDialog(
      {ReviewResponse? reviewToUpdate, required String userId}) async {
    final _contentController = TextEditingController();
    int _localRating = reviewToUpdate?.rating ?? 1; // 로컬 변수로 변경
    if (reviewToUpdate != null) {
      _contentController.text = reviewToUpdate.content;
    }

    showDialog(
      context: context,
      builder: (context) {
        return StatefulBuilder(
          // StatefulBuilder 추가
          builder: (BuildContext context, StateSetter setDialogState) {
            return AlertDialog(
              title: Text(
                  reviewToUpdate == null ? 'Write a Review' : 'Edit Review'),
              content: SingleChildScrollView(
                child: Column(
                  mainAxisSize: MainAxisSize.min,
                  children: [
                    TextField(
                      controller: _contentController,
                      decoration: InputDecoration(
                        hintText: 'Enter your review here',
                      ),
                      maxLines: 3,
                    ),
                    SizedBox(height: 20),
                    DropdownButton<int>(
                      value: _localRating,
                      items: List.generate(
                        5,
                        (index) => DropdownMenuItem(
                          value: index + 1,
                          child: Text('${index + 1} Stars'),
                        ),
                      ),
                      onChanged: (value) {
                        if (value != null) {
                          setDialogState(() {
                            _localRating = value;
                          });
                        }
                      },
                    ),
                    ElevatedButton(
                      onPressed: getImages,
                      child: Text('Select Images'),
                    ),
                  ],
                ),
              ),
              actions: [
                TextButton(
                  child: Text('Cancel'),
                  onPressed: () => Navigator.of(context).pop(),
                ),
                TextButton(
                  child: Text('Submit'),
                  onPressed: () async {
                    final String content = _contentController.text;
                    if (content.isNotEmpty && widget.cosmeticId.isNotEmpty) {
                      ReviewRequest newReviewRequest = ReviewRequest(
                        content: content,
                        rating: _localRating, // 여기에서 _localRating 사용
                        cosmeticId: widget.cosmeticId,
                      );

                      try {
                        ReviewResponse responseReview;
                        if (reviewToUpdate == null) {
                          responseReview = await ReviewService.addReview(
                              newReviewRequest, _imageFiles!);
                        } else {
                          // 기존 리뷰 수정 로직
                          responseReview = await ReviewService.updateReview(
                              reviewToUpdate.id,
                              newReviewRequest,
                              _imageFiles!);
                          // UI 업데이트
                          int index = _cosmeticReviews.indexWhere(
                              (review) => review.id == responseReview.id);
                          if (index != -1) {
                            _cosmeticReviews[index] = responseReview;
                          }
                        }

                        setState(() {
                          if (reviewToUpdate == null) {
                            _cosmeticReviews.add(responseReview);
                          } else {
                            int index = _cosmeticReviews.indexWhere(
                                (review) => review.id == responseReview.id);
                            if (index != -1) {
                              _cosmeticReviews[index] = responseReview;
                            }
                          }
                          //_searchResults.clear();
                          //_searchController.clear();
                        });
                        Navigator.of(context).pop();
                        _showSnackBar(reviewToUpdate == null
                            ? 'Review added successfully'
                            : 'Review updated successfully');
                      } catch (e) {
                        Navigator.of(context).pop();
                        _showSnackBar('Failed to add/update review: $e');
                      }
                    } else {
                      _showSnackBar('Review content cannot be empty');
                    }
                  },
                ),
              ],
            );
          },
        );
      },
    );
  }

  void _showSnackBar(String message) {
    ScaffoldMessenger.of(context).showSnackBar(
      SnackBar(content: Text(message)),
    );
  }

  Widget _buildReviewList() {
    return Expanded(
      child: ListView.separated(
        itemCount: _cosmeticReviews.length,
        separatorBuilder: (context, index) =>
            Divider(height: 1, color: Colors.grey),
        itemBuilder: (context, index) {
          var review = _cosmeticReviews[index];
          return Card(
            elevation: 2,
            margin: EdgeInsets.all(8),
            shape:
                RoundedRectangleBorder(borderRadius: BorderRadius.circular(10)),
            child: ListTile(
              title: Row(
                children: [
                  ...List.generate(5, (starIndex) {
                    return Icon(
                      starIndex < review.rating
                          ? Icons.star
                          : Icons.star_border,
                      color: Colors.amber,
                      size: 20,
                    );
                  }),
                  SizedBox(width: 8),
                  Text(
                    '${review.rating} Stars',
                    style: TextStyle(color: Colors.grey),
                  ),
                ],
              ),
              subtitle: Padding(
                padding: const EdgeInsets.only(top: 8.0),
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    if (review.isFiltered)
                      Text(
                        '누가 욕쓰라고 했냐?', // 필터링된 메시지 표시
                        style: TextStyle(fontSize: 16, color: Colors.red),
                      )
                    else
                      Text(
                        review.content, // 리뷰 내용 표시
                        style: TextStyle(fontSize: 16),
                      ),
                    SizedBox(height: 10),
                    if (review.nlpAnalysis.isNotEmpty)
                      Text('NLP Analysis: ${review.nlpAnalysis}')
                    // NLP 분석 결과 표시
                  ],
                ),
              ),
              /***trailing: Row(
                  mainAxisSize: MainAxisSize.min,
                  children: [
                  TextButton(
                  child: Text('Edit', style: TextStyle(color: Colors.blue)),
                  onPressed: () {
                  _addOrEditReview(review);
                  },
                  ),
                  TextButton(
                  child: Text('Delete', style: TextStyle(color: Colors.red)),
                  onPressed: () async {
                  await _deleteReview(review.id);
                  },
                  ),
                  ],
                  )***/
            ),
          );
        },
      ),
    );
  }

  Future<void> _deleteReview(String reviewId) async {
    setState(() => _isLoading = true);
    try {
      // ReviewService를 사용하여 서버에 삭제 요청
      await ReviewService.deleteReview(reviewId);
      // UI의 리뷰 목록에서 해당 리뷰를 제거
      setState(() {
        _cosmeticReviews.removeWhere((review) => review.id == reviewId);
        _isLoading = false;
      });
      _showSnackBar('Review deleted successfully');
    } catch (e) {
      setState(() => _isLoading = false);
      _showSnackBar('Failed to delete review: $e');
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: CommonAppBar(),
      body: Column(
        children: [
          if (_isLoading)
            Expanded(
              child: Center(
                child: SpinKitThreeInOut(
                  color: Color(0xffd86a04),
                  size: 50.0,
                  duration: Duration(seconds: 2),
                ),
              ),
            )
          else
            Expanded(
              child: _buildReviewList(),
            ),
        ],
      ),
      floatingActionButton: FloatingActionButton(
        onPressed: () {
          if (widget.cosmeticId.isNotEmpty) {
            _addOrEditReview(null);
          } else {
            _showSnackBar('Please select a cosmetic to review.');
          }
        },
        child: Icon(Icons.add),
        backgroundColor: Color(0xffd86a04),
        // elevation: 0, // 그림자 크기를 0으로 설정
      ),
    );
  }
}
