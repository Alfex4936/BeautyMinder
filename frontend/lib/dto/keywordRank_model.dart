// API요청으로 받아오는 모델
class KeyWordRank{
  final List<String>? keywords;

  KeyWordRank({
    required this.keywords,
  });

  factory KeyWordRank.fromJson(List<dynamic> json){
    return KeyWordRank(
        keywords: List<String>.from(json)
    );
  }

  @override
  String toString() {
    return 'KeyWordRank{keywords: ${keywords?.join(', ')}}';
  }

}
