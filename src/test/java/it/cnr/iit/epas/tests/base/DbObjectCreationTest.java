/*
 * Copyright (C) 2023  Consiglio Nazionale delle Ricerche
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

package it.cnr.iit.epas.tests.base;

import static org.junit.Assert.assertTrue;

import it.cnr.iit.epas.tests.db.h2support.H2Examples;
import java.time.LocalDate;
import java.util.Optional;
import javax.inject.Inject;
import javax.transaction.Transactional;
import lombok.val;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@Transactional
@SpringBootTest
class DbObjectCreationTest {
  
  @Inject
  private H2Examples h2Examples;


  @Test
  void testNormalEmployee() {
    val normalEmployee = h2Examples.normalEmployee(LocalDate.now(), Optional.empty());
    assertTrue(normalEmployee.getId() != null);
    assertTrue(normalEmployee.getId().intValue() > 0);
  }
}