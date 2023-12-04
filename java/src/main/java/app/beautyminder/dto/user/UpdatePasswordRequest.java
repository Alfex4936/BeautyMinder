package app.beautyminder.dto.user;

public record UpdatePasswordRequest(String currentPassword, String newPassword) {
}