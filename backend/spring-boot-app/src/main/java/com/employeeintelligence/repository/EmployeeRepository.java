package com.employeeintelligence.api.repository;

import com.employeeintelligence.api.model.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface EmployeeRepository extends JpaRepository<Employee, Long> {

    @Query("""
            select e from Employee e
            where (:query is null
                or lower(e.name) like lower(concat('%', :query, '%'))
                or lower(e.department) like lower(concat('%', :query, '%'))
                or lower(e.jobRole) like lower(concat('%', :query, '%')))
              and (:department is null or lower(e.department) = lower(:department))
            """)
    Page<Employee> search(@Param("query") String query,
                          @Param("department") String department,
                          Pageable pageable);

	Optional<Employee> findByEmployeeNumber(Integer employeeNumber);
}
