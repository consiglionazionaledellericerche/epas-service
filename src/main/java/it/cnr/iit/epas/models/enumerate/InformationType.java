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

package it.cnr.iit.epas.models.enumerate;

import it.cnr.iit.epas.manager.configurations.EpasParam;
import java.util.Optional;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Enumerato che gestisce la tipologia di richiesta informativa.
 *
 * @author dario
 *
 */
@Getter
@RequiredArgsConstructor
public enum InformationType {

  ILLNESS_INFORMATION(true, false, false,
      Optional.of(EpasParam.ILLNESS_INFORMATION_I_III_OFFICE_HEAD_APPROVAL_REQUIRED),
      Optional.of(EpasParam.ILLNESS_INFORMATION_IV_VIII_OFFICE_HEAD_APPROVAL_REQUIRED),
      Optional.of(EpasParam.ILLNESS_INFORMATION_I_III_ADMINISTRATIVE_APPROVAL_REQUIRED),
      Optional.of(EpasParam.ILLNESS_INFORMATION_IV_VIII_ADMINISTRATIVE_APPROVAL_REQUIRED),
      Optional.empty(),
      Optional.empty()),
  TELEWORK_INFORMATION(false, true, false,
      Optional.of(EpasParam.TELEWORK_INFORMATION_I_III_OFFICE_HEAD_APPROVAL_REQUIRED),
      Optional.of(EpasParam.TELEWORK_INFORMATION_IV_VIII_OFFICE_HEAD_APPROVAL_REQUIRED),
      Optional.empty(),
      Optional.empty(),
      Optional.empty(),
      Optional.empty()),
  SERVICE_INFORMATION(false, true, false,
      Optional.empty(),
      Optional.of(EpasParam.SERVICE_INFORMATION_IV_VIII_OFFICE_HEAD_APPROVAL_REQUIRED),
      Optional.empty(),
      Optional.empty(),
      Optional.empty(),
      Optional.of(EpasParam.SERVICE_INFORMATION_IV_VIII_MANAGER_APPROVAL_REQUIRED)),
  PARENTAL_LEAVE_INFORMATION(true, false, true,
      Optional.empty(),
      Optional.empty(),
      Optional.of(EpasParam.FATHER_PARENTAL_LEAVE_I_III_ADMINISTRATIVE_APPROVAL_REQUIRED),
      Optional.of(EpasParam.FATHER_PARENTAL_LEAVE_IV_VIII_ADMINISTRATIVE_APPROVAL_REQUIRED),
      Optional.empty(),
      Optional.empty());
  
  private final boolean alwaysSkipOfficeHeadApproval;
  private final boolean alwaysSkipAdministrativeApproval;
  private final boolean alwaysSkipManagerApproval;
  private final Optional<EpasParam> officeHeadApprovalRequiredTopLevel;
  private final Optional<EpasParam> officeHeadApprovalRequiredTechnicianLevel;
  private final Optional<EpasParam> administrativeApprovalRequiredTopLevel;
  private final Optional<EpasParam> administrativeApprovalRequiredTechnicianLevel;
  private final Optional<EpasParam> managerApprovalRequiredTopLevel;
  private final Optional<EpasParam> managerApprovalRequiredTechnicianLevel;

}