package com.portfolio.backend.controller;

import com.portfolio.backend.dto.ContactSubmissionRequest;
import com.portfolio.backend.dto.ContactSubmissionResponse;
import com.portfolio.backend.service.ContactSubmissionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/contact")
@Validated
public class ContactSubmissionController {

    private final ContactSubmissionService service;

    public ContactSubmissionController(ContactSubmissionService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<ContactSubmissionResponse> submit(
            @RequestBody ContactSubmissionRequest request) {
        ContactSubmissionResponse response = service.submit(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }
}