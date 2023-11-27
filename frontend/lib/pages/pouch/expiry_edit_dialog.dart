import 'package:flutter/material.dart';
import 'package:intl/intl.dart';

import '../../dto/cosmetic_expiry_model.dart';

class ExpiryEditDialog extends StatefulWidget {
  final CosmeticExpiry expiry;

  ExpiryEditDialog({required this.expiry});

  @override
  _ExpiryEditDialogState createState() => _ExpiryEditDialogState();
}

class _ExpiryEditDialogState extends State<ExpiryEditDialog> {
  late bool isOpened;
  late DateTime expiryDate;
  DateTime? openedDate;

  String formatDate(DateTime date) {
    return DateFormat('yyyy-MM-dd').format(date);
  }

  @override
  void initState() {
    super.initState();
    isOpened = widget.expiry.isOpened;
    expiryDate = widget.expiry.expiryDate;
    openedDate = widget.expiry.openedDate;
  }

  Future<void> _selectDate(BuildContext context,
      {bool isExpiryDate = true}) async {
    final DateTime? picked = await showDatePicker(
      context: context,
      initialDate: isExpiryDate ? expiryDate : openedDate ?? DateTime.now(),
      firstDate: DateTime(2010),
      lastDate: DateTime(2040),
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

  @override
  Widget build(BuildContext context) {
    return AlertDialog(
      title: Text('${widget.expiry.productName} 정보 수정'),
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
            title: Text('유통기한: ${formatDate(expiryDate)}'),
            trailing: Icon(Icons.calendar_today),
            onTap: () => _selectDate(context),
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
          onPressed: () {
            // 새로운 CosmeticExpiry 객체 생성 및 현재 상태로 업데이트
            CosmeticExpiry updatedExpiry = CosmeticExpiry(
              id: widget.expiry.id,
              productName: widget.expiry.productName,
              brandName: widget.expiry.brandName,
              expiryDate: expiryDate,
              // 수정된 expiryDate
              isExpiryRecognized: widget.expiry.isExpiryRecognized,
              imageUrl: widget.expiry.imageUrl,
              cosmeticId: widget.expiry.cosmeticId,
              isOpened: isOpened,
              openedDate: openedDate, // 수정된 openedDate
            );
            Navigator.of(context).pop(updatedExpiry);
          },
          child: Text('Submit'),
        ),
      ],
    );
  }
}
