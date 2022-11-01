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
package it.cnr.iit.epas.tests.contracts;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import it.cnr.iit.epas.dao.ContractDao;
import it.cnr.iit.epas.dao.wrapper.IWrapperFactory;
import it.cnr.iit.epas.tests.db.h2support.H2Examples;
import java.time.LocalDate;
import java.util.Optional;
import javax.inject.Inject;
import javax.transaction.Transactional;
import lombok.val;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class ContractTest {

  @Inject
  H2Examples example;
  @Inject
  ContractDao contractDao;
  @Inject
  IWrapperFactory wrapperFactory;

  @Test
  @Transactional
  void testCurrentContract() {
    val beginDate = LocalDate.of(2009, 2, 01);
    val person = example.normalEmployee(beginDate, Optional.empty());
    assertEquals(1, person.getContracts().size());
    val contract = person.getContracts().get(0);
    assertEquals(beginDate, contract.getBeginDate());
    assertNull(contract.getEndDate());
    assertNull(contract.getEndContract());
    assertNotNull(contractDao.getContract(beginDate, person));
    assertEquals(contract, contractDao.getContract(beginDate, person));
    val wrapperPerson = wrapperFactory.create(person);
    assertTrue(wrapperPerson.getCurrentContract().isPresent());
    assertEquals(contract, wrapperPerson.getCurrentContract().get());
  }
}