package org.example.expert.domain.auth.service;

import org.example.expert.config.JwtUtil;
import org.example.expert.config.PasswordEncoder;
import org.example.expert.domain.auth.dto.request.SigninRequest;
import org.example.expert.domain.auth.dto.request.SignupRequest;
import org.example.expert.domain.auth.dto.response.SigninResponse;
import org.example.expert.domain.auth.dto.response.SignupResponse;
import org.example.expert.domain.auth.exception.AuthException;
import org.example.expert.domain.common.exception.InvalidRequestException;
import org.example.expert.domain.user.entity.User;
import org.example.expert.domain.user.enums.UserRole;
import org.example.expert.domain.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
public class AuthServiceTest {
    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private JwtUtil jwtUtil;
    @InjectMocks
    private AuthService authService;

    @Test
    public void 회원가입을_하려고_하는데_이미_존재하는_이메일이라서_실패한다() {
        // given
        SignupRequest request = new SignupRequest("email", "password", "USER");

        given(userRepository.existsByEmail(anyString())).willReturn(true);

        // when
        InvalidRequestException exception = assertThrows(InvalidRequestException.class, () -> {
            authService.signup(request);
        });

        // then
        assertEquals("이미 존재하는 이메일입니다.", exception.getMessage());
    }

    @Test
    public void 회원가입에_성공한다() {
        // given
        SignupRequest request = new SignupRequest("email", "password", "USER");

        String encodedPassword = "encodedPassword";  // 인코딩된 패스워드
        given(passwordEncoder.encode(request.getPassword())).willReturn(encodedPassword);

        UserRole userRole = UserRole.of(request.getUserRole());

        User savedUser = new User(request.getEmail(), encodedPassword, userRole);
        given(userRepository.save(any(User.class))).willReturn(savedUser);

        String bearerToken = "Bearer jwt-token";  // JWT 토큰
        given(jwtUtil.createToken(savedUser.getId(), savedUser.getEmail(), userRole)).willReturn(bearerToken);

        // when
        SignupResponse result = authService.signup(request);

        // then
        assertNotNull(result);
        assertEquals(bearerToken, result.getBearerToken());
    }

    @Test
    public void 로그인을_하려는데_가입되지_않은_유저라서_실패한다() {
        // given
        SigninRequest request = new SigninRequest("email", "password");

        given(userRepository.findByEmail(anyString())).willReturn(Optional.empty());

        // when
        InvalidRequestException exception = assertThrows(InvalidRequestException.class, () -> {
            authService.signin(request);
        });

        // then
        assertEquals("가입되지 않은 유저입니다.", exception.getMessage());
    }

    @Test
    public void 로그인을_하려는데_잘못된_비밀번호라서_실패한다() {
        // given
        SigninRequest request = new SigninRequest("email", "password");

        User user = new User("email", "encodedPassword", UserRole.USER);
        given(userRepository.findByEmail(request.getEmail())).willReturn(Optional.of(user));

        given(!passwordEncoder.matches(request.getPassword(), user.getPassword())).willReturn(false);

        // when
        AuthException exception = assertThrows(AuthException.class, () -> {
            authService.signin(request);
        });

        // then
        assertEquals("잘못된 비밀번호입니다.", exception.getMessage());
    }

    @Test
    public void 로그인을_성공한다() {
        // given
        SigninRequest request = new SigninRequest("email", "password");

        User user = new User("email", "encodedPassword", UserRole.USER);
        given(userRepository.findByEmail(request.getEmail())).willReturn(Optional.of(user));

        given(passwordEncoder.matches(request.getPassword(), user.getPassword())).willReturn(true);

        String bearerToken = "Bearer jwt-token";  // JWT 토큰
        given(jwtUtil.createToken(user.getId(), user.getEmail(), UserRole.USER)).willReturn(bearerToken);

        // when
        SigninResponse result = authService.signin(request);

        // then
        assertNotNull(result);
        assertEquals(bearerToken, result.getBearerToken());
    }
}
