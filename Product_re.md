ProcureAI Nigeria — Section 20: Nigeria Operating Realities
21 Oct 2026 · @Bolarinwa
Purpose
This section adapts Sections 5–19 to how trade, payment and transport actually work in Nigeria. Where it conflicts with an earlier section on a Nigeria-specific matter, this section takes precedence, and the earlier requirement should be updated to match.
Each subsection states the local reality, the product requirement, and the risk if it is ignored. Items marked Validate in discovery must be confirmed during the Weeks 1–2 interviews before build.
20.1 Payments, trade credit and transfer fraud
Bank transfer is the default B2B payment method, and much wholesale trade runs on informal credit between parties who know each other. The payment module must support both, through a licensed payment provider.
Requirements
• Issue a dedicated virtual account (or unique payment reference) per order or invoice through the partner microfinance bank (20.1.1) or a licensed payment provider, so each transfer reconciles automatically to one purchase order.
• Mark an order as paid only on a verified provider webhook or settlement record. Screenshots, forwarded SMS alerts and “I have paid” messages must never change payment status. This blocks the common fake-transfer-alert fraud.
• Support payment terms per purchase order: pay before dispatch, pay on delivery (transfer at handover, confirmed by webhook before the driver leaves), part payment, and supplier-granted credit (for example, 7 or 14 days).
• Supplier credit is offered by the supplier at the supplier's own risk. ProcureAI records the terms, due dates and outstanding balances, and sends reminders. Platform credit is only provided by the partner microfinance bank or another licensed lender, through ProcureAI Credit (20.1.2).
• Support partial payments and multiple transfers against one order, with a clear outstanding balance.
• Record POS-terminal and cash payments made directly to suppliers as supplier-attested, distinct from platform-verified payments, so reports never mix them.
• Drivers must never collect payment on ProcureAI's behalf unless a documented, licensed collection arrangement exists.
Risk if ignored: buyers who rely on credit will not switch, and fake alerts will cause goods to leave suppliers unpaid.
Validate in discovery: typical credit periods by category, and how many pilot buyers currently buy on credit.
20.1.1 Partner microfinance bank accounts
Every buyer, supplier and transporter organisation gets a dedicated account number generated through a partnership with a CBN-licensed microfinance bank (MFB). The bank holds the funds and the customer relationship for the account; ProcureAI never holds customer money itself.
Requirements
• Generate the account automatically once the user completes the KYC tier the bank requires. Account limits follow the bank's tiered KYC rules, and higher tiers unlock as verification increases.
• Collect NIN, BVN and other details through the secure verification flow (20.7) and pass them to the bank only with the user's recorded consent.
• Use the account to: pay for orders, receive supplier and transporter payouts, receive credit disbursements (20.1.2) and make repayments.
• Show balance, statement and transaction history in the dashboard, and a short balance summary on WhatsApp after authentication.
• Update payment and account status only from the bank's verified webhooks or API, never from user messages.
• Reconcile platform records against the bank's statement daily; surface mismatches to finance operations.
• Bank-detail and withdrawal-destination changes require re-authentication and trigger a payout hold (20.7).
• Keep a fallback route (a second partner bank or a licensed payment provider) so orders can still be paid if the partner bank's systems are down.
Partnership agreement must cover: which party owns the customer relationship, account structure (full customer account or virtual sub-account), fees and revenue share, complaint handling, dormant and closed accounts, data sharing under the NDPA 2023, and exit and data-migration terms.
20.1.2 ProcureAI Credit: reward points and buy now, pay later
Buyers earn points for good trading behaviour. Points and order history build a ProcureAI Trade Score, which lets reliable buyers restock on credit when cash is short and repay later. The partner MFB (or another licensed lender) provides the money and makes the final lending decision; ProcureAI provides the trading data, the experience and the reminders.
Earning points
• Points are earned per completed order: paid, delivered and receipt confirmed. Cancelled orders earn nothing, and refunded orders have their points reversed.
• Bonus points for repaying credit on or before the due date, completing higher verification levels and consistent monthly ordering.
• Points are lost for late repayment and for disputes found to be the buyer's fault.
• Anti-gaming controls: daily and monthly earning caps, and detection of fake trading such as a buyer and supplier who are linked, circular orders, or orders that are repeatedly refunded. Flagged accounts are reviewed by a person before any penalty.
ProcureAI Trade Score
• Inputs: completed order volume and consistency, payment and repayment timeliness, dispute history, verification level, time on the platform, and points balance. With the buyer's consent, the lender may add a credit bureau check.
• The score must be explainable. Buyers see their tier, what moved it, and what they can do to improve it.
• The score is an input to the lender's decision, not a lending approval. AI may calculate and explain the score but must not approve credit (Section 10).
Credit tiers (all values set with the lender and configurable)
Tier
Typical requirement
Credit limit
Repayment period
Starter
New or few completed orders
No credit
—
Bronze
Minimum completed orders over at least 3 months, verified business details
Small, set with lender
Short (for example 7 days)
Silver
Bronze history plus credit repaid on time
Higher limit
Longer (for example 14 days)
Gold
Long, clean repayment history and higher KYC tier
Highest limit
Up to the lender's maximum
Using credit at checkout
1. An eligible buyer sees “Pay with ProcureAI Credit” when the order is within their available limit.
2. Before accepting, the buyer sees the amount, all interest and fees, total repayable, due date and late charges in plain language.
3. The buyer accepts through an authenticated step (secure link or OTP), not a plain WhatsApp text. The acceptance and terms shown are stored.
4. The lender pays the supplier directly into the supplier's account. Credit funds never go to the buyer as cash, so credit can only be used for goods bought on the platform.
5. The order then follows the normal fulfilment flow.
Repayment and late payment
• Repay by transfer into the buyer's MFB account, or by automatic debit where the buyer has given a clear mandate.
• Reminders before the due date, on the due date and after it, by WhatsApp and SMS.
• A short grace period, then: late charges as disclosed, credit limit frozen, points reduced and tier lowered.
• Reporting to credit bureaus is done by the lender, only as disclosed and consented to at sign-up.
• Collections must be lawful and respectful. Never contact a buyer's phone contacts, family or customers, and never shame or threaten a buyer.
Responsible lending rules
• Limits start small and grow only with on-time repayment.
• A buyer cannot take new credit while an earlier credit is overdue, and total outstanding credit is capped per buyer.
• Credit is for restocking goods that the buyer will resell, not for personal emergencies; categories eligible for credit are configured with the lender.
• Show the buyer how much of their limit is in use before every credit purchase.
Regulatory and legal checks before launch
• Confirm the lender's licence covers this lending, and whether FCCPC digital lending registration or other CBN rules apply to the product and to ProcureAI's role (rules can differ for business versus individual borrowers).
• Disclose ProcureAI's role and any fee it earns from the lender. Add this as a revenue line in Section 15.
• Agree in the partnership contract who bears the default loss, who handles collections and complaints, and how data is shared.
Risk if ignored: without these controls, credit can trap struggling traders in debt, attract fraud and create regulatory exposure for ProcureAI.
Validate in discovery: typical restocking cycles and amounts, how long buyers need before repaying, and which partner MFBs offer account generation and lending through an API.
20.2 Addresses, landmarks and market locations
Many delivery points are described by landmarks rather than street numbers, and market shops are located by market, line and shop number. The location model must capture how people actually give directions.
Requirements
• Extend organization_locations with: landmark description (free text), market name, market section or line, shop or plaza number, map pin (latitude/longitude with accuracy), and an on-site contact name and phone number.
• Let buyers and suppliers set the pin by dropping it on a map or sharing a WhatsApp location, then save it for reuse.
• Allow multiple saved locations per organisation (for example, a shop and a warehouse) with a default.
• Show the driver the landmark text, pin and on-site contact together once the job is assigned (Section 7.4 still applies: no exact details in broadcast offers).
• Treat a driver call to the recipient as a normal part of delivery. Add a driver_called_recipient event and an exception reason location_not_found_after_call.
• Record market operating days and hours, and closures, so pickups and deliveries are not scheduled when a market is shut.
Risk if ignored: high failed-delivery rates and drivers spending most of their time searching for locations.
20.3 Price volatility and quote validity
Exchange-rate and fuel-price movements can make goods and delivery quotes stale within days. Every price on the platform must carry an explicit validity window.
Requirements
• Add valid_until to quotations and delivery_quotes. Suppliers set it per quote, with a category default (for example, 48 hours for imported goods).
• A buyer cannot accept an expired quote. The system offers a one-tap re-quote request to the same supplier instead.
• When a supplier reconfirms an order (Section 5, step 9) at a different price, treat it as a new price that the buyer must approve. Never apply the change silently.
• Delivery fees are fixed at assignment. If fuel prices change sharply before assignment, operations can reprice the open job; once accepted, any change follows the additional-charge approval in Section 7.8.
• Store price history per product and supplier so the AI anomaly check (Section 10) compares against recent prices, not old ones.
Risk if ignored: suppliers refuse to honour old quotes, causing cancellations and disputes.
20.4 Road, route and security realities
Delays and extra costs on Nigerian roads often come from levies, checkpoints, traffic, flooding and access restrictions. These must be recordable as normal exceptions, with clear rules on who pays.
Requirements
• Add exception reasons to Section 7.8: levy_or_toll_demand, checkpoint_delay, flooding_or_impassable_road, vehicle_access_restriction, market_congestion, and security_concern_route_changed.
• Agree in transporter contracts which costs are included in the fee (for example, official tolls and union charges) and which can be claimed as extras. Extras need operations approval before they are passed to the buyer.
• Record known access rules per route, such as time-of-day truck restrictions in parts of Lagos, and check them when scheduling pickup and delivery windows.
• Add a rainy-season flag per route so delivery windows widen automatically on flood-prone routes.
• High-value loads: no unaccompanied night movement unless the transporter's security arrangements are approved; share driver location with operations during transit; set a load-value ceiling per transporter tier.
• Security incidents go to a human operator immediately. The driver's safety comes before the goods, and the workflow must say so.
Risk if ignored: disputed extra charges, unrealistic delivery promises and unsafe practices.
Validate in discovery: the regular levies and restrictions on the pilot routes, and how transporters price them today.
20.5 Languages, voice notes and local units
Many buyers will order by voice note, often in Nigerian Pidgin, Yoruba, Hausa or Igbo, or a mix with English. Quantities are often given in local trade units rather than kilograms.
Requirements
• The AI request extraction (Section 10) must accept voice notes and text in English, Nigerian Pidgin and code-switched speech at launch, with Yoruba, Hausa and Igbo added based on pilot demand.
• Always read the structured order back to the buyer in plain English (and the buyer's language where supported) for confirmation before an RFQ is sent.
• Below the confidence threshold, ask a specific clarifying question (“Do you mean 50kg bags or 25kg bags?”) or hand off to a human agent.
• Maintain a local unit dictionary mapping trade units to standard units per product: for example bag (25kg/50kg), carton (with pack count), crate, paint rubber, mudu, and derica. Unit sizes vary by product and market, so each mapping is per product and can be overridden by the supplier.
• Maintain a synonyms list for local product and brand names.
• Every buyer-facing message must be short and clear on a small phone screen.
Risk if ignored: wrong quantities ordered, and buyers abandoning the bot for a phone call.
Validate in discovery: which languages and units pilot buyers actually use.
20.6 Devices, connectivity and OTP delivery
Many drivers and traders use low-cost Android phones, pay for data per bundle, and face patchy network coverage and power cuts. Every field workflow must work on a weak connection and a cheap phone.
Requirements
• Driver and transporter web pages must load fast on slow networks: small page sizes, no heavy frameworks on field screens, and a target load under 200 KB per page.
• Compress evidence photos on the device before upload. Queue uploads and status updates offline and retry automatically, keeping the original capture timestamp.
• Mark events received late with both the device time and the server receipt time, so delays are visible but not treated as fraud by default.
• OTP delivery: send by WhatsApp first, with SMS and voice-call fallbacks. SMS alone is unreliable for numbers with Do Not Disturb enabled, so use a provider route that supports transactional OTPs.
• Provide an SMS or USSD fallback for the most critical driver actions (arrived, picked up, delivered, problem) if WhatsApp is unavailable. Validate in discovery whether USSD is worth its cost for the pilot.
• Never require app installation for drivers in the MVP.
Risk if ignored: missing tracking events, failed confirmations, and drivers refusing the workflow.
20.7 Identity and business verification in Nigeria
Sections 8 and 9 stay provider-neutral. In Nigeria, verification will rely on national identifiers and CAC records, checked through licensed verification providers with the person's consent.
Requirements
• Individuals: verify NIN through a licensed identity-verification provider, with a selfie liveness check for higher-risk roles (suppliers receiving payouts, transporters carrying high-value loads).
• BVN checks only where needed for payouts or financing, with explicit consent and through the payment or financing partner where possible. Do not store full BVN or NIN values; store the provider's verification result and reference.
• Businesses: check CAC records for registered companies and business names, and confirm the representative's authority (director listing, letter of authority or similar).
• Bank account name match: before any payout, confirm the account name matches the verified business or individual. Any bank-detail change triggers re-verification and a hold (Section 10 already bars AI from changing bank details).
• Drivers: driver's licence check where a provider supports it, plus vehicle registration documents; otherwise manual review.
• Informal traders and transporters: accept trade references, market association membership, or a known guarantor as alternative evidence, with lower transaction limits until a history builds up.
• Never collect ID numbers or document photos in an ordinary WhatsApp chat; use a secure link (Section 8).
Risk if ignored: payout fraud and account takeovers, or excluding most of the informal market.
Obtain legal advice on consent wording and data retention under the NDPA 2023.
20.8 Disintermediation and platform value
Nigerian trade is relationship-driven, so once a buyer finds a good supplier, the next order may happen by direct phone call. The platform must earn repeat orders by being more useful than going direct, not by trying to block contact.
Reasons to stay on the platform (build into the MVP)
• Payment protection: payment released or confirmed only against the agreed order, with a dispute route.
• Delivery handled end to end, with proof of delivery and a single point of contact when something goes wrong.
• Credit terms recorded and tracked (20.1), building a repayment history the buyer can use to get better terms.
• One-tap reorder of a previous order from the same supplier.
• Procurement records, receipts and spending reports useful for accounting and tax.
• Supplier benefits: steady new buyers, verified buyer details, faster payment confirmation and fewer fake alerts.
Product rules
• Hide full contact details between buyer and supplier until an order is confirmed; after that, sharing is allowed, because coordination needs it.
• Track repeat-buyer rate per supplier pair. A pair that stops ordering after one or two orders is a signal to investigate, not a reason to penalise.
• Keep the commission at a level that is cheaper than the cost of going direct once delivery and protection are counted. Validate in discovery.
Risk if ignored: strong early order volume that does not repeat.
20.9 WhatsApp messaging costs
Meta charges for business-initiated template messages, and the logistics flow sends many of them to buyers, suppliers and transporters. Messaging cost must be tracked per order and kept within the margin.
Requirements
• Log every outbound WhatsApp message with its template category and cost, linked to the order or delivery, so cost per completed order is visible.
• Prefer replies inside the free customer-service window where possible; for example, ask the buyer a question that keeps the conversation open rather than sending separate templates later.
• Combine updates: send one assignment message with transporter, driver and estimated time, not three.
• Let each user choose notification level (all updates or key milestones only). Key milestones are pickup, arrival, delivery evidence and confirmation request.
• Offer jobs to a small controlled group of transporters (Section 7.4) rather than broad broadcasts.
• Check current Meta pricing for Nigeria before setting fees, as rates and rules change.
Risk if ignored: messaging costs eating most of the margin on small orders.
20.10 Tax and invoicing
Nigeria's tax framework has recently been reformed, and administration now sits with the Nigeria Revenue Service. Tax handling must be configurable rather than hard-coded, and confirmed with a tax adviser before launch.
Requirements
• Store VAT rate and VAT status per supplier and per product category as configuration, not code. Some goods may be exempt or zero-rated.
• Record each supplier's tax identification number where available, and whether they are VAT-registered.
• Generate separate documents for goods (supplier's invoice to buyer), delivery (transporter fee) and ProcureAI's own fees, so each party's tax position is clear.
• Design invoices so they can later connect to any mandatory e-invoicing or fiscalisation system without restructuring.
• Keep the platform's own fee revenue separate from goods value and transporter fees in reports (Section 15).
Open question for tax adviser: ProcureAI's VAT and withholding obligations on commission and logistics coordination fees, and any reporting duties as a marketplace.
20.11 Timeline, data model additions and acceptance criteria
Timeline realism. Payment-provider onboarding, WhatsApp Business verification, identity-provider contracts and transporter recruitment often take longer than planned in Nigeria. Start all four in Week 1, in parallel with discovery, and plan a fallback pilot of 14–16 weeks if any are delayed.
Data model additions
Entity
New or changed
Purpose
payment_terms
New
Pay before dispatch, on delivery, part payment or supplier credit, with due dates
credit_balances
New
Outstanding amounts and reminders per buyer–supplier pair
bank_accounts
New
Partner-MFB account per organisation: account number, KYC tier, limits, status
bank_transactions
New
Bank webhook events and statement lines for reconciliation
consents
New
Recorded consent for BVN/NIN sharing, bureau checks and auto-debit mandates
points_ledger
New
Points earned, reversed and lost, with the order or repayment that caused each
trade_scores
New
Score, tier, inputs used and explanation, with history
credit_lines
New
Lender, limit, used amount, tier, freeze status
credit_drawdowns
New
Each credit purchase: order, terms shown, acceptance, amount paid to supplier
repayments
New
Due dates, payments, late charges and reminders sent
organization_locations
Changed
Landmark, market, line, shop number, pin, on-site contact
quotations, delivery_quotes
Changed
valid_until field
price_history
New
Recent prices per product and supplier
unit_conversions
New
Local trade units to standard units, per product
product_synonyms
New
Local names and spellings
route_rules
New
Access restrictions, rainy-season flags, known levies
message_costs
New
Cost of each outbound WhatsApp message, linked to order
tax_profiles
New
VAT status, tax ID and rates per supplier and category
Added acceptance criteria
[ ] A screenshot or forwarded alert cannot mark an order as paid.
[ ] Each order reconciles to a unique virtual account or payment reference.
[ ] A partner-MFB account is generated automatically once the required KYC tier is complete, with consent recorded.
[ ] Platform records reconcile daily against the partner bank's statement.
[ ] Supplier credit terms and outstanding balances are recorded and reminded.
[ ] Points are awarded only for completed orders and are reversed on refund.
[ ] Buyers can see their Trade Score tier and what affects it.
[ ] A credit purchase shows full cost and due date and requires authenticated acceptance.
[ ] Credit funds are paid directly to the supplier, never to the buyer.
[ ] Overdue credit freezes new credit for that buyer.
[ ] Expired quotes cannot be accepted; price changes at reconfirmation require buyer approval.
[ ] A driver can see landmark, pin and on-site contact once assigned, not before.
[ ] A Pidgin voice note is converted into a correct structured RFQ and read back for confirmation.
[ ] Local units are converted using the per-product unit dictionary.
[ ] Driver updates made offline are delivered when connectivity returns, with original timestamps.
[ ] OTP reaches the buyer through at least one fallback channel when WhatsApp delivery fails.
[ ] Bank-detail changes trigger re-verification and a payout hold.
[ ] WhatsApp cost per completed order is reported.