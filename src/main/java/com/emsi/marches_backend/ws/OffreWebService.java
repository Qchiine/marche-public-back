package com.emsi.marches_backend.ws;

import com.emsi.marches_backend.model.OffreMarcheDocument;
import com.emsi.marches_backend.repository.OffreRepository;
import com.emsi.marches_backend.ws.dto.GetOffreRequest;
import com.emsi.marches_backend.ws.dto.GetOffreResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ws.server.endpoint.annotation.Endpoint;
import org.springframework.ws.server.endpoint.annotation.PayloadRoot;
import org.springframework.ws.server.endpoint.annotation.RequestPayload;
import org.springframework.ws.server.endpoint.annotation.ResponsePayload;

import java.util.Optional;

/**
 * Web Service SOAP pour les offres (JAXWS Pattern)
 * Endpoint: http://localhost:8080/ws/offre
 * WSDL: http://localhost:8080/ws/offre.wsdl
 */
@Slf4j
@Endpoint
@RequiredArgsConstructor
public class OffreWebService {

    private static final String NAMESPACE_URI = "http://marches-backend.emsi.com/ws";
    private final OffreRepository offreRepository;

    /**
     * Opération SOAP: getOffre
     * Récupère une offre par sa référence
     */
    @PayloadRoot(namespace = NAMESPACE_URI, localPart = "getOffreRequest")
    @ResponsePayload
    public GetOffreResponse getOffre(@RequestPayload GetOffreRequest request) {
        log.info("Web Service SOAP - Requête getOffre: {}", request.getReference());

        GetOffreResponse response = new GetOffreResponse();

        try {
            if (request.getReference() == null || request.getReference().isEmpty()) {
                response.setSuccess(false);
                response.setMessage("Référence obligatoire");
                return response;
            }

            // Recherche dans MongoDB
            Optional<OffreMarcheDocument> offre = offreRepository.findByReference(request.getReference());

            if (offre.isPresent()) {
                OffreMarcheDocument doc = offre.get();

                // Remplir la réponse
                response.setReference(doc.getReference());
                response.setIntitule(doc.getIntitule());
                response.setOrganisme(doc.getOrganisme());
                response.setSecteur(doc.getSecteur());
                response.setLocalisation(doc.getLocalisation());
                response.setDatePublication(doc.getDatePublication() != null ? doc.getDatePublication().toString() : null);
                response.setDateCloture(doc.getDateCloture() != null ? doc.getDateCloture().toString() : null);
                response.setDescription(doc.getDescription());
                response.setSuccess(true);
                response.setMessage("Offre trouvée");

                log.info("Offre trouvée: {}", doc.getIntitule());
            } else {
                response.setSuccess(false);
                response.setMessage("Offre non trouvée");
                log.warn("Offre non trouvée pour référence: {}", request.getReference());
            }

        } catch (Exception e) {
            log.error("Erreur Web Service SOAP: {}", e.getMessage());
            response.setSuccess(false);
            response.setMessage("Erreur serveur: " + e.getMessage());
        }

        return response;
    }

    /**
     * Opération SOAP: searchOffres
     * Recherche les offres par secteur
     */
    @PayloadRoot(namespace = NAMESPACE_URI, localPart = "searchOffresRequest")
    @ResponsePayload
    public GetOffreResponse searchOffres(@RequestPayload GetOffreRequest request) {
        log.info("Web Service SOAP - Requête searchOffres par secteur: {}", request.getSecteur());

        GetOffreResponse response = new GetOffreResponse();

        try {
            if (request.getSecteur() == null || request.getSecteur().isEmpty()) {
                response.setSuccess(false);
                response.setMessage("Secteur obligatoire");
                return response;
            }

            // Note: Pour une vraie recherche, utiliser un repository custom
            // qui retournerait une liste. Ici on simplifie.
            response.setSuccess(true);
            response.setMessage("Recherche effectuée pour secteur: " + request.getSecteur());

            log.info("Recherche SOAP effectuée pour secteur: {}", request.getSecteur());

        } catch (Exception e) {
            log.error("Erreur Web Service SOAP search: {}", e.getMessage());
            response.setSuccess(false);
            response.setMessage("Erreur serveur: " + e.getMessage());
        }

        return response;
    }
}
