package com.mystery.loan.service;

import com.mystery.loan.model.Loan;
import com.mystery.loan.model.LoanApplication;
import com.mystery.loan.repository.LoanApplicationRepository;
import com.mystery.loan.repository.LoanRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class LoanService {

    private final LoanApplicationRepository appRepo;
    private final LoanRepository loanRepo;
    private final CreditScoringService creditScoringService;
    private final InterestEngine interestEngine;

    public LoanApplication apply(LoanApplication application) {
        application.setStatus("PENDING");
        application.setCreatedAt(LocalDateTime.now());
        return appRepo.save(application);
    }

    public Optional<LoanApplication> findApplication(Long id) {
        return appRepo.findById(id);
    }

    @Transactional
    public Loan approve(Long applicationId, double annualRate) {
        LoanApplication app = appRepo.findById(applicationId).orElseThrow();
        app.setStatus("APPROVED");
        appRepo.save(app);

        Loan loan = Loan.builder()
                .applicationId(app.getId())
                .borrowerId(app.getApplicantId())
                .principal(app.getAmount())
                .outstanding(app.getAmount())
                .interestRate(annualRate)
                .disbursedAt(LocalDateTime.now())
                .build();

        return loanRepo.save(loan);
    }

    @Transactional
    public void reject(Long applicationId, String reason) {
        LoanApplication app = appRepo.findById(applicationId).orElseThrow();
        app.setStatus("REJECTED");
        appRepo.save(app);
        // reason could be logged/audited
    }

    public BigDecimal calculateAccruedInterest(Long loanId, int months) {
        Loan loan = loanRepo.findById(loanId).orElseThrow();
        return interestEngine.calculateAccruedInterest(loan.getOutstanding(), loan.getInterestRate(), months);
    }
}

