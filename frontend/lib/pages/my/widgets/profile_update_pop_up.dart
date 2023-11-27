import 'dart:async';

import 'package:beautyminder/pages/my/widgets/profile_default_dialog.dart';
import 'package:flutter/material.dart';
import 'package:image_picker/image_picker.dart';

Future<XFile?> profilePopUp({
  required String title,
  required BuildContext context,
  String? subTitle,
}) {
  Completer<XFile?> completer = Completer();
  XFile? image;

  showDialog(
    context: context,
    builder: (context) => ProfileDefaultDialog(
      onImageChanged: (changedImage) {
        image = changedImage; // Update the image in profilePopUp
      },
      onBarrierTap: () {
        completer.complete(image);
        Navigator.of(context).pop();
      },
      title: title,
      buttons: [
        ProfileDefaultDialogButton(
          onTap: () {
            completer.complete(image);

            Navigator.of(context).pop();
          },
          text: "닫기",
          backgroundColor: const Color(0xFFF5F5F5),
          textColor: Colors.black,
        ),
        ProfileDefaultDialogButton(
          onTap: () {
            completer.complete(image);

            Navigator.of(context).pop();
          },
          text: "확인",
          backgroundColor: Colors.orange,
          textColor: Colors.white,
        ),
      ],
    ),
  );
  return completer.future;
}
