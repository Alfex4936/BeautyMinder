import 'package:beautyminder/pages/baumann/baumann_test_start_page.dart';
import 'package:beautyminder/pages/baumann/watch_result_page.dart';
import 'package:beautyminder/services/baumann_service.dart';
import 'package:flutter/material.dart';

import '../../dto/baumann_result_model.dart';
import '../../services/api_service.dart';
import '../../widget/commonAppBar.dart';
import '../home/home_page.dart';

class BaumannHistoryPage extends StatelessWidget {
  final List<BaumannResult>? resultData;

  const BaumannHistoryPage({Key? key, required this.resultData})
      : super(key: key);

  @override
  Widget build(BuildContext context) {
    print("This is History Page : $resultData");
    return Scaffold(
      appBar: CommonAppBar(automaticallyImplyLeading: false,),
      body: Column(
        children: [
          _baumannHistoryUI(),
          _divider(),
          Stack(
            children: [
              Positioned.fill(
                child: Align(
                  alignment: Alignment.bottomLeft,
                  child: AnimatedTrainText(),
                ),
              ),
              Row(
                children: [
                  Spacer(),
                  _retestButton(context),
                ],
              ),
            ],
          ),
          SizedBox(
            height: 10,
          ),
          Expanded(
            child: _baumannHistoryListView(),
          ),
          SizedBox(
            height: 100,
          ),
        ],
      ),
      bottomNavigationBar: Padding(
        padding: EdgeInsets.symmetric(vertical: 50, horizontal: 20),
        child: Container(
          width: double.infinity,
          child: ElevatedButton(
            onPressed: () async {
              final userProfileResult = await APIService.getUserProfile();
              // 버튼을 클릭했을 때 홈페이지로 이동하는 함수 호출
              Navigator.push(
                context,
                MaterialPageRoute(
                    builder: (context) => HomePage(
                      user: userProfileResult.value,
                    )),
              );
            },
            style: ElevatedButton.styleFrom(
              backgroundColor: const Color(0xffe58731),
              elevation: 0,
              shape: RoundedRectangleBorder(
                borderRadius: BorderRadius.circular(5.0), // Adjust the radius as needed
              ),
            ),
            child: Text(
              '홈으로 가기',
              style: TextStyle(
                color: Colors.white,
                fontSize: 18,
              ),
            ),
          ),
        ),
      ),
    );
  }

  Widget _baumannHistoryUI() {
    return Align(
      alignment: Alignment.topLeft,
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          SizedBox(
            height: 30,
          ),
          Padding(
            padding: const EdgeInsets.only(left: 20),
            child: Text(
              "바우만 피부 타입 결과",
              style: TextStyle(
                color: Color(0xFF868383),
                fontSize: 15,
              ),
            ),
          ),
        ],
      ),
    );
  }

  Widget _baumannHistoryListView() {
    return ListView.builder(
      itemCount: resultData?.length ?? 0,
      itemBuilder: (context, index) {
        final result = resultData![index];
        final isEven = index.isEven;

        return Column(
          children: [
            SizedBox(height: 5),
            _resultButton(context, result, isEven),
          ],
        );
      },
    );
  }

  Widget _resultButton(BuildContext context, BaumannResult result, bool isEven) {
    Color buttonColor = isEven ? Colors.white : Color(0xffffca97);
    Color textColor = isEven ? Colors.black : Colors.white;
    print("\n\nhello ::::: ${result}:::::\n\n");

    return Padding(
      padding: EdgeInsets.symmetric(horizontal: 20),
      child: Dismissible(
        key: UniqueKey(),
        direction: DismissDirection.endToStart, // Set direction to right-to-left
        background: Container(
          color: Colors.red,
          padding: EdgeInsets.symmetric(horizontal: 20),
          alignment: AlignmentDirectional.centerEnd,
          child: Icon(
            Icons.delete,
            color: Colors.white,
          ),
        ),
        onDismissed: (direction) async {
          // Implement your delete logic here
          print("HelloHelloHello");

          print(":!:!: : ${result.id} :!:!:");
          final deletionResult = await BaumannService.deleteBaumannHistory(result.id);

          if (deletionResult == "Success to Delete") {
            resultData?.remove(result);

            ScaffoldMessenger.of(context).showSnackBar(
              SnackBar(
                content: Text("삭제되었습니다."),
              ),
            );
          } else {
            ScaffoldMessenger.of(context).showSnackBar(
              SnackBar(
                content: Text("삭제에 실패했습니다."),
              ),
            );
          }
        },

        confirmDismiss: (direction) async {

          return await showDialog(
            context: context,
            builder: (BuildContext context) {
              return AlertDialog(
                title: Text("정말로 삭제하시겠습니까?"),
                actions: [
                  TextButton(
                    onPressed: () {
                      Navigator.of(context).pop(false);
                    },
                    child: Text("취소"),
                  ),
                  TextButton(
                    onPressed: () {
                      Navigator.of(context).pop(true);
                    },
                    child: Text("삭제"),
                  ),
                ],
              );
            },
          );
        },
        child: Container(
          height: 100,
          margin: EdgeInsets.symmetric(vertical: 5),
          child: ElevatedButton(
            onPressed: () {
              Navigator.of(context).push(MaterialPageRoute(
                  builder: (context) => WatchResultPage(resultData: result)));
            },
            style: ElevatedButton.styleFrom(
              backgroundColor: buttonColor,
              side: BorderSide(color: Color(0xffffca97)),
              elevation: 0,
              shape: RoundedRectangleBorder(
                borderRadius: BorderRadius.circular(5.0),
              ),
            ),
            child: Column(
              mainAxisAlignment: MainAxisAlignment.center,
              children: [
                Row(
                  mainAxisAlignment: MainAxisAlignment.center,
                  children: [
                    Text('피부타입: ${result.baumannType}',
                        style: TextStyle(
                            color: textColor,
                            fontSize: 18,
                            fontWeight: FontWeight.bold)),
                    SizedBox(width: 16),
                    Text('일시: ${result.date}',
                        style: TextStyle(color: textColor, fontSize: 12)),
                  ],
                ),
              ],
            ),
          ),
        ),
      ),
    );
  }



  Widget _retestButton(BuildContext context) {
    return Padding(
      padding: EdgeInsets.symmetric(horizontal: 20),
      child: Align(
        alignment: Alignment.topRight,
        child: SizedBox(
          height: 30,
          child: ElevatedButton(
            onPressed: () {
              Navigator.of(context).push(MaterialPageRoute(
                builder: (context) => BaumannStartPage(),
              ));
            },
            style: ElevatedButton.styleFrom(
              backgroundColor: Color(0xffefefef), // Background color
              elevation: 0, // color
              shape: RoundedRectangleBorder(
                borderRadius: BorderRadius.circular(10.0), // Rounded corners
                side: BorderSide(color: Colors.blueGrey),
              ),
            ),
            child: Padding(
              padding: EdgeInsets.all(0.0),
              child: Text(
                '다시 테스트하기',
                style: TextStyle(
                  color: Colors.blueGrey, // Text color
                ),
              ),
            ),
          ),
        ),
      ),
    );
  }

  Widget _divider() {
    return const Divider(
      height: 20,
      thickness: 1,
      indent: 20,
      endIndent: 20,
      color: Colors.grey,
    );
  }
}





//글씨 애니메이션
class AnimatedTrainText extends StatefulWidget {
  @override
  _AnimatedTrainTextState createState() => _AnimatedTrainTextState();
}

class _AnimatedTrainTextState extends State<AnimatedTrainText>
    with SingleTickerProviderStateMixin {
  late AnimationController _animationController;
  late Animation<Offset> _animation;

  @override
  void initState() {
    super.initState();

    _animationController = AnimationController(
      duration: Duration(seconds: 10), // Adjust the duration as needed
      vsync: this,
    );

    _animation = Tween<Offset>(
      begin: Offset(1, 0),
      end: Offset(-1, 0),
    ).animate(
      CurvedAnimation(
        parent: _animationController,
        curve: Curves.linear, // Adjust the curve for a linear motion
      ),
    );

    _animationController.repeat();
  }

  @override
  Widget build(BuildContext context) {
    return SlideTransition(
      position: _animation,
      child: Text(
        "* 결과 삭제를 원하실 경우 좌측으로 슬라이드 해주세요",
        style: TextStyle(fontSize: 16),
      ),
    );
  }

  @override
  void dispose() {
    _animationController.dispose();
    super.dispose();
  }
}
