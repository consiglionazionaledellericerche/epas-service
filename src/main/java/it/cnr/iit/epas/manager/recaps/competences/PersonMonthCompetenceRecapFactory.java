package it.cnr.iit.epas.manager.recaps.competences;

import it.cnr.iit.epas.dao.CompetenceCodeDao;
import it.cnr.iit.epas.dao.CompetenceDao;
import it.cnr.iit.epas.dao.wrapper.IWrapperFactory;
import it.cnr.iit.epas.models.Contract;
import java.util.Optional;
import javax.inject.Inject;
import org.springframework.stereotype.Component;

/**
 * Factory per PersonMonthCompetenceRecap.
 */
@Component
public class PersonMonthCompetenceRecapFactory {

  private final CompetenceCodeDao competenceCodeDao;
  private final CompetenceDao competenceDao;
  private final IWrapperFactory wrapperFactory;

  /**
   * Costruttore per l'injection.
   */
  @Inject
  PersonMonthCompetenceRecapFactory(CompetenceCodeDao competenceCodeDao,
      CompetenceDao competenceDao, IWrapperFactory wrapperFactory) {
    this.competenceCodeDao = competenceCodeDao;
    this.competenceDao = competenceDao;
    this.wrapperFactory = wrapperFactory;
  }

  /**
   * Il riepilogo competenze per il dipendente.
   *
   * @param contract requires not null.
   */
  public Optional<PersonMonthCompetenceRecap> create(Contract contract, int month, int year) {

    return Optional.ofNullable(
        new PersonMonthCompetenceRecap(
            competenceCodeDao, competenceDao, wrapperFactory,
            contract, month, year));

  }

}
