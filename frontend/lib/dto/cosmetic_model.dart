// API요청으로 받아오는 모델
class Cosmetic{
  final String id;
  final String name;
  final String? brand;
  final List<String>? images;
  final String? category;
  final List<String>? keywords;

  Cosmetic({
    required this.id,
    required this.images,
    required this.brand,
    required this.category,
    required this.keywords,
    required this.name
  });

  factory Cosmetic.fromJson(Map<String, dynamic> json){
    return Cosmetic(
        id: json["id"],
        images: List<String>.from(json["images"]),
        brand: json["brand"],
        category: json["category"],
        keywords: List<String>.from(json["keywords"]),
        name: json["name"]);
  }

  @override
  String toString() {
    return 'CosmeticModel{id: $id, name: $name, brand: $brand, images: ${images?.join(', ')}, category: $category, keywords: ${keywords?.join(', ')}}';
  }

}