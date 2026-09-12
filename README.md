# employee-attrition-ml

An end-to-end Machine Learning application that predicts whether an employee is likely to leave an organization based on employee and workplace attributes.

The project covers the complete ML lifecycle:

**Data → EDA → Preprocessing → Model Training → Evaluation → Model Persistence → FastAPI → Streamlit → Testing**

---

## Business Problem

Employee attrition can increase recruitment costs, reduce productivity, and create workforce planning challenges.

This project builds a machine learning system that estimates an employee's attrition risk so HR teams can identify employees who may require further attention.

> This project uses a public IBM HR Analytics dataset and is intended for learning and portfolio demonstration. It does not use confidential or internal company data.

---

## Project Objectives

- Analyze employee attrition patterns
- Identify factors associated with employee attrition
- Build and compare multiple classification models
- Handle class imbalance
- Select a prediction threshold using validation data
- Evaluate the final model on an untouched test set
- Save the trained ML pipeline
- Expose predictions through a REST API
- Build an interactive Streamlit dashboard
- Add automated tests
- Package the project for GitHub

---

## Dataset

The project uses the **IBM HR Analytics Employee Attrition & Performance** dataset.

Dataset characteristics:

- 1,470 employee records
- 35 original columns
- Target variable: `Attrition`
- `No`: 1,233 employees
- `Yes`: 237 employees

The target is imbalanced, with significantly fewer employees in the `Yes` class.

---

## Exploratory Data Analysis

The analysis investigated:

- Missing values
- Duplicate records
- Attrition distribution
- Overtime and attrition
- Job satisfaction
- Monthly income
- Age
- Years at company
- Feature correlations
- Constant and identifier columns

### Key observations

Employees working overtime showed a higher observed attrition rate than employees who did not work overtime.

Employees who had left the organization had lower average monthly income, lower average age, and fewer average years at the company than employees who remained.

These are statistical associations in the dataset and should not be interpreted as causal relationships.

---

## Machine Learning Models

The following models were evaluated:

1. Logistic Regression
2. Decision Tree
3. Random Forest
4. Balanced Logistic Regression

Because the target variable is imbalanced, class weighting was used for the final Logistic Regression model.

### Final Model

**Balanced Logistic Regression**

Configuration:

```text
class_weight = "balanced"
max_iter = 1000
random_state = 42