package org.example.reminderapp.service;

import org.example.reminderapp.dto.response.UserProfileResponseDto;
import org.example.reminderapp.dto.request.UserProfileUpdateDto;
import org.example.reminderapp.entity.User;
import org.example.reminderapp.exception.ResourceNotFoundException;
import org.example.reminderapp.mapper.UserMapperDto;
import org.example.reminderapp.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapperDto userMapperDto;

    @InjectMocks
    private UserService userService;

    private User testUser;
    private UserProfileResponseDto responseDto;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        testUser.setFirstname("John");
        testUser.setLastname("Doe");

        responseDto = new UserProfileResponseDto();
        responseDto.setId(1L);
        responseDto.setUsername("testuser");
        responseDto.setEmail("test@example.com");
    }

    @Test
    void findUserByIdSuccess() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userMapperDto.toDto(testUser)).thenReturn(responseDto);

        UserProfileResponseDto result = userService.findUserById(1L);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        verify(userRepository).findById(1L);
    }

    @Test
    void findUserByIdNotFound() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.findUserById(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("User not found with id: 999");
    }

    @Test
    void updateUserSuccess() {
        UserProfileUpdateDto updateDto = new UserProfileUpdateDto();
        updateDto.setFirstname("Jane");

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        when(userMapperDto.toDto(testUser)).thenReturn(responseDto);

        UserProfileResponseDto result = userService.updateUser(1L, 1L, updateDto);

        assertThat(result).isNotNull();
        verify(userMapperDto).updateEntity(updateDto, testUser);
        verify(userRepository).save(testUser);
    }

    @Test
    void updateUserAccessDenied() {
        UserProfileUpdateDto updateDto = new UserProfileUpdateDto();

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        assertThatThrownBy(() -> userService.updateUser(1L, 2L, updateDto))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("You can only update your own profile");

        verify(userRepository, never()).save(any());
    }

    @Test
    void deleteUserSuccess() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        userService.deleteUser(1L, 1L);

        verify(userRepository).deleteById(1L);
    }
}
