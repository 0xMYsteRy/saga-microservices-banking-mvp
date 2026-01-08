package com.mystery.loan.controller;

import com.mystery.loan.model.Loan;
import com.mystery.loan.model.LoanApplication;
import com.mystery.loan.saga.LoanApprovalSaga;
import com.mystery.loan.service.CreditScoringService;
import com.mystery.loan.service.LoanService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Optional;

@RestController
@RequestMapping("/api/loan")
@RequiredArgsConstructor
public class LoanController {

    private final LoanService loanService;
    private final CreditScoringService scoringService;
    private final LoanApprovalSaga loanApprovalSaga;

    @PostMapping("/apply")
    public ResponseEntity<LoanApplication> apply(@RequestBody LoanApplication application) {
        LoanApplication saved = loanService.apply(application);
        // start small approval saga asynchronously
        String sagaId = loanApprovalSaga.startSaga(saved.getId());
        return ResponseEntity.accepted().body(saved);
    }

    @GetMapping("/application/{id}")
    public ResponseEntity<?> getApplication(@PathVariable Long id) {
        Optional<LoanApplication> app = loanService.findApplication(id);
        return app.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping("/application/{id}/approve")
    public ResponseEntity<Loan> approve(@PathVariable Long id, @RequestParam(defaultValue = "0.05") double annualRate) {
        // basic scoring guardian
        LoanApplication app = loanService.findApplication(id).orElseThrow();
        int score = scoringService.scoreApplicant(app.getApplicantId());
        if (!scoringService.isAcceptable(score, 500)) {
            loanService.reject(id, "Low credit score: " + score);
            return ResponseEntity.status(403).build();
        }
        Loan loan = loanService.approve(id, annualRate);
        return ResponseEntity.ok(loan);
    }

    @PostMapping("/application/{id}/reject")
    public ResponseEntity<Void> reject(@PathVariable Long id, @RequestParam(required = false) String reason) {
        loanService.reject(id, reason == null ? "Rejected by reviewer" : reason);
        return ResponseEntity.accepted().build();
    }

    @GetMapping("/loan/{id}/interest")
    public ResponseEntity<BigDecimal> getAccruedInterest(@PathVariable Long id, @RequestParam int months) {
        BigDecimal interest = loanService.calculateAccruedInterest(id, months);
        return ResponseEntity.ok(interest);
    }
}

