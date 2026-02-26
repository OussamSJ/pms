package com.pms.pms.services;

import com.opencsv.CSVReader;
import com.opencsv.CSVParser;
import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.exceptions.CsvValidationException;
import com.pms.pms.dto.Dtos;
import com.pms.pms.models.LogAction;
import com.pms.pms.models.Reservation;
import com.pms.pms.models.StatutReservation;
import com.pms.pms.repositories.LogActionRepository;
import com.pms.pms.repositories.ReservationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ImportCsvService {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private final ReservationRepository reservationRepo;
    private final LogActionRepository logRepo;

    /**
     * Colonnes attendues (ordre strict) :
     * booking_id, nom_client, prenom_client, date_arrivee, date_depart,
     * mobil_home, nombre_adultes, nombre_enfants, solde_a_payer, taxe_sejour_estimee
     */
    @Transactional
    public Dtos.ImportResult importerCsv(MultipartFile file, String operateur) {
        int importes = 0, ignores = 0, erreurs = 0;
        List<String> details = new ArrayList<>();

        try {
            // 1️⃣ Détection séparateur
        	char separator = detectSeparator(file);
            // 2️⃣ Parser avec bon séparateur
            CSVParser parser = new CSVParserBuilder()
                    .withSeparator(separator)
                    .build();

            // 3️⃣ Reader principal
            try (CSVReader reader = new CSVReaderBuilder(
                    new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))
                    .withCSVParser(parser)
                    .build()) {

                String[] headers = reader.readNext();
                if (headers == null) {
                    return new Dtos.ImportResult(0, 0, 1,
                            List.of("Fichier vide ou sans en-tête"));
                }

                String[] line;
                int lineNum = 1;

                while ((line = reader.readNext()) != null) {
                    lineNum++;

                    if (line.length < 10) {
                        erreurs++;
                        details.add("Ligne " + lineNum +
                                " : colonnes insuffisantes (" + line.length + ")");
                        continue;
                    }

                    String bookingId = line[0].trim();

                    if (reservationRepo.existsByBookingId(bookingId)) {
                        ignores++;
                        details.add("Ligne " + lineNum +
                                " : " + bookingId + " déjà existant (ignoré)");
                        continue;
                    }

                    try {
                        Reservation r = Reservation.builder()
                                .bookingId(bookingId)
                                .nomClient(line[1].trim())
                                .prenomClient(line[2].trim())
                                .dateArrivee(LocalDate.parse(line[3].trim(), DATE_FMT))
                                .dateDepart(LocalDate.parse(line[4].trim(), DATE_FMT))
                                .mobilHome(line[5].trim())
                                .nombreAdultes(parseIntOr(line[6].trim(), 1))
                                .nombreEnfants(parseIntOr(line[7].trim(), 0))
                                .soldeAPayer(parseBdOr(line[8].trim(), BigDecimal.ZERO))
                                .taxeSejourEstimee(parseBdOr(line[9].trim(), BigDecimal.ZERO))
                                .statut(StatutReservation.A_ARRIVER)
                                .build();

                        reservationRepo.save(r);
                        importes++;

                    } catch (DateTimeParseException | NumberFormatException ex) {
                        erreurs++;
                        details.add("Ligne " + lineNum +
                                " [" + bookingId + "] : " + ex.getMessage());
                    }
                }
            }

        } catch (IOException | CsvValidationException e) {
            log.error("Erreur lecture CSV", e);
            return new Dtos.ImportResult(0, 0, 1,
                    List.of("Erreur lecture fichier : " + e.getMessage()));
        }

        logRepo.save(LogAction.builder()
                .typeAction("IMPORT_CSV")
                .utilisateur(operateur)
                .detail(importes + " importés, " + ignores + " ignorés, " + erreurs + " erreurs")
                .build());

        return new Dtos.ImportResult(importes, ignores, erreurs, details);
    }

    private int parseIntOr(String val, int def) {
        try { return Integer.parseInt(val); } catch (NumberFormatException e) { return def; }
    }

    private BigDecimal parseBdOr(String val, BigDecimal def) {
        try { return new BigDecimal(val.replace(",", ".")); } catch (NumberFormatException e) { return def; }
    }
    
    private char detectSeparator(MultipartFile file) throws IOException {
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {

            reader.mark(4096);
            String firstLine = reader.readLine();
            reader.reset();

            if (firstLine == null) {
                return ','; // défaut
            }

            int commaCount = firstLine.split(",", -1).length;
            int semicolonCount = firstLine.split(";", -1).length;

            return semicolonCount > commaCount ? ';' : ',';
        }
    }
}
