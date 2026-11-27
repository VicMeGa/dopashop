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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Duration;
import java.time.LocalDateTime;

@Service
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

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
        String email = request.email().trim().toLowerCase();
        if (userRepository.findByEmail(email).isPresent()) {
            log.warn("Intento de registro con email ya existente: {}", email);
            throw new ResponseStatusException(HttpStatus.CONFLICT, "El email ya está registrado");
        }
        User user = new User();
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setFullName(request.fullName().trim());
        user.setRole(UserRole.USER);
        userRepository.save(user);
        log.info("Registro OK userId={} email={}", user.getId(), email);
        return generateAuthResponse(user);
    }

    @Transactional
    public AuthResponse login(LoginRequest request) {
        String email = request.email().trim().toLowerCase();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    log.warn("Intento de login fallido: email no encontrado = {}", email);
                    return new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Credenciales inválidas");
                });
        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            log.warn("Intento de login fallido: contraseña incorrecta userId={} email={}", user.getId(), email);
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Credenciales inválidas");
        }
        log.info("Login OK userId={} email={}", user.getId(), email);
        return generateAuthResponse(user);
    }

    @Transactional
    public AuthResponse refresh(RefreshRequest request) {
        String hash = jwtService.hashToken(request.refreshToken());
        RefreshToken stored = refreshTokenRepository.findByTokenHash(hash)
                .orElseThrow(() -> {
                    log.warn("Refresh token inválido o inexistente");
                    return new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Refresh token inválido");
                });
        if (stored.isRevoked() || stored.getExpiresAt().isBefore(LocalDateTime.now())) {
            log.warn("Refresh token expirado o revocado userId={}", stored.getUser().getId());
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Refresh token expirado o revocado");
        }
        stored.setRevoked(true);
        refreshTokenRepository.save(stored);
        log.info("Refresh OK userId={}", stored.getUser().getId());
        return generateAuthResponse(stored.getUser());
    }

    @Transactional
    public void logout(String refreshToken) {
        String hash = jwtService.hashToken(refreshToken);
        refreshTokenRepository.findByTokenHash(hash).ifPresent(token -> {
            token.setRevoked(true);
            refreshTokenRepository.save(token);
            log.info("Logout OK userId={}", token.getUser().getId());
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
