package org.ecom.econwaai.web.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.ecom.econwaai.common.context.OrgContextHolder;
import org.ecom.econwaai.common.exception.AppException;
import org.ecom.econwaai.common.exception.ErrorCode;
import org.ecom.econwaai.compliance.service.*;
import org.ecom.econwaai.domain.model.BankAccountAuditLog;
import org.ecom.econwaai.domain.model.Consent;
import org.ecom.econwaai.domain.model.IdentityVerification;
import org.ecom.econwaai.domain.model.InformalTraderProfile;
import org.ecom.econwaai.domain.model.Organization;
import org.ecom.econwaai.web.dto.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/compliance")
@Tag(name = "Compliance & Verification", description = "NDPA 2023 Consent recording, tokenized NIN/CAC verification, and 48-hour anti-fraud holds")
public class ComplianceController {

    private final ConsentService consentService;
    private final IdentityVerificationService identityVerificationService;
    private final CacVerificationService cacVerificationService;
    private final InformalTraderService informalTraderService;
    private final PayoutHoldService payoutHoldService;
    private final WebviewTokenService webviewTokenService;

    public ComplianceController(
            ConsentService consentService,
            IdentityVerificationService identityVerificationService,
            CacVerificationService cacVerificationService,
            InformalTraderService informalTraderService,
            PayoutHoldService payoutHoldService,
            WebviewTokenService webviewTokenService
    ) {
        this.consentService = consentService;
        this.identityVerificationService = identityVerificationService;
        this.cacVerificationService = cacVerificationService;
        this.informalTraderService = informalTraderService;
        this.payoutHoldService = payoutHoldService;
        this.webviewTokenService = webviewTokenService;
    }

    @PostMapping("/consent")
    @Operation(summary = "Record explicit legal consent", description = "Mandatory under NDPA 2023 before any identity processing")
    public ResponseEntity<ConsentResponseDto> recordConsent(
            @Valid @RequestBody ConsentRequestDto request,
            HttpServletRequest servletRequest
    ) {
        UUID userId = getCurrentUserId();
        UUID orgId = getCurrentOrgId();
        String clientIp = servletRequest.getRemoteAddr();

        Consent consent = consentService.recordConsent(
                userId,
                orgId,
                request.consentType(),
                request.consentText(),
                clientIp,
                request.grantedChannel()
        );

        return ResponseEntity.ok(new ConsentResponseDto(
                consent.getId(),
                consent.getUserId(),
                consent.getOrgId(),
                consent.getConsentType(),
                consent.getGrantedChannel().name(),
                consent.getGrantedIp(),
                consent.getGrantedAt(),
                "ACTIVE"
        ));
    }

    @PostMapping("/verify-nin")
    @Operation(summary = "Tokenized NIN verification", description = "Verifies NIN with external identity partner with ZERO-RAW storage")
    public ResponseEntity<Map<String, Object>> verifyNin(@Valid @RequestBody NinVerificationRequestDto request) {
        UUID userId = getCurrentUserId();
        UUID orgId = getCurrentOrgId();

        IdentityVerification verification = identityVerificationService.verifyNin(userId, orgId, request);

        return ResponseEntity.ok(Map.of(
                "status", verification.getStatus(),
                "providerReference", verification.getProviderReference(),
                "identifierHash", verification.getIdentifierHash(),
                "verifiedAt", verification.getVerifiedAt() != null ? verification.getVerifiedAt().toString() : "PENDING_LIVENESS"
        ));
    }

    @PostMapping("/verify-cac")
    @Operation(summary = "CAC corporate registration verification", description = "Verifies business name and RC/BN number with CAC registry")
    public ResponseEntity<Map<String, Object>> verifyCac(@Valid @RequestBody CacVerificationRequestDto request) {
        UUID userId = getCurrentUserId();
        UUID orgId = getCurrentOrgId();

        Organization org = cacVerificationService.verifyCac(userId, orgId, request);

        return ResponseEntity.ok(Map.of(
                "orgId", org.getId(),
                "legalName", org.getLegalName(),
                "cacNumber", org.getCacNumber(),
                "kycStatus", org.getKycStatus().name(),
                "kycVerifiedAt", org.getKycVerifiedAt() != null ? org.getKycVerifiedAt().toString() : ""
        ));
    }

    @PostMapping("/informal-trader")
    @Operation(summary = "Informal trader profile submission", description = "Alternative verification with market leader endorsement and guarantor details")
    public ResponseEntity<InformalTraderProfile> submitInformalTrader(@Valid @RequestBody InformalTraderRegistrationDto request) {
        UUID userId = getCurrentUserId();
        UUID orgId = getCurrentOrgId();

        InformalTraderProfile profile = informalTraderService.submitProfile(userId, orgId, request);
        return ResponseEntity.ok(profile);
    }

    @PostMapping("/liveness-callback")
    @Operation(summary = "3D selfie liveness check callback", description = "Upstream partner webhook elevating to TIER_3_LIVENESS_INSPECTED")
    public ResponseEntity<Map<String, Object>> handleLivenessCallback(@Valid @RequestBody LivenessCallbackDto callback) {
        IdentityVerification verification = identityVerificationService.handleLivenessCallback(callback);

        return ResponseEntity.ok(Map.of(
                "status", verification.getStatus(),
                "providerReference", verification.getProviderReference(),
                "livenessScore", verification.getLivenessScore() != null ? verification.getLivenessScore() : 0
        ));
    }

    @PostMapping("/webview-url")
    @Operation(summary = "Generate single-use KYC webview URL", description = "Generates a signed 15-minute expiring link for WhatsApp KYC handoff")
    public ResponseEntity<WebviewUrlResponseDto> generateWebviewUrl(@Valid @RequestBody WebviewUrlRequestDto request) {
        UUID userId = getCurrentUserId();
        UUID orgId = getCurrentOrgId();

        WebviewUrlResponseDto response = webviewTokenService.generateWebviewUrl(
                userId,
                orgId,
                request.verificationType(),
                request.returnUrl()
        );

        return ResponseEntity.ok(response);
    }

    @PostMapping("/bank-account")
    @Operation(summary = "Update organization bank account details", description = "Triggers automated 48-hour anti-fraud payout hold")
    public ResponseEntity<Map<String, Object>> updateBankAccount(@Valid @RequestBody UpdateBankAccountDto request) {
        UUID userId = getCurrentUserId();
        UUID orgId = getCurrentOrgId();

        BankAccountAuditLog auditLog = payoutHoldService.updateBankAccount(
                orgId,
                userId,
                request,
                null,
                null
        );

        return ResponseEntity.ok(Map.of(
                "status", "UPDATED",
                "orgId", orgId,
                "payoutHoldUntil", auditLog.getPayoutHoldUntil().toString(),
                "message", "Bank account details saved. Payouts are frozen for 48 hours for anti-fraud verification."
        ));
    }

    private UUID getCurrentUserId() {
        return OrgContextHolder.getUserId()
                .orElseThrow(() -> new AppException(ErrorCode.UNAUTHORIZED, "User authentication context missing"));
    }

    private UUID getCurrentOrgId() {
        return OrgContextHolder.getOrgId()
                .orElseThrow(() -> new AppException(ErrorCode.UNAUTHORIZED, "Organization context missing"));
    }
}
