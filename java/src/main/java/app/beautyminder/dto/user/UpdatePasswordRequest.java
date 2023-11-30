package app.beautyminder.dto.user;

import lombok.Getter;

public record UpdatePasswordRequest(String currentPassword, String newPassword) {}