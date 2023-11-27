// import 'package:frontend/dto/user_model.dart';
import 'user_model.dart';

class Baumann {
  Baumann({
    this.id,
    required this.date,
    required this.morningTasks,
    required this.dinnerTasks,
    required this.user,
    this.createdAt,
  });

  late final String? id;
  late final DateTime date;
  late final List<String> morningTasks;
  late final List<String> dinnerTasks;
  late final User user;
  late final DateTime? createdAt;

  @override
  String toString() {
    return '''
Todo {
  id: $id,
  date: $date,
  morningTasks: $morningTasks,
  dinnerTasks: $dinnerTasks,
  user: $user,
  createdAt: $createdAt
}''';
  }

  factory Baumann.fromJson(Map<String, dynamic> json) {
    return Baumann(
      id: json['id'],
      date: DateTime.parse(json['date']),
      morningTasks: List<String>.from(json['morningTasks']),
      dinnerTasks: List<String>.from(json['dinnerTasks']),
      user: User.fromJson(json['user']),
      createdAt: DateTime.parse(json['createdAt']),
    );
  }

  Map<String, dynamic> toJson() {
    return {
      'id': id,
      'date': date.toIso8601String(),
      'morningTasks': morningTasks,
      'dinnerTasks': dinnerTasks,
      'user': user.toJson(), // Assuming you have a toJson in User model
      'createdAt': createdAt?.toIso8601String(),
    };
  }
}
