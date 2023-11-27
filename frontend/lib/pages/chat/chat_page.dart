import 'dart:async';

import 'package:beautyminder/widget/commonAppBar.dart';
import 'package:flutter/material.dart';
import 'package:flutter_inappwebview/flutter_inappwebview.dart';

import '../../config.dart';

class ChatPage extends StatefulWidget {
  const ChatPage({Key? key}) : super(key: key);

  @override
  State<ChatPage> createState() => _ChatPageState();
}

class _ChatPageState extends State<ChatPage> {
  late InAppWebViewController webViewController;

  String api = "http://${Config.apiURL}${Config.chatAPI}";

  @override
  void initState() {
    super.initState();
  }

  @override
  Widget build(BuildContext context) {
    CookieManager cookieManager = CookieManager.instance();

    return Scaffold(
        appBar: CommonAppBar(),
        body: WillPopScope(
            onWillPop: () => _goBack(context),
            child: InAppWebView(
                onWebViewCreated: (controller) {
                  webViewController = controller;
                },
                initialUrlRequest: URLRequest(url: Uri.parse(api), headers: {
                  "Authorization": "Bearer ${Config.acccessToken}"
                }),
                shouldOverrideUrlLoading: (controller, navigationAction) async {
                  var request = navigationAction.request;
                  var url = request.url;
                  var isUrlMatching = url != null &&
                      url.host.contains('ec2') &&
                      url.path.contains('/chat');

                  // set the cookie
                  await cookieManager.setCookie(
                    url: url!,
                    name: "Access-Token",
                    value: Config.acccessToken,
                  );

                  if (isUrlMatching) {
                    request.headers = {
                      "Authorization": "Bearer ${Config.acccessToken}"
                    };
                    controller.loadUrl(urlRequest: request);
                    return NavigationActionPolicy.CANCEL;
                  }

                  // always allow all the other requests
                  return NavigationActionPolicy.ALLOW;
                },
                initialOptions: InAppWebViewGroupOptions(
                  crossPlatform: InAppWebViewOptions(
                    useShouldOverrideUrlLoading: true, // URL 로딩 제어
                    javaScriptEnabled: true, // 자바스크립트 실행 여부
                    javaScriptCanOpenWindowsAutomatically: true, // 팝업 여부
                  ),
                  // 안드로이드 옵션
                  android: AndroidInAppWebViewOptions(
                    useHybridComposition: true, // 하이브리드 사용을 위한 안드로이드 웹뷰 최적화
                  ),
                ))));
  }

  Future<bool> _goBack(BuildContext context) async {
    if (await webViewController.canGoBack()) {
      webViewController.evaluateJavascript(source: "quit()");
      webViewController.goBack();
      return Future.value(false);
    } else {
      return Future.value(true);
    }
  }
}
