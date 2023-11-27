import 'package:beautyminder/dto/vision_response_dto.dart';
import 'package:dio/dio.dart';
import 'package:file_picker/file_picker.dart';
import '../config.dart';

class OCRService {
  static final Dio client = Dio(BaseOptions(baseUrl: Config.apiURL));
  static String accessToken = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJpc3MiOiJiZWF1dHltaW5kZXIiLCJpYXQiOjE3MDA1NTA3MjUsImV4cCI6MTcwMTc2MDMyNSwic3ViIjoidG9rZW5AdGVzdCIsImlkIjoiNjU1MGFmZWYxYWI2ZDU4YjNmMTVmZTFjIn0.MESeOCDgBOPiXj9Zn-UiFqSbN0Oo30cEibwk__7IZEo";
  static String refreshToken = 'eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJpc3MiOiJiZWF1dHltaW5kZXIiLCJpYXQiOjE3MDA1NTA3MjUsImV4cCI6MTcwMjM2NTEyNSwic3ViIjoidG9rZW5AdGVzdCIsImlkIjoiNjU1MGFmZWYxYWI2ZDU4YjNmMTVmZTFjIn0.Pl1s8CyrVYDeBor4gtD4i6ibt1CI0tDVU9bipqP5ozI';

  // 엑세스 토큰 설정 (필요한 경우)
  static void setAccessToken() {
    client.options.headers['Authorization'] = 'Bearer $accessToken';
  }

  // 이미지 선택 및 업로드 함수
  static Future<dynamic> selectAndUploadImage(PlatformFile file) async {
    setAccessToken();
    final url = Uri.http(Config.apiURL, Config.ocrAPI).toString();

    // PlatformFile에서 MultipartFile 생성
    MultipartFile multipartFile = MultipartFile.fromBytes(
      file.bytes!,
      filename: file.name,
    );

    // FormData 생성
    FormData formData = FormData.fromMap({
      'image': multipartFile,
    });

    try {
      // 서버에 업로드
      var response = await client.post(url, data: formData);
      if (response.statusCode == 200) {


        // OCR 결과 반환
        return response.data;
      } else {
        throw Exception('Failed to upload image for OCR: ${response.statusMessage}');
      }
    } catch (e) {
      throw Exception('Error uploading image for OCR: $e');
    }
  }
}