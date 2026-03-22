package com.emsi.marches_backend.model;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import com.emsi.marches_backend.model.enums.NotificationFrequence;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProfilRecherche {

    private List<String> motsCles;
    private List<String> secteurs;
    private String localisation;
    private NotificationFrequence frequenceNotification = NotificationFrequence.DAILY;
}