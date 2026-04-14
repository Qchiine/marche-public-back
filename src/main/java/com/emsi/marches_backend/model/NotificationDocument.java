package com.emsi.marches_backend.model;

import com.emsi.marches_backend.model.enums.NotificationCanal;
import lombok.AllArgsConstructor;
import lombok.Builder;
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
@Builder
@Document(collection = "notifications")
public class NotificationDocument {

    @Id
    private String id;

    @Indexed
    private String userId;

    @Indexed
    private String offreId;

    private String referenceOffre;

    @Builder.Default
    private NotificationCanal canal = NotificationCanal.IN_APP;

    private String titre;
    private String message;

    @Builder.Default
    private boolean lue = false;

    @CreatedDate
    private LocalDateTime dateCreation;
}
