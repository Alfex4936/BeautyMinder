import 'package:intl/intl.dart';

class CosmeticExpiry {
  final String? id;
  final String productName;
  final String? brandName;
  DateTime expiryDate;
  final bool expiryRecognized;
  String? imageUrl;
  final String? cosmeticId;
  final bool opened;
  DateTime? openedDate;

  CosmeticExpiry({
    this.id,
    required this.productName,
    this.brandName,
    required this.expiryDate,
    this.expiryRecognized = false,
    this.imageUrl,
    this.cosmeticId,
    required this.opened,
    this.openedDate,
  });

  factory CosmeticExpiry.fromJson(Map<String, dynamic> json) {
    return CosmeticExpiry(
      id: json['id'] as String,
      productName: json['productName'] as String,
      brandName: json['brandName'] as String?,
      expiryDate: DateTime.parse(json['expiryDate'] as String),
      expiryRecognized: (json['expiryRecognized'] as bool),
      imageUrl: json['imageUrl'] as String?,
      cosmeticId: json['cosmeticId'] as String?,
      opened: (json['opened'] as bool),
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
      'isExpiryRecognized': expiryRecognized,
      'imageUrl': imageUrl,
      'cosmeticId': cosmeticId,
      'opened': opened,
      'openedDate': openedDate != null ? formatter.format(openedDate!) : null,
    };
  }
}
