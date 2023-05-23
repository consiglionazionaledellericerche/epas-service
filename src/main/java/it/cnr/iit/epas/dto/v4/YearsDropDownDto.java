package it.cnr.iit.epas.dto.v4;

import com.google.common.collect.Lists;
import java.util.List;
import lombok.Data;

/**
 * DTO contenente la lista degli anni in cui ha lavorato un utente
 */
@Data
public class YearsDropDownDto {

  List<Integer> years = Lists.newArrayList();

}
