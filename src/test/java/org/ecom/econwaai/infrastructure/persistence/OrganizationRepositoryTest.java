package org.ecom.econwaai.infrastructure.persistence;

import org.ecom.econwaai.domain.enums.ConsentChannel;
import org.ecom.econwaai.domain.enums.OrgType;
import org.ecom.econwaai.domain.enums.UserRole;
import org.ecom.econwaai.domain.model.Consent;
import org.ecom.econwaai.domain.model.Organization;
import org.ecom.econwaai.domain.model.OrganizationLocation;
import org.ecom.econwaai.domain.model.User;
import org.ecom.econwaai.domain.repository.ConsentRepository;
import org.ecom.econwaai.domain.repository.OrganizationLocationRepository;
import org.ecom.econwaai.domain.repository.OrganizationRepository;
import org.ecom.econwaai.domain.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@TestPropertySource(locations = "classpath:application-test.properties")
class OrganizationRepositoryTest {

    @Autowired
    private OrganizationRepository organizationRepository;

    @Autowired
    private OrganizationLocationRepository locationRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ConsentRepository consentRepository;

    @Test
    @DisplayName("Organization repository saves and retrieves organizations")
    void testOrganizationCrud() {
        Organization org = Organization.create(
                "Alaba Traders Ltd",
                "Alaba Foods",
                OrgType.SUPPLIER,
                "RC-12345678",
                "TIN-98765432",
                "+2348012345678",
                "info@alabatraders.ng"
        );

        Organization saved = organizationRepository.save(org);
        assertThat(saved.getId()).isNotNull();

        Optional<Organization> found = organizationRepository.findById(saved.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getTradeName()).isEqualTo("Alaba Foods");
        assertThat(found.get().getOrgType()).isEqualTo(OrgType.SUPPLIER);

        Optional<Organization> byCac = organizationRepository.findByCacNumber("RC-12345678");
        assertThat(byCac).isPresent();
        assertThat(byCac.get().getId()).isEqualTo(saved.getId());

        List<Organization> suppliers = organizationRepository.findAllByOrgType(OrgType.SUPPLIER);
        assertThat(suppliers).isNotEmpty();
    }

    @Test
    @DisplayName("Location repository saves landmark description, status and coordinates")
    void testOrganizationLocationCrud() {
        UUID orgId = UUID.randomUUID();
        OrganizationLocation location = OrganizationLocation.create(
                orgId,
                "Main Warehouse",
                "Alaba International Market",
                "Fancy Line",
                "Shop 44, Peace Plaza",
                "Opposite Central Mosque, yellow gate",
                new BigDecimal("6.4520000"),
                new BigDecimal("3.4150000"),
                "Musa Danladi",
                "+2348099887766",
                true
        );

        OrganizationLocation saved = locationRepository.save(location);
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getStatus()).isEqualTo("active");
        assertThat(saved.isActive()).isTrue();

        List<OrganizationLocation> orgLocations = locationRepository.findByOrgId(orgId);
        assertThat(orgLocations).hasSize(1);
        assertThat(orgLocations.get(0).getLandmarkDescription()).contains("Opposite Central Mosque");

        Optional<OrganizationLocation> defaultLoc = locationRepository.findFirstByOrgIdAndIsDefaultTrue(orgId);
        assertThat(defaultLoc).isPresent();
        assertThat(defaultLoc.get().isDefault()).isTrue();
    }

    @Test
    @DisplayName("User and NDPA consent repository saves and validates consent records")
    void testUserAndConsentCrud() {
        UUID orgId = UUID.randomUUID();
        User user = User.create(
                orgId,
                "+2348033221100",
                "Chukwudi Okafor",
                "chukwudi@example.com",
                null,
                UserRole.OWNER
        );

        User savedUser = userRepository.save(user);
        assertThat(savedUser.getId()).isNotNull();

        Optional<User> byPhone = userRepository.findByPhoneNumber("+2348033221100");
        assertThat(byPhone).isPresent();
        assertThat(byPhone.get().getRole()).isEqualTo(UserRole.OWNER);

        Consent consent = Consent.grant(
                savedUser.getId(),
                orgId,
                "bvn_verification",
                "I hereby consent to verification of my BVN for KYC",
                "197.210.64.12",
                ConsentChannel.WHATSAPP_OTP
        );

        Consent savedConsent = consentRepository.save(consent);
        assertThat(savedConsent.getId()).isNotNull();

        Optional<Consent> retrievedConsent = consentRepository.findByUserIdAndConsentTypeAndRevokedAtIsNull(savedUser.getId(), "bvn_verification");
        assertThat(retrievedConsent).isPresent();
        assertThat(retrievedConsent.get().getGrantedChannel()).isEqualTo(ConsentChannel.WHATSAPP_OTP);
    }
}
