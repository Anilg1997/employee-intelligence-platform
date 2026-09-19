package com.employeeintelligence.api.service;

import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import com.employeeintelligence.api.model.Employee;
import com.employeeintelligence.api.repository.EmployeeRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
public class EmployeeCsvImporter implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(EmployeeCsvImporter.class);

    private final EmployeeRepository employeeRepository;
    private final Path csvPath;

    public EmployeeCsvImporter(
            EmployeeRepository employeeRepository,
            @Value("${employee.import.file:C:/Users/Lenovo/Downloads/WA_Fn-UseC_-HR-Employee-Attrition.csv}") String csvFile) {
        this.employeeRepository = employeeRepository;
        this.csvPath = Path.of(csvFile);
    }

    @Override
    public void run(String... args) throws IOException {
        if (!Files.exists(csvPath)) {
            logger.info("Employee CSV not found at {}; skipping import", csvPath);
            return;
        }

        CsvSchema schema = CsvSchema.emptySchema().withHeader();
        ObjectReader reader = new CsvMapper().readerFor(Map.class).with(schema);
        Map<Integer, Employee> existingEmployees = new HashMap<>();
        employeeRepository.findAll().stream()
            .filter(employee -> employee.getEmployeeNumber() != null)
            .forEach(employee -> existingEmployees.put(employee.getEmployeeNumber(), employee));
        List<Employee> employeesToSave = new ArrayList<>();

        try (MappingIterator<Map<String, String>> rows = reader.readValues(csvPath.toFile())) {
            while (rows.hasNext()) {
                Map<String, String> row = rows.next();
                Integer employeeNumber = integer(row, "EmployeeNumber");
            Employee employee = existingEmployees.getOrDefault(employeeNumber, new Employee());

                employee.setName("Employee " + employeeNumber);
                employee.setAge(integer(row, "Age"));
                employee.setAttrition(row.get("Attrition"));
                employee.setBusinessTravel(row.get("BusinessTravel"));
                employee.setDailyRate(integer(row, "DailyRate"));
                employee.setDepartment(row.get("Department"));
                employee.setDistanceFromHome(integer(row, "DistanceFromHome"));
                employee.setEducation(integer(row, "Education"));
                employee.setEducationField(row.get("EducationField"));
                employee.setEmployeeCount(integer(row, "EmployeeCount"));
                employee.setEmployeeNumber(employeeNumber);
                employee.setEnvironmentSatisfaction(integer(row, "EnvironmentSatisfaction"));
                employee.setGender(row.get("Gender"));
                employee.setHourlyRate(integer(row, "HourlyRate"));
                employee.setJobInvolvement(integer(row, "JobInvolvement"));
                employee.setJobLevel(integer(row, "JobLevel"));
                employee.setJobRole(row.get("JobRole"));
                employee.setJobSatisfaction(integer(row, "JobSatisfaction"));
                employee.setMaritalStatus(row.get("MaritalStatus"));
                employee.setMonthlyIncome(integer(row, "MonthlyIncome"));
                employee.setMonthlyRate(integer(row, "MonthlyRate"));
                employee.setNumCompaniesWorked(integer(row, "NumCompaniesWorked"));
                employee.setOver18(row.get("Over18"));
                employee.setOverTime(row.get("OverTime"));
                employee.setPercentSalaryHike(integer(row, "PercentSalaryHike"));
                employee.setPerformanceRating(integer(row, "PerformanceRating"));
                employee.setRelationshipSatisfaction(integer(row, "RelationshipSatisfaction"));
                employee.setStandardHours(integer(row, "StandardHours"));
                employee.setStockOptionLevel(integer(row, "StockOptionLevel"));
                employee.setTotalWorkingYears(integer(row, "TotalWorkingYears"));
                employee.setTrainingTimesLastYear(integer(row, "TrainingTimesLastYear"));
                employee.setWorkLifeBalance(integer(row, "WorkLifeBalance"));
                employee.setYearsAtCompany(integer(row, "YearsAtCompany"));
                employee.setYearsInCurrentRole(integer(row, "YearsInCurrentRole"));
                employee.setYearsSinceLastPromotion(integer(row, "YearsSinceLastPromotion"));
                employee.setYearsWithCurrManager(integer(row, "YearsWithCurrManager"));

                employeesToSave.add(employee);
            }
        }

        employeeRepository.saveAll(employeesToSave);
        logger.info("Imported {} employee records from {}", employeesToSave.size(), csvPath);
    }

    private Integer integer(Map<String, String> row, String column) {
        String value = row.get(column);
        return value == null || value.isBlank() ? null : Integer.valueOf(value);
    }
}
