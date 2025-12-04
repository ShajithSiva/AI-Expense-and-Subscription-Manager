# Financial Tracker App

![Status](https://img.shields.io/badge/Status-Active%20Development-brightgreen)
![FinTech](https://img.shields.io/badge/Domain-FinTech-blue)

**Smart Expense Tracking from SMS, Emails & Receipts (All‑in‑One System)**

---

## Description

The **Financial Tracker App** is an intelligent personal finance management system that automatically extracts, organizes, and analyzes financial information from multiple sources:

* Bank SMS alerts
* Other transaction-related SMS (bills, food delivery, online shopping, fuel, subscriptions)
* Emails
* Digital and printed receipts

Using **OCR (Optical Character Recognition)** and **NLP (Natural Language Processing)**, the system accurately extracts key transaction details such as:

* Amount
* Date
* Merchant
* Category
* Transaction type (Credit / Debit)
* Account reference & payment method

The app intelligently differentiates **bank SMS from other transaction-related SMS**, significantly improving categorization and analysis accuracy.

It also supports **family collaboration**, allowing multiple family members to connect their SMS and emails to a **shared family dashboard** for tracking combined expenses, incomes, and budgets.

✅ Initial language support: **English**
🔜 Planned support: **Tamil & Sinhala**

---

## Features

* Multi-source automatic expense extraction
* Smart classification of bank SMS vs other transaction SMS
* Family dashboard for collaborative financial tracking
* Recurring subscription detection and notifications
* Budget alerts, bill reminders, and daily spending insights
* Interactive charts and income vs expense progress bars
* Monthly and yearly spending summaries
* Secure data storage with privacy protection
* Simple and easy-to-use user interface

---

## Tech Stack

* **Frontend:** React Native / Flutter
* **Backend:** Node.js / Express
* **Database:** MongoDB / PostgreSQL
* **AI Engine:** Python (PyTorch / TensorFlow)
* **OCR & NLP:** Tesseract OCR, spaCy, NLTK
* **Notifications:** Firebase / Push APIs
* **Version Control:** Git / GitHub

---

## Datasets

* **Bank SMS Dataset:** Sample anonymized SMS alerts from local banks
* **Other Transaction SMS Dataset:** Food delivery, utility bills, fuel, subscriptions, online shopping
* **Email Dataset:** Transactional emails from e-commerce and utility providers
* **Receipts Dataset:** Scanned digital and printed receipts for OCR testing
* **Custom Dataset Creation:** Users can add their own SMS, emails, and receipts for model training and personalization

---

## Model Pipeline

1. **Data Collection**

   * Gather SMS, emails, and receipt images

2. **Preprocessing**

   * Clean text data
   * Normalize dates and currencies
   * Remove duplicate transactions

3. **OCR Processing**

   * Convert receipt images into text using Tesseract OCR

4. **NLP Classification & Extraction**

   * Extract amount, merchant, date, category, and transaction type
   * Classify messages as bank-related or non-bank transaction

5. **Family Aggregation**

   * Merge financial data from multiple users into a shared family dashboard

6. **Visualization & Alerts**

   * Generate charts, summaries, budget alerts, and notifications

---

## UI Structure

* **Home Dashboard:** Daily spending summary, monthly overview, income vs expense
* **Family Dashboard:** Combined family expenses, incomes, and budgets
* **Transactions Page:** Complete list of categorized transactions
* **Subscription Tracker:** Recurring subscriptions and renewal alerts
* **Reports & Charts:** Visual spending patterns and trends
* **Settings:** SMS, email integration, notifications, and account preferences

---

## Folder Structure

```bash
financial-tracker-app/
├── app/            # Mobile application source code
├── backend/        # Backend server and APIs
├── ai-engine/      # AI models (OCR & NLP)
├── datasets/       # Sample SMS, email & receipt datasets
├── docs/           # Documentation, diagrams, SRS
├── .github/        # Issue & PR templates
├── .gitignore
├── README.md
└── LICENSE
```

---

## Installation

### Clone Repository

```bash
git clone https://github.com/your-username/financial-tracker-app.git
cd financial-tracker-app
```

### Backend Setup

```bash
cd backend
npm install
npm run dev
```

### Mobile App Setup

```bash
cd ../app
npm install
npm run start
```

### AI Engine Setup

```bash
cd ../ai-engine
pip install -r requirements.txt
```

---

## Usage

1. Start the backend server
2. Launch the mobile application
3. AI engine runs automatically in the background
4. Access dashboards, reports, subscriptions, and family expense summaries

---

## Contributing

We welcome contributions from the community:

* Fork the repository
* Create a new branch for features or bug fixes
* Commit changes with clear and descriptive messages
* Submit a pull request referencing related issues

---

## License

MIT License. See the `LICENSE` file for more information.

---

## Contact

For queries, suggestions, or collaboration:
**[your-email@example.com](mailto:your-email@example.com)**
