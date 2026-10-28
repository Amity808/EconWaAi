package org.ecom.econwaai.compliance.gateway;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class PremblyIdentityVerificationGateway implements IdentityVerificationGateway {

    private static final Logger log = LoggerFactory.getLogger(PremblyIdentityVerificationGateway.class);

    @Override
    public VerificationResult verifyNin(String nin, String firstName, String lastName, String dateOfBirth) {
        log.info("Dispatching tokenized NIN verification request to Prembly/SmileID sandbox for name={} dob={}",
                firstName + " " + lastName, dateOfBirth);

        // Simulation/sandbox logic: validate format
        if (nin == null || nin.length() != 11 || !nin.matches("\\d{11}")) {
            return new VerificationResult(
                    "PREM-ERR-" + UUID.randomUUID().toString().substring(0, 8),
                    false,
                    false,
                    "Invalid NIN format. Must be 11 numeric digits."
            );
        }

        // Test rejection hook: NIN starting with '00000' is treated as fraudulent/rejected
        if (nin.startsWith("00000")) {
            return new VerificationResult(
                    "PREM-REJ-" + UUID.randomUUID().toString().substring(0, 8),
                    false,
                    false,
                    "NIN record not found or flagged in NIMC database."
            );
        }

        // Standard simulated successful verification with Prembly reference
        String providerRef = "PREM-NIN-" + UUID.randomUUID().toString().substring(0, 12).toUpperCase();
        return new VerificationResult(
                providerRef,
                true,
                true, // Liveness required for tier 3 upgrade
                "NIN identity validated successfully with NIMC"
        );
    }

    @Override
    public CacLookupResult verifyCac(String cacNumber, String companyName) {
        log.info("Dispatching corporate CAC lookup to registry for cacNumber={} companyName={}",
                cacNumber, companyName);

        if (cacNumber == null || cacNumber.isBlank() || companyName == null || companyName.isBlank()) {
            return new CacLookupResult(
                    "CAC-ERR-" + UUID.randomUUID().toString().substring(0, 8),
                    false,
                    cacNumber,
                    companyName,
                    "INACTIVE"
            );
        }

        // Test rejection hook: CAC starting with 'INVALID'
        if (cacNumber.toUpperCase().startsWith("INVALID")) {
            return new CacLookupResult(
                    "CAC-REJ-" + UUID.randomUUID().toString().substring(0, 8),
                    false,
                    cacNumber,
                    companyName,
                    "DISSOLVED"
            );
        }

        String providerRef = "CAC-REG-" + UUID.randomUUID().toString().substring(0, 12).toUpperCase();
        return new CacLookupResult(
                providerRef,
                true,
                cacNumber.toUpperCase(),
                companyName.toUpperCase(),
                "ACTIVE"
        );
    }
}
