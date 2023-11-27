import 'package:intl/intl.dart';

class CosmeticExpiry {
  final String? id;
  final String productName;
  final String? brandName;
  DateTime expiryDate;
  final bool isExpiryRecognized;
  String? imageUrl;
  final String? cosmeticId;
  bool isOpened;
  DateTime? openedDate;

  CosmeticExpiry({
    this.id,
    required this.productName,
    this.brandName,
    required this.expiryDate,
    this.isExpiryRecognized = false,
    this.imageUrl,
    this.cosmeticId,
    this.isOpened = false,
    this.openedDate,
  });

  factory CosmeticExpiry.fromJson(Map<String, dynamic> json) {
    return CosmeticExpiry(
      id: json['id'] as String,
      productName: json['productName'] as String,
      brandName: json['brandName'] as String?,
      expiryDate: DateTime.parse(json['expiryDate'] as String),
      isExpiryRecognized: (json['isExpiryRecognized'] as bool?) ?? false,
      imageUrl: json['imageUrl'] as String?,
      cosmeticId: json['cosmeticId'] as String?,
      isOpened: (json['opened'] as bool?) ?? false,
      openedDate: json['openedDate'] != null
          ? DateTime.parse(json['openedDate'] as String)
          : null,
    );
  }

  Map<String, dynamic> toJson() {
    var formatter = DateFormat('yyyy-MM-dd');
    return {
      'id': id,
      'productName': productName,
      'brandName': brandName,
      'expiryDate': formatter.format(expiryDate),
      'isExpiryRecognized': isExpiryRecognized,
      'imageUrl': imageUrl,
      'cosmeticId': cosmeticId,
      'opened': isOpened,
      'openedDate': openedDate != null ? formatter.format(openedDate!) : null,
    };
  }
}
