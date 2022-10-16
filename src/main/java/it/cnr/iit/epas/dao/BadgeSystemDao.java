/*
 * Copyright (C) 2022  Consiglio Nazionale delle Ricerche
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

import com.google.common.base.Splitter;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.JPQLQuery;
import it.cnr.iit.epas.dao.common.DaoBase;
import it.cnr.iit.epas.helpers.jpa.ModelQuery;
import it.cnr.iit.epas.helpers.jpa.ModelQuery.SimpleResults;
import it.cnr.iit.epas.models.Badge;
import it.cnr.iit.epas.models.BadgeReader;
import it.cnr.iit.epas.models.BadgeSystem;
import it.cnr.iit.epas.models.Office;
import it.cnr.iit.epas.models.QBadge;
import it.cnr.iit.epas.models.QBadgeReader;
import it.cnr.iit.epas.models.QBadgeSystem;
import java.util.List;
import java.util.Optional;
import javax.inject.Inject;
import javax.inject.Provider;
import javax.persistence.EntityManager;
import org.springframework.stereotype.Component;

/**
 * Dao per l'accesso alle informazioni dei BadgeSystem.
 *
 * @author Alessandro Martelli
 */
@Component
public class BadgeSystemDao extends DaoBase {

  public static final Splitter TOKEN_SPLITTER = Splitter.on(' ')
      .trimResults().omitEmptyStrings();

  @Inject
  BadgeSystemDao(Provider<EntityManager> emp) {
    super(emp);
  }

  /**
   * Ritorna il gruppo badge relativo all'id passato.
   *
   * @return il badge system associato al codice passato come parametro.
   */
  public BadgeSystem byId(Long id) {

    final QBadgeSystem badgeSystem = QBadgeSystem.badgeSystem;

    return getQueryFactory().selectFrom(badgeSystem)
        .where(badgeSystem.id.eq(id)).fetchOne();
  }

  /**
   * Ritorna il gruppo badge associato al nome passato.
   *
   * @return il badge system con quel nome.
   */
  public BadgeSystem byName(String name) {

    final QBadgeSystem badgeSystem = QBadgeSystem.badgeSystem;

    return getQueryFactory().selectFrom(badgeSystem)
        .where(badgeSystem.name.eq(name)).fetchOne();
  }

  /**
   * Ritorna il gruppo badge con nome (opzionale) e appartenente al lettore
   * badge passati.
   *
   * @param name il nome del gruppo badge
   * @param badgeReader il lettore badge
   * @return il gruppo badge relativo a nome e lettore badge passati.
   */
  public SimpleResults<BadgeSystem> badgeSystems(Optional<String> name,
      Optional<BadgeReader> badgeReader) {

    final QBadgeSystem badgeSystem = QBadgeSystem.badgeSystem;
    final QBadgeReader qBadgeReader = QBadgeReader.badgeReader;

    JPQLQuery<BadgeSystem> query;

    final BooleanBuilder condition = new BooleanBuilder();

    if (badgeReader.isPresent()) {
      query = getQueryFactory().select(badgeSystem)
          .from(qBadgeReader)
          .rightJoin(qBadgeReader.badgeSystems, badgeSystem);
      condition.and(qBadgeReader.eq(badgeReader.get()));
    } else {
      query = getQueryFactory().selectFrom(badgeSystem);
    }
    if (name.isPresent()) {
      condition.and(matchBadgeSystemName(badgeSystem, name.get()));
    }

    query.where(condition).distinct();

    return ModelQuery.wrap(query, badgeSystem);

  }


  private BooleanBuilder matchBadgeSystemName(QBadgeSystem badgeSystem, String name) {
    final BooleanBuilder nameCondition = new BooleanBuilder();
    for (String token : TOKEN_SPLITTER.split(name)) {
      nameCondition.and(badgeSystem.name.containsIgnoreCase(token));
    }
    return nameCondition.or(badgeSystem.name.startsWithIgnoreCase(name));
  }

  /**
   * Tutti i badge di badgeSystem ordinati per codice badge (e persona).
   */
  public List<Badge> badges(BadgeSystem badgeSystem) {

    final QBadge badge = QBadge.badge;

    return getQueryFactory()
        .selectFrom(badge)
        .where(badge.badgeSystem.eq(badgeSystem))
        .orderBy(badge.code.asc()).orderBy(badge.person.id.asc()).fetch();

  }
  
  /**
   * Restituisce la lista di tutti i BagdeSystem (gruppi badge) associati ad
   * una sede.
   */
  public List<BadgeSystem> byOffice(Office office) {
    final QBadgeSystem badgeSystem = QBadgeSystem.badgeSystem;
    return getQueryFactory().selectFrom(badgeSystem)
        .where(badgeSystem.office.eq(office)).fetch();
  }

}