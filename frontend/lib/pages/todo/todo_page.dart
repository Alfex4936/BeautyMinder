import 'package:beautyminder/pages/recommend/recommend_bloc_screen.dart';
import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:flutter_slidable/flutter_slidable.dart';
import 'package:flutter_spinkit/flutter_spinkit.dart';
import 'package:table_calendar/table_calendar.dart';

import '../../Bloc/TodoPageBloc.dart';
import '../../State/TodoState.dart';
import '../../dto/task_model.dart';
import '../../dto/todo_model.dart';
import '../../event/TodoPageEvent.dart';
import '../../services/api_service.dart';
import '../../widget/commonAppBar.dart';
import '../../widget/commonBottomNavigationBar.dart';
import '../home/home_page.dart';
import '../my/my_page.dart';
import '../pouch/expiry_page.dart';
import 'Todo_add_page.dart';

class _CalendarPageState extends State<CalendarPage> {
  int _currentIndex = 3;
  CalendarFormat _calendarFormat = CalendarFormat.month;

  DateTime _focusedDay = DateTime.now();
  DateTime? _selectedDay;

  @override
  void initState() {
    super.initState();
    _selectedDay = _focusedDay;
  }

  @override
  Widget build(BuildContext context) {
    // String todayFormatted = DateFormat('yyyy-MM-dd').format(_focusedDay); 하면 될 듯?

    return BlocProvider(
        create: (_) => TodoPageBloc()..add(TodoPageInitEvent()),
        lazy: false,
        child: Scaffold(
            appBar: CommonAppBar(),
            body: Column(
              children: [
                BlocBuilder<TodoPageBloc, TodoState>(builder: (context, state) {
                  return Expanded(child: todoListWidget());
                })
              ],
            ),
            bottomNavigationBar: CommonBottomNavigationBar(
              currentIndex: _currentIndex,
              onTap: (int index) async {
                // 페이지 전환 로직 추가
                if (index == 0) {
                  Navigator.of(context)
                      .push(MaterialPageRoute(builder: (context) => RecPage()));
                } else if (index == 1) {
                  Navigator.of(context).push(MaterialPageRoute(
                      builder: (context) => CosmeticExpiryPage()));
                } else if (index == 2) {
                  final userProfileResult = await APIService.getUserProfile();
                  Navigator.of(context).push(MaterialPageRoute(
                      builder: (context) =>
                          HomePage(user: userProfileResult.value)));
                } else if (index == 4) {
                  Navigator.of(context).push(
                      MaterialPageRoute(builder: (context) => const MyPage()));
                }
              },
            )));
  }
}

class todoListWidget extends StatefulWidget {
  @override
  _todoListWidget createState() => _todoListWidget();
}

class CalendarPage extends StatefulWidget {
  const CalendarPage({Key? key}) : super(key: key);

  @override
  _CalendarPageState createState() => _CalendarPageState();
}

class _todoListWidget extends State<todoListWidget> {
  TextEditingController _controller = TextEditingController();

  CalendarFormat _calendarFormat = CalendarFormat.month;

  DateTime _focusedDay = DateTime.now();
  DateTime? _selectedDay;
  late List<Task> taskList;

  @override
  void initState() {
    super.initState();
    _selectedDay = _focusedDay;
  }

  @override
  void dispose() {
    _controller.dispose();
    super.dispose();
  }

  Widget _todoList(List<Todo>? todos) {
    return Padding(
        padding: EdgeInsets.all(10),
        child: Column(
          children: _buildChildren(todos),
        ));
  }

  List<Widget> _buildChildren(List<Todo>? todos) {
    List<Widget> _children = [];
    List<Widget> _morningTasks = [];
    List<Widget> _dinnerTasks = [];
    List<Widget> _otherTasks = [];

    if (todos == null || todos.isEmpty) {
      return _children;
    }

    taskList = todos!.expand((todo) => todo.tasks).toList();

    // taskList를 순회하며 작업 수행
    for (var task in taskList) {
      if (task.category == 'morning') {
        print(task.taskId);
        _morningTasks.add(_todo(task, todos[0], todos));
      } else if (task.category == 'dinner') {
        print(task.taskId);
        _dinnerTasks.add(_todo(task, todos[0], todos));
      } else {
        _otherTasks.add(_todo(task, todos[0], todos));
      }
    }

    if (_morningTasks.length != 0) {
      _children.add(_row('morning'));
      _children.addAll(_morningTasks);
    }

    if (_dinnerTasks.length != 0) {
      _children.add(_row('dinner'));
      _children.addAll(_dinnerTasks);
    }

    if (_otherTasks.length != 0) {
      _children.add(_row('other'));
      _children.addAll(_otherTasks);
    }

    return _children;
  }

  Widget _calendar(List<Todo>? todos) {
    List<Todo> _getTodosForDay(DateTime day) {
      return todos?.where((todo) => isSameDay(todo.createdAt, day)).toList() ??
          [];
    }

    return TableCalendar(
      firstDay: DateTime.utc(2010, 10, 16),
      lastDay: DateTime.utc(2030, 3, 14),
      focusedDay: _focusedDay,
      selectedDayPredicate: (day) {
        return isSameDay(_selectedDay, day);
      },
      onDaySelected: (selectedDay, focusedDay) {
        setState(() {
          _selectedDay = selectedDay;
          _focusedDay = focusedDay;
        });
      },
      eventLoader: _getTodosForDay,
      calendarFormat: _calendarFormat,
      onFormatChanged: (format) {
        setState(() {
          _calendarFormat = format;
        });
      },
      onPageChanged: (focusedDay) {
        _focusedDay = focusedDay;
      },
    );
  }

  Widget _row(String name) {
    return Row(
      children: [
        Padding(
          padding: EdgeInsets.zero,
          child: Container(
            width: 100,
            height: 35,
            decoration: BoxDecoration(
              border: Border.all(color: Colors.black),
              borderRadius: BorderRadius.circular(15),
            ),
            child: Center(
              child: Text(name),
            ),
          ),
        ),
      ],
    );
  }

  Widget _todo(Task task, Todo todo, List<Todo>? todos) {
    return Slidable(
      startActionPane: ActionPane(
        motion: const DrawerMotion(),
        extentRatio: 0.25,
        dragDismissible: false,
        children: [
          SlidableAction(
            label: 'Update',
            backgroundColor: Colors.orange,
            icon: Icons.archive,
            onPressed: (context) {
              List<bool> isSelected = [
                task.category == 'morning',
                task.category == 'dinner',
                task.category != 'morning' && task.category != 'dinner'
              ];

              // context.read<TodoPageBloc>().add(TodoPageUpdateEvent(task: task, todo: todo));
              showDialog(
                  context: context,
                  builder: (BuildContext dialogcontext) {
                    _controller.text = task.description;
                    return BlocProvider.value(
                        value: BlocProvider.of<TodoPageBloc>(context),
                        child:
                            StatefulBuilder(builder: (context, setDialogState) {
                          return AlertDialog(
                            title: Text('Update Todo'),
                            content: SingleChildScrollView(
                              child: Column(
                                children: [
                                  ToggleButtons(
                                    isSelected: isSelected,
                                    onPressed: (int index) {
                                      setDialogState(() {
                                        for (int buttonIndex = 0;
                                            buttonIndex < isSelected.length;
                                            buttonIndex++) {
                                          isSelected[buttonIndex] =
                                              buttonIndex == index;
                                        }
                                        if (index == 0) {
                                          task.category = 'morning';
                                        } else if (index == 1) {
                                          task.category = 'dinner';
                                        } else {
                                          task.category = 'other';
                                        }
                                      });
                                    },
                                    children: const [
                                      Padding(
                                        padding: EdgeInsets.symmetric(
                                            horizontal: 10),
                                        child: Text('Morning'),
                                      ),
                                      Padding(
                                        padding: EdgeInsets.symmetric(
                                            horizontal: 10),
                                        child: Text('Dinner'),
                                      ),
                                      Padding(
                                        padding: EdgeInsets.symmetric(
                                            horizontal: 10),
                                        child: Text('Other'),
                                      ),
                                    ],
                                  ),
                                  Row(
                                    children: [
                                      Expanded(
                                          child: TextField(
                                        controller: _controller,
                                        onChanged: (value) {},
                                      )),
                                      IconButton(
                                        icon: Icon(Icons.edit),
                                        onPressed: () {
                                          task.description = _controller.text;

                                          context
                                              .read<TodoPageBloc>()
                                              .onCloseCallback = () {
                                            Navigator.of(context).pop();
                                          };

                                          print(
                                              "task.description : ${task.description}, task.category : ${task.category}");
                                          print(
                                              "task type : ${task.runtimeType}");
                                          print("task : ${task.toString()}");
                                          context.read<TodoPageBloc>().add(
                                              TodoPageTaskUpdateEvent(
                                                  task: task,
                                                  todo: todo,
                                                  todos: todos));
                                        },
                                      )
                                    ],
                                  ),
                                  const Padding(
                                      padding:
                                          EdgeInsets.symmetric(vertical: 10)),
                                  TextButton.icon(
                                      onPressed: () {
                                        Navigator.of(context).pop();
                                      },
                                      icon: Icon(Icons.cancel),
                                      label: Text('취소'))
                                ],
                              ),
                            ),
                          );
                        }));
                  });
            },
          ),
        ],
      ),
      endActionPane: ActionPane(
        motion: const DrawerMotion(),
        extentRatio: 0.25,
        dragDismissible: false,
        dismissible: DismissiblePane(onDismissed: () {}),
        children: [
          SlidableAction(
            label: 'Delete',
            backgroundColor: Colors.red,
            icon: Icons.delete,
            onPressed: (context) async {
              context
                  .read<TodoPageBloc>()
                  .add(TodoPageDeleteEvent(task: task, todo: todo));
            },
          ),
        ],
      ),
      child: ListTile(
        title: Text(
          task.description,
          style: TextStyle(
            decoration: task.done ? TextDecoration.lineThrough : null,
          ),
        ),
        leading: Checkbox(
          value: task.done,
          onChanged: (bool? newValue) {
            setState(() {
              task.done = newValue ?? false;
            });
            context.read<TodoPageBloc>().add(
                TodoPageTaskUpdateEvent(task: task, todo: todo, todos: todos));
          },
        ),
        onTap: () {
          setState(() {
            task.done = !task.done;
          });
        },
      ),
    );
  }

  @override
  Widget build(BuildContext context) {
    return SafeArea(
      child: BlocBuilder<TodoPageBloc, TodoState>(
        builder: (context, state) {
          if (state is TodoInitState || state is TodoDownloadedState) {
            return SizedBox(
                height: MediaQuery.of(context).size.height,
                width: MediaQuery.of(context).size.width,
                child: const Column(
                  mainAxisAlignment: MainAxisAlignment.center,
                  crossAxisAlignment: CrossAxisAlignment.center,
                  children: [
                    SpinKitThreeInOut(
                      color: Color(0xffd86a04),
                      size: 50.0,
                      duration: Duration(seconds: 2),
                    )
                  ],
                ));
          } else if (state is TodoLoadedState) {
            return SingleChildScrollView(
                child: Column(
              mainAxisSize: MainAxisSize.min,
              children: [
                _calendar(state.todos),
                ElevatedButton.icon(
                  onPressed: () {
                    Navigator.of(context).push(MaterialPageRoute(
                        builder: (context) => const TodoAddPage()));
                  },
                  icon: const Icon(Icons.add, color: Color(0xffd86a04)),
                  label: const Text(
                    "Todo Add",
                    style: TextStyle(color: Color(0xffd86a04)),
                  ),
                  style: ElevatedButton.styleFrom(
                      foregroundColor: const Color(0xffffecda),
                      backgroundColor: const Color(0xffffecda)),
                ),
                _todoList(state.todos),
              ],
            ));
          } else if (state is TodoDeletedState) {
            return Column(
              mainAxisSize: MainAxisSize.max,
              children: [
                _calendar(state.todos),
                Text("else"),
                ElevatedButton.icon(
                  onPressed: () {
                    Navigator.of(context).push(MaterialPageRoute(
                        builder: (context) => const TodoAddPage()));
                  },
                  icon: const Icon(Icons.add, color: Color(0xffd86a04)),
                  label: const Text(
                    "Todo Add",
                    style: TextStyle(color: Color(0xffd86a04)),
                  ),
                  style: ElevatedButton.styleFrom(
                      foregroundColor: const Color(0xffffecda),
                      backgroundColor: const Color(0xffffecda)),
                ),
                _todoList(state.todos),
              ],
            );
          } else {
            return Column(
              children: [
                _calendar(state.todos),
                ElevatedButton.icon(
                  onPressed: () {
                    Navigator.of(context).push(MaterialPageRoute(
                        builder: (context) => const TodoAddPage()));
                  },
                  icon: const Icon(Icons.add, color: Color(0xffd86a04)),
                  label: const Text(
                    "Todo Add",
                    style: TextStyle(color: Color(0xffd86a04)),
                  ),
                  style: ElevatedButton.styleFrom(
                      foregroundColor: const Color(0xffffecda),
                      backgroundColor: const Color(0xffffecda)),
                )
              ],
            );
          }
        },
      ),
    );
  }
}
