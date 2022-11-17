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
package it.cnr.iit.epas.dao.wrapper;

import it.cnr.iit.epas.models.ContractMonthRecap;
import java.time.YearMonth;
import java.util.Optional;
import javax.inject.Inject;
import javax.inject.Provider;
import org.springframework.stereotype.Component;

/**
 * Implementazione contractMonthRecap.
 *
 * @author Alessandro Martelli
 */
@Component
public class WrapperContractMonthRecap implements IWrapperContractMonthRecap {

  private ContractMonthRecap value;
  private final Provider<IWrapperFactory> wrapperFactoryProvider;

  @Inject
  WrapperContractMonthRecap(
      Provider<IWrapperFactory> wrapperFactoryProvider) {
    this.wrapperFactoryProvider = wrapperFactoryProvider;
  }

  public IWrapperContractMonthRecap setValue(ContractMonthRecap cmr) {
    this.value = cmr;
    return this;
  }

  @Override
  public ContractMonthRecap getValue() {
    return value;
  }

  @Override
  public IWrapperContract getContract() {
    return wrapperFactoryProvider.get().create(value.getContract());
  }

  @Override
  public boolean residualInitInYearMonth(int year, int month) {
    if (getContract().getValue().getSourceDateResidual() == null) {
      return false;
    }
    return YearMonth.from(getContract().getValue().getSourceDateResidual()).equals(YearMonth.of(year, month));
  }

  /**
   * Il recap precedente se presente. Istanzia una variabile lazy.
   */
  @Override
  public Optional<ContractMonthRecap> getPreviousRecap() {
    return wrapperFactoryProvider.get().create(value.getContract())
              .getContractMonthRecap(YearMonth.of(value.year, value.month)
                      .minusMonths(1));
  }

  /**
   * Il recap precedente se presente.
   */
  @Override
  public Optional<ContractMonthRecap> getPreviousRecapInYear() {
    
    if (this.value.month != 1) {
      return wrapperFactoryProvider.get().create(value.contract)
              .getContractMonthRecap(YearMonth.of(value.year, value.month)
                      .minusMonths(1));
    }
    return Optional.<ContractMonthRecap>empty();
  }

  /**
   * Se visualizzare il prospetto sul monte ore anno precedente.
   */
  @Override
  public boolean hasResidualLastYear() {

    return value.possibileUtilizzareResiduoAnnoPrecedente;
  }

  /**
   * Il valore iniziale del monte ore anno precedente.
   */
  @Override
  public int getResidualLastYearInit() {

    if (!hasResidualLastYear()) {
      return 0;
    }
    //Preconditions.checkState(hasResidualLastYear());
    Optional<ContractMonthRecap> previous = getPreviousRecap();
    if (previous.isPresent()) {

      if (value.month == 1) {
        return value.initMonteOreAnnoPassato;
      } else {
        return previous.get().remainingMinutesLastYear;
      }

    } else {
      return this.value.initMonteOreAnnoPassato;
    }
  }

}
