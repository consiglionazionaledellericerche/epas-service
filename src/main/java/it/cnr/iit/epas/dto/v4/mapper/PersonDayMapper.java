package it.cnr.iit.epas.dto.v4.mapper;

import it.cnr.iit.epas.dto.v4.PersonDayDto;
import it.cnr.iit.epas.models.PersonDay;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface PersonDayMapper {

  PersonDayDto convert(PersonDay personDay);
}
