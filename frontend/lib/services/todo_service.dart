import 'dart:convert';

import 'package:beautyminder/dto/task_model.dart';
import 'package:beautyminder/services/dio_client.dart';
import 'package:intl/intl.dart';
import 'package:beautyminder/services/api_service.dart';

import '../../config.dart';
import '../dto/todo_model.dart';
import 'shared_service.dart';

class TodoService {
  // Get All Todos
  // 유저의 모둔 Routine(todo)를 가지고 오는 서비스
  static Future<Result<List<Todo>>> getAllTodos() async {
    final user = await SharedService.getUser();

    // AccessToken가지고오기
    final accessToken = await SharedService.getAccessToken();
    final refreshToken = await SharedService.getRefreshToken();

    final headers = {
      'Authorization': 'Bearer $accessToken',
      'Cookie': 'XRT=$refreshToken',
    };

    final url = Uri.http(Config.apiURL, Config.todoAPI).toString();
    try {
      final response =
          await DioClient.sendRequest('GET', url, headers: headers);

      if (response.statusCode == 200) {
        Map<String, dynamic> decodedResponse;
        if (response.data is String) {
          decodedResponse = jsonDecode(response.data);
        } else if (response.data is Map) {
          decodedResponse = response.data;
        } else {
          return Result.failure("Unexpected response data type");
        }

        if (decodedResponse.containsKey('todos')) {
          List<dynamic> todoList = decodedResponse['todos'];
          try {
            List<Todo> todos =
                todoList.map((data) => Todo.fromJson(data)).toList();
            return Result.success(todos);
          } catch (e) {
            print("Error : ${e}");
          }
        }
        return Result.failure("Failed to get todos: No todos key in response");
      }
      return Result.failure("Failed to get todos");
    } catch (e) {
      return Result.failure("An error occurred: $e");
    }
  }

  // Add a new Todo
  // 새로운 루틴(Todo)를 추가
  static Future<Result<Todo>> addTodo(Todo todo) async {
    final user = await SharedService.getUser();

    // AccessToken가지고오기
    final accessToken = await SharedService.getAccessToken();
    final refreshToken = await SharedService.getRefreshToken();

    final url = Uri.http(Config.apiURL, Config.todoAddAPI).toString();
    final headers = {
      'Authorization': 'Bearer $accessToken',
      'Cookie': 'XRT=$refreshToken',
    };

    try {
      final response = await DioClient.sendRequest('POST', url,
          body: todo.toJson(), headers: headers);

      print("response : ${response}");
      return Result.success(todo);
    } catch (e) {
      return Result.failure("An error occurred: $e");
    }
  }

  // 오늘의 Routine을 받아오는 함수
  static Future<Result<Todo>> getTodoOf() async {
    final user = await SharedService.getUser();

    // AccessToken가지고오기
    final accessToken = await SharedService.getAccessToken();
    final refreshToken = await SharedService.getRefreshToken();

    final userId = user?.id ?? '-1';

    String now = DateFormat('yyyy-MM-dd').format(DateTime.now());

    final url = Uri.http(Config.apiURL, Config.Todo + now).toString();
    print(url);
    final headers = {
      'Authorization': 'Bearer $accessToken',
      'Cookie': 'XRT=$refreshToken',
    };

    try {
      final response =
          await DioClient.sendRequest('GET', url, headers: headers);

      if (response.statusCode == 200) {
        Map<String, dynamic> decodedResponse;
        if (response.data is String) {
          decodedResponse = jsonDecode(response.data);
        } else if (response.data is Map) {
          decodedResponse = response.data;
        } else {
          return Result.failure("Unexpected response date Type");
        }
        if (decodedResponse.containsKey('todos') &&
            decodedResponse['todos'] != []) {
          List<dynamic> todos = decodedResponse['todos'];

          if (todos.isNotEmpty) {
            Todo todo = Todo.fromJson(todos[0]);
            return Result.success(todo);
          } else {
            // todos가 비어 있을 때 빈 리스트 반환
            Todo? todo = null;
            return Result.success(todo);
          }
        }

        return Result.failure("Failed to get todos: No todos key in response");
      }

      return Result.success(response.data);
    } catch (e) {
      print("Todoservice : ${e}");
      return Result.failure("error");
    }
  }

  // rotutine의 task를 삭제
  static Future<Result<Map<String, dynamic>>> deleteTask(
      Todo? todo, Task? task) async {
    final user = await SharedService.getUser();

    // AccessToken가지고오기
    final accessToken = await SharedService.getAccessToken();
    final refreshToken = await SharedService.getRefreshToken();

    final url = Uri.http(
      Config.apiURL,
      Config.todoUpdateAPI + todo!.id!,
    ).toString();

    final headers = {
      'Authorization': 'Bearer $accessToken',
      'Cookie': 'XRT=$refreshToken',
    };

    Map<String, dynamic> delete = {
      "taskIdsToDelete": [task?.taskId]
    };

    try {
      final response = await DioClient.sendRequest('PUT', url,
          body: delete, headers: headers);

      return Result.success(response.data);
    } catch (e) {
      return Result.failure("An error occurred: $e");
    }
  }

  //존
  static Future<Result<Map<String, dynamic>>> taskUpdateTodo(
      Todo? todo, Task? task) async {
    final user = await SharedService.getUser();

    // AccessToken가지고오기
    final accessToken = await SharedService.getAccessToken();
    final refreshToken = await SharedService.getRefreshToken();

    final url = Uri.http(
      Config.apiURL,
      Config.todoUpdateAPI + todo!.id!,
    ).toString();

    final headers = {
      'Authorization': 'Bearer $accessToken',
      'Cookie': 'XRT=$refreshToken',
    };

    Map<String, dynamic> taskUpdate = {
      "tasksToUpdate": [
        {
          "taskId": task?.taskId,
          "description": task?.description,
          "isDone": task?.done,
          "category": task?.category,
        }
      ]
    };

    try {
      final response = await DioClient.sendRequest('PUT', url,
          body: taskUpdate, headers: headers);

      print("response : ${response}");
      return Result.success(response.data);
    } catch (e) {
      return Result.failure("An error occurred: $e");
    }
  }
}
