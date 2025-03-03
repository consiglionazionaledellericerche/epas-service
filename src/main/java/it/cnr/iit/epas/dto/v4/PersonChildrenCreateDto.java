package it.cnr.iit.epas.dto.v4;

import javax.validation.constraints.NotNull;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class PersonChildrenCreateDto extends PersonChildrenBaseDto {

  @Schema(description = "Id della persona a cui è associato il bambino")
  @NotNull
  private Long personId;
}
