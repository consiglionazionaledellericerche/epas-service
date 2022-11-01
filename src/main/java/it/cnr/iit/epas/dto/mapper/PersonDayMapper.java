package it.cnr.iit.epas.dto.mapper;

import it.cnr.iit.epas.dto.PersonDayDto;
import it.cnr.iit.epas.models.PersonDay;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface PersonDayMapper {

  PersonDayDto convert(PersonDay personDay);
}
