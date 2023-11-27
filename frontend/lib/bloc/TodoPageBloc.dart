import 'package:flutter_bloc/flutter_bloc.dart';

import '../State/TodoState.dart';
import '../dto/todo_model.dart';
import '../event/TodoPageEvent.dart';
import '../services/todo_service.dart';

class TodoPageBloc extends Bloc<TodoPageEvent, TodoState> {
  Function()? onCloseCallback;

  TodoPageBloc() : super(const TodoInitState()) {
    on<TodoPageInitEvent>(_initEvent);
    on<TodoPageAddEvent>(_addEvent);
    on<TodoPageTaskUpdateEvent>(_TaskUpdateEvent);
    on<TodoPageDeleteEvent>(_deleteEvent);
    on<TodoPageErrorEvent>(_errorEvent);
  }

  // Todo를 불러오는 Event
  Future<void> _initEvent(
      TodoPageInitEvent event, Emitter<TodoState> emit) async {
    emit(TodoDownloadedState(isError: state.isError));
    print("initEvent");

    // userId를 통해서 todo받아오기
    // 없으면 아무것도 노출 안됨
    final result = (await TodoService.getAllTodos());

    if (result == null) {
      emit(TodoLoadedState(todos: const [], isError: state.isError));
      return;
    }
    print("result.value in _initEvent : ${result.value}");

    try {
      List<Todo>? todos = result.value;
      if (todos != null) {
        print("TodoLoadedState");
        //정상적으로 데이터를 받아옴
        emit(TodoLoadedState(todos: todos, isError: state.isError));
        print("emit complete");
      } else {
        print("TodoErrorState");
        emit(TodoErrorState(isError: state.isError));
      }
    } catch (e) {
      print("Error : ${e}");
    }
  }

  Future<void> _changeEvent(
      TodoPageTaskUpdateEvent event, Emitter<TodoState> emit) async {
    print("event.todos : ${event.todos}");

    emit(TodoLoadedState());
  }

  // Todo를 추가하는 Event
  Future<void> _addEvent(
      TodoPageAddEvent event, Emitter<TodoState> emit) async {
    print("addevent");
    print("TodoLoadedstate in addevent");
    // Todo가 로드된 상태에서만 Todo add event가 가능
    emit(TodoAddState(todo: state.todo, isError: state.isError));
    print("state.todo : ${state.todo}");
    try {
      final Todo todo = event.todo;
      print("event.todo : ${event.todo}");
      print("call addTodo in addEvent");
      final result = await TodoService.addTodo(todo);
      print("result.value : ${result.value}");

      if (result.value != null) {
        emit(TodoAddedState(todo: state.todo, isError: state.isError));
        print(todo);
        emit(TodoLoadedState(todos: state.todos, isError: state.isError));
      } else {
        print("Error : ${result.error}");
      }
    } catch (e) {
      print("Error : ${e}");
    }
  }

  Future<void> _TaskUpdateEvent(
      TodoPageTaskUpdateEvent event, Emitter<TodoState> emit) async {
    Todo? todo;
    List<Todo>? todos = [];

    print("update");
    //emit(TodoLoadedState(todos: event.todos, todo: event.todo, task: event.task, isError :state.isError));

    if (state is TodoLoadedState) {
      emit(TodoUpdateState(
          isError: false,
          task: state.task,
          todo: state.todo,
          todos: state.todos));

      print("this is TodoLoadedState");

      try {
        final result = await TodoService.taskUpdateTodo(event.todo, event.task);

        print("result.value : ${result.value}");
        if (result.value!.containsKey('todo')) {
          todo = Todo.fromJson(result.value?['todo']);

          if (todo != null) {
            todos.add(todo);
          }
        }

        if (result.value != null) {
          print("TodoUpdatedState!!");
          emit(TodoUpdatedState(todo: todo, isError: false, todos: todos));
          print("TodoLoadedState!!");
          emit(TodoLoadedState(
              isError: state.isError, todos: state.todos, todo: state.todo));
        } else {
          // emit(TodoErrorState(isError: true));
        }
      } catch (e) {
        print("Error : ${e}");
      }
    } else {
      emit(TodoErrorState());
    }
    onCloseCallback?.call();
  }

  Future<void> _deleteEvent(
      TodoPageDeleteEvent event, Emitter<TodoState> emit) async {
    Todo? todo;
    List<Todo>? todos = [];

    if (state is TodoLoadedState) {
      emit(TodoDeleteState(
          todo: state.todo, isError: state.isError, todos: state.todos));
      print("event.todo : ${event.todo}");
      print("event.task: ${event.task}");
      try {
        final String? taskid = event.task?.taskId;
        final result = await TodoService.deleteTask(event.todo, event.task);
        print("result.value.runtimeType: ${result.value.runtimeType}");
        print("result: ${result.value}");

        if (result == null || result.value == null) {
          emit(TodoLoadedState(
              isError: state.isError, todos: [], todo: state.todo));
          return;
        }

        if (result.value!.containsKey('todo')) {
          todo = Todo.fromJson(result.value?['todo']);

          if (todo != null) {
            todos.add(todo);
          }
        }

        emit(TodoDeletedState(todo: todo, isError: false, todos: todos));
        //print(taskid);
        emit(TodoLoadedState(
            isError: state.isError, todos: state.todos, todo: state.todo));
      } catch (e) {
        print("Error in Delete: ${e}");
      }
    } else {
      //emit(TodoErrorState(isError: true));
    }
  }

  Future<void> _errorEvent(
      TodoPageErrorEvent event, Emitter<TodoState> emit) async {
    if (state is TodoLoadedState) {
      print("TodoErrorEvent");
      emit(TodoErrorState(isError: true));
    }
  }
}
