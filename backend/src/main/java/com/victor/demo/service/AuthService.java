package com.victor.demo.service;

import com.victor.demo.dto.request.LoginRequest;
import com.victor.demo.dto.request.RefreshRequest;
import com.victor.demo.dto.request.RegisterRequest;
import com.victor.demo.dto.response.AuthResponse;
import com.victor.demo.dto.response.UserSummaryResponse;
import com.victor.demo.entity.RefreshToken;
import com.victor.demo.entity.User;
import com.victor.demo.entity.UserRole;
import com.victor.demo.repository.RefreshTokenRepository;
import com.victor.demo.repository.UserRepository;
import com.victor.demo.security.JwtService;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Duration;
import java.time.LocalDateTime;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;

    public AuthService(UserRepository userRepository,
                       RefreshTokenRepository refreshTokenRepository,
                       JwtService jwtService,
                       PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.jwtService = jwtService;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.findByEmail(request.email()).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already registered");
        }
        User user = new User();
        user.setEmail(request.email());
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setFullName(request.fullName());
        user.setRole(UserRole.USER);
        userRepository.save(user);
        return generateAuthResponse(user);
    }

    @Transactional
    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials"));
        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials");
        }
        return generateAuthResponse(user);
    }

    @Transactional
    public AuthResponse refresh(RefreshRequest request) {
        String hash = jwtService.hashToken(request.refreshToken());
        RefreshToken stored = refreshTokenRepository.findByTokenHash(hash)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid refresh token"));
        if (stored.isRevoked() || stored.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Refresh token expired or revoked");
        }
        stored.setRevoked(true);
        refreshTokenRepository.save(stored);
        return generateAuthResponse(stored.getUser());
    }

    @Transactional
    public void logout(String refreshToken) {
        String hash = jwtService.hashToken(refreshToken);
        refreshTokenRepository.findByTokenHash(hash).ifPresent(token -> {
            token.setRevoked(true);
            refreshTokenRepository.save(token);
        });
    }

    private AuthResponse generateAuthResponse(User user) {
        String accessToken = jwtService.generateAccessToken(user);
        String rawRefresh = jwtService.generateRefreshTokenValue();
        String hashedRefresh = jwtService.hashToken(rawRefresh);

        RefreshToken refreshEntity = new RefreshToken();
        refreshEntity.setUser(user);
        refreshEntity.setTokenHash(hashedRefresh);
        refreshEntity.setExpiresAt(LocalDateTime.now().plus(Duration.ofMillis(jwtService.getRefreshExpirationMs())));
        refreshEntity.setRevoked(false);
        refreshEntity.setCreatedAt(LocalDateTime.now());
        refreshTokenRepository.save(refreshEntity);

        UserSummaryResponse userSummary = new UserSummaryResponse(
                user.getId(), user.getEmail(), user.getFullName(), user.getRole());

        return new AuthResponse(accessToken, rawRefresh, jwtService.getRefreshExpirationMs(), userSummary);
    }
}
