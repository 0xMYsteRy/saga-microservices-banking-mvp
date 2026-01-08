package com.mystery.loan.saga;

import org.springframework.stereotype.Component;

@Component
public class LoanApprovalSaga {

    // Placeholder saga orchestration; integrate with saga-orchestrator-service for full flows.
    public String startSaga(Long applicationId) {
        // In a real system, send events to orchestrator, etc.
        return "saga-" + applicationId;
    }
}

