package com.example.userservice.service;

import com.example.userservice.dto.UserDto;
import com.example.userservice.entity.User;
import com.example.userservice.exception.ResourceNotFoundException;
import com.example.userservice.mapper.UserMapper;
import com.example.userservice.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserService Unit Tests")
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private UserService userService;

    private User user;
    private UserDto userDto;

    @BeforeEach
    void setUp() {
        user = new User("John Doe", "john@example.com", "encodedPassword");
        user.setId(1L);

        userDto = new UserDto();
        userDto.setId(1L);
        userDto.setName("John Doe");
        userDto.setEmail("john@example.com");
        userDto.setCreatedAt(LocalDateTime.now());
    }

    @Test
    @DisplayName("createUser - should save and return user")
    void createUser_ShouldSaveAndReturnUser() {
        given(userRepository.save(any(User.class))).willReturn(user);

        User result = userService.createUser(user);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getEmail()).isEqualTo("john@example.com");
        then(userRepository).should(times(1)).save(user);
    }

    @Test
    @DisplayName("getUserById - should return UserDto when user exists")
    void getUserById_WhenUserExists_ShouldReturnUserDto() {
        given(userRepository.findById(1L)).willReturn(Optional.of(user));
        given(userMapper.userToUserDto(user)).willReturn(userDto);

        UserDto result = userService.getUserById(1L);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getEmail()).isEqualTo("john@example.com");
        assertThat(result.getCreatedAt()).isNotNull();
    }

    @Test
    @DisplayName("getUserById - should throw ResourceNotFoundException when user does not exist")
    void getUserById_WhenUserNotFound_ShouldThrowResourceNotFoundException() {
        given(userRepository.findById(99L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getUserById(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("User")
                .hasMessageContaining("99");
    }

    @Test
    @DisplayName("getAllUsers - should return list of UserDtos")
    void getAllUsers_ShouldReturnAllUsers() {
        User user2 = new User("Jane Doe", "jane@example.com", "encodedPassword");
        user2.setId(2L);
        given(userRepository.findAll()).willReturn(List.of(user, user2));
        given(userMapper.userToUserDto(user)).willReturn(userDto);
        UserDto userDto2 = new UserDto();
        userDto2.setId(2L);
        given(userMapper.userToUserDto(user2)).willReturn(userDto2);

        List<UserDto> result = userService.getAllUsers();

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getId()).isEqualTo(1L);
        assertThat(result.get(1).getId()).isEqualTo(2L);
    }

    @Test
    @DisplayName("getAllUsers - should return empty list when no users exist")
    void getAllUsers_WhenNoUsers_ShouldReturnEmptyList() {
        given(userRepository.findAll()).willReturn(List.of());

        List<UserDto> result = userService.getAllUsers();

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("deleteUser - should throw ResourceNotFoundException when user does not exist")
    void deleteUser_WhenUserNotFound_ShouldThrowException() {
        given(userRepository.existsById(99L)).willReturn(false);

        assertThatThrownBy(() -> userService.deleteUser(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("User")
                .hasMessageContaining("99");

        then(userRepository).should(times(0)).deleteById(any());
    }

    @Test
    @DisplayName("deleteUser - should delete user when user exists")
    void deleteUser_WhenUserExists_ShouldDeleteSuccessfully() {
        given(userRepository.existsById(1L)).willReturn(true);

        userService.deleteUser(1L);

        then(userRepository).should(times(1)).deleteById(1L);
    }
}
