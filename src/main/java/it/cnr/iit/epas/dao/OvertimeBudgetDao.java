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

package it.cnr.iit.epas.dao;

import com.google.common.base.MoreObjects;
import it.cnr.iit.epas.dao.common.DaoBase;
import it.cnr.iit.epas.models.GroupOvertime;
import it.cnr.iit.epas.models.Person;
import it.cnr.iit.epas.models.QCompetence;
import it.cnr.iit.epas.models.QGroupOvertime;
import it.cnr.iit.epas.models.QPersonOvertime;
import it.cnr.iit.epas.models.flows.Group;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import java.util.Collection;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;

/**
 * Totali del monte ore di straordinario assegnato e utilizzato.
 */
@Component
public class OvertimeBudgetDao extends DaoBase<GroupOvertime> {

  /** Codice di competenza dello straordinario. */
  public static final String OVERTIME_CODE = "S1";

  @Inject
  OvertimeBudgetDao(ObjectProvider<EntityManager> emp) {
    super(emp);
  }

  /**
   * Ore di straordinario assegnate al gruppo nell'anno.
   */
  public int groupHours(Group group, int year) {
    final QGroupOvertime groupOvertime = QGroupOvertime.groupOvertime;
    return orZero(getQueryFactory().select(groupOvertime.numberOfHours.sum())
        .from(groupOvertime)
        .where(groupOvertime.group.eq(group), groupOvertime.year.eq(year))
        .fetchOne());
  }

  /**
   * Ore di straordinario assegnate alla persona nell'anno.
   */
  public int personHours(Person person, int year) {
    final QPersonOvertime personOvertime = QPersonOvertime.personOvertime;
    return orZero(getQueryFactory().select(personOvertime.numberOfHours.sum())
        .from(personOvertime)
        .where(personOvertime.person.eq(person), personOvertime.year.eq(year))
        .fetchOne());
  }

  /**
   * Ore di straordinario già approvate nell'anno alle persone indicate.
   */
  public int approvedHours(Collection<Long> personIds, int year) {
    if (personIds.isEmpty()) {
      return 0;
    }
    final QCompetence competence = QCompetence.competence;
    return orZero(getQueryFactory().select(competence.valueApproved.sum())
        .from(competence)
        .where(competence.person.id.in(personIds), competence.year.eq(year),
            competence.competenceCode.code.eq(OVERTIME_CODE))
        .fetchOne());
  }

  private static int orZero(Integer value) {
    return MoreObjects.firstNonNull(value, 0);
  }
}
