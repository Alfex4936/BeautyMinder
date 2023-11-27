class SurveyWrapper {
  Map<String, BaumannSurveys> surveys;

  SurveyWrapper({required this.surveys});

  factory SurveyWrapper.fromJson(Map<String, dynamic> json) {
    Map<String, BaumannSurveys> surveysMap =
        json.map((key, value) => MapEntry(key, BaumannSurveys.fromJson(value)));

    return SurveyWrapper(surveys: surveysMap);
  }

  Map<String, dynamic> toJson() {
    return surveys.map((key, value) => MapEntry(key, value.toJson()));
  }
}

class BaumannSurveys {
  final String questionKr;
  final List<Option> options;

  BaumannSurveys({
    required this.questionKr,
    required this.options,
  });

  Map<String, dynamic> toJson() {
    return {
      'question_kr': questionKr,
      'options': options.map((option) => option.toJson()).toList(),
    };
  }

  factory BaumannSurveys.fromJson(Map<String, dynamic> json) {
    return BaumannSurveys(
      questionKr: json['question_kr'],
      options: (json['options'] as List)
          .map((item) => Option.fromJson(item))
          .toList(),
    );
  }
}

class Option {
  final int option;
  final String description;

  Option({
    required this.option,
    required this.description,
  });

  Map<String, dynamic> toJson() {
    return {
      'option': option,
      'description': description,
    };
  }

  factory Option.fromJson(Map<String, dynamic> json) {
    return Option(
      option: json['option'],
      description: json['description'],
    );
  }
}
