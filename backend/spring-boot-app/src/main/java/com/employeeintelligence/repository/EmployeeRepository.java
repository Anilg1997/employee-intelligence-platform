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
    Optional<Employee> findByIdAndTenantId(Long id, String tenantId);
    boolean existsByIdAndTenantId(Long id, String tenantId);
    void deleteByIdAndTenantId(Long id, String tenantId);
}
