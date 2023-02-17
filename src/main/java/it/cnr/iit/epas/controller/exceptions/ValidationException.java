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

package it.cnr.iit.epas.controller.exceptions;

/**
 * Eccezione sollevata quando i parametri passati per compiere un'azione non sono validi.
 *
 * @author Cristian Lucchesi
 *
 */
public class ValidationException extends RuntimeException {

  private static final long serialVersionUID = 7959492196986638363L;

  public ValidationException(String message) {
    super(message);
  }
}