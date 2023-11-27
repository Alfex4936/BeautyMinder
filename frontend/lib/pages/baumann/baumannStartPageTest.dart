import 'dart:html';

import 'package:beautyminder/pages/baumann/baumann_test_page.dart';
import 'package:beautyminder/pages/home/home_page.dart';
import 'package:beautyminder/services/baumann_service.dart';
import 'package:flutter/material.dart';
import 'package:fluttertoast/fluttertoast.dart';

import '../../dto/baumann_model.dart';

class BaumannStartTestPage extends StatefulWidget {
  BaumannStartTestPage({Key? key, this.title}) : super(key: key);

  final String? title;

  @override
  _BaumannStartTestPageState createState() => _BaumannStartTestPageState();
}



class _BaumannStartTestPageState extends State<BaumannStartTestPage> {

  bool isApiCallProcess = false;
  GlobalKey<FormState> globalFormKey = GlobalKey<FormState>();

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: Color(0xffffb876),
      body:_baumannStartUI(),
    );
  }



  //바우만 시작페이지 UI
  Widget _baumannStartUI() {
    return Container(
      padding: EdgeInsets.symmetric(horizontal: 20),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.center,
        mainAxisAlignment: MainAxisAlignment.center,
        children: <Widget>[
          _title(),
          SizedBox(height: 50,),
          _baumannStartContent(),
          SizedBox(height: 50,),
          _testStartButton(),
          SizedBox(height: 20,),
          _testLaterButton(),
          SizedBox(
            height: 20,
          ),
          // _label()
        ],
      ),
    );
  }



  //타이틀 UI
  Widget _title() {
    return Column(
      children: [
        Text(
          "바우만 피부 테스트",
          textAlign: TextAlign.center,
          style: TextStyle(
            fontFamily: 'Oswald',
            fontSize: 30,
            fontWeight: FontWeight.w700,
            color: Colors.white,
          ),
        ),
      ],
    );
  }



  //안내사항 UI
  Widget _baumannStartContent() {
    return Column(
      children: [
        Text(
          "정확한 피부 진단을 바탕으로 한 제품 추천을 위해 테스트를 진행합니다.",
          textAlign: TextAlign.center,
          style: TextStyle(
            fontFamily: 'Oswald',
            fontSize: 20,
            fontWeight: FontWeight.w700,
            color: Colors.white,
          ),

        ),
        Text(
          "* 소요시간은 10-15분 입니다. *",
          textAlign: TextAlign.center,
          style: TextStyle(
            fontFamily: 'Oswald',
            fontSize: 20,
            fontWeight: FontWeight.w700,
            color: Colors.white,
          ),
        ),
        // 다른 내용을 추가하려면 여기에 추가하십시오.
      ],
    );
  }



  //테스트 시작 버튼 UI
  Widget _testStartButton() {
    double screenWidth = MediaQuery.of(context).size.width;
    return InkWell(
      // onTap: () {
      //   Navigator.push(
      //       context, MaterialPageRoute(builder: (context) => BaumannTestPage(surveys: [],)));
      // },
      child: Container(
        width: screenWidth,
        padding: EdgeInsets.symmetric(vertical: 13),
        alignment: Alignment.center,
        decoration: BoxDecoration(
            borderRadius: BorderRadius.all(Radius.circular(5)),
            boxShadow: <BoxShadow>[
              BoxShadow(
                  color: Color(0xffffb876).withAlpha(100),
                  offset: Offset(2, 4),
                  blurRadius: 8,
                  spreadRadius: 2)
            ],
            color: Colors.white),
        child: Text(
          '테스트 시작하기',
          style: TextStyle(fontSize: 20, color: Color(0xffffb876)),
        ),
      ),
      onTap: () async {
        if(validateAndSave()) {
          setState(() {
            isApiCallProcess = true;
          });
          try {
            //바우만 설문 API 호출
            // final model = Baumann();
            final result = await BaumannService.getAllSurveys();

            if (result.value == true) {
              Navigator.pushNamedAndRemoveUntil(
                context, '/baumann/survey', (route) => false
              );
            }
            else {
              Fluttertoast.showToast(
                msg: result.error ?? "설문지 불러오기에 실패하였습니다.",
                toastLength: Toast.LENGTH_SHORT,
                gravity: ToastGravity.BOTTOM,
              );
            }
          }
          finally {
            setState(() {
              isApiCallProcess = false;
            });
          }
        }
      },
    );

  }



  //건너뛰기 버튼 UI
  Widget _testLaterButton() {
    return InkWell(
      onTap: () {
        showDialog(
          context: context,
          builder: (BuildContext context) {
            return AlertDialog(
              title: Text('알림'),
              content: Text('테스트를 보지 않으시면 정확한 추천 결과를 얻기 어렵습니다. 추후 마이페이지에서 테스트를 할 수 있습니다.'),
              actions: <Widget>[
                TextButton(
                  child: Text('확인'),
                  onPressed: () {
                    Navigator.pop(context); // 팝업 닫기
                    Navigator.push(context, MaterialPageRoute(builder: (context) => HomePage()));
                  },
                ),
                TextButton(
                  child: Text('취소'),
                  onPressed: () {
                    Navigator.pop(context); // 팝업 닫기
                  },
                ),
              ],
            );
          },
        );
      },
      child: Container(
        width: MediaQuery.of(context).size.width,
        padding: EdgeInsets.symmetric(vertical: 13),
        alignment: Alignment.center,
        decoration: BoxDecoration(
          borderRadius: BorderRadius.all(Radius.circular(5)),
          border: Border.all(color: Colors.white, width: 2),
        ),
        child: Text(
          '건너뛰기',
          style: TextStyle(fontSize: 20, color: Colors.white),
        ),
      ),
    );
  }

  // 입력 유효성 검사
  bool validateAndSave() {
    final form = globalFormKey.currentState;
    if (form!.validate()) {
      form.save();
      return true;
    }
    return false;
  }

}