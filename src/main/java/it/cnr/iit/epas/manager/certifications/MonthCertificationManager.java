/*
 * Copyright (C) 2026  Consiglio Nazionale delle Ricerche
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Affero General Public License as
 *     published by the Free Software Foundation, either version 3 of the
 *     License, or (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Affero General Public License for more details.
 *
 *     You should have received a copy of the GNU Affero General Public License
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package it.cnr.iit.epas.manager.certifications;

import com.google.common.collect.Lists;
import it.cnr.iit.epas.dao.AbsenceDao;
import it.cnr.iit.epas.dao.CompetenceDao;
import it.cnr.iit.epas.dao.ContractDao;
import it.cnr.iit.epas.dao.ContractMonthRecapDao;
import it.cnr.iit.epas.dao.PersonMonthRecapDao;
import it.cnr.iit.epas.dto.v4.certifications.CertificationRowDto;
import it.cnr.iit.epas.manager.services.mealtickets.IMealTicketsService;
import it.cnr.iit.epas.models.Contract;
import it.cnr.iit.epas.models.Person;
import it.cnr.iit.epas.models.absences.Absence;
import it.cnr.iit.epas.models.enumerate.CertificationType;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.joda.time.YearMonth;
import org.springframework.stereotype.Component;

/**
 * Calcola le righe dell'attestato mensile di una persona: formazione, assenze, competenze e
 * buoni pasto. Portato da PersonMonthlySituationData del monolite: l'invio ad Attestati è a
 * carico di epas-certifications-service.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MonthCertificationManager {

  private final PersonMonthRecapDao personMonthRecapDao;
  private final AbsenceDao absenceDao;
  private final CompetenceDao competenceDao;
  private final IMealTicketsService mealTicketService;
  private final ContractDao contractDao;
  private final ContractMonthRecapDao contractMonthRecapDao;

  /**
   * Le righe dell'attestato della persona nel mese.
   */
  public List<CertificationRowDto> monthCertifications(Person person, int year, int month) {
    List<CertificationRowDto> rows = Lists.newArrayList();
    rows.addAll(trainingHours(person, year, month));
    rows.addAll(absences(person, year, month));
    rows.addAll(competences(person, year, month));
    rows.add(mealTicket(person, year, month));
    return rows;
  }

  private List<CertificationRowDto> trainingHours(Person person, int year, int month) {
    return personMonthRecapDao.getPersonMonthRecapInYearOrWithMoreDetails(person, year,
        Optional.of(month), Optional.empty()).stream()
        .map(recap -> new CertificationRowDto(CertificationType.FORMATION,
            recap.getFromDate().getDayOfMonth() + ";" + recap.getToDate().getDayOfMonth()
                + ";" + recap.getTrainingHours()))
        .toList();
  }

  /**
   * Le assenze dello stesso codice in giorni consecutivi formano un'unica riga.
   */
  private List<CertificationRowDto> absences(Person person, int year, int month) {
    List<CertificationRowDto> rows = Lists.newArrayList();
    LocalDate previousDate = null;
    String previousCode = null;
    Integer dayBegin = null;
    CertificationRowDto row = null;

    for (Absence absence : absenceDao.getAbsencesNotInternalUseInMonth(person, year, month)) {
      val type = absence.getAbsenceType();
      if (type.isInternalUse()) {
        continue;
      }
      String code = type.getCode().toUpperCase();
      if (type.getCertificateCode() != null && !type.getCertificateCode().trim().isEmpty()) {
        code = type.getCertificateCode().toUpperCase();
      }
      val date = absence.getPersonDay().getDate();
      if (previousDate != null && previousDate.plusDays(1).equals(date)
          && code.equals(previousCode)) {
        row.setContent(code + ";" + dayBegin + ";" + date.getDayOfMonth());
        previousDate = date;
        continue;
      }
      dayBegin = date.getDayOfMonth();
      previousDate = date;
      previousCode = code;
      row = new CertificationRowDto(CertificationType.ABSENCE,
          code + ";" + dayBegin + ";" + dayBegin);
      rows.add(row);
    }
    return rows;
  }

  private List<CertificationRowDto> competences(Person person, int year, int month) {
    return competenceDao.getCompetenceInMonthForUploadSituation(person, year, month,
        Optional.empty()).stream()
        .map(competence -> new CertificationRowDto(CertificationType.COMPETENCE,
            competence.getCompetenceCode().getCode() + ";" + competence.getValueApproved()))
        .toList();
  }

  private CertificationRowDto mealTicket(Person person, int year, int month) {
    val begin = LocalDate.of(year, month, 1);
    val end = begin.withDayOfMonth(begin.lengthOfMonth());
    int papery = 0;
    int electronic = 0;
    for (Contract contract : contractDao
        .getActiveContractsInPeriod(person, begin, Optional.of(end))) {
      val monthRecap =
          contractMonthRecapDao.getContractMonthRecap(contract, new YearMonth(year, month));
      if (monthRecap == null) {
        log.info("ContractMonthRecap non presente nel mese {}/{} per {} ( id = {} )",
            month, year, person.getFullname(), person.getId());
        continue;
      }
      val recap = mealTicketService.create(contract).orElse(null);
      val composition = mealTicketService.whichBlock(recap, monthRecap, contract);
      papery += composition.getPaperyMealTicket();
      electronic += composition.getElectronicMealTicket();
    }
    return new CertificationRowDto(CertificationType.MEAL, papery + ";" + electronic);
  }
}
