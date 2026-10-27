# 09 — Security & Regulatory Compliance

## 1. Compliance: Nigeria Data Protection Act (NDPA 2023)

EconWaAi processes sensitive personal and financial identifiers across millions of Nigerian traders. Security architecture adheres strictly to the NDPA 2023 and CBN Digital Banking regulations:

### 1.1 Zero-Raw Identifier Policy (NIN & BVN)
- **Prohibition on Raw Storage**: Full National Identity Numbers (NIN) and Bank Verification Numbers (BVN) are **never stored** in the EconWaAi PostgreSQL database or application logs.
- **Tokenized Verification**: Details are submitted through TLS 1.3 encrypted channels directly to licensed verification providers or the partner MFB. EconWaAi persists only:
  - Verification status (`verified` / `rejected`).
  - Provider verification reference ID.
  - One-way cryptographic hash of the identifier for duplicate registration detection.
- **Explicit Recorded Consent**: Before submitting identifiers to any external API, user consent is cryptographically recorded in the `consents` table with timestamp, IP, channel, and disclosed purpose.

---

## 2. Anti-Fraud & Financial Defenses

### 2.1 Fake Bank Transfer Alert Mitigation
Fake SMS alerts, spoofed banking apps, and altered transfer screenshots are widespread vectors in Nigerian wholesale markets:
- **Zero Human Override**: Orders **cannot** be marked as `paid_escrow` by user upload of receipts or customer service assertion.
- **Deterministic Webhook Verification**: State transitions require a cryptographically signed webhook from the partner MFB matching the unique virtual NUBAN allocated to that specific purchase order.

### 2.2 Payout Bank Detail Change Freeze
To thwart account takeovers:
- Any change to an organization's payout bank account triggers an automatic **48-hour settlement hold**.
- An immediate notification is dispatched to all registered directors via WhatsApp and SMS.
- Re-verification of identity (selfie liveness or CAC cross-check) is required before the hold can be lifted.
- In accordance with Section 10, **AI agents are strictly barred from changing bank details**.

### 2.3 Tiered Load Value Caps for Transporters
To prevent cargo diversion and theft:

| Transporter Tier | Maximum Load Value | Mandatory Requirements |
|:---|:---|:---|
| **Tier 1 (New)** | ₦2,000,000 | Verified driver's licence, guarantor reference |
| **Tier 2 (Established)**| ₦10,000,000 | 50+ completed deliveries, verified vehicle papers |
| **Tier 3 (High-Value)** | Unlimited | Goods-In-Transit (GIT) insurance, active GPS telematics, approved night transit protocols |

---

## 3. Webhook Cryptographic Verification

All inbound webhooks (Partner MFB, Meta WhatsApp) undergo strict signature validation before any payload parsing:

```java
// Spring Security / Filter: Webhook HMAC-SHA256 Signature Verification
@Component
public class MfbWebhookSignatureFilter extends OncePerRequestFilter {
    private final String webhookSecret;

    public MfbWebhookSignatureFilter(@Value("${mfb.webhook.secret}") String secret) {
        this.webhookSecret = secret;
    }

    public boolean isValidSignature(byte[] body, String signatureHeader) {
        try {
            Mac hmac = Mac.getInstance("HmacSHA256");
            hmac.init(new SecretKeySpec(webhookSecret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            byte[] computedHash = hmac.doFinal(body);
            String computedHex = HexFormat.of().formatHex(computedHash);
            
            // Constant-time equals to prevent timing attacks
            return MessageDigest.isEqual(
                computedHex.getBytes(StandardCharsets.UTF_8),
                signatureHeader.getBytes(StandardCharsets.UTF_8)
            );
        } catch (Exception e) {
            return false;
        }
    }
}
```

---

## 4. Anti-Collusion & Trade Score Gaming Defenses

To prevent unscrupulous actors from fabricating trades to inflate their ProcureAI Trade Score and extract BNPL credit:
1. **Device & IP Footprinting**: Identifies shared browser fingerprints, IP subnets, or IMEI patterns between buyer and supplier accounts.
2. **Circular Flow Detection**: Graph algorithms flag closed loops where Organization A sells to Organization B, which subsequently sells back to Organization A.
3. **Reversal Penalty**: When orders are cancelled or refunded, all accrued reward points are immediately reversed, and repeat refund patterns trigger human review.
