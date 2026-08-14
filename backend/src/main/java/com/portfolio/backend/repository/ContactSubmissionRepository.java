package com.portfolio.backend.repository;

import com.portfolio.backend.model.ContactSubmission;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ContactSubmissionRepository extends MongoRepository<ContactSubmission, String> {
}