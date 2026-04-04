package com.emsi.marches_backend.scraper;

import com.emsi.marches_backend.dto.ia.AnalyseResultDTO;
import com.emsi.marches_backend.dto.ia.FiltreRechercheDTO;
import com.emsi.marches_backend.model.OffreMarche.TypeMarche;
import com.emsi.marches_backend.model.OffreMarche.StatutOffre;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class MarchePublicScraper {

    private static final String BASE_URL  = "https://www.marchespublics.gov.ma";
    private static final String SEARCH_URL = BASE_URL + "/pmmp/facettes/";
    private static final String SOURCE     = "marchespublics.gov.ma";

    @Value("${scraper.delay-ms:1500}")
    private long delayMs;

    @Value("${scraper.timeout-ms:30000}")
    private int timeoutMs;

    @Value("${scraper.use-mock:false}")
    private boolean useMock;

    private static final Map<TypeMarche, String> TYPE_MAP = Map.of(
            TypeMarche.TRAVAUX,     "1",
            TypeMarche.FOURNITURES, "2",
            TypeMarche.SERVICES,    "3",
            TypeMarche.ALL,         ""
    );
    private static final Map<StatutOffre, String> STATUT_MAP = Map.of(
            StatutOffre.EN_COURS, "1",
            StatutOffre.CLOTURE,  "2",
            StatutOffre.PUBLIE,   "3",
            StatutOffre.ALL,      ""
    );

    // ── Méthode principale ────────────────────────────────────────────────────
    public List<AnalyseResultDTO> scraperOffres(FiltreRechercheDTO filtre) {
        if (useMock) {
            log.info("Mode mock activé");
            return genererMock(filtre);
        }

        List<AnalyseResultDTO> offres = new ArrayList<>();
        int maxPages = Math.max(1, filtre.getMaxResults() / 10 + 1);

        for (int page = 1; page <= maxPages && offres.size() < filtre.getMaxResults(); page++) {
            try {
                List<AnalyseResultDTO> pageOffres = scraperPage(filtre, page);
                if (pageOffres.isEmpty()) break;
                offres.addAll(pageOffres);
                Thread.sleep(delayMs);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            } catch (IOException e) {
                log.error("Erreur scraping page {}: {}", page, e.getMessage());
                if (page == 1) {
                    log.warn("Portail inaccessible → données mock");
                    return genererMock(filtre);
                }
                break;
            }
        }

        return filtrerLocal(offres, filtre);
    }

    // ── Scraping d'une page ───────────────────────────────────────────────────
    private List<AnalyseResultDTO> scraperPage(FiltreRechercheDTO filtre, int page) throws IOException {
        String url = construireUrl(filtre, page);
        log.info("Scraping page {} : {}", page, url);

        Document doc = Jsoup.connect(url)
                .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                .header("Accept-Language", "fr-FR,fr;q=0.9")
                .timeout(timeoutMs)
                .get();

        return parserDocument(doc);
    }

    // ── Construction URL ──────────────────────────────────────────────────────
    private String construireUrl(FiltreRechercheDTO filtre, int page) {
        StringBuilder sb = new StringBuilder(SEARCH_URL)
                .append("?page=").append(page)
                .append("&nbr=10");

        if (filtre.getTypeMarche() != null && filtre.getTypeMarche() != TypeMarche.ALL)
            sb.append("&type=").append(TYPE_MAP.get(filtre.getTypeMarche()));

        if (filtre.getStatut() != null && filtre.getStatut() != StatutOffre.ALL)
            sb.append("&statut=").append(STATUT_MAP.get(filtre.getStatut()));

        if (filtre.getMotsCles() != null && !filtre.getMotsCles().isEmpty())
            sb.append("&objet=").append(String.join("+", filtre.getMotsCles()));

        if (filtre.getMaitreDOuvrage() != null)
            sb.append("&maitreOuvrage=").append(filtre.getMaitreDOuvrage());

        if (filtre.getRegion() != null)
            sb.append("&region=").append(filtre.getRegion());

        return sb.toString();
    }

    // ── Parser HTML ───────────────────────────────────────────────────────────
    private List<AnalyseResultDTO> parserDocument(Document doc) {
        List<AnalyseResultDTO> offres = new ArrayList<>();
        Elements rows = doc.select("table tbody tr");

        for (Element row : rows) {
            AnalyseResultDTO offre = extraireOffre(row);
            if (offre != null) offres.add(offre);
        }
        return offres;
    }

    private AnalyseResultDTO extraireOffre(Element row) {
        try {
            Elements cells = row.select("td");
            if (cells.size() < 2) return null;

            String objet = cells.get(0).text().trim();
            if (objet.length() < 5) return null;

            Element lien = row.selectFirst("a[href]");
            String urlDetail = lien != null ? BASE_URL + lien.attr("href") : null;

            return AnalyseResultDTO.builder()
                    .objet(objet)
                    .maitreDOuvrage(cells.size() > 1 ? cells.get(1).text().trim() : null)
                    .typeMarche(cells.size() > 2 ? cells.get(2).text().trim() : null)
                    .statut(cells.size() > 3 ? cells.get(3).text().trim() : null)
                    .datePublication(cells.size() > 4 ? cells.get(4).text().trim() : null)
                    .dateLimite(cells.size() > 5 ? cells.get(5).text().trim() : null)
                    .budgetEstime(cells.size() > 6 ? cells.get(6).text().trim() : null)
                    .region(cells.size() > 7 ? cells.get(7).text().trim() : null)
                    .urlDetail(urlDetail)
                    .source(SOURCE)
                    .build();
        } catch (Exception e) {
            log.debug("Erreur extraction ligne: {}", e.getMessage());
            return null;
        }
    }

    // ── Filtrage local ────────────────────────────────────────────────────────
    private List<AnalyseResultDTO> filtrerLocal(List<AnalyseResultDTO> offres, FiltreRechercheDTO filtre) {
        return offres.stream()
                .filter(o -> {
                    if (filtre.getMotsCles() == null || filtre.getMotsCles().isEmpty()) return true;
                    String texte = (o.getObjet() + " " + nvl(o.getMaitreDOuvrage())).toLowerCase();
                    return filtre.getMotsCles().stream().anyMatch(m -> texte.contains(m.toLowerCase()));
                })
                .filter(o -> {
                    if (filtre.getRegion() == null) return true;
                    return o.getRegion() != null && o.getRegion().toLowerCase().contains(filtre.getRegion().toLowerCase());
                })
                .limit(filtre.getMaxResults())
                .toList();
    }

    // ── Mock ──────────────────────────────────────────────────────────────────
    public List<AnalyseResultDTO> genererMock(FiltreRechercheDTO filtre) {
        List<AnalyseResultDTO> mock = List.of(
                AnalyseResultDTO.builder()
                        .referencePortail("AO-2025-001")
                        .objet("Fourniture et installation d'équipements informatiques et réseau")
                        .maitreDOuvrage("Ministère de l'Intérieur – Direction Régionale de Marrakech")
                        .typeMarche("Fournitures").statut("En cours")
                        .datePublication("2025-04-01").dateLimite("2025-04-30")
                        .budgetEstime("500 000 MAD").region("Marrakech-Safi")
                        .urlDetail("https://www.marchespublics.gov.ma/pmmp/index.jsp?id=12345")
                        .source(SOURCE).build(),

                AnalyseResultDTO.builder()
                        .referencePortail("AO-2025-002")
                        .objet("Développement d'une application web de gestion des ressources humaines")
                        .maitreDOuvrage("Commune de Marrakech")
                        .typeMarche("Services").statut("En cours")
                        .datePublication("2025-04-02").dateLimite("2025-05-10")
                        .budgetEstime("350 000 MAD").region("Marrakech-Safi")
                        .urlDetail("https://www.marchespublics.gov.ma/pmmp/index.jsp?id=12346")
                        .source(SOURCE).build(),

                AnalyseResultDTO.builder()
                        .referencePortail("AO-2025-003")
                        .objet("Maintenance et support technique des systèmes d'information")
                        .maitreDOuvrage("Caisse Nationale de Sécurité Sociale (CNSS)")
                        .typeMarche("Services").statut("En cours")
                        .datePublication("2025-04-01").dateLimite("2025-04-25")
                        .budgetEstime("2 000 000 MAD").region("Casablanca-Settat")
                        .urlDetail("https://www.marchespublics.gov.ma/pmmp/index.jsp?id=12349")
                        .source(SOURCE).build(),

                AnalyseResultDTO.builder()
                        .referencePortail("AO-2025-004")
                        .objet("Fourniture de matériel de cybersécurité et protection des données")
                        .maitreDOuvrage("ONEE – Office National de l'Électricité")
                        .typeMarche("Fournitures").statut("En cours")
                        .datePublication("2025-04-03").dateLimite("2025-05-05")
                        .budgetEstime("800 000 MAD").region("Rabat-Salé-Kénitra")
                        .urlDetail("https://www.marchespublics.gov.ma/pmmp/index.jsp?id=12348")
                        .source(SOURCE).build()
        );

        return mock.stream()
                .filter(o -> {
                    if (filtre.getMotsCles() == null || filtre.getMotsCles().isEmpty()) return true;
                    return filtre.getMotsCles().stream()
                            .anyMatch(m -> o.getObjet().toLowerCase().contains(m.toLowerCase()));
                })
                .filter(o -> {
                    if (filtre.getTypeMarche() == null || filtre.getTypeMarche() == TypeMarche.ALL) return true;
                    return o.getTypeMarche() != null &&
                            o.getTypeMarche().equalsIgnoreCase(filtre.getTypeMarche().name());
                })
                .filter(o -> {
                    if (filtre.getRegion() == null) return true;
                    return o.getRegion() != null &&
                            o.getRegion().toLowerCase().contains(filtre.getRegion().toLowerCase());
                })
                .limit(filtre.getMaxResults())
                .toList();
    }

    private String nvl(String s) { return s != null ? s : ""; }
}