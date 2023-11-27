import 'package:beautyminder/widget/commonAppBar.dart';
import 'package:flutter/material.dart';
import 'package:beautyminder/services/review_service.dart';

import '../../dto/review_response_model.dart'; // Import the ReviewService

class AllReviewPage extends StatefulWidget {
  final String cosmeticId;

  AllReviewPage({Key? key, required this.cosmeticId}) : super(key: key);

  @override
  _AllReviewPageState createState() => _AllReviewPageState();
}

class _AllReviewPageState extends State<AllReviewPage> {
  late Future<List<ReviewResponse>> _reviews;

  @override
  void initState() {
    super.initState();
    _reviews = ReviewService.getReviewsForCosmetic(widget.cosmeticId);
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: CommonAppBar(),
      body: FutureBuilder<List<ReviewResponse>>(
        future: _reviews,
        builder: (context, snapshot) {
          if (snapshot.connectionState == ConnectionState.waiting) {
            return CircularProgressIndicator();
          } else if (snapshot.hasError) {
            return Text('Error: ${snapshot.error}');
          } else if (!snapshot.hasData) {
            return Text('No reviews available for this cosmetic.');
          } else {
            final reviews = snapshot.data!;
            return _buildReviewsList(reviews);
          }
        },
      ),
    );
  }

  Widget _buildReviewsList(List<ReviewResponse> reviews) {
    return ListView.builder(
      itemCount: reviews.length,
      itemBuilder: (context, index) {
        final review = reviews[index];
        return ListTile(
          title: Text(review.content),
          subtitle: Text('Rating: ${review.rating}'),
          // Add more details as needed
        );
      },
    );
  }
}
