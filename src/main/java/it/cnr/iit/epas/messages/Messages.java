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

package it.cnr.iit.epas.messages;

import java.util.Locale;
import javax.inject.Inject;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;

/**
 * Classe di supporto per prelavare i messages.
 *
 * @author Cristian Lucchesi
 *
 */
@Component
public class Messages {

  private final MessageSource messageSource;
  private static final  Locale DEFAULT_LOCALE = Locale.ITALIAN;

  @Inject
  Messages(MessageSource messageSource) {
    this.messageSource = messageSource;
  }

  /**
   * Messaggio con default uguale all'id del messaggio e la lingua
   * predefinita (Italiano).
   */
  public String get(String messageId) {
    return messageSource.getMessage(messageId, null, messageId, DEFAULT_LOCALE);
  }

  /**
   * Messaggio con default uguale all'id del messaggio ed i parametri passati.
   * La lingua è quella predefinita (Italiano).
   */
  public String get(String messageId, Object... obj) {
    return messageSource.getMessage(messageId, obj, messageId, DEFAULT_LOCALE);
  }

  /**
   * Messaggio con default uguale all'id del messaggio ed i parametri passati.
   * La lingua è quella passata per parametro.
   */
  public String get(String messageId, Locale locale, Object... obj) {
    return messageSource.getMessage(messageId, obj, messageId, locale);
  }
}
