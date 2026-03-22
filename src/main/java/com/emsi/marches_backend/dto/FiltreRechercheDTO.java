package com.emsi.marches_backend.dto;
import lombok.Data;
import java.time.LocalDate;

@Data
public class FiltreRechercheDTO {

    private String motCle;
    private String secteur;
    private LocalDate dateMin;
    private int page = 0;
    private int size = 20;
    private String sort = "date_desc";
}