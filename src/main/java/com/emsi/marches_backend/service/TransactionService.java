package com.emsi.marches_backend.service;

import com.emsi.marches_backend.exception.BusinessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class TransactionService {

    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void operationCritique(Runnable action) {
        try {
            log.info("Démarrage transaction REQUIRED");
            action.run();
            log.info("Transaction REQUIRED complétée");
        } catch (Exception e) {
            log.error("Erreur transaction: {}", e.getMessage());
            throw new BusinessException("Opération échouée, rollback effectué", e);
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
    public void operationIndependante(Runnable action) {
        try {
            log.info("Démarrage transaction REQUIRES_NEW (isolée)");
            action.run();
            log.info("Transaction REQUIRES_NEW complétée");
        } catch (Exception e) {
            log.error("Erreur transaction isolée: {}", e.getMessage());
            throw new BusinessException("Opération indépendante échouée", e);
        }
    }

    @Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
    public void operationLecture(Runnable action) {
        try {
            log.info("Démarrage opération lecture (SUPPORTS)");
            action.run();
            log.info("Opération lecture complétée");
        } catch (Exception e) {
            log.error("Erreur lecture: {}", e.getMessage());
            throw new BusinessException("Opération de lecture échouée", e);
        }
    }
    @Transactional(propagation = Propagation.NESTED, rollbackFor = Exception.class)
    public void operationImbriquee(Runnable action) {
        try {
            log.info("Démarrage transaction NESTED (avec savepoint)");
            action.run();
            log.info("Transaction NESTED complétée");
        } catch (Exception e) {
            log.error("Erreur transaction imbriquée: {}", e.getMessage());
            throw new BusinessException("Transaction imbriquée échouée", e);
        }
    }
}
