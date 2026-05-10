package com.emsi.marches_backend.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "password_reset_codes")
public class PasswordResetCodeDocument {

    @Id
    private String id;

    @Indexed
    private String email;

    private String codeHash;

    @Indexed(expireAfterSeconds = 0)
    private LocalDateTime expiresAt;

    private boolean used;

    private int attempts;

    @CreatedDate
    private LocalDateTime createdAt;
}
