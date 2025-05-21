/*
 * Copyright (C) 2025  Consiglio Nazionale delle Ricerche
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

package it.cnr.iit.epas.models;

import it.cnr.iit.epas.models.base.MutableModel;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.NotNull;
import java.time.YearMonth;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.envers.Audited;

/**
 * Oggetto che contiene l'approvazione delle ore di turno.
 *
 * @author Daniele Murgia
 * 
 * @since 09/06/17
 */
@Getter
@Setter
@Entity
@Audited
@Table(name = "shift_type_month", uniqueConstraints = @UniqueConstraint(columnNames = {
    "shift_type_id", "year_month"}))
public class ShiftTypeMonth extends MutableModel {

  private static final long serialVersionUID = 4745667554574561506L;

  @NotNull
  @Column(name = "year_month", nullable = false)
  private YearMonth yearMonth;

  @NotNull
  @ManyToOne
  @JoinColumn(name = "shift_type_id", nullable = false)
  private ShiftType shiftType;

  private boolean approved;
}
