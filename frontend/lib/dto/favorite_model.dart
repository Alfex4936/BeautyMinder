class FavoriteModel {
  FavoriteModel({
    required this.id,
    required this.name,
    required this.brand,
    required this.images,
    required this.glowpickUrl,
    required this.expirationDate,
    required this.createdAt,
    required this.purchasedDate,
    required this.category,
    required this.averageRating,
    required this.reviewCount,
    required this.totalRating,
    required this.keywords,
  });

  late final String id;
  late final String name;
  late final String brand;
  late final List<String> images;
  late final String glowpickUrl;
  late final String expirationDate;
  late final String createdAt;
  late final String purchasedDate;
  late final String category;
  late final double averageRating;
  late final int reviewCount;
  late final int totalRating;
  late final List<String> keywords;

  @override
  String toString() {
    return '''
FavoriteModel {
  id: $id,
  name: $name,
  brand: $brand,
  images: $images,
  glowpickUrl: $glowpickUrl,
  expirationDate: $expirationDate,
  createdAt: $createdAt,
  purchasedDate: $purchasedDate,
  category: $category,
  averageRating: $averageRating,
  reviewCount: $reviewCount,
  totalRating: $totalRating,
  keywords: $keywords
}''';
  }

  factory FavoriteModel.fromJson(Map<String, dynamic> json) {
    return FavoriteModel(
      id: json['id'],
      name: json['name'],
      brand: json['brand'],
      images: List<String>.from(json['images']),
      glowpickUrl: json['glowpickUrl'],
      expirationDate: json['expirationDate'],
      createdAt: json['createdAt'],
      purchasedDate: json['purchasedDate'],
      category: json['category'],
      averageRating: json['averageRating'].toDouble(),
      reviewCount: json['reviewCount'],
      totalRating: json['totalRating'],
      keywords: List<String>.from(json['keywords']),
    );
  }

  Map<String, dynamic> toJson() {
    return {
      'id': id,
      'name': name,
      'brand': brand,
      'images': images,
      'glowpickUrl': glowpickUrl,
      'expirationDate': expirationDate,
      'createdAt': createdAt,
      'purchasedDate': purchasedDate,
      'category': category,
      'averageRating': averageRating,
      'reviewCount': reviewCount,
      'totalRating': totalRating,
      'keywords': keywords,
    };
  }
}
