import 'package:beautyminder/pages/baumann/baumann_test_start_page.dart';
import 'package:beautyminder/pages/baumann/watch_result_page.dart';
import 'package:flutter/material.dart';

import '../../dto/baumann_result_model.dart';
import '../../widget/commonAppBar.dart';

class BaumannHistoryPage extends StatelessWidget {
  final List<BaumannResult>? resultData;

  const BaumannHistoryPage({Key? key, required this.resultData})
      : super(key: key);

  @override
  Widget build(BuildContext context) {
    print("This is History Page : $resultData");
    return Scaffold(
      appBar: CommonAppBar(),
      body: Column(
        children: [
          _baumannHistoryUI(),
          _divider(),
          _retestButton(context),
          SizedBox(
            height: 10,
          ),
          Expanded(
            child: _baumannHistoryListView(),
          ),
        ],
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

  Widget _resultButton(
      BuildContext context, BaumannResult result, bool isEven) {
    Color buttonColor = isEven ? Colors.white : Color(0xffffb876);
    Color textColor = isEven ? Colors.black : Colors.white;

    return Padding(
      padding: EdgeInsets.symmetric(horizontal: 10),
      child: Container(
        margin: EdgeInsets.symmetric(vertical: 8),
        child: ElevatedButton(
          onPressed: () {
            Navigator.of(context).push(MaterialPageRoute(
                builder: (context) => WatchResultPage(resultData: result)));
          },
          style: ElevatedButton.styleFrom(
            backgroundColor: buttonColor,
            side: BorderSide(color: Color(0xffffb876)),
            elevation: 0,
          ),
          child: Column(
            mainAxisAlignment: MainAxisAlignment.center,
            children: [
              SizedBox(height: 10),
              Row(
                mainAxisAlignment: MainAxisAlignment.center,
                children: [
                  Text('피부타입: ${result.baumannType}',
                      style: TextStyle(
                          color: textColor,
                          fontSize: 18,
                          fontWeight: FontWeight.bold)),
                  SizedBox(width: 16),
                  Text('테스트 일시: ${result.date}',
                      style: TextStyle(color: textColor, fontSize: 18)),
                ],
              ),
              _baumannResultContent(result, isEven),
            ],
          ),
        ),
      ),
    );
  }

  Widget _baumannResultContent(BaumannResult result, bool isEven) {
    Color cardColor = isEven ? Colors.white : Color(0xffffb876);
    Color textColor = isEven ? Color(0xff6e6e6e) : Colors.white;

    return Card(
      color: cardColor,
      elevation: 0,
      child: Column(
        children: [
          ListTile(
            subtitle: Column(
              children: [
                SizedBox(height: 5),
                Text('색소침착도: ${result.baumannScores['pigmentation']}/57',
                    style: TextStyle(color: textColor)),
                Text('유수분 밸런스: ${result.baumannScores['hydration']}/44',
                    style: TextStyle(color: textColor)),
                Text('탄력: ${result.baumannScores['elasticity']}/85',
                    style: TextStyle(color: textColor)),
                Text('수분 보유력: ${result.baumannScores['moistureRetention']}/65',
                    style: TextStyle(color: textColor)),
                Text('민감도: ${result.baumannScores['sensitivity']}/64',
                    style: TextStyle(color: textColor)),
                SizedBox(height: 10),
              ],
            ),
          ),
        ],
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
