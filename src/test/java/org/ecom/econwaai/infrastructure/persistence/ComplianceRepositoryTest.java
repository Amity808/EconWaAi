package org.ecom.econwaai.infrastructure.persistence;

import org.ecom.econwaai.common.util.HashUtil;
import org.ecom.econwaai.domain.model.BankAccountAuditLog;
import org.ecom.econwaai.domain.model.IdentityVerification;
import org.ecom.econwaai.domain.model.InformalTraderProfile;
import org.ecom.econwaai.domain.repository.BankAccountAuditLogRepository;
import org.ecom.econwaai.domain.repository.IdentityVerificationRepository;
import org.ecom.econwaai.domain.repository.InformalTraderProfileRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@TestPropertySource(locations = "classpath:application-test.properties")
class ComplianceRepositoryTest {

    @Autowired
    private IdentityVerificationRepository identityVerificationRepository;

    @Autowired
    private InformalTraderProfileRepository informalTraderProfileRepository;

    @Autowired
    private BankAccountAuditLogRepository bankAccountAuditLogRepository;

    @Test
    @DisplayName("IdentityVerification repository persists tokenized SHA-256 hash without raw data")
    void testIdentityVerificationPersistence() {
        UUID orgId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        String rawNin = "22334455667";
        String sha256 = HashUtil.sha256Hex(rawNin);

        IdentityVerification verification = new IdentityVerification(
                UUID.randomUUID(),
                orgId,
                userId,
                "NIN",
                sha256,
                "PREM-REF-12345",
                "verified",
                new BigDecimal("0.965"),
                Instant.now(),
                Instant.now()
        );

        IdentityVerification saved = identityVerificationRepository.save(verification);
        assertThat(saved.getId()).isNotNull();

        Optional<IdentityVerification> found = identityVerificationRepository.findByProviderReference("PREM-REF-12345");
        assertThat(found).isPresent();
        assertThat(found.get().getIdentifierHash()).isEqualTo(sha256);
        assertThat(found.get().getIdentifierHash()).isNotEqualTo(rawNin);
        assertThat(found.get().getLivenessScore()).isEqualByComparingTo("0.965");
    }

    @Test
    @DisplayName("InformalTraderProfile repository persists market leader endorsement and guarantor hashes")
    void testInformalTraderProfilePersistence() {
        UUID orgId = UUID.randomUUID();
        String guarantorNinHash = HashUtil.sha256Hex("11122233344");
        String guarantorBvnHash = HashUtil.sha256Hex("55566677788");

        InformalTraderProfile profile = new InformalTraderProfile(
                UUID.randomUUID(),
                orgId,
                "Mile 12 International Market",
                "Perishable Goods Sellers Association",
                "PGSA/2021/045",
                "Alhaji Haruna Danladi",
                "+2348022334455",
                "https://s3.econwa.ng/endorsements/mile12.pdf",
                "Chief Emeka Eze",
                "+2348033445566",
                guarantorNinHash,
                guarantorBvnHash,
                "pending_review",
                null,
                Instant.now(),
                Instant.now()
        );

        InformalTraderProfile saved = informalTraderProfileRepository.save(profile);
        assertThat(saved.getId()).isNotNull();

        Optional<InformalTraderProfile> found = informalTraderProfileRepository.findByOrgId(orgId);
        assertThat(found).isPresent();
        assertThat(found.get().getMarketName()).isEqualTo("Mile 12 International Market");
        assertThat(found.get().getGuarantorNinHash()).isEqualTo(guarantorNinHash);
        assertThat(found.get().getGuarantorBvnHash()).isEqualTo(guarantorBvnHash);
    }

    @Test
    @DisplayName("BankAccountAuditLog repository captures 48-hour payout hold audit entries")
    void testBankAccountAuditLogPersistence() {
        UUID orgId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        Instant payoutHoldUntil = Instant.now().plusSeconds(172800); // 48 hours

        BankAccountAuditLog auditLog = new BankAccountAuditLog(
                UUID.randomUUID(),
                orgId,
                userId,
                "0123456789",
                "9876543210",
                "058",
                "033",
                payoutHoldUntil,
                true,
                Instant.now()
        );

        BankAccountAuditLog saved = bankAccountAuditLogRepository.save(auditLog);
        assertThat(saved.getId()).isNotNull();

        List<BankAccountAuditLog> logs = bankAccountAuditLogRepository.findAllByOrgIdOrderByCreatedAtDesc(orgId);
        assertThat(logs).hasSize(1);
        assertThat(logs.get(0).getNewAccountNumber()).isEqualTo("9876543210");
        assertThat(logs.get(0).getPayoutHoldUntil()).isEqualTo(payoutHoldUntil);
    }
}
