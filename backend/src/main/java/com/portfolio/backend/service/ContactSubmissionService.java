package com.portfolio.backend.service;

import com.portfolio.backend.dto.ContactSubmissionRequest;
import com.portfolio.backend.dto.ContactSubmissionResponse;
import com.portfolio.backend.model.ContactSubmission;
import com.portfolio.backend.repository.ContactSubmissionRepository;
import org.springframework.stereotype.Service;

@Service
public class ContactSubmissionService {

    private final ContactSubmissionRepository repository;

    public ContactSubmissionService(ContactSubmissionRepository repository) {
        this.repository = repository;
    }

    public ContactSubmissionResponse submit(ContactSubmissionRequest request) {
        ContactSubmission entity = new ContactSubmission(
                request.getName(),
                request.getEmail(),
                request.getSubject(),
                request.getMessage()
        );
        ContactSubmission saved = repository.save(entity);
        return new ContactSubmissionResponse(saved);
    }
}