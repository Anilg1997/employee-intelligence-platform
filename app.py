import streamlit as st
import requests


API_URL = "http://127.0.0.1:8000/predict"


# --------------------------------------------------
# Page Configuration
# --------------------------------------------------

st.set_page_config(
    page_title="Employee Attrition Prediction",
    page_icon="👨‍💼",
    layout="wide"
)


# --------------------------------------------------
# Header
# --------------------------------------------------

st.title("👨‍💼 Employee Attrition Prediction System")

st.markdown(
    """
    **HR Analytics Dashboard**

    Enter employee information below to estimate the probability
    of employee attrition.
    """
)

st.divider()


# --------------------------------------------------
# Employee Information
# --------------------------------------------------

st.header("📋 Employee Information")


col1, col2, col3 = st.columns(3)


# --------------------------------------------------
# Column 1
# --------------------------------------------------

with col1:

    Age = st.number_input(
        "Age",
        min_value=18,
        max_value=60,
        value=30
    )

    BusinessTravel = st.selectbox(
        "Business Travel",
        [
            "Travel_Rarely",
            "Travel_Frequently",
            "Non-Travel"
        ]
    )

    DailyRate = st.number_input(
        "Daily Rate",
        min_value=100,
        max_value=1500,
        value=800
    )

    Department = st.selectbox(
        "Department",
        [
            "Sales",
            "Research & Development",
            "Human Resources"
        ]
    )

    DistanceFromHome = st.number_input(
        "Distance From Home",
        min_value=1,
        max_value=30,
        value=10
    )

    Education = st.selectbox(
        "Education",
        [1, 2, 3, 4, 5]
    )

    EducationField = st.selectbox(
        "Education Field",
        [
            "Life Sciences",
            "Medical",
            "Marketing",
            "Technical Degree",
            "Human Resources",
            "Other"
        ]
    )

    EnvironmentSatisfaction = st.selectbox(
        "Environment Satisfaction",
        [1, 2, 3, 4]
    )

    Gender = st.selectbox(
        "Gender",
        ["Male", "Female"]
    )

    HourlyRate = st.number_input(
        "Hourly Rate",
        min_value=30,
        max_value=100,
        value=60
    )


# --------------------------------------------------
# Column 2
# --------------------------------------------------

with col2:

    JobInvolvement = st.selectbox(
        "Job Involvement",
        [1, 2, 3, 4]
    )

    JobLevel = st.selectbox(
        "Job Level",
        [1, 2, 3, 4, 5]
    )

    JobRole = st.selectbox(
        "Job Role",
        [
            "Sales Executive",
            "Research Scientist",
            "Laboratory Technician",
            "Manufacturing Director",
            "Healthcare Representative",
            "Manager",
            "Sales Representative",
            "Research Director",
            "Human Resources"
        ]
    )

    JobSatisfaction = st.selectbox(
        "Job Satisfaction",
        [1, 2, 3, 4]
    )

    MaritalStatus = st.selectbox(
        "Marital Status",
        [
            "Single",
            "Married",
            "Divorced"
        ]
    )

    MonthlyIncome = st.number_input(
        "Monthly Income",
        min_value=1000,
        max_value=20000,
        value=5000
    )

    MonthlyRate = st.number_input(
        "Monthly Rate",
        min_value=2000,
        max_value=27000,
        value=15000
    )

    NumCompaniesWorked = st.number_input(
        "Number of Companies Worked",
        min_value=0,
        max_value=10,
        value=2
    )

    OverTime = st.selectbox(
        "Overtime",
        ["Yes", "No"]
    )

    PercentSalaryHike = st.number_input(
        "Percent Salary Hike",
        min_value=11,
        max_value=25,
        value=15
    )


# --------------------------------------------------
# Column 3
# --------------------------------------------------

with col3:

    PerformanceRating = st.selectbox(
        "Performance Rating",
        [1, 2, 3, 4]
    )

    RelationshipSatisfaction = st.selectbox(
        "Relationship Satisfaction",
        [1, 2, 3, 4]
    )

    StockOptionLevel = st.selectbox(
        "Stock Option Level",
        [0, 1, 2, 3]
    )

    TotalWorkingYears = st.number_input(
        "Total Working Years",
        min_value=0,
        max_value=40,
        value=8
    )

    TrainingTimesLastYear = st.number_input(
        "Training Times Last Year",
        min_value=0,
        max_value=10,
        value=3
    )

    WorkLifeBalance = st.selectbox(
        "Work Life Balance",
        [1, 2, 3, 4]
    )

    YearsAtCompany = st.number_input(
        "Years At Company",
        min_value=0,
        max_value=40,
        value=3
    )

    YearsInCurrentRole = st.number_input(
        "Years In Current Role",
        min_value=0,
        max_value=20,
        value=2
    )

    YearsSinceLastPromotion = st.number_input(
        "Years Since Last Promotion",
        min_value=0,
        max_value=15,
        value=1
    )

    YearsWithCurrManager = st.number_input(
        "Years With Current Manager",
        min_value=0,
        max_value=20,
        value=2
    )


# --------------------------------------------------
# Prediction Button
# --------------------------------------------------

st.divider()

predict_button = st.button(
    "🔍 Predict Attrition Risk",
    type="primary",
    use_container_width=True
)


# --------------------------------------------------
# Prediction
# --------------------------------------------------

if predict_button:

    employee_data = {

        "Age": Age,
        "BusinessTravel": BusinessTravel,
        "DailyRate": DailyRate,
        "Department": Department,
        "DistanceFromHome": DistanceFromHome,
        "Education": Education,
        "EducationField": EducationField,
        "EnvironmentSatisfaction": EnvironmentSatisfaction,
        "Gender": Gender,
        "HourlyRate": HourlyRate,
        "JobInvolvement": JobInvolvement,
        "JobLevel": JobLevel,
        "JobRole": JobRole,
        "JobSatisfaction": JobSatisfaction,
        "MaritalStatus": MaritalStatus,
        "MonthlyIncome": MonthlyIncome,
        "MonthlyRate": MonthlyRate,
        "NumCompaniesWorked": NumCompaniesWorked,
        "OverTime": OverTime,
        "PercentSalaryHike": PercentSalaryHike,
        "PerformanceRating": PerformanceRating,
        "RelationshipSatisfaction": RelationshipSatisfaction,
        "StockOptionLevel": StockOptionLevel,
        "TotalWorkingYears": TotalWorkingYears,
        "TrainingTimesLastYear": TrainingTimesLastYear,
        "WorkLifeBalance": WorkLifeBalance,
        "YearsAtCompany": YearsAtCompany,
        "YearsInCurrentRole": YearsInCurrentRole,
        "YearsSinceLastPromotion": YearsSinceLastPromotion,
        "YearsWithCurrManager": YearsWithCurrManager
    }

    try:

        with st.spinner("Analyzing employee information..."):

            response = requests.post(
                API_URL,
                json=employee_data,
                timeout=10
            )


        if response.status_code == 200:

            result = response.json()

            prediction = result["attrition_prediction"]
            probability = result["attrition_probability"]

            risk_percentage = probability * 100


            # ------------------------------------------
            # Result Section
            # ------------------------------------------

            st.divider()

            st.header("📊 Prediction Result")


            result_col1, result_col2 = st.columns(2)


            with result_col1:

                st.metric(
                    "Attrition Probability",
                    f"{risk_percentage:.1f}%"
                )


            with result_col2:

                if prediction == "Yes":

                    st.metric(
                        "Prediction",
                        "High Risk"
                    )

                else:

                    st.metric(
                        "Prediction",
                        "Low Risk"
                    )


            # ------------------------------------------
            # Risk Progress Bar
            # ------------------------------------------

            st.subheader("Risk Score")

            st.progress(
                min(probability, 1.0)
            )


            # ------------------------------------------
            # Business Interpretation
            # ------------------------------------------

            if probability >= 0.70:

                st.error(
                    "🔴 High Attrition Risk — "
                    "This employee has a high estimated probability "
                    "of leaving the organization."
                )

            elif probability >= 0.40:

                st.warning(
                    "🟡 Medium Attrition Risk — "
                    "This employee shows some indicators of potential "
                    "attrition."
                )

            else:

                st.success(
                    "🟢 Low Attrition Risk — "
                    "This employee has a relatively low estimated "
                    "probability of attrition."
                )


            st.info(
                "Note: This prediction is a machine-learning estimate "
                "and should support HR decision-making rather than "
                "replace human judgment."
            )


        else:

            st.error(
                f"API returned an error: {response.status_code}"
            )


    except requests.exceptions.RequestException:

        st.error(
            "❌ Unable to connect to the prediction API. "
            "Please make sure FastAPI is running on "
            "http://127.0.0.1:8000."
        )