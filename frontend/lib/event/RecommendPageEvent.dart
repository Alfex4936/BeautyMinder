import 'package:equatable/equatable.dart';

abstract class RecommendPageEvent extends Equatable {
  final String? category;

  const RecommendPageEvent({this.category});
// event는 두개
// Init event : 맨첨은 API를 호출하여 추천 상품을 불러오는 이벤트
// CategoryChange event : 카테고리를 바꾸면 각 카테고리에 해당하는 추천 상품을 불러오는 이벤트
}

class RecommendPageInitEvent extends RecommendPageEvent {
  const RecommendPageInitEvent();

  @override
  List<Object?> get props => [];
}

class RecommendPageCategoryChangeEvent extends RecommendPageEvent {
  const RecommendPageCategoryChangeEvent({super.category});

  @override
  List<Object?> get props => [category];
}
