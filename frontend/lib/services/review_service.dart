import 'dart:developer';
import 'dart:convert';

import 'package:dio/dio.dart';
import 'package:file_picker/file_picker.dart';
import 'package:flutter/services.dart';

import '../config.dart';

import '../dto/review_request_model.dart';
import '../dto/review_response_model.dart';

import 'package:http_parser/http_parser.dart';
import 'package:mime/mime.dart';


class ReviewService {
  static final Dio client = Dio(BaseOptions(baseUrl: Config.apiURL));


  static Future<List<PlatformFile>> getImages() async {
    List<PlatformFile> paths = List.empty();
    try {
      paths = (await FilePicker.platform.pickFiles(
        type: FileType.custom,
        allowMultiple: false,
        allowedExtensions: ['png', 'jpg', 'jpeg', 'heic'],
      ))
          !.files;
    } on PlatformException catch (e) {
      log('Unsupported operation' + e.toString());
    } catch (e) {
      log(e.toString());
    }
    return paths;
  }


  // 리뷰 추가 함수
  static Future<ReviewResponse> addReview(
      ReviewRequest reviewRequest, List<PlatformFile> imageFiles) async {
    final url = '/review';

    // Convert the PlatformFile objects to MultipartFile objects
    List<MultipartFile> multipartImageList = imageFiles.map((file) {
      final MediaType contentType = MediaType.parse(lookupMimeType(file.name) ?? 'application/octet-stream');
      return MultipartFile.fromBytes(
        file.bytes!,
        filename: file.name,
        contentType: contentType,
      );
    }).toList();

// Convert the review JSON into a string
    String reviewJson = jsonEncode({
      'content': reviewRequest.content,
      'rating': reviewRequest.rating,
      'cosmeticId': reviewRequest.cosmeticId,
      'userId': reviewRequest.userId,
    });

    // Create a MultipartFile from the JSON string
    MultipartFile reviewMultipart = MultipartFile.fromString(
      reviewJson,
      contentType: MediaType('application', 'json'),
    );

// Add the JSON MultipartFile to the FormData
    var formData = FormData.fromMap({
      'review': reviewMultipart,
      // Here we send the review as a MultipartFile
      'images': multipartImageList,
      // The images are sent as usual
    });
    print('FormData: $formData');

    // Dio 클라이언트를 사용하여 서버로 요청을 보내고 응답을 받습니다.
    var response = await client.post(url, data: formData);
    if (response.statusCode == 201) {
      // 성공적으로 리뷰가 생성되었을 때의 처리
      return ReviewResponse.fromJson(response.data);
    } else {
      // 서버에서 오류 응답이 왔을 때의 처리
      throw Exception('Failed to add review: ${response.statusMessage}');
    }
  }

  // 리뷰 조회 함수
  static Future<List<ReviewResponse>> getReviewsForCosmetic(
      String cosmeticId) async {
    final url = '/review/$cosmeticId';
    var response = await client.get(url);
    if (response.statusCode == 200) {
      return (response.data as List)
          .map((e) => ReviewResponse.fromJson(e))
          .toList();
    } else {
      throw Exception('Failed to load reviews');
    }
  }

  // 리뷰 삭제 함수
  static Future<void> deleteReview(String reviewId) async {
    final url = '/review/$reviewId';
    var response = await client.delete(url);
    if (response.statusCode != 200) {
      throw Exception('Failed to delete review');
    }
  }

  // 리뷰 수정 함수
  static Future<ReviewResponse> updateReview(String reviewId,
      ReviewRequest reviewRequest, List<PlatformFile> imageFiles) async {
    final url = '/review/$reviewId';
    var formData = FormData();

    // 리뷰 텍스트 데이터를 JSON 문자열로 변환
    String reviewJson = jsonEncode(reviewRequest.toJson());

    // 이미지 파일을 MultipartFile 객체로 변환하고 FormData에 추가
    List<MultipartFile> multipartImageList = imageFiles.map((file) {
      final MediaType contentType = MediaType.parse(lookupMimeType(file.name) ?? 'application/octet-stream');
      return MultipartFile.fromBytes(
        file.bytes!,
        filename: file.name,
        contentType: contentType,
      );
    }).toList();

    // 리뷰 JSON과 이미지 파일을 FormData에 추가
    formData.files.add(MapEntry(
      'review', // 서버에서 기대하는 필드 이름
      MultipartFile.fromString(
        reviewJson,
        filename: 'review.json', // JSON 파일 이름 지정
        contentType: MediaType('application', 'json'),
      ),
    ));
    formData.files.addAll(multipartImageList.map((file) => MapEntry('images', file)));

    // Dio 클라이언트를 사용하여 서버로 요청을 보내고 응답을 받습니다.
    var response = await client.put(url, data: formData);
    if (response.statusCode == 200) {
      return ReviewResponse.fromJson(response.data);
    } else {
      throw Exception('Failed to update review: ${response.statusMessage}');
    }
  }

  // 이미지 로드 함수
  static Future<String> loadImage(String filename) async {
    final parameters={
      'filename' : '$filename',
    };
    final url = Uri.http(Config.apiURL, Config.AllReviewAPI, parameters).toString();

    // final url = '${Config.apiURL}/review/image?filename=$filename';
    try {
      var response = await client.get(url);
      if (response.statusCode == 200) {
        // 서버에서 이미지의 URL
        return response.data.toString(); // 이는 이미지의 URL
      } else {
        throw Exception('Failed to load image');
      }
    } catch (e) {
      throw Exception('Error loading image: $e');
    }
  }

}
