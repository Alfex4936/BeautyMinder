class Baumann {
  Baumann({
    required this.questions,
  });

  late final Map<String, dynamic> questions;

  @override
  String toString() {
    return 'Baumann { questions: $questions }';
  }

  factory Baumann.fromJson(Map<String, dynamic> json) {
    return Baumann(
      questions: Map<String, dynamic>.from(json),
    );
  }

  //백으로 전송
  Map<String, dynamic> toJson() {
    final Map<String, dynamic> json = {
      'reponses': {},
    };
    questions.forEach((key, value) {
      json['responses'][key]=value['option'];
    });
    return json;
  }
}
