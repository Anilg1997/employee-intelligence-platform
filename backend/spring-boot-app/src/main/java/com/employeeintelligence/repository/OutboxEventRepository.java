package com.employeeintelligence.api.repository;

import com.employeeintelligence.api.model.OutboxEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface OutboxEventRepository extends JpaRepository<OutboxEvent, Long> {
    @Query(value = "SELECT * FROM outbox_events WHERE published_at IS NULL ORDER BY created_at, id LIMIT :limit FOR UPDATE SKIP LOCKED", nativeQuery = true)
    List<OutboxEvent> lockPending(@Param("limit") int limit);
}
