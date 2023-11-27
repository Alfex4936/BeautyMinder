import 'package:file_picker/file_picker.dart';
import 'package:flutter/material.dart';
import 'package:intl/intl.dart';

import '../../dto/cosmetic_model.dart';
import '../../dto/vision_response_dto.dart';
import '../../services/ocr_service.dart';

class ExpiryInputDialog extends StatefulWidget {
  final Cosmetic cosmetic;

  ExpiryInputDialog({required this.cosmetic});

  @override
  _ExpiryInputDialogState createState() => _ExpiryInputDialogState();
}

class _ExpiryInputDialogState extends State<ExpiryInputDialog> {
  bool isOpened = false;

  //DateTime expiryDate = DateTime.now().add(Duration(days: 365));
  DateTime? expiryDate = DateTime.now();
  DateTime? openedDate = DateTime.now(); // 개봉 날짜 기본값

  String formatDate(DateTime date) {
    return DateFormat('yyyy-MM-dd').format(date);
  }

  Future<void> _selectDate(BuildContext context,
      {bool isExpiryDate = true}) async {
    final DateTime? picked = await showDatePicker(
      context: context,
      initialDate: (isExpiryDate ? expiryDate : openedDate) ?? DateTime.now(),
      firstDate: DateTime(2000),
      lastDate: DateTime(2101),
      builder: (BuildContext context, Widget? child) {
        return Theme(
          data: ThemeData.light().copyWith(
            colorScheme: const ColorScheme.light(
              primary: Colors.orange, // 달력의 주요 색상을 오렌지로 설정
            ),
          ),
          child: child!,
        );
      },
    );
    if (picked != null) {
      setState(() {
        if (isExpiryDate) {
          expiryDate = picked;
        } else {
          openedDate = picked;
        }
      });
    }
  }

  // OCR 페이지로 이동하고 결과를 받아오는 함수
  Future<void> _navigateAndProcessOCR() async {
    final PlatformFile? pickedFile = await FilePicker.platform.pickFiles(
      type: FileType.custom,
      allowedExtensions: ['png', 'jpg', 'jpeg'],
    ).then((result) => result?.files.first);

    if (pickedFile != null) {
      // OCR 처리를 요청하고 결과를 받습니다.
      try {
        final response = await OCRService.selectAndUploadImage(pickedFile);
        if (response != null) {
          final VisionResponseDTO result = VisionResponseDTO.fromJson(response);
          final expiryDateFromOCR = DateFormat('yyyy-MM-dd').parse(result.data);

          // 받아온 유통기한으로 상태 업데이트
          setState(() {
            expiryDate = expiryDateFromOCR;
          });
        }
      } catch (e) {
        // 오류 처리
        _showErrorDialog(e.toString());
      }
    } else {
      _showErrorDialog("No image selected for OCR.");
    }
  }

  // 에러 메시지를 보여주는 함수
  void _showErrorDialog(String message) {
    showDialog(
      context: context,
      builder: (context) => AlertDialog(
        title: Text('Error'),
        content: Text(message),
        actions: [
          TextButton(
            child: Text('OK'),
            onPressed: () => Navigator.of(context).pop(),
          ),
        ],
      ),
    );
  }

  // 유통기한 선택 방법을 선택하는 다이얼로그를 표시하는 함수
  void _showExpiryDateChoiceDialog() {
    showDialog(
      context: context,
      builder: (context) => AlertDialog(
        title: Text('유통기한 입력 방법 선택'),
        content: Column(
          mainAxisSize: MainAxisSize.min,
          children: <Widget>[
            ListTile(
              leading: Icon(Icons.edit),
              title: Text('직접 입력'),
              onTap: () {
                Navigator.of(context).pop();
                _selectDate(context);
              },
            ),
            ListTile(
              leading: Icon(Icons.camera_alt),
              title: Text('OCR 입력'),
              onTap: () {
                Navigator.of(context).pop();
                _navigateAndProcessOCR();
              },
            ),
          ],
        ),
      ),
    );
  }

  @override
  Widget build(BuildContext context) {
    return AlertDialog(
      title: Text('${widget.cosmetic.name}의 유통기한 정보를 입력해주세요'),
      content: Column(
        mainAxisSize: MainAxisSize.min,
        children: [
          SwitchListTile(
            title: Text('개봉여부'),
            value: isOpened,
            onChanged: (bool value) {
              setState(() {
                isOpened = value;
                if (!isOpened) {
                  openedDate = null;
                }
              });
            },
            activeColor: Colors.orange,
            activeTrackColor: Colors.orangeAccent,
          ),
          ListTile(
            title: Text(expiryDate != null
                ? '유통기한: ${formatDate(expiryDate!)}'
                : '유통기한 선택'),
            trailing: Icon(Icons.calendar_today),
            onTap: () =>
                _showExpiryDateChoiceDialog(), // 유통기한 선택 방법을 선택하는 다이얼로그를 표시하는 함수 호출
          ),
          if (isOpened)
            ListTile(
              title: Text(openedDate != null
                  ? '개봉 날짜: ${formatDate(openedDate!)}'
                  : '개봉 날짜 선택'),
              trailing: Icon(Icons.calendar_today),
              onTap: () => _selectDate(context, isExpiryDate: false),
            ),
        ],
      ),
      actions: [
        TextButton(
          onPressed: () =>
              Navigator.of(context).pop([isOpened, expiryDate, openedDate]),
          child: Text('Submit'),
          style: TextButton.styleFrom(foregroundColor: Colors.orange),
        ),
      ],
    );
  }
}
