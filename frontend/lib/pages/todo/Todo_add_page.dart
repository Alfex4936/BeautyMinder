import 'package:beautyminder/pages/todo/todo_page.dart';
import 'package:flutter/material.dart';

import '../../dto/task_model.dart';
import '../../dto/todo_model.dart';
import '../../services/api_service.dart';
import '../../services/todo_service.dart';
import '../../widget/commonAppBar.dart';
import '../../widget/commonBottomNavigationBar.dart';
import '../home/home_page.dart';
import '../my/my_page.dart';
import '../pouch/expiry_page.dart';

class TodoAddPage extends StatefulWidget {
  const TodoAddPage({Key? key}) : super(key: key);

  @override
  _TodoAddPage createState() => _TodoAddPage();
}

class _TodoAddPage extends State<TodoAddPage> {
  int _currentIndex = 3;
  late List<TextEditingController> _controllers = [];
  TextEditingController _dateController = TextEditingController();
  List<List<bool>> _toggleSelections = [];
  List<String> categorys = [];
  Todo? todo;
  DateTime? picked;
  bool isEmptyTextField = false;

  late List<Task> tasks;

  @override
  void initState() {
    // 모든 controller을 dispose
    super.initState();
    _controllers.add(TextEditingController());
    _dateController.text = DateTime.now().toString().substring(0, 10);
    _toggleSelections.add([false, false, true]);
    picked = DateTime.parse(_dateController.text);
  }

  @override
  void dispose() {
    _controllers.forEach((controller) {
      controller.dispose();
    });
    _dateController.dispose();
    super.dispose();
  }

  void _addNewTextField() {
    setState(() {
      // 새로운 TextEditingContrller을 추가
      _controllers.add(TextEditingController());
      _toggleSelections.add([false, false, true]);
    });
  }

  void _removeTextField() {
    if (_controllers.length > 1) {
      setState(() {
        _controllers.removeLast();
        _toggleSelections.removeLast();
      });
    }
  }

  Future<void> _selectDate(BuildContext context) async {
    // Date를 저장하는 함수
    picked = await showDatePicker(
        context: context,
        initialDate: DateTime.now(),
        firstDate: DateTime(2000),
        lastDate: DateTime(2101),
        builder: (context, child) {
          return Theme(
            data: Theme.of(context).copyWith(
                disabledColor: Colors.black87,
                colorScheme: const ColorScheme.light(
                    primary: Color(0xffffecda),
                    onPrimary: Colors.black87,
                    onSurface: Colors.black
                    // onSurface: Colors.black87
                    ),
                textButtonTheme: TextButtonThemeData(
                    style: TextButton.styleFrom(
                        backgroundColor: const Color(0xffffecda),
                        foregroundColor: const Color(0xffd86a04)))),
            child: child!,
          );
        });
    if (picked != null && picked != DateTime.now()) {
      setState(() {
        _dateController.text =
            picked.toString().substring(0, 10); // 선택된 날짜를 TextField에 반영
        print(picked);
      });
    }
  }

  List<Task> createTasks() {
    tasks = List.generate(_controllers.length, (index) {
      String description = _controllers[index].text;
      String category = getCategory(_toggleSelections[index]);
      return Task(category: category, description: description, done: false);
    });

    for (int i = 0; i < tasks.length; i++) {
      if (tasks[i].description.isEmpty) {
        tasks.removeAt(i);
      }
    }

    return tasks.length < 1 ? [] : tasks;
  }

  Todo? createRoutine() {
    createTasks();
    todo = Todo(date: picked, tasks: tasks);
    return todo;
  }

  String getCategory(List<bool> categorys) {
    if (categorys[0]) {
      return "dinner";
    } else if (categorys[1]) {
      return "morning";
    } else {
      return "other";
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: CommonAppBar(),
      body: SingleChildScrollView(
        child: Column(
          children: [
            Row(
              mainAxisAlignment: MainAxisAlignment.end,
              children: [
                TextButton.icon(
                  onPressed: () async {
                    createRoutine();

                    if (tasks.length == 0) {
                      Navigator.of(context).push(MaterialPageRoute(
                          builder: (context) => const CalendarPage()));
                    } else {
                      await TodoService.addTodo(todo!);

                      Navigator.of(context).push(MaterialPageRoute(
                          builder: (context) => const CalendarPage()));
                    }
                  },
                  icon: const Icon(Icons.add_box_rounded,
                      size: 50, color: Color(0xffd86a04)),
                  label: const Text("완료",
                      style: TextStyle(fontSize: 25, color: Color(0xffd86a04))),
                )
              ],
            ),
            Padding(
                padding: const EdgeInsets.all(20),
                child: GestureDetector(
                  onTap: () => {
                    _selectDate(context),
                  },
                  child: AbsorbPointer(
                    child: TextField(
                      controller: _dateController,
                      decoration: InputDecoration(
                          prefixStyle: TextStyle(color: Color(0xffd86a04)),
                          labelText: 'Date',
                          hintText: 'Date',
                          icon: const Icon(
                            Icons.calendar_month,
                            color: Color(0xffd86a04),
                          ),
                          border: OutlineInputBorder(
                              borderRadius: BorderRadius.circular(10),
                              borderSide: const BorderSide(
                                  color: Colors.black, width: 1.0)),
                          contentPadding: EdgeInsets.all(3)),
                    ),
                  ),
                )),
            ..._controllers.asMap().entries.map((entry) {
              int index = entry.key;
              TextEditingController controller = entry.value;
              return Padding(
                  padding:
                      const EdgeInsets.symmetric(horizontal: 20, vertical: 10),
                  child: Row(
                    children: [
                      Expanded(
                        child: TextField(
                          controller: controller,
                          decoration: InputDecoration(
                              labelText: 'Todo $index',
                              hintText: 'Enter Todo $index',
                              icon: const Icon(Icons.add_task_sharp,
                                  color: Color(0xffd86a04)),
                              border: OutlineInputBorder(
                                borderRadius: BorderRadius.circular(10),
                              ),
                              focusedBorder: const OutlineInputBorder(
                                  borderSide: BorderSide(
                                      color: Colors.amber, width: 2.0))),
                        ),
                      ),
                      const SizedBox(width: 10),
                      ToggleButtons(
                        isSelected: _toggleSelections[index],
                        onPressed: (int buttonIndex) {
                          setState(() {
                            if (!_toggleSelections[index][buttonIndex]) {
                              for (int i = 0;
                                  i < _toggleSelections[index].length;
                                  i++) {
                                _toggleSelections[index][i] = i == buttonIndex;
                              }
                            }
                          });
                        },
                        color: Colors.black,
                        borderRadius: BorderRadius.circular(10),
                        selectedColor: Colors.white,
                        fillColor: const Color(0xffffecda),
                        borderColor: Colors.grey,
                        selectedBorderColor: const Color(0xffffecda),
                        children: const <Widget>[
                          Padding(
                            padding: EdgeInsets.symmetric(horizontal: 8),
                            child: Text(
                              'Dinner',
                              style: TextStyle(color: Colors.black),
                            ),
                          ),
                          Padding(
                            padding: EdgeInsets.symmetric(horizontal: 8),
                            child: Text(
                              'Morning',
                              style: TextStyle(color: Colors.black),
                            ),
                          ),
                          Padding(
                            padding: EdgeInsets.symmetric(horizontal: 8),
                            child: Text(
                              'Other',
                              style: TextStyle(color: Colors.black),
                            ),
                          ),
                        ],
                      )
                    ],
                  ));
            }).toList(),
            Row(
              mainAxisAlignment: MainAxisAlignment.center,
              children: [
                Padding(
                  padding:
                      const EdgeInsets.symmetric(horizontal: 20, vertical: 20),
                  child: ElevatedButton(
                    style: FilledButton.styleFrom(
                        backgroundColor: const Color(0xffffecda)),
                    onPressed: _addNewTextField,
                    child: const Icon(Icons.add, color: Color(0xffd86a04)),
                  ),
                ),
                if (_controllers.length > 1)
                  Padding(
                    padding: const EdgeInsets.symmetric(
                        horizontal: 20, vertical: 20),
                    child: ElevatedButton(
                      style: FilledButton.styleFrom(
                          backgroundColor: const Color(0xffffecda)
                          //Color(0xffffecda),
                          ),
                      onPressed: _removeTextField,
                      child: const Icon(
                        Icons.remove,
                        color: Color(0xffd86a04),
                      ),
                    ),
                  ),
              ],
            )
          ],
        ),
      ),
      bottomNavigationBar: CommonBottomNavigationBar(
        currentIndex: _currentIndex,
        onTap: (int index) async {
          // 페이지 전환 로직 추가
          if (index == 1) {
            Navigator.of(context).push(
                MaterialPageRoute(builder: (context) => CosmeticExpiryPage()));
          } else if (index == 2) {
            final userProfileResult = await APIService.getUserProfile();
            Navigator.of(context).push(MaterialPageRoute(
                builder: (context) => HomePage(user: userProfileResult.value)));
          } else if (index == 3) {
            Navigator.of(context).push(
                MaterialPageRoute(builder: (context) => const CalendarPage()));
          } else if (index == 4) {
            Navigator.of(context)
                .push(MaterialPageRoute(builder: (context) => const MyPage()));
          }
        },
      ),
    );
  }
}
