package it.cnr.iit.epas.dto.v4;

import lombok.Data;

@Data
public class BadgeDto {
  private String code;
  private BadgeReaderDto badgeReader;
  private BadgeSystemDto badgeSystem;
}
