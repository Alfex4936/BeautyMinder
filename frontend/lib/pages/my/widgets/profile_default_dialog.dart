import 'dart:io';

import 'package:flutter/material.dart';

import 'package:image_picker/image_picker.dart';


class ProfileDefaultDialog extends StatefulWidget {
  final void Function(XFile?) onImageChanged;

  const ProfileDefaultDialog({
    Key? key,
    this.icon,
    required this.onBarrierTap,
    required this.title,
    this.caption,
    required this.buttons,
    required this.onImageChanged,
  }) : super(key: key);

  final Widget? icon;
  final String title;
  final String? caption;
  final Function() onBarrierTap;
  final List<ProfileDefaultDialogButton> buttons;

  @override
  State<ProfileDefaultDialog> createState() => _ProfileDefaultDialogState();
}

class _ProfileDefaultDialogState extends State<ProfileDefaultDialog> {
  TextEditingController cosmeticNameController = TextEditingController();

  String? imagePath;
  XFile? _image;

  Future<void> _pickImage() async {
    final ImagePicker picker = ImagePicker();
    final XFile? image = await picker.pickImage(source: ImageSource.gallery);

    if (image != null) {
      setState(() {
        imagePath = image.path;
        if (imagePath != '') _image = XFile(imagePath!);
        widget.onImageChanged(_image); // Notify the callback
      });
    }
  }

  Future<void> _takePhoto() async {
    final ImagePicker picker = ImagePicker();
    final XFile? photo = await picker.pickImage(source: ImageSource.camera);

    if (photo != null) {
      setState(() {
        imagePath = photo.path;
        if (imagePath != '') _image = XFile(imagePath!);
        widget.onImageChanged(_image); // Notify the callback
      });
    }
  }

  @override
  Widget build(BuildContext context) {
    return GestureDetector(
      onTap: () {
        widget.onBarrierTap();
      },
      child: Material(
        color: Colors.transparent,
        child: Center(
          child: GestureDetector(
            onTap: () {},
            child: Container(
              constraints: const BoxConstraints(maxWidth: (305)),
              decoration: BoxDecoration(
                  color: Colors.white,
                  borderRadius: BorderRadius.circular((10)),
                  boxShadow: [
                    BoxShadow(
                      offset: const Offset(0, 4),
                      blurRadius: 4,
                      spreadRadius: 0,
                      color: Colors.black.withOpacity(0.08),
                    )
                  ]),
              child: Column(
                mainAxisSize: MainAxisSize.min,
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  const SizedBox(height: 25),
                  const Row(),
                  Padding(
                    padding: const EdgeInsets.symmetric(horizontal: 20),
                    child: Column(
                      mainAxisSize: MainAxisSize.min,
                      crossAxisAlignment: CrossAxisAlignment.start,
                      children: [
                        const Row(),
                        Text(
                          widget.title,
                          style: const TextStyle(
                            fontSize: (19),
                            fontWeight: FontWeight.w600,
                          ),
                        ),
                        const SizedBox(height: 15),
                        Center(
                          child: (imagePath == null)
                              ? Container(
                                  width: 200,
                                  height: 200,
                                  decoration: BoxDecoration(
                                    borderRadius: BorderRadius.circular(15),
                                    border: Border.all(
                                      color: Colors.grey,
                                    ),
                                  ),
                                  child: const Center(
                                    child: Text('선택된 이미지가 없습니다.'),
                                  ),
                                )
                              : Image.file(
                                  File(imagePath!),
                                  width: 200,
                                  height: 200,
                                ),
                        ),
                        Row(
                          mainAxisAlignment: MainAxisAlignment.spaceEvenly,
                          children: [
                            ElevatedButton(
                              onPressed: _pickImage,
                              child: const Text('앨범에서 선택'),
                            ),
                            ElevatedButton(
                              onPressed: _takePhoto,
                              child: const Text('카메라로 촬영'),
                            )
                          ],
                        ),
                        if (widget.caption != null) ...[
                          const SizedBox(height: 20),
                          Text(
                            widget.caption ?? '',
                            textAlign: TextAlign.left,
                            style: const TextStyle(
                              height: 1.3,
                              color: Colors.grey,
                            ),
                          ),
                        ]
                      ],
                    ),
                  ),
                  const SizedBox(height: 25),
                  Padding(
                    padding: const EdgeInsets.symmetric(horizontal: 20),
                    child: Row(
                      children: [
                        Expanded(
                          child: widget.buttons[0],
                        ),
                        if (widget.buttons.length == 2) ...[
                          const SizedBox(width: 10),
                          Expanded(
                            child: widget.buttons[1],
                          ),
                        ]
                      ],
                    ),
                  ),
                  const SizedBox(height: 15),
                ],
              ),
            ),
          ),
        ),
      ),
    );
  }
}

class ProfileDefaultDialogButton extends StatelessWidget {
  const ProfileDefaultDialogButton(
      {Key? key,
      required this.onTap,
      required this.text,
      required this.backgroundColor,
      required this.textColor})
      : super(key: key);
  final Function() onTap;
  final String text;
  final Color backgroundColor;
  final Color textColor;

  @override
  Widget build(BuildContext context) {
    return GestureDetector(
      onTap: onTap,
      child: Container(
        height: (50),
        padding: const EdgeInsets.symmetric(horizontal: (20)),
        decoration: BoxDecoration(
          color: backgroundColor,
          borderRadius: BorderRadius.circular((10)),
        ),
        alignment: Alignment.center,
        child: Text(
          text,
          style: TextStyle(
            color: textColor,
            fontWeight: FontWeight.w500,
          ),
        ),
      ),
    );
  }
}
