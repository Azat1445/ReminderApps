package org.example.reminderapp.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import org.example.reminderapp.config.CustomUserDetails;
import org.example.reminderapp.dto.request.UserProfileUpdateDto;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.RequestBody;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.reminderapp.dto.request.UserFilterDto;
import org.example.reminderapp.dto.response.UserProfileResponseDto;
import org.example.reminderapp.service.UserService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Users", description = "User management")
@RestController
@RequestMapping("api/users")
@RequiredArgsConstructor
@Slf4j
public class UserController {

    private final UserService userService;


    @GetMapping
    public ResponseEntity<Page<UserProfileResponseDto>> findAllUser(@ModelAttribute UserFilterDto filter,
                                                                    @PageableDefault(size = 20, sort = "createdAt",
                                                                    direction = Sort.Direction.DESC) Pageable pageable) {
        log.info("Fetching users with filter: {}, page: {}", filter, pageable);

        Page<UserProfileResponseDto> users = userService.findAllUser(filter, pageable);
        log.info("Found {} users", users.getTotalElements());

        return ResponseEntity.ok(users);
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserProfileResponseDto> findUserById(@PathVariable Long id) {
        log.info("Getting user by id {}", id);

        UserProfileResponseDto user = userService.findUserById(id);
        return ResponseEntity.ok(user);
    }

    @GetMapping("/username/{username}")
    public ResponseEntity<UserProfileResponseDto> findByUsername(@PathVariable String username) {
        UserProfileResponseDto user = userService.findByUsername(username);
        return ResponseEntity.ok(user);
    }


    @GetMapping("/profile")
    public ResponseEntity<UserProfileResponseDto> getCurrentUserProfiles(@AuthenticationPrincipal UserDetails userDetails) {

        Long userId = ((CustomUserDetails) userDetails).getId();
        log.info("Getting current user profile for user id {}", userId);

        UserProfileResponseDto user = userService.findUserById(userId);
        return ResponseEntity.ok(user);
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserProfileResponseDto> updateUser(@PathVariable Long id,
                                                             @AuthenticationPrincipal UserDetails userDetails,
                                                             @Valid @RequestBody UserProfileUpdateDto updateDto) {
        Long currenUserId = ((CustomUserDetails) userDetails).getId();
        log.info("Updating user: {} by user: {}, dto: {}", id, currenUserId, updateDto);

        UserProfileResponseDto updated = userService.updateUser(id, currenUserId, updateDto);
        log.info("User {} updated successfully", id);

        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id,
                                           @AuthenticationPrincipal UserDetails userDetails) {
       Long currentUserId = ((CustomUserDetails) userDetails).getId();
       log.info("Deleting user: {} by user: {}", id, currentUserId);

       userService.deleteUser(id, currentUserId);
       log.info("User {} deleted successfully", id);

       return ResponseEntity.noContent().build();
    }
}
