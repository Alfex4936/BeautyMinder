import 'package:equatable/equatable.dart';

import '../dto/task_model.dart';
import '../dto/todo_model.dart';

abstract class TodoPageEvent extends Equatable {
  // event는 총4개
  // Init event : API를 통해 Todo를 불러오는 이벤트
  // Add event : API를 통해 Todo를 추가하는 이벤트
  // Delete event : API를 통해 Todo를 삭제하는 이벤트
  // Update event : API를 통해 Todo를 수정하는 이벤트

  final List<Todo>? todos;
  final Todo? todo;
  final Task? task;
  final List<Map<String, dynamic>>? update_todo;
  final bool? isDone;

  const TodoPageEvent(
      {this.todos, this.update_todo, this.isDone, this.task, this.todo});
}

class TodoPageInitEvent extends TodoPageEvent {
  @override
  List<Object?> get props => [];

  TodoPageInitEvent();
}

class TodoPageAddEvent extends TodoPageEvent {
  // 추가할 객체를 생성
  final Todo todo;

  TodoPageAddEvent(this.todo);

  @override
  List<Object?> get props => [todo];
}

class TodoPageDeleteEvent extends TodoPageEvent {
  TodoPageDeleteEvent({super.task, super.todo});

  @override
  List<Object?> get props => [];
}

class TodoPageTaskUpdateEvent extends TodoPageEvent {
  const TodoPageTaskUpdateEvent({super.task, super.todos, super.todo});

  @override
  List<Object?> get props => [task, todo, todos];
}

class TodoPageTaskUpdatedEvent extends TodoPageEvent {
  const TodoPageTaskUpdatedEvent({super.task, super.todos, super.todo});

  @override
  List<Object?> get props => [task, todo, todos];
}

class TodoPageErrorEvent extends TodoPageEvent {
  const TodoPageErrorEvent();

  @override
  List<Object?> get props => [];
}
