package com.employeeintelligence.api.dto;
import static org.junit.jupiter.api.Assertions.assertFalse;
import java.lang.reflect.Field;
import org.junit.jupiter.api.Test;
class SalaryPredictionContractTest {
    @Test void salaryRequestDoesNotAcceptTargetAsAFeature() {
        for (Field field : SalaryPredictionRequest.class.getDeclaredFields()) assertFalse(field.getName().equalsIgnoreCase("monthlyIncome"));
    }
}
