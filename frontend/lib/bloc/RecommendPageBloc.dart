import 'package:beautyminder/State/RecommendState.dart';
import 'package:beautyminder/event/RecommendPageEvent.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:beautyminder/services/Cosmetic_Recommend_Service.dart';

import '../dto/cosmetic_model.dart';

late List<Cosmetic> AllCosmetics;

class RecommendPageBloc extends Bloc<RecommendPageEvent, RecommendState>{



  RecommendPageBloc() : super(const RecommendInitState()){
    on<RecommendPageInitEvent>(_initEvent);
    on<RecommendPageCategoryChangeEvent>(_categoryChangeEvent);
  }

  Future<void> _initEvent(RecommendPageInitEvent event, Emitter<RecommendState> emit) async{
    await Future.delayed(const Duration(seconds: 0),() async {
      print("호");
      emit(RecommendDownloadedState(isError: state.isError));

      //추천 상품 받아오기 전체 추천상
      // 추천 상품 받아오는 로직 구현이 필요
      final result = (await CosmeticSearchService
          .getAllCosmetics());

      AllCosmetics = result.value!;

      //print("RecommendPageBloc cosmetics : ${cosmetics}");

      if (AllCosmetics != null) {
        // 정상적으로 데이터를 받아왔다면
        emit(RecommendLoadedState(
            recCosmetics: AllCosmetics, category: state.category));
      } else {
        emit(RecommendErrorState(recCosmetics: [], isError: true));
      }
    });

  }

  Future<void> _categoryChangeEvent(RecommendPageCategoryChangeEvent event , Emitter<RecommendState> emit) async{
    await Future.delayed(const Duration(seconds: 0), () async {

      print("category change event call");

      if(state is RecommendLoadedState){
        print(" RecommendLoadedState in _categoryChangeEvent");
        // 카테고리별로 추천상품 받아오는 로직이 필요
        print("event.category : ${event.category}");

        //print("RecommendPageBloc e.category:${event.category}");
        if(event.category == null){
          print("event.categorys is all");
          emit(RecommendCategoryChangeState(category: state.category, isError: state.isError, recCosmetics: AllCosmetics));
        }else{
          print("event.categorys is skincare");
          emit(RecommendCategoryChangeState(category: event.category, isError: state.isError, recCosmetics: AllCosmetics));
        }


        //print("this is categoryChageEvent");
        //print("state.recCosmetics in category change : ${state.recCosmetics}");
        //print("RecommendPageBloc category : ${state.category}");

        if(event.category == "전체"){
          emit(RecommendLoadedState(recCosmetics:AllCosmetics, category: state.category, isError: state.isError));
        }else{
          List<Cosmetic>? categorySelect = state.recCosmetics?.where((e) {
            return e.category == event.category;
          }).toList();
          //print("category_select : $categorySelect");
          emit(RecommendLoadedState(recCosmetics: categorySelect, category: state.category, isError: state.isError));
        }

      }else{
        emit(const RecommendErrorState(recCosmetics: [], isError: true));
      }
    });
  }


}