class CosmeticExpiry {
  final String? id;
  final String productName;
  final String? brandName;
  final DateTime expiryDate;
  final bool isExpiryRecognized;
  final String? imageUrl;
  final DateTime createdAt;
  final DateTime updatedAt;
  final String userId;
  final String? cosmeticId;

  CosmeticExpiry({
    this.id,
    required this.productName,
    this.brandName,
    required this.expiryDate,
    this.isExpiryRecognized = false,
    this.imageUrl,
    required this.createdAt,
    required this.updatedAt,
    required this.userId,
    this.cosmeticId,
  });




  factory CosmeticExpiry.fromJson(Map<String, dynamic> json) {
    return CosmeticExpiry(
      id: json['id'] as String,
      productName: json['productName'] as String,
      brandName: json['brandName'] as String?,
      expiryDate: DateTime.parse(json['expiryDate'] as String),
      isExpiryRecognized: (json['isExpiryRecognized'] as bool?) ?? false,
      imageUrl: json['imageUrl'] as String?,
      createdAt: DateTime.parse(json['createdAt'] as String),
      updatedAt: DateTime.parse(json['updatedAt'] as String),
      userId: json['userId'] as String,
      cosmeticId: json['cosmeticId'] as String?,
    );
  }

  Map<String, dynamic> toJson() {
    return {
      'id': id,
      'productName': productName,
      'brandName': brandName,
      'expiryDate': expiryDate.toIso8601String(),
      'isExpiryRecognized': isExpiryRecognized,
      'imageUrl': imageUrl,
      'createdAt': createdAt.toIso8601String(),
      'updatedAt': updatedAt.toIso8601String(),
      'userId': userId,
      'cosmeticId': cosmeticId,
    };
  }
}
