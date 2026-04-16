package com.emsi.marches_backend.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Service de gestion des transactions distribuées (JTA Pattern)
 * Implémente les concepts Jakarta JTA pour la gestion des transactions
 * dans un environnement distribué
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TransactionService {

    /**
     * Propagation.REQUIRED: Utilise la transaction actuelle ou en crée une nouvelle
     * C'est le comportement par défaut pour les opérations critiques
     */
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void operationCritique(Runnable action) {
        try {
            log.info("Démarrage transaction REQUIRED");
            action.run();
            log.info("Transaction REQUIRED complétée");
        } catch (Exception e) {
            log.error("Erreur transaction: {}", e.getMessage());
            throw new RuntimeException("Opération échouée, rollback effectué", e);
        }
    }

    /**
     * Propagation.REQUIRES_NEW: Crée toujours une nouvelle transaction
     * Utilisé pour les opérations indépendantes qui doivent être isolées
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
    public void operationIndependante(Runnable action) {
        try {
            log.info("Démarrage transaction REQUIRES_NEW (isolée)");
            action.run();
            log.info("Transaction REQUIRES_NEW complétée");
        } catch (Exception e) {
            log.error("Erreur transaction isolée: {}", e.getMessage());
            throw new RuntimeException("Opération indépendante échouée", e);
        }
    }

    /**
     * Propagation.SUPPORTS: Utilise la transaction si elle existe, sinon sans transaction
     * Utilisé pour les opérations de lecture
     */
    @Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
    public void operationLecture(Runnable action) {
        try {
            log.info("Démarrage opération lecture (SUPPORTS)");
            action.run();
            log.info("Opération lecture complétée");
        } catch (Exception e) {
            log.error("Erreur lecture: {}", e.getMessage());
            throw new RuntimeException("Opération de lecture échouée", e);
        }
    }

    /**
     * Propagation.NESTED: Crée un savepoint dans la transaction actuelle
     * Permet un rollback partiel sans annuler toute la transaction
     */
    @Transactional(propagation = Propagation.NESTED, rollbackFor = Exception.class)
    public void operationImbriquee(Runnable action) {
        try {
            log.info("Démarrage transaction NESTED (avec savepoint)");
            action.run();
            log.info("Transaction NESTED complétée");
        } catch (Exception e) {
            log.error("Erreur transaction imbriquée: {}", e.getMessage());
            // Ne lance pas d'exception - permet à la transaction parent de continuer
        }
    }
}
