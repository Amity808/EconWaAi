package org.ecom.econwaai.compliance.gateway;

public interface IdentityVerificationGateway {

    record VerificationResult(
            String providerReference,
            boolean verified,
            boolean livenessRequired,
            String message
    ) {
    }

    record CacLookupResult(
            String providerReference,
            boolean verified,
            String rcNumber,
            String companyName,
            String status
    ) {
    }

    /**
     * Verifies NIN with external identity partner (Prembly / Smile ID).
     */
    VerificationResult verifyNin(String nin, String firstName, String lastName, String dateOfBirth);

    /**
     * Verifies registered corporate entity with CAC registry.
     */
    CacLookupResult verifyCac(String cacNumber, String companyName);
}
