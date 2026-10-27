# Week 4 — AI Voice Note & Speech-to-Text Pipeline

> **Goal:** Deploy the Python 3.11 Faster-Whisper microservice, tune speech recognition for Nigerian English and Pidgin, and implement domain Named Entity Recognition (NER) for local trade units.

---

## Monday — Python FastAPI Inference Service Setup

- [ ] Initialize Python 3.11 project structure in `services/ai-stt-service`.
- [ ] Configure `Faster-Whisper` (CTranslate2) with 8-bit quantized models for low-latency inference.
- [ ] Implement `POST /v1/audio/transcribe` endpoint accepting audio streams and returning text + token-level confidence scores.
- [ ] Integrate `ffmpeg` for automatic audio conversion (WhatsApp `.ogg`/Opus to 16kHz mono `.wav`).
- [ ] Create multi-stage `Dockerfile` with CPU/AVX-512 and CUDA GPU support.
- [ ] Implement basic health check and warm-up model inference at container boot.

**Deliverable:** Python FastAPI service transcribing benchmark audio files in < 1.5 seconds.

---

## Tuesday — Speech Model Tuning for Nigerian Accents & Pidgin

- [ ] Assemble evaluation dataset of Nigerian wholesale voice notes (English, Nigerian Pidgin, code-switched phrases).
- [ ] Configure acoustic adaptation prompts and vocabulary biasing for commodity names (*Mama Gold*, *Dangote Sugar*, *Garri Ijebu*).
- [ ] Benchmark Word Error Rate (WER) against raw Whisper; tune prompt context to handle vernacular trade terms (*"I wan buy"*, *"derica"*, *"paint rubber"*).
- [ ] Implement audio noise gate filter to suppress market background chatter and vehicular horns.

**Deliverable:** Transcription accuracy for Nigerian wholesale voice notes exceeds 88% on pilot test set.

---

## Wednesday — Named Entity Recognition (NER) & Trade Unit Extraction

- [ ] Implement rule-based and regex NER extractor in Python service:
  - Extracts `commodity` (e.g., "rice", "sugar", "white garri").
  - Extracts `quantity` (numeric or words: "twenty", "fifty").
  - Extracts `trade_unit` (*mudu*, *derica*, *paint rubber*, *50kg bag*, *carton*).
  - Extracts `delivery_destination` (market names, plazas, lines).
- [ ] Implement confidence scoring model combining acoustic confidence and entity match certainty.
- [ ] Return structured JSON payload with extraction certainty scores.

**Deliverable:** Speech service converts raw voice notes into structured `{commodity, quantity, trade_unit, destination}` DTOs.

---

## Thursday — Java Spring Boot Audio Ingestion Bridge

- [ ] Implement `econwa-ai-client` module in Java backend using Spring `RestClient`.
- [ ] Create async audio worker in Java:
  - When a WhatsApp voice note arrives, download media binary from Meta Graph API.
  - Stream audio to Cloudflare R2 / S3 storage and generate signed URL.
  - Dispatch audio processing job to Python microservice over HTTP/2.
- [ ] Receive structured extraction in Java callback.
- [ ] Implement Section 10 safeguard: **Zero Financial Authority** check (AI output is strictly advisory; never creates confirmed orders).

**Deliverable:** Voice note sent to WhatsApp number is downloaded, processed by AI, and returned to the Java backend as structured entities.

---

## Friday — Plain-Language Confirmation & Clarification Gates

- [ ] Implement Section 10 **Read-Back Requirement**:
  - Format structured output into a concise plain English summary:
    *"You requested: 20 x 50kg bags of Mama Gold Rice to Trade Fair Peace Plaza. Is this correct?"*
  - Deliver with interactive WhatsApp buttons: `[ ✅ Yes, Send to Suppliers ]` `[ ❌ Modify ]`.
- [ ] Implement Confidence Threshold Gate (< 0.85):
  - If confidence is low, trigger specific clarifying question (e.g., *"Did you mean 25kg or 50kg bags?"*).
  - If ambiguity persists, route to the Human-in-the-Loop ops triage queue on WhatsApp.

**Deliverable:** End-to-end voice RFQ parsing with mandatory buyer confirmation and clarifying fallback loops.

---

## Weekend / Catch-Up
- Stress-test the audio pipeline with concurrent voice notes.
- Review Section 4 local unit dictionary mappings for Week 5.
