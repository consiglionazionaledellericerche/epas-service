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

package it.cnr.iit.epas.models;

import it.cnr.iit.epas.models.base.MutableModel;
import it.cnr.iit.epas.models.enumerate.AttachmentType;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import org.hibernate.envers.Audited;

/**
 * Oggetto che modella gli allegati.
 *
 * @author Daniele Murgia
 * @since 06/10/16
 */
@Audited
@Entity
@Table(name = "attachments")
public class Attachment extends MutableModel {

  private static final long serialVersionUID = 7907525510585924187L;

  @NotNull
  public String filename;

  public String description;

  @NotNull
  @Enumerated(EnumType.STRING)
  public AttachmentType type;

//  @NotNull
//  @Column(nullable = false)
//  public Blob file;

  @ManyToOne(optional = true)
  @JoinColumn(name = "office_id")
  public Office office;

//  /**
//   * Dimensione dell'allegato.
//   */
//  @Transient
//  public long getLength() {
//    return file == null ? 0 : file.length();
//  }
//
//  @PreRemove
//  private void onDelete() {
//    this.file.getFile().delete();
//  }
}
