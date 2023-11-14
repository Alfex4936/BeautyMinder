import 'dart:convert';
import 'package:flutter/material.dart';
import '../dto/user_model.dart';
import '../services/shared_service.dart';
import '../services/todo_service.dart';
import '../dto/todo_model.dart';

class HomePage extends StatefulWidget {
  const HomePage({Key? key}) : super(key: key);

  @override
  _HomePageState createState() => _HomePageState();
}

class _HomePageState extends State<HomePage> {
  late Future<Result<List<Todo>>> futureTodoList;

  @override
  void initState() {
    super.initState();
    futureTodoList = TodoService.getAllTodos();
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('beautyMinder'),
        elevation: 0,
        actions: [
          IconButton(
            icon: const Icon(
              Icons.logout,
              color: Colors.black,
            ),
            onPressed: () => SharedService.logout(context),
          ),
          const SizedBox(
            width: 10,
          ),
        ],
      ),
      backgroundColor: Colors.grey[200],
      body: userProfile(),
      floatingActionButton: FloatingActionButton(
        child: const Icon(Icons.add),
        onPressed: () async {
          // final User? user = await SharedService.getUser();
          // if (user == null) {
          //   // Navigate to login or show an error.
          //   return;
          // }

          // Here, add logic to show a dialog and add a new ToDo.
          // For now, let's simulate adding a new ToDo
          final newTodo = Todo(
            date: DateTime.now(),
            morningTasks: ['Morning task'],
            dinnerTasks: ['Dinner task'],
            user: (await SharedService.getUser())!,
          );

          final result = await TodoService.addTodo(newTodo);

          if (result.value != null) {
            setState(() {
              futureTodoList = TodoService.getAllTodos();
            });
          }
        },
      ),
    );
  }

  Widget userProfile() {
    return FutureBuilder(
      future: futureTodoList,
      builder:
          (BuildContext context, AsyncSnapshot<Result<List<Todo>>> snapshot) {
        if (snapshot.connectionState == ConnectionState.waiting) {
          return const Center(
            child: CircularProgressIndicator(),
          );
        }

        if (snapshot.hasError) {
          return Center(
            child: Text("Error: ${snapshot.error}"),
          );
        }

        final todosResult = snapshot.data;

        if (todosResult == null || todosResult.value == null) {
          return Center(
            child: Text(
                "Failed to load todos: ${todosResult?.error ?? 'Unknown error'}"),
          );
        }

        final todos = todosResult.value!;

        return ListView.builder(
          itemCount: todos.length,
          itemBuilder: (context, index) {
            final todo = todos[index];
            return ListTile(
              title: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Text("Date: ${todo.date.toString()}"),
                  Text("Morning Tasks: ${todo.morningTasks.join(', ')}"),
                  Text("Dinner Tasks: ${todo.dinnerTasks.join(', ')}"),
                ],
              ),
              trailing: IconButton(
                icon: const Icon(Icons.delete),
                onPressed: () async {
                  final result = await TodoService.deleteTodo(todo.id ?? '-1');
                  if (result.value != null) {
                    setState(() {
                      futureTodoList = TodoService.getAllTodos();
                    });
                  }
                },
              ),
            );
          },
        );
      },
    );
  }
}
