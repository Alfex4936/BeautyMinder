import 'package:flutter/cupertino.dart';
import 'package:flutter/material.dart';
import 'package:intl/intl.dart';

import '../../dto/cosmetic_model.dart';

class ExpiryInputDialog extends StatefulWidget {
  final Cosmetic cosmetic;

  ExpiryInputDialog({required this.cosmetic});

  @override
  _ExpiryInputDialogState createState() => _ExpiryInputDialogState();
}

class _ExpiryInputDialogState extends State<ExpiryInputDialog> {
  bool isOpened = false;
  //DateTime expiryDate = DateTime.now().add(Duration(days: 365));
  DateTime? expiryDate;
  DateTime? openedDate;

  String formatDate(DateTime date) {
    return DateFormat('yyyy-MM-dd').format(date);
  }


  Future<void> _selectDate(BuildContext context, {bool isExpiryDate = true}) async {
    final DateTime? picked = await showDatePicker(
      context: context,
      initialDate: (isExpiryDate ? expiryDate : openedDate) ?? DateTime.now(),
      firstDate: DateTime(2000),
      lastDate: DateTime(2101),
    );
    if (picked != null)
      setState(() {
        if (isExpiryDate) {
          expiryDate = picked;
        } else {
          openedDate = picked;
        }
      });
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
          ),
          ListTile(
            title: Text(expiryDate != null ? '유통기한: ${formatDate(expiryDate!)}' : '유통기한 선택'),
            trailing: Icon(Icons.calendar_today),
            onTap: () => _selectDate(context),
          ),
          if (isOpened)
            ListTile(
              title: Text(openedDate != null ? '개봉 날짜: ${formatDate(openedDate!)}' : '개봉 날짜 선택'),
              trailing: Icon(Icons.calendar_today),
              onTap: () => _selectDate(context, isExpiryDate: false),
            ),
        ],
      ),
      actions: [
        TextButton(
          onPressed: () => Navigator.of(context).pop([isOpened, expiryDate, openedDate]),
          child: Text('Submit'),
        ),
      ],
    );
  }
}

