package org.example.expert.domain.user;

import org.example.expert.domain.common.exception.InvalidRequestException;
import org.example.expert.domain.user.dto.request.UserRoleChangeRequest;
import org.example.expert.domain.user.entity.User;
import org.example.expert.domain.user.enums.UserRole;
import org.example.expert.domain.user.repository.UserRepository;
import org.example.expert.domain.user.service.UserAdminService;
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
public class UserAdminServiceTest {
    @Mock
    private UserRepository userRepository;
    @InjectMocks
    private UserAdminService userAdminService;

    @Test
    public void changeUserRole_실행을_성공한다() {
        // given
        Long userId = 1L;
        UserRoleChangeRequest userRoleChangeRequest = new UserRoleChangeRequest("ADMIN");

        User user = new User("validEmail", "password", UserRole.USER);
        given(userRepository.findById(userId)).willReturn(Optional.of(user));

        // when
        userAdminService.changeUserRole(userId, userRoleChangeRequest);

        // then
        assertEquals(UserRole.ADMIN, user.getUserRole());
        verify(userRepository, times(1)).findById(userId);
    }

    @Test
    public void changeUserRole_예외_처리_실행한다() {
        // given
        long userId = 1L;
        UserRoleChangeRequest roleChangeRequest = new UserRoleChangeRequest("ADMIN");

        // when
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // then
        assertThrows(InvalidRequestException.class, () -> {
            userAdminService.changeUserRole(userId, roleChangeRequest);
        });
    }
}
