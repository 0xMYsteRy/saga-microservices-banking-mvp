package com.mystery.sagaorchestrator.service;

import com.mystery.sagaorchestrator.entity.SagaInstance;

import java.util.List;
import java.util.Optional;

public interface SagaStateManager {

    // Saga lifecycle
    SagaInstance startSaga(String sagaName);
    void completeSaga(Long sagaId);
    void failSaga(Long sagaId);
    
    // Step lifecycle (with mandatory payload for audit trail)
    void startStep(Long sagaId, String stepName, Object payload);
    void completeStep(Long sagaId, String stepName, Object payload);
    void failStep(Long sagaId, String stepName, Object errorMessage);

    // Saga query operations
    List<SagaInstance> getAllSagaInstances();

    // Get single saga instance by id
    Optional<SagaInstance> getSagaInstanceById(Long sagaId);

}
