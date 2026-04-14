package com.emsi.marches_backend.model;

import com.emsi.marches_backend.model.enums.SuiviStatut;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection = "suivis")
@CompoundIndex(name = "uniq_user_offre", def = "{'userId':1,'offreId':1}", unique = true)
public class SuiviDocument {

    @Id
    private String id;

    @Indexed
    private String userId;

    @Indexed
    private String offreId;

    @Builder.Default
    private SuiviStatut statut = SuiviStatut.INTERESSE;

    private String note;

    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;
}
