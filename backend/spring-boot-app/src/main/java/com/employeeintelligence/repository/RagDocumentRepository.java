package com.employeeintelligence.api.repository;

import com.employeeintelligence.api.model.RagDocument;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RagDocumentRepository extends JpaRepository<RagDocument, Long> {
}