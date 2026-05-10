package com.emsi.marches_backend.service;

import com.emsi.marches_backend.dto.auth.PasswordResetConfirmRequest;
import com.emsi.marches_backend.dto.auth.PasswordResetRequest;
import com.emsi.marches_backend.dto.auth.PasswordResetVerifyRequest;
import com.emsi.marches_backend.model.PasswordResetCodeDocument;
import com.emsi.marches_backend.model.UtilisateurDocument;
import com.emsi.marches_backend.repository.PasswordResetCodeRepository;
import com.emsi.marches_backend.repository.UtilisateurRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class PasswordResetService {

    private static final int CODE_TTL_MINUTES = 15;
    private static final int MAX_ATTEMPTS = 5;

    private final UtilisateurRepository utilisateurRepository;
    private final PasswordResetCodeRepository resetCodeRepository;
    private final PasswordEncoder passwordEncoder;
    private final JavaMailSender mailSender;
    private final SecureRandom secureRandom = new SecureRandom();

    public void requestReset(PasswordResetRequest request) {
        String email = normalizeEmail(request.email());
        utilisateurRepository.findByEmail(email).ifPresent(utilisateur -> {
            String code = generateCode();
            resetCodeRepository.deleteByEmail(email);

            PasswordResetCodeDocument resetCode = new PasswordResetCodeDocument();
            resetCode.setEmail(email);
            resetCode.setCodeHash(passwordEncoder.encode(code));
            resetCode.setExpiresAt(LocalDateTime.now().plusMinutes(CODE_TTL_MINUTES));
            resetCode.setUsed(false);
            resetCode.setAttempts(0);
            resetCode.setCreatedAt(LocalDateTime.now());
            resetCodeRepository.save(resetCode);

            sendResetCodeEmail(utilisateur, code);
        });
    }

    public void verifyCode(PasswordResetVerifyRequest request) {
        String email = normalizeEmail(request.email());
        PasswordResetCodeDocument resetCode = findValidResetCode(email);

        if (!passwordEncoder.matches(request.code(), resetCode.getCodeHash())) {
            registerFailedAttempt(resetCode);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Code invalide");
        }
    }

    public void confirmReset(PasswordResetConfirmRequest request) {
        String email = normalizeEmail(request.email());
        PasswordResetCodeDocument resetCode = findValidResetCode(email);

        if (!passwordEncoder.matches(request.code(), resetCode.getCodeHash())) {
            registerFailedAttempt(resetCode);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Code invalide");
        }

        UtilisateurDocument utilisateur = utilisateurRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Utilisateur introuvable"));

        utilisateur.setMotDePasseHash(passwordEncoder.encode(request.newPassword()));
        utilisateurRepository.save(utilisateur);

        resetCode.setUsed(true);
        resetCodeRepository.save(resetCode);
    }

    private PasswordResetCodeDocument findValidResetCode(String email) {
        PasswordResetCodeDocument resetCode = resetCodeRepository.findTopByEmailAndUsedFalseOrderByCreatedAtDesc(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Code invalide ou expiré"));

        if (resetCode.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Code invalide ou expiré");
        }
        if (resetCode.getAttempts() >= MAX_ATTEMPTS) {
            throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS, "Trop de tentatives. Demandez un nouveau code.");
        }

        return resetCode;
    }

    private void registerFailedAttempt(PasswordResetCodeDocument resetCode) {
        resetCode.setAttempts(resetCode.getAttempts() + 1);
        resetCodeRepository.save(resetCode);
    }

    private void sendResetCodeEmail(UtilisateurDocument utilisateur, String code) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(utilisateur.getEmail());
        message.setFrom("noreply@veillemarche.ma");
        message.setSubject("Code de récupération de mot de passe - VeilleMarche.ma");
        message.setText("""
                Bonjour %s %s,

                Vous avez demandé la réinitialisation de votre mot de passe.

                Votre code de vérification est : %s

                Ce code est valable pendant %d minutes. Si vous n'êtes pas à l'origine de cette demande, ignorez cet email.

                Cordialement,
                VeilleMarche.ma
                """.formatted(
                utilisateur.getPrenom() != null ? utilisateur.getPrenom() : "",
                utilisateur.getNom() != null ? utilisateur.getNom() : "",
                code,
                CODE_TTL_MINUTES
        ));
        mailSender.send(message);
    }

    private String generateCode() {
        return String.format("%06d", secureRandom.nextInt(1_000_000));
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }
}
