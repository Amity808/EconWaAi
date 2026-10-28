package org.ecom.econwaai.web.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.ecom.econwaai.common.exception.AppException;
import org.ecom.econwaai.common.exception.ErrorCode;
import org.ecom.econwaai.domain.enums.OrgType;
import org.ecom.econwaai.domain.enums.UserRole;
import org.ecom.econwaai.domain.model.Organization;
import org.ecom.econwaai.domain.model.User;
import org.ecom.econwaai.domain.repository.OrganizationRepository;
import org.ecom.econwaai.domain.repository.UserRepository;
import org.ecom.econwaai.security.JwtTokenService;
import org.ecom.econwaai.security.OtpService;
import org.ecom.econwaai.web.dto.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Authentication", description = "Phone-based OTP and JWT token issuance endpoints")
public class AuthController {

    private final OtpService otpService;
    private final JwtTokenService jwtTokenService;
    private final UserRepository userRepository;
    private final OrganizationRepository organizationRepository;

    public AuthController(
            OtpService otpService,
            JwtTokenService jwtTokenService,
            UserRepository userRepository,
            OrganizationRepository organizationRepository
    ) {
        this.otpService = otpService;
        this.jwtTokenService = jwtTokenService;
        this.userRepository = userRepository;
        this.organizationRepository = organizationRepository;
    }

    @PostMapping("/request-otp")
    @Operation(summary = "Request phone OTP", description = "Generates a 6-digit OTP with 5-minute TTL and max 3 attempts per 15 minutes")
    public ResponseEntity<Map<String, Object>> requestOtp(@Valid @RequestBody RequestOtpDto request) {
        String normalizedPhone = otpService.normalizePhoneNumber(request.phoneNumber());
        otpService.requestOtp(normalizedPhone);

        return ResponseEntity.ok(Map.of(
                "status", "PENDING_VERIFICATION",
                "message", "Verification OTP sent to " + normalizedPhone,
                "expiresInSeconds", 300
        ));
    }

    @PostMapping("/verify-otp")
    @Operation(summary = "Verify phone OTP", description = "Validates 6-digit OTP and emits 15-minute access token and 30-day refresh token")
    public ResponseEntity<AuthResponseDto> verifyOtp(@Valid @RequestBody VerifyOtpDto request) {
        String normalizedPhone = otpService.normalizePhoneNumber(request.phoneNumber());
        otpService.verifyOtp(normalizedPhone, request.otpCode());

        // Find or provision user
        User user = userRepository.findByPhoneNumber(normalizedPhone).orElseGet(() -> {
            // Provision default organization and user for new phone onboarding
            Organization org = Organization.create(
                    "Trader " + normalizedPhone,
                    "Trader " + normalizedPhone,
                    OrgType.BUYER,
                    null,
                    null,
                    normalizedPhone,
                    null
            );
            Organization savedOrg = organizationRepository.save(org);

            User newUser = User.create(
                    savedOrg.getId(),
                    normalizedPhone,
                    "Trader (" + normalizedPhone + ")",
                    null,
                    null,
                    UserRole.OWNER
            );
            return userRepository.save(newUser);
        });

        // Update last login
        user.setLastLoginAt(Instant.now());
        userRepository.save(user);

        // Fetch organization type
        OrgType orgType = organizationRepository.findById(user.getOrgId())
                .map(Organization::getOrgType)
                .orElse(OrgType.BUYER);

        String accessToken = jwtTokenService.generateAccessToken(user.getId(), user.getOrgId(), orgType, user.getRole());
        String refreshToken = jwtTokenService.generateRefreshToken(user.getId());

        UserSummaryDto userSummary = new UserSummaryDto(
                user.getId(),
                user.getOrgId(),
                user.getPhoneNumber(),
                user.getFullName(),
                user.getRole().name(),
                orgType.name()
        );

        return ResponseEntity.ok(AuthResponseDto.of(
                accessToken,
                refreshToken,
                JwtTokenService.ACCESS_TOKEN_EXPIRATION_MS / 1000,
                userSummary
        ));
    }

    @PostMapping("/refresh-token")
    @Operation(summary = "Refresh access token", description = "Validates 30-day refresh token and emits new 15-minute access token")
    public ResponseEntity<AuthResponseDto> refreshToken(@Valid @RequestBody RefreshTokenRequestDto request) {
        String token = request.refreshToken();

        if (!jwtTokenService.validateToken(token) || !jwtTokenService.isRefreshToken(token)) {
            throw new AppException(ErrorCode.UNAUTHORIZED, "Invalid or expired refresh token");
        }

        UUID userId = jwtTokenService.getUserId(token);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.UNAUTHORIZED, "User account not found"));

        if (!user.isActive()) {
            throw new AppException(ErrorCode.FORBIDDEN, "User account is suspended");
        }

        OrgType orgType = organizationRepository.findById(user.getOrgId())
                .map(Organization::getOrgType)
                .orElse(OrgType.BUYER);

        String newAccessToken = jwtTokenService.generateAccessToken(user.getId(), user.getOrgId(), orgType, user.getRole());
        String newRefreshToken = jwtTokenService.generateRefreshToken(user.getId());

        UserSummaryDto userSummary = new UserSummaryDto(
                user.getId(),
                user.getOrgId(),
                user.getPhoneNumber(),
                user.getFullName(),
                user.getRole().name(),
                orgType.name()
        );

        return ResponseEntity.ok(AuthResponseDto.of(
                newAccessToken,
                newRefreshToken,
                JwtTokenService.ACCESS_TOKEN_EXPIRATION_MS / 1000,
                userSummary
        ));
    }
}
