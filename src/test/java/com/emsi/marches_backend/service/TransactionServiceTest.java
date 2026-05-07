package com.emsi.marches_backend.service;

import com.emsi.marches_backend.exception.BusinessException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    @InjectMocks
    private TransactionService transactionService;

    @Test
    void operationCritique_shouldExecuteAction() {
        transactionService.operationCritique(() -> {
            // Action réussie
        });
    }

    @Test
    void operationCritique_shouldThrowBusinessExceptionOnError() {
        assertThatThrownBy(() -> transactionService.operationCritique(() -> {
            throw new RuntimeException("Erreur simulée");
        })).isInstanceOf(BusinessException.class);
    }

    @Test
    void operationIndependante_shouldExecuteAction() {
        transactionService.operationIndependante(() -> {
            // Action réussie
        });
    }

    @Test
    void operationLecture_shouldExecuteReadOnlyAction() {
        transactionService.operationLecture(() -> {
            // Lecture réussie
        });
    }
}
