package org.example.expert.domain.user;
import org.example.expert.config.PasswordEncoder;
import org.example.expert.domain.common.exception.InvalidRequestException;
import org.example.expert.domain.user.dto.request.UserChangePasswordRequest;
import org.example.expert.domain.user.dto.response.UserResponse;
import org.example.expert.domain.user.entity.User;
import org.example.expert.domain.user.enums.UserRole;
import org.example.expert.domain.user.repository.UserRepository;
import org.example.expert.domain.user.service.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {
    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @InjectMocks
    private UserService userService;

    @Test
    void getUser_성공한다() {
        // given
        long userId = 1L;
        User user = new User("email", "password", UserRole.USER);
        user.setId(userId);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        // when
        UserResponse userResponse = userService.getUser(userId);

        // then
        assertNotNull(userResponse);
        assertEquals(userId, userResponse.getId());  // 이제 userResponse.getId()는 1이어야 합니다.
        assertEquals("email", userResponse.getEmail());
    }

    @Test
    void getUser_예외_처리한다() {
        // given
        long userId = 1L;
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // when & then
        assertThrows(InvalidRequestException.class, () -> userService.getUser(userId));
    }

    @Test
    void changePassword_User가_존재하지_않을_때_예외_처리한다() {
        // given
        long userId = 1L;
        UserChangePasswordRequest request = new UserChangePasswordRequest("oldPassword1", "newPassword1");

        given(userRepository.findById(userId)).willReturn(Optional.empty());

        // when & then
        assertThrows(InvalidRequestException.class, () -> userService.changePassword(userId, request));
        verifyNoMoreInteractions(userRepository, passwordEncoder);
    }

    @Test
    void changePassword_새_비밀번호가_기존_비밀번호와_같은_때_예외_처리한다() {
        // given
        long userId = 1L;
        User user = new User("email", "newPassword1", UserRole.USER);
        UserChangePasswordRequest request = new UserChangePasswordRequest("newPassword1", "newPassword1");

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        given(passwordEncoder.matches(request.getNewPassword(), user.getPassword())).willReturn(true);

        // when & then
        assertThrows(InvalidRequestException.class, () -> userService.changePassword(userId, request));
    }

    @Test
    void changePassword_비밀번호가_잘못되었을_때_예외_처리한다() {
        // given
        long userId = 1L;
        User user = new User("email", "oldPassword1", UserRole.USER);
        UserChangePasswordRequest request = new UserChangePasswordRequest("oldPassword1", "newPassword1");

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("newPassword1", user.getPassword())).thenReturn(false);
        when(passwordEncoder.matches("oldPassword1", user.getPassword())).thenReturn(false);

        // when & then
        assertThrows(InvalidRequestException.class, () -> userService.changePassword(userId, request));
    }

    @Test
    void changePassword_성공한다() {
        // given
        long userId = 1L;
        User user = new User("email", "encodedOldPassword1", UserRole.USER);
        user.setId(userId);
        UserChangePasswordRequest request = new UserChangePasswordRequest("oldPassword1", "NewPassword1");

        // Mock repository and encoder behavior
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("NewPassword1", "encodedOldPassword1")).thenReturn(false); // 새 비밀번호는 기존 비밀번호와 다름
        when(passwordEncoder.matches("oldPassword1", "encodedOldPassword1")).thenReturn(true); // 기존 비밀번호 일치
        when(passwordEncoder.encode("NewPassword1")).thenReturn("encodedNewPassword1");

        // when
        userService.changePassword(userId, request);

        // then
        assertNotNull(request.getNewPassword());
    }

}