package org.ecom.econwaai.compliance;

import org.ecom.econwaai.compliance.service.AdminNotificationService;
import org.ecom.econwaai.compliance.service.PayoutHoldService;
import org.ecom.econwaai.domain.model.BankAccountAuditLog;
import org.ecom.econwaai.domain.model.Organization;
import org.ecom.econwaai.domain.repository.BankAccountAuditLogRepository;
import org.ecom.econwaai.domain.repository.OrganizationRepository;
import org.ecom.econwaai.web.dto.UpdateBankAccountDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PayoutHoldServiceTest {

    @Mock
    private OrganizationRepository organizationRepository;

    @Mock
    private BankAccountAuditLogRepository auditLogRepository;

    @Mock
    private AdminNotificationService adminNotificationService;

    private PayoutHoldService payoutHoldService;

    @BeforeEach
    void setUp() {
        payoutHoldService = new PayoutHoldService(
                organizationRepository,
                auditLogRepository,
                adminNotificationService
        );
    }

    @Test
    @DisplayName("Should activate 48-hour anti-fraud payout hold when bank account details are updated")
    void testUpdateBankAccountActivates48HourHold() {
        UUID orgId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        Organization org = new Organization();
        org.setId(orgId);
        org.setPayoutHoldUntil(null);

        when(organizationRepository.findById(orgId)).thenReturn(Optional.of(org));
        when(auditLogRepository.save(any(BankAccountAuditLog.class))).thenAnswer(i -> i.getArgument(0));

        UpdateBankAccountDto dto = new UpdateBankAccountDto("058", "0123456789", "Dangote Foods Ltd");
        Instant before = Instant.now();

        BankAccountAuditLog result = payoutHoldService.updateBankAccount(orgId, userId, dto, "0987654321", "033");

        Instant after = Instant.now();

        // 1. Verify organization payout_hold_until is ~48 hours in the future
        assertThat(org.getPayoutHoldUntil()).isNotNull();
        assertThat(org.getPayoutHoldUntil())
                .isAfterOrEqualTo(before.plus(Duration.ofHours(47)))
                .isBeforeOrEqualTo(after.plus(Duration.ofHours(49)));
        verify(organizationRepository).save(org);

        // 2. Verify audit log captured old and new bank details
        ArgumentCaptor<BankAccountAuditLog> captor = ArgumentCaptor.forClass(BankAccountAuditLog.class);
        verify(auditLogRepository).save(captor.capture());
        BankAccountAuditLog logged = captor.getValue();
        assertThat(logged.getOrgId()).isEqualTo(orgId);
        assertThat(logged.getChangedByUserId()).isEqualTo(userId);
        assertThat(logged.getOldAccountNumber()).isEqualTo("0987654321");
        assertThat(logged.getNewAccountNumber()).isEqualTo("0123456789");
        assertThat(logged.getOldBankCode()).isEqualTo("033");
        assertThat(logged.getNewBankCode()).isEqualTo("058");
        assertThat(logged.isNotificationSent()).isTrue();

        // 3. Verify admin notification was dispatched
        verify(adminNotificationService).notifyBankAccountChange(
                eq(orgId),
                eq(userId),
                eq("0987654321"),
                eq("0123456789"),
                eq("058"),
                any(Instant.class)
        );

        // 4. Verify payout is reported as blocked
        assertThat(payoutHoldService.isPayoutBlocked(orgId)).isTrue();
    }
}
