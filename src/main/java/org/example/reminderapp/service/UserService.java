package org.example.reminderapp.service;


import lombok.extern.slf4j.Slf4j;
import org.example.reminderapp.dto.request.UserCreateDto;
import org.example.reminderapp.dto.request.UserFilterDto;
import org.example.reminderapp.dto.request.UserProfileUpdateDto;
import org.example.reminderapp.entity.User;
import org.springframework.security.access.AccessDeniedException;
import org.example.reminderapp.exception.ResourceNotFoundException;
import org.example.reminderapp.mapper.UserMapperDto;
import org.example.reminderapp.repository.specification.UserSpecification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.example.reminderapp.dto.response.UserProfileResponseDto;
import org.example.reminderapp.repository.UserRepository;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final UserMapperDto userMapperDto;
    private final PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    public Page<UserProfileResponseDto> findAllUser(UserFilterDto filter, Pageable pageable) {
        log.info("Fetching users with filter: {}, page: {}", filter, pageable);

        Specification<User> specification = UserSpecification.withFilters(filter);
        Page<User> users = userRepository.findAll(specification, pageable);

        log.info("Found {} users", users.getTotalElements());
        return users.map(userMapperDto::toDto);
    }

    @Transactional(readOnly = true)
    public UserProfileResponseDto findUserById(Long id) {
        log.info("Fetching user with id: {}", id);

        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));

        return userMapperDto.toDto(user);
    }

    @Transactional(readOnly = true)
    public UserProfileResponseDto findByUsername(String username) {
        log.info("Fetching user with username: {}", username);

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with username: " + username));

        return userMapperDto.toDto(user);
    }

    @Transactional
    public UserProfileResponseDto updateUser(Long id, Long currentUserId, UserProfileUpdateDto userDto) {
        log.info("Updating user: {} by user: {}", id, currentUserId);

        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));

        if (!user.getId().equals(currentUserId)) {
            log.warn("User {} attempted to update user {} profile", currentUserId, id);
            throw new AccessDeniedException("You can only update your own profile");
        }

        userMapperDto.updateEntity(userDto, user);
        User updated = userRepository.save(user);
        log.info("User {} updated successfully", id);

        return userMapperDto.toDto(updated);
    }

    @Transactional
    public void deleteUser(Long id, Long currentUserId) {
        log.info("Deleting user: {} by user: {}", id, currentUserId);

        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));

        if (!user.getId().equals(currentUserId)) {
            log.warn("User {} attempted to delete user {}", currentUserId, id);
            throw new AccessDeniedException("You can only delete your own account");
        }

        userRepository.deleteById(id);
        log.info("User {} deleted successfully", id);
    }
}
