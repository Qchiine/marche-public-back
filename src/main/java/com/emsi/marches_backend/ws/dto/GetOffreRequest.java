package com.emsi.marches_backend.ws.dto;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;


@XmlRootElement(name = "getOffreRequest")
@XmlAccessorType(XmlAccessType.FIELD)
public class GetOffreRequest {

    @XmlElement(required = true)
    private String reference;

    @XmlElement
    private String localisation;

    @XmlElement
    private String secteur;

    // Constructeurs
    public GetOffreRequest() {
    }

    public GetOffreRequest(String reference) {
        this.reference = reference;
    }

    // Getters et Setters
    public String getReference() {
        return reference;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }

    public String getLocalisation() {
        return localisation;
    }

    public void setLocalisation(String localisation) {
        this.localisation = localisation;
    }

    public String getSecteur() {
        return secteur;
    }

    public void setSecteur(String secteur) {
        this.secteur = secteur;
    }
}
