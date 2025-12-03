# AI Powered Expense & Subscription Manager for Family Finance

## Introduction
Modern households manage a diverse range of financial responsibilities. The **AI Powered Expense & Subscription Manager for Family Finance** is a system designed to integrate natural language processing (NLP), machine learning (ML), OCR, and behavioral analytics to automatically extract financial information from SMS messages, emails, receipts, bank statements, and mobile app usage patterns.

## Aims & Objectives
The project aims to develop an intelligent, automated financial management system that uses AI/ML to extract, analyze, and categorize expenses, detect subscriptions, and provide predictive insights through Personal and Family dashboards.

### Key Objectives
1. **Automate Financial Data Extraction**: From SMS, emails, receipts (OCR), and app usage logs.
2. **Classify Expenses**: Using ML to categorize expenses (e.g., groceries, utilities).
3. **Detect Subscriptions**: Identify recurring payments and renewal cycles.
4. **Dual Dashboards**: Separate Personal and Family dashboards for privacy and collaboration.
5. **Predictive Analytics**: Forecast future expenses and detect spending anomalies.

## Proposed Methodology
The system follows a pipeline:
1. **Data Acquisition**: SMS, Email, OCR, Manual Input.
2. **Preprocessing**: Noise removal, normalization.
3. **NLP Engine**: Entity extraction (Amount, Merchant, Date).
4. **Expense Classification**: ML-based categorization.
5. **Subscription Detection**: Interval analysis and pattern recognition.
6. **Forecasting**: Time-series prediction (e.g., LSTM, Prophet).
7. **Visualization**: Personal and Family Dashboards.

## Tech Stack
- **Language**: Python
- **ML/AI**: TensorFlow, Transformers, Scikit-learn
- **Data**: Pandas, NumPy
- **OCR**: Tesseract / Deep Learning models

## Directory Structure
- `src/`: Source code for the application.
- `tests/`: Unit and integration tests.
- `data/`: Sample data for testing and development.
- `docs/`: Project documentation.
