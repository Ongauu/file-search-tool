package com.crimson.fileSearch.controller;


import com.crimson.fileSearch.dto.request.ChangePasswordRequest;
import com.crimson.fileSearch.dto.response.MessageResponse;
import com.crimson.fileSearch.dto.response.UserResponse;
import com.crimson.fileSearch.security.UserDetailsImpl;
import com.crimson.fileSearch.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor

public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    public ResponseEntity<UserResponse> getCurrentUser(@AuthenticationPrincipal UserDetailsImpl user) {
        return ResponseEntity.ok(userService.getCurrentUser(user.getUsername()));
    }

    @PutMapping("/me/password")
    public ResponseEntity<MessageResponse> changePassword(
            @AuthenticationPrincipal UserDetailsImpl user,
            @Valid @RequestBody ChangePasswordRequest request) {
        userService.changePassword(user.getUsername(), request);
        return ResponseEntity.ok(new MessageResponse("Password updated successfully. Please log in again."));
    }
}
