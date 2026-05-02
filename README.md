# HackerRank Orchestrate – Support Triage Agent (Java/Gradle)

A **terminal-based, multi-domain support triage agent** built using **Java 17, Spring Boot 3.2, and Gradle**.

The system processes support tickets across **HackerRank, Claude, and Visa** ecosystems using a **Retrieval-Augmented Generation (RAG)** pipeline with **risk-aware escalation logic** to ensure safe, grounded, and reliable responses.

---

## 🎯 Problem Approach

Each ticket is processed through a deterministic pipeline:

1. **Classify** → domain, product area, request type
2. **Retrieve** → top-K relevant documents from corpus
3. **Assess Risk** → decide reply vs escalate
4. **Generate Response** → grounded, safe answer
5. **Log Decision** → full reasoning written to `log.txt`

This ensures **no hallucination, traceable decisions, and consistent outputs**.

---

## 🏗️ System Architecture

```
Input CSV
   ↓
TicketClassifier
   ↓
DocumentRetriever (Top-K = 3)
   ↓
RiskAssessor
   ↓
ResponseGenerator
   ↓
Output CSV + log.txt
```

---

## 🧠 Key Design Decisions

### 1. Corpus-Only Grounding

* No external APIs used for answering
* All responses are derived from provided support documents
* Prevents hallucinated policies

---

### 2. Conservative Risk Strategy

The system prioritizes **safety over coverage**.

Escalation is triggered when:

* Account access / authentication issues
* Billing or payment-related queries
* Fraud, compliance, or sensitive data
* Missing or low-confidence documentation
* Ambiguous or invalid inputs

👉 Rationale: Incorrect automated responses in these areas are high-risk.

---

### 3. Deterministic Processing

* No randomness in classification or retrieval
* Same input → same output
* Ensures reproducibility for evaluation

---

### 4. Explainability (Critical for Judge)

Each decision is logged in `log.txt` with:

* Ticket input
* Classification results
* Retrieved documents (summary)
* Risk decision
* Final justification

---

## ⚙️ Components

### TicketClassifier

* Identifies:

  * Domain (HackerRank / Claude / Visa)
  * Request Type (product_issue, bug, feature_request, invalid)
  * Product Area (Account, Billing, Technical, etc.)

---

### DocumentRetriever

* Loads ~700+ documents from corpus
* Uses keyword-based relevance scoring
* Returns top 3 matches per ticket

---

### RiskAssessor

* Core decision engine
* Determines:

  * `replied` → safe + supported
  * `escalated` → high-risk or unsupported

---

### ResponseGenerator

* Builds responses using retrieved documents
* Ensures:

  * grounded answers
  * no fabricated policies
  * safe tone

---

### AgentLogger

* Writes detailed reasoning to `log.txt`
* Enables auditability and interview explanation

---

## ▶️ How to Run

```bash
./gradlew bootRun
```

Execution:

* Loads corpus
* Reads `support_tickets/support_tickets.csv`
* Processes all tickets
* Outputs:

  * `support_tickets/output.csv`
  * `log.txt`

---

## 📊 Output Format

| Column        | Description                                     |
| ------------- | ----------------------------------------------- |
| status        | replied / escalated                             |
| product_area  | Classified domain area                          |
| response      | Generated answer                                |
| justification | Reason for decision                             |
| request_type  | product_issue / bug / feature_request / invalid |

---

## 📈 Sample Results

* Total Tickets: 29
* Replied: 12 (41%)
* Escalated: 17 (59%)

### Interpretation:

* High escalation rate reflects **safety-first design**
* Replies only generated when supported by corpus

---

## 🤖 AI Usage

AI tools were used for:

* Structuring the pipeline architecture
* Improving classification and retrieval logic
* Validating edge cases and safety rules

Final implementation, logic, and validation were manually verified.

---

## 📁 Submission Artifacts

* Code (Java Spring Boot project)
* `output.csv` (predictions)
* `log.txt` (decision transcript)

---

## 🧪 Edge Cases Handled

* Missing or empty ticket descriptions
* Unsupported queries (no matching documents)
* Ambiguous intent
* Multi-domain overlap
* High-risk sensitive requests

---

## 🚀 Summary

This system is designed to be:

* **Safe** → avoids risky automated responses
* **Accurate** → grounded in real documentation
* **Explainable** → every decision is logged
* **Deterministic** → consistent and reproducible

Prioritizing **correct escalation over incorrect automation** ensures reliability in real-world support scenarios.
