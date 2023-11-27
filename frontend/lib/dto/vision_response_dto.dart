class VisionResponseDTO {
  final String data;
  final String message;

  VisionResponseDTO({required this.data, required this.message});

  factory VisionResponseDTO.fromJson(Map<String, dynamic> json) {
    return VisionResponseDTO(
      data: json['data'] as String,
      message: json['message'] as String,
    );
  }

  Map<String, dynamic> toJson() {
    return {
      'data': data,
      'message': message,
    };
  }
}
