import 'package:flutter/material.dart';

import '/dto/cosmetic_model.dart';
import '/dto/cosmetic_expiry_model.dart';

import '/services/expiry_service.dart';
import '/services/api_service.dart';
import '/services/search_service.dart';

class CosmeticExpiryPage extends StatefulWidget {
  @override
  _CosmeticExpiryPageState createState() => _CosmeticExpiryPageState();
}

class _CosmeticExpiryPageState extends State<CosmeticExpiryPage> {
  List<CosmeticExpiry> cosmetics = [];

  @override
  void initState() {
    super.initState();
    _loadExpiryData();
  }

  void _loadExpiryData() async {
    final result = await APIService.getUserProfile();
    final userId = result.isSuccess ? result.value?.id ?? '-1' : '-1';  // 사용자 프로필을 가져오지 못하면 기본값 '-1'을 사용
    final expiryData = await ExpiryService.getAllExpiriesByUserId(userId);
    setState(() {
      cosmetics = expiryData;
    });
  }

  void _deleteExpiry(String userId, String expiryId, int index) async {
    try {
      await ExpiryService.deleteExpiryByUserIdAndExpiryId(userId, expiryId);
      setState(() {
        cosmetics.removeAt(index); // 로컬 목록에서 해당 항목 제거
      });
    } catch (e) {
      // 에러 처리
      print("Error deleting expiry: $e");
    }
  }


  void _addCosmetic() async {
    final result = await APIService.getUserProfile();
    final userId = result.isSuccess ? result.value?.id ?? '-1' : '-1';  // 사용자 프로필을 가져오지 못하면 기본값 '-1'을 사용

    final Cosmetic? selectedCosmetic = await showDialog<Cosmetic>(
      context: context,
      builder: (context) => CosmeticSearchWidget(),
    );
    if (selectedCosmetic != null) {
      final List<dynamic>? expiryInfo = await showDialog<List<dynamic>>(
        context: context,
        builder: (context) => ExpiryInputDialog(cosmetic: selectedCosmetic),
      );
      if (expiryInfo != null) {
        final bool isOpened = expiryInfo[0] as bool;
        final DateTime expiryDate = expiryInfo[1] as DateTime;

        final CosmeticExpiry newExpiry = CosmeticExpiry(
          productName: selectedCosmetic.name,
          expiryDate: expiryDate,
          isExpiryRecognized: isOpened,
          createdAt: DateTime.now(),
          updatedAt: DateTime.now(),
          userId: userId,
          // 다른 필드들도 여기에 추가
        );
        final CosmeticExpiry addedExpiry = await ExpiryService.createCosmeticExpiry(newExpiry);
        setState(() {
          cosmetics.add(addedExpiry);
        });
      }
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: Text('Expiry Management'),
        actions: [
          IconButton(
            icon: Icon(Icons.add),
            onPressed: _addCosmetic,
          ),
        ],
      ),
      body: GridView.builder(
        padding: EdgeInsets.all(8),
        gridDelegate: SliverGridDelegateWithFixedCrossAxisCount(
          crossAxisCount: 2, // 한 줄에 표시할 아이템 수
          crossAxisSpacing: 8, // 가로 간격
          mainAxisSpacing: 8, // 세로 간격
          childAspectRatio: 0.8, // 아이템의 가로 세로 비율
        ),
        itemCount: cosmetics.length,
        itemBuilder: (context, index) {
          final cosmetic = cosmetics[index];
          final daysLeft = cosmetic.expiryDate.difference(DateTime.now()).inDays;

          return Card(
              clipBehavior: Clip.antiAlias,
              child: Stack(
                  alignment: Alignment.center,
                  children: [
                    Positioned(
                      top: 10,
                      child: cosmetic.imageUrl != null
                          ? Image.network(cosmetic.imageUrl!, width: 128, height: 128, fit: BoxFit.cover)
                          : Icon(Icons.image, size: 128), // 이미지가 없는 경우 아이콘 표시
                    ),
                    Positioned(
                      bottom: 30,
                      child: Text(
                        cosmetic.productName,
                        style: TextStyle(fontSize: 16),
                      ),
                    ),
                    Positioned(
                      bottom: 10,
                      child: Text(
                        'D-${daysLeft}',
                        style: TextStyle(fontSize: 20, fontWeight: FontWeight.bold),
                      ),
                    ),
                    Positioned(
                        right: 4,
                        top: 4,
                        child: IconButton(
                            icon: Icon(Icons.delete),
                            onPressed: () {
                              if (cosmetic.userId != null && cosmetic.id != null) {
                                _deleteExpiry(cosmetic.userId!, cosmetic.id!, index);
                              } else {
                                print("Invalid data");
                              }}
                        )
                    )])
          );
        },
      ),

    );
  }
}

class CosmeticSearchWidget extends StatefulWidget {
  @override
  _CosmeticSearchWidgetState createState() => _CosmeticSearchWidgetState();
}

class _CosmeticSearchWidgetState extends State<CosmeticSearchWidget> {
  List<Cosmetic> cosmetics = [];
  String query = '';

  void _search() async {
    if (query.isNotEmpty) {
      try {
        // SearchService를 사용하여 서버에서 화장품을 검색
        cosmetics = await SearchService.searchCosmeticsByName(query);
        setState(() {
          // 검색 결과로 UI를 업데이트
        });
      } catch (e) {
        print('Search error: $e');
        // 필요하면 setState를 사용하여 UI에 에러 메시지를 표시
      }
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: TextField(
          onChanged: (text) {
            query = text;
          },
          onSubmitted: (text) {
            _search();
          },
          decoration: InputDecoration(
            hintText: 'Search cosmetics...',
          ),
        ),
        actions: [
          IconButton(
            icon: Icon(Icons.search),
            onPressed: _search,
          ),
        ],
      ),
      body: ListView.builder(
        itemCount: cosmetics.length,
        itemBuilder: (context, index) {
          final cosmetic = cosmetics[index];
          return ListTile(
            title: Text(cosmetic.name),
            onTap: () {
              Navigator.of(context).pop(cosmetic);
            },
          );
        },
      ),
    );
  }
}

class ExpiryInputDialog extends StatefulWidget {
  final Cosmetic cosmetic;

  ExpiryInputDialog({required this.cosmetic});

  @override
  _ExpiryInputDialogState createState() => _ExpiryInputDialogState();
}

class _ExpiryInputDialogState extends State<ExpiryInputDialog> {
  bool isOpened = false;
  DateTime expiryDate = DateTime.now().add(Duration(days: 365));

  Future<void> _selectDate(BuildContext context) async {
    final DateTime? picked = await showDatePicker(
      context: context,
      initialDate: expiryDate,
      firstDate: DateTime(2000),
      lastDate: DateTime(2101),
    );
    if (picked != null && picked != expiryDate)
      setState(() {
        expiryDate = picked;
      });
  }

  @override
  Widget build(BuildContext context) {
    return AlertDialog(
      title: Text('Enter expiry info for ${widget.cosmetic.name}'),
      content: Column(
        mainAxisSize: MainAxisSize.min,
        children: [
          SwitchListTile(
            title: Text('Opened'),
            value: isOpened,
            onChanged: (bool value) {
              setState(() {
                isOpened = value;
              });
            },
          ),
          ListTile(
            title: Text("${expiryDate.toLocal()}"),
            trailing: Icon(Icons.calendar_today),
            onTap: () {
              _selectDate(context);
            },
          ),
        ],
      ),
      actions: [
        TextButton(
          onPressed: () {
            Navigator.of(context).pop([isOpened, expiryDate]);
          },
          child: Text('Submit'),
        ),
      ],
    );
  }

}

class Result<T> {
  final T? value;
  final String? error;

  Result.success(this.value) : error = null;
  Result.failure(this.error) : value = null;

  bool get isSuccess => error == null;
  bool get isFailure => !isSuccess;
}
