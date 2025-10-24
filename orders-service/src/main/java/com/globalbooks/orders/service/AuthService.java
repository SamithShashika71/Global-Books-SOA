package com.globalbooks.orders.service;

import com.globalbooks.orders.dto.LoginRequest;
import com.globalbooks.orders.dto.LoginResponse;
import com.globalbooks.orders.dto.RegisterRequest;
import com.globalbooks.orders.model.User;
import com.globalbooks.orders.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.expiration:3600}")
    private long jwtExpiration;

    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest loginRequest) {
        log.info("Authenticating user: {}", loginRequest.getUsername());

        User user = userRepository.findByUsername(loginRequest.getUsername())
                .orElseThrow(() -> {
                    log.warn("Authentication failed - user not found: {}", loginRequest.getUsername());
                    return new BadCredentialsException("Invalid username or password");
                });

        if (!user.isEnabled()) {
            log.warn("Authentication failed - user disabled: {}", loginRequest.getUsername());
            throw new BadCredentialsException("User account is disabled");
        }

        if (!passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
            log.warn("Authentication failed - invalid password for user: {}", loginRequest.getUsername());
            throw new BadCredentialsException("Invalid username or password");
        }

        String token = generateToken(user);
        String refreshToken = generateRefreshToken(user);

        log.info("User authenticated successfully: {}", loginRequest.getUsername());

        return LoginResponse.builder()
                .accessToken(token)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(jwtExpiration)
                .username(user.getUsername())
                .roles(user.getRoles())
                .build();
    }

    @Transactional
    public void register(RegisterRequest registerRequest) {
        log.info("Registering new user: {}", registerRequest.getUsername());

        if (userRepository.existsByUsername(registerRequest.getUsername())) {
            log.warn("Registration failed - username already exists: {}", registerRequest.getUsername());
            throw new RuntimeException("Username already exists: " + registerRequest.getUsername());
        }

        if (userRepository.existsByEmail(registerRequest.getEmail())) {
            log.warn("Registration failed - email already exists: {}", registerRequest.getEmail());
            throw new RuntimeException("Email already exists: " + registerRequest.getEmail());
        }

        User user = new User();
        user.setUsername(registerRequest.getUsername());
        user.setEmail(registerRequest.getEmail());
        user.setPassword(passwordEncoder.encode(registerRequest.getPassword()));
        user.setRoles(List.of("ROLE_USER")); // Default role
        user.setEnabled(true);

        userRepository.save(user);
        log.info("User {} registered successfully", user.getUsername());
    }

    public LoginResponse refreshToken(String refreshToken) {
        log.info("Token refresh request received");
        log.warn("Refresh token functionality not fully implemented");
        throw new UnsupportedOperationException("Refresh token not implemented in demo");
    }

    private String generateToken(User user) {
        try {
            Instant now = Instant.now();
            Instant expiry = now.plus(jwtExpiration, ChronoUnit.SECONDS);

            List<String> authorities = user.getRoles().stream()
                    .map(role -> role.startsWith("ROLE_") ? role : "ROLE_" + role)
                    .collect(Collectors.toList());

            // Create JWT header (compact, no formatting)
            String header = "{\"alg\":\"HS256\",\"typ\":\"JWT\"}";

            // Create JWT payload with proper Spring Security format
            String payload = String.format(
                    "{\"iss\":\"globalbooks-orders-service\"," +
                            "\"sub\":\"%s\"," +
                            "\"iat\":%d," +
                            "\"exp\":%d," +
                            "\"scope\":\"%s\"," +
                            "\"userId\":%d," +
                            "\"email\":\"%s\"}",
                    user.getUsername(),
                    now.getEpochSecond(),
                    expiry.getEpochSecond(),
                    String.join(" ", authorities), // Space-separated authorities for Spring Security
                    user.getId(),
                    user.getEmail()
            );

            // Encode header and payload
            String encodedHeader = Base64.getUrlEncoder().withoutPadding()
                    .encodeToString(header.getBytes(StandardCharsets.UTF_8));
            String encodedPayload = Base64.getUrlEncoder().withoutPadding()
                    .encodeToString(payload.getBytes(StandardCharsets.UTF_8));

            // Create signature
            String data = encodedHeader + "." + encodedPayload;
            String signature = createHmacSha256Signature(data, jwtSecret);

            return data + "." + signature;

        } catch (Exception e) {
            log.error("Error generating JWT token", e);
            throw new RuntimeException("Failed to generate JWT token", e);
        }
    }

    private String generateRefreshToken(User user) {
        try {
            Instant now = Instant.now();
            Instant expiry = now.plus(7, ChronoUnit.DAYS);

            String header = "{\"alg\":\"HS256\",\"typ\":\"JWT\"}";
            String payload = String.format(
                    "{\"iss\":\"globalbooks-orders-service\"," +
                            "\"sub\":\"%s\"," +
                            "\"iat\":%d," +
                            "\"exp\":%d," +
                            "\"type\":\"refresh\"}",
                    user.getUsername(),
                    now.getEpochSecond(),
                    expiry.getEpochSecond()
            );

            String encodedHeader = Base64.getUrlEncoder().withoutPadding()
                    .encodeToString(header.getBytes(StandardCharsets.UTF_8));
            String encodedPayload = Base64.getUrlEncoder().withoutPadding()
                    .encodeToString(payload.getBytes(StandardCharsets.UTF_8));

            String data = encodedHeader + "." + encodedPayload;
            String signature = createHmacSha256Signature(data, jwtSecret);

            return data + "." + signature;

        } catch (Exception e) {
            log.error("Error generating refresh token", e);
            throw new RuntimeException("Failed to generate refresh token", e);
        }
    }

    private String createHmacSha256Signature(String data, String secret)
            throws NoSuchAlgorithmException, InvalidKeyException {
        Mac mac = Mac.getInstance("HmacSHA256");
        SecretKeySpec secretKeySpec = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        mac.init(secretKeySpec);
        byte[] signature = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
        return Base64.getUrlEncoder().withoutPadding().encodeToString(signature);
    }
}