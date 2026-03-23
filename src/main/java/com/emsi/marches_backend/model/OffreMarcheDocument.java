package com.emsi.marches_backend.model;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.index.Indexed;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "offres")
public class OffreMarcheDocument {

    @Id
    private String id;

    @Indexed(unique = true)
    private String reference;

    private String intitule;
    private String description;
    private String organisme;
    private String secteur;
    private String localisation;
    private String emailContact;
    private String urlOfficielle;

    private LocalDate datePublication;
    private LocalDate dateCloture;
    private LocalDateTime dateCollecte = LocalDateTime.now();
}