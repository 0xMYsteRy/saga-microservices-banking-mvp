package com.mystery.sagaorchestrator.controller;

import com.mystery.common.entity.Payment;
import com.mystery.common.entity.User;
import com.mystery.common.util.SecurityUtil;
import com.mystery.sagaorchestrator.entity.SagaInstance;
import com.mystery.sagaorchestrator.saga.payment.PaymentProcessingSaga;
import com.mystery.sagaorchestrator.saga.payment.PaymentRequest;
import com.mystery.sagaorchestrator.saga.useronboarding.UserOnboardingSaga;
import com.mystery.sagaorchestrator.service.SagaStateManager;
import com.mystery.sagaorchestrator.util.SecurityUserUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Optional;

@Slf4j
@RestController
@RequestMapping("/api/saga")
@RequiredArgsConstructor
public class SagaController {

    private final UserOnboardingSaga userOnboardingSaga;
    private final PaymentProcessingSaga paymentProcessingSaga;
    private final SagaStateManager sagaStateManager;

    @GetMapping("/instances")
    @PreAuthorize("hasRole(T(com.mystery.common.AppConstants).ROLE_BAAS_ADMIN)")
    public ResponseEntity<List<SagaInstance>> getAllSagaInstances() {
        log.info("Fetching all saga instances");

        List<SagaInstance> sagaInstances = sagaStateManager.getAllSagaInstances();

        log.info("Returning {} saga instances", sagaInstances.size());
        return ResponseEntity.ok(sagaInstances);
    }

    @GetMapping("/instances/{id}")
    @PreAuthorize("hasRole(T(com.mystery.common.AppConstants).ROLE_BAAS_ADMIN) or isAuthenticated()")
    public ResponseEntity<?> getSagaInstanceById(@PathVariable Long id) {
        log.info("Fetching saga instance with id={}", id);

        Optional<SagaInstance> opt = sagaStateManager.getSagaInstanceById(id);
        return opt.map(ResponseEntity::ok)
                .orElseGet(() -> {
                    log.warn("Saga instance with id={} not found", id);
                    return ResponseEntity.notFound().build();
                });
    }

    @PostMapping("/instances/{id}/fail")
    @PreAuthorize("hasRole(T(com.mystery.common.AppConstants).ROLE_BAAS_ADMIN)")
    public ResponseEntity<String> failSagaInstance(@PathVariable Long id) {
        log.info("Failing saga instance id={}", id);

        boolean exists = sagaStateManager.getSagaInstanceById(id).isPresent();
        if (!exists) {
            log.warn("Cannot fail saga instance: id={} not found", id);
            return ResponseEntity.notFound().build();
        }

        sagaStateManager.failSaga(id);
        log.info("Saga instance id={} marked as FAILED", id);
        return ResponseEntity.accepted().body("Saga instance " + id + " marked as failed");
    }

    @PostMapping("/start/user-onboarding")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<String> startUserOnboardingSaga() {
        log.info("Starting user onboarding saga request");

        User user = SecurityUserUtil.buildUserFromSecurityContext();

        // Use Saga interface to start saga with payload - this will automatically trigger the first command
        SagaInstance sagaInstance = userOnboardingSaga.startSaga(user);

        log.info("Started user onboarding saga id={} for user={}", sagaInstance.getId(), user.getUsername());
        return ResponseEntity.accepted().body("User onboarding process started with saga ID: " + sagaInstance.getId());
    }

    // Endpoint to start payment processing saga
    @PostMapping("/start/payment-processing")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<String> startPaymentProcessingSaga(@RequestBody PaymentRequest paymentRequest) {
        log.info("Starting payment processing saga for from={} to={} amount={}", paymentRequest.getSourceAccountNumber(), paymentRequest.getDestinationAccountNumber(), paymentRequest.getAmount());

        Payment payment = new Payment();
        payment.setSourceAccountNumber(paymentRequest.getSourceAccountNumber());
        payment.setDestinationAccountNumber(paymentRequest.getDestinationAccountNumber());
        payment.setAmount(paymentRequest.getAmount());
        payment.setDescription(paymentRequest.getDescription());
        payment.setCreatedBy(SecurityUtil.getCurrentUsername());

        SagaInstance sagaInstance = paymentProcessingSaga.startSaga(payment);

        log.info("Payment processing saga started id={}", sagaInstance.getId());
        return ResponseEntity.accepted().body("Payment processing started with saga ID: " + sagaInstance.getId());
    }

}
