package com.employeeintelligence.api.repository;

import com.employeeintelligence.api.model.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.List;

public interface EmployeeRepository extends JpaRepository<Employee, Long> {

    @Query("""
            select e from Employee e
            where e.tenantId = :tenantId and (cast(:query as string) is null
                or lower(e.name) like lower(concat('%', cast(:query as string), '%'))
                or lower(e.department) like lower(concat('%', cast(:query as string), '%'))
                or lower(e.jobRole) like lower(concat('%', cast(:query as string), '%')))
              and (cast(:department as string) is null or lower(e.department) = lower(cast(:department as string)))
            """)
    Page<Employee> search(@Param("tenantId") String tenantId, @Param("query") String query,
                          @Param("department") String department,
                          Pageable pageable);

	Optional<Employee> findByEmployeeNumberAndTenantId(Integer employeeNumber, String tenantId);
    List<Employee> findAllByTenantId(String tenantId);
    interface WorkforceAggregate {
        String getDepartment();
        Long getEmployeeCount();
        Long getAttritionCount();
        Long getRetainedCount();
        Long getAgeCount();
        Long getAgeTotal();
        Long getIncomeCount();
        Long getIncomeTotal();
    }

    interface PerformanceAggregate {
        Integer getRating();
        Long getEmployeeCount();
    }

    // Aggregate in the database: no employee entities, per-employee queries or row caps.
    @Query("""
            select nullif(trim(e.department), '') as department, count(e) as employeeCount,
                sum(case when lower(trim(e.attrition)) = 'yes' then 1 else 0 end) as attritionCount,
                sum(case when lower(trim(e.attrition)) = 'no' then 1 else 0 end) as retainedCount,
                count(e.age) as ageCount, sum(e.age) as ageTotal,
                count(e.monthlyIncome) as incomeCount, sum(e.monthlyIncome) as incomeTotal
            from Employee e where e.tenantId = :tenantId
            group by nullif(trim(e.department), '')
            """)
    List<WorkforceAggregate> summarizeWorkforce(@Param("tenantId") String tenantId);

    @Query("""
            select e.performanceRating as rating, count(e) as employeeCount
            from Employee e where e.tenantId = :tenantId group by e.performanceRating
            """)
    List<PerformanceAggregate> summarizePerformance(@Param("tenantId") String tenantId);
    Optional<Employee> findByIdAndTenantId(Long id, String tenantId);
    boolean existsByIdAndTenantId(Long id, String tenantId);
    void deleteByIdAndTenantId(Long id, String tenantId);
}
