package com.emsi.marches_backend.dto;
import lombok.Data;
import java.util.List;

@Data
public class QuestionnaireDTO {

    private List<String> motsClesInteret;
    private List<String> secteursChoisis;
    private String localisation;
    private String frequenceNotification;
}